package aero.geosystems.formats

import aero.geosystems.formats.utils.copyToArray
import aero.geosystems.formats.utils.getOrPut
import aero.geosystems.formats.utils.subByteBuffer
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.text.NumberFormat
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by aimozg on 22.12.2016.
 * Confidential.
 */


private fun invpp(src:IntArray) = IntArray(src.size*2){ src[it%src.size]*2+it/src.size}
val INVERSE_1BITS = intArrayOf(0b0,0b1)
val INVERSE_2BITS = invpp(INVERSE_1BITS)
val INVERSE_3BITS = invpp(INVERSE_2BITS)
val INVERSE_4BITS = invpp(INVERSE_3BITS)
val INVERSE_5BITS = invpp(INVERSE_4BITS)
val INVERSE_6BITS = invpp(INVERSE_5BITS)
val INVERSE_7BITS = invpp(INVERSE_6BITS)
val INVERSE_BYTES = invpp(INVERSE_7BITS)
val INVERSE_SUBBYTES = arrayOf(
		intArrayOf(0),
		INVERSE_1BITS,
		INVERSE_2BITS,
		INVERSE_3BITS,
		INVERSE_4BITS,
		INVERSE_5BITS,
		INVERSE_6BITS,
		INVERSE_7BITS,
		INVERSE_BYTES
)

fun lfset(scale: Double, shift: Double, value: Double): Long {
	return Math.round((value - shift) / scale)
}

fun lfget(fscale: Double, fshift: Double, value: Long): Double {
	return fscale * value + fshift
}

inline fun readBitStream(buffer:ByteBuffer, offset:Int, count:Int, process:(bits:Int, nbits:Int)->Unit) {
	/*if (offset%8 == 0 && count<=8) {
		if (count == 8) process(buffer.get(offset/8).toInt().and(0xFF),8)
		else process(buffer.get(offset/8).toInt().and(1.shl(count).minus(1)),count)
		return
	}*/
	var i = offset
	var n = count
	while(n>0) {
		val byt = buffer.get(i / 8).toInt().and(0xFF)
		val avail = 8 - (i % 8)
		val request = if (n > avail) avail else n
		process(byt.shr(avail-request).and(1.shl(request)-1),request)
		i += request
		n -= request
	}
}
inline fun<R> mapReduceBitStream(buffer:ByteBuffer, offset:Int,count:Int,initial:R,process:(bits:Int,nbits:Int,accum:R)->R):R {
	var i = initial
	readBitStream(buffer,offset,count) {bits,nbits -> i = process(bits,nbits,i)}
	return i
}
fun readBits(buffer: ByteBuffer, offset: Int, count: Int): Long {
	return mapReduceBitStream(buffer,offset,count,0L) { bits,nbits,accum ->
		accum.shl(nbits).or(bits.toLong())
	}
}

fun readBytes(buffer: ByteBuffer, bitOffset: Int, nBits: Int): ByteArray {
	val bb = ByteArray((nBits+7)/8)
	var i = 0
	var n = nBits
	while(n>0) {
		bb[i/8] = readBits(buffer,i+bitOffset,Math.min(n,8)).toByte()
		n -= 8
		i += 8
	}
	return bb
}

fun writeBytes(buffer: ByteBuffer, bitOffset: Int, nBits: Int, arr: ByteArray) {
	var i = 0
	var n = nBits
	while(n>0) {
		writeBits(buffer, arr[i/8].toLong().and(0xFF), bitOffset + i, Math.min(n,8))
		n -= 8
		i += 8
	}
}

fun toSigned(l: Long, bitSize: Int): Long {
	if (l.and(1L.shl(bitSize - 1)) == 0L) return l
	return l.or(-1L.shl(bitSize - 1))
}
fun flipBitSequence(arr:ByteArray,count:Int):ByteArray {
	val n = arr.size
	val rem = if (count%8 == 0) 8 else count%8
	return ByteArray(n){
		val invertor = INVERSE_SUBBYTES[if (it == n - 1) rem else 8]
		invertor[arr[it].toInt().and(0xFF)].toByte()
	}
}
fun readBitMask(buffer: ByteBuffer, offset: Int, count: Int): BitSet {
	return BitSet.valueOf(flipBitSequence(readBytes(buffer, offset, count),count))
}

fun writeBitMask(buffer: ByteBuffer, offset: Int, count: Int, value: BitSet) {
	val arr = value.toByteArray().let { src ->
		if (src.size>=(count+7)/8) src
		else {
			val dst = ByteArray((count+7)/8)
			System.arraycopy(src,0,dst,0,src.size)
			dst
		}
	}
	writeBytes(buffer, offset, count, flipBitSequence(arr, count))
}

fun writeBits(buffer: ByteBuffer, value: Long, offset: Int, count: Int) {
	var x = if (count < 64) value.and(1L.shl(count) - 1) else value
	var i = offset
	var n = count
	while (n > 0) {
		val idx = i / 8
		var byt = buffer.get(idx).toLong().and(0xFF)
		val avail = 8 - (i % 8)
		if (avail > n) {
			val mask = 1L.shl(n).minus(1).shl(avail - n)
			byt = byt.and(mask.inv()).or(x.shl(avail - n))
			i += n
			n = 0
		} else {
			val mask = 1L.shl(avail).minus(1)
			val part = x.shr(n - avail).and(1L.shl(avail) - 1)
			byt = byt.and(mask.inv()).or(part)
			i += avail
			n -= avail
			x = x.and(1L.shl(n) - 1)
		}
		buffer.put(idx, byt.toByte())
	}
}

open class StructBinding(def_:StructDef<*>, val buffer: ByteBuffer, val structOffset: Int) {
	protected open val def:StructDef<*> = def_
	internal val cached_sizes = arrayOfNulls<Int?>(def_.ref_count+1)
	internal val cached_starts = arrayOfNulls<Int?>(def_.ref_count+1)
	internal val cached_ends = arrayOfNulls<Int?>(def_.ref_count+1)
	fun bufferCopy():ByteBuffer = ByteBuffer.wrap(buffer.copyToArray())
	companion object {
		fun <T> errAccessor(): ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {
			override operator fun getValue(thisRef: Any, property: KProperty<*>): T = error(this.javaClass)
			override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) = error(this.javaClass)
		}

		val errIntAccessor = errAccessor<Int>()
		val errLongAccessor = errAccessor<Long>()
		val errDoubleAccessor = errAccessor<Double>()
		val errBoolAccessor = errAccessor<Boolean>()
		val errIntArrayAccessor = errAccessor<IntArray>()
		val errLongArrayAccessor = errAccessor<LongArray>()
		val errDoubleArrayAccessor = errAccessor<DoubleArray>()
		val errBoolArrayAccessor = errAccessor<BooleanArray>()

		fun toRawString(what:Any?):String = when(what) {
			is String -> what
			is Double,
			is Float -> doubleFormatter.format(what)
			is Enum<*> -> what.ordinal.toString()
			is StructBinding -> what.toRawString()
			is Iterable<*> -> what.joinToString(separator=" ",prefix="[",postfix="]") { toRawString(it) }
			is Sequence<*> -> what.joinToString(separator=" ",prefix="[",postfix="]") { toRawString(it) }
			is Array<*> -> what.joinToString(separator=" ",prefix="[",postfix="]") { toRawString(it) }
			is ByteArray -> BigInteger(1,what).toString(16)
			else -> what.toString()
		}
	}

	fun clearCaches() {
		cached_starts.fill(null)
		cached_sizes.fill(null)
		cached_ends.fill(null)
	}

	override fun toString(): String {
		return def.members.joinToString { md ->
			md.getValue(this).toString()
		}
	}

	fun toRawString(): String {
		return def.members.joinToString(separator=" ",prefix="{",postfix="}") { md ->
			when (md) {
				is StructDef.MemberWithToRawString -> md.toRawString(this)
				else -> toRawString(md.getValue(this))
			}
		}
	}
}

private val doubleFormatter = NumberFormat.getNumberInstance(Locale.ENGLISH).also {
	it.isGroupingUsed = false
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun align(pos: Int, alignment: Int): Int {
	return if (alignment == 0 || pos % alignment == 0) pos else ((pos / alignment) + 1) * alignment
}

abstract class StructDef<out BINDING : StructBinding> {
	internal var ref_count = 0
	abstract inner class BitRef(
			val prev: BitRef?,
			val fixedSize: Int?,
			val alignment: Int
	) {
		internal val ref_index = this@StructDef.ref_count++

		open fun calcStart(binding: StructBinding): Int {
			return align(fixedStart?.plus(binding.structOffset) ?: prev?.end(binding) ?: binding.structOffset, alignment)
		}
		abstract fun calcBitSize(binding: StructBinding): Int
		open fun calcEnd(binding: StructBinding): Int {
			return (fixedStart?.plus(binding.structOffset) ?: start(binding)) + (fixedSize ?: calcBitSize(binding))
		}

		fun start(binding: StructBinding, useCache:Boolean=true): Int {
			if (!useCache) {
				val v = calcStart(binding)
				binding.cached_starts[ref_index] = v
				return v
			}
			return binding.cached_starts.getOrPut(ref_index){calcStart(binding)}
		}
		fun bitSize(binding: StructBinding): Int = binding.cached_sizes.getOrPut(ref_index){calcBitSize(binding)}
		fun end(binding: StructBinding): Int = binding.cached_ends.getOrPut(ref_index){calcEnd(binding)}

		protected open val fixedStart: Int? by lazy { if (prev == null) 0 else prev.fixedEnd?.let { align(it, alignment) } }
		protected open val fixedEnd: Int? by lazy { if (fixedSize != null && fixedStart != null) (fixedStart!! + fixedSize) else null }

		fun impl(binding:StructBinding) = Triple(bitSize(binding),start(binding),end(binding))
	}

	inner class FixedSizeRef(prev: BitRef?, val bitSize: Int, alignment: Int) : BitRef(prev, bitSize, alignment) {
		override fun toString(): String {
			if (fixedStart == null) return "?+$bitSize"
			return "$bitSize:$fixedStart..$fixedEnd"
		}

		override fun calcBitSize(binding: StructBinding): Int = bitSize
	}

	inner class FnSizePosRef(prev: BitRef?, val elementBitSize:Int=8, alignment: Int = 0, val countFn: (binding: StructBinding) -> Int) : BitRef(prev,null,alignment) {
		override fun calcBitSize(binding: StructBinding): Int {
			return countFn(binding)*elementBitSize
		}

	}

	private val frontMembers = ArrayList<Member<*>>()
	private val tailMembers = ArrayList<Member<*>>()
	val members: List<Member<*>> get() = Collections.unmodifiableList(frontMembers + tailMembers)
	private var tailMode = false
	var gAlignment: Int = 0

	private inner class TailMarkerBitRef : BitRef(null, null, 0) {
		override fun calcStart(binding: StructBinding): Int {
			return frontMembers.lastOrNull()?.pos?.end(binding) ?: 0
		}

		override fun calcBitSize(binding: StructBinding): Int {
			return 0
		}

		override val fixedStart: Int?
			get() = null
		override val fixedEnd: Int?
			get() = null
	}

	private val tailMarker = object : Member<Unit>() {
		override val pos = TailMarkerBitRef()
		override fun getValue(binding: StructBinding) = Unit

		init {
			frontMembers.remove(this)
		}
	}

	protected fun tailModeOn() {
		if (tailMode) error("Duplicate tailModeOn")
		tailMode = true
	}

	protected fun tailModeOff() {
		if (!tailMode) error("Duplicate tailModeOff")
		tailMode = false
	}

	abstract inner class Member<T> : ReadOnlyProperty<StructBinding,T> {
		val mAlignment = this@StructDef.gAlignment
		abstract val pos: BitRef
		protected val index by lazy { this@StructDef.members.indexOf(this) }
		val prev: Member<*>? =
				if (tailMode) tailMembers.firstOrNull() ?: tailMarker
				else frontMembers.lastOrNull()

		override fun getValue(thisRef: StructBinding, property: KProperty<*>): T = getValue(thisRef)

		abstract fun getValue(binding: StructBinding): T

		override fun toString(): String {
			return "${javaClass.simpleName}($pos)"
		}

		init {
			@Suppress("LeakingThis")
			if (tailMode) tailMembers.add(this)
			else frontMembers.add(this)
		}
	}
	interface MemberWithToRawString {
		fun toRawString(binding: StructBinding):String
	}

	protected inline fun <M : Member<*>> tailMember(code: () -> M): M {
		tailModeOn()
		val m = code()
		tailModeOff()
		return m
	}

	protected inline fun <M : Member<*>> alignMember(bitAlignment: Int, code: () -> M): M {
		val a = gAlignment
		gAlignment = bitAlignment
		val m = code()
		gAlignment = a
		return m
	}

	abstract inner class FixedSizeMember<T>(_bitSize: Int) : Member<T>() {
		final override val pos = FixedSizeRef(prev?.pos, _bitSize, mAlignment)

		val bitSize = _bitSize
	}

	abstract inner class NumberMember<T>(bitSize: Int) : FixedSizeMember<T>(bitSize), MemberWithToRawString {
		val maxSigned = 1L.shl(bitSize - 1) - 1
		val maxUnsigned = 1L.shl(bitSize) - 1
		val minSigned = -1L.shl(bitSize - 1)

		fun getUnsigned(binding: StructBinding): Long {
			return readBits(binding.buffer, pos.start(binding), bitSize).and(maxUnsigned)
		}

		fun getSigned(binding: StructBinding): Long {
			return toSigned(getUnsigned(binding), bitSize)
		}

		fun setValue(i: Long, binding: StructBinding) {
			writeBits(binding.buffer, i, pos.start(binding), bitSize)
		}

		override fun toRawString(binding: StructBinding): String {
			return getUnsigned(binding).toString()
		}
	}

	inner class LongMember(bitSize: Int) : NumberMember<Long>(bitSize), ReadWriteProperty<StructBinding, Long> {
		override fun getValue(binding: StructBinding): Long {
			return getSigned(binding)
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Long) {
			setValue(value, thisRef)
		}
	}

	inner class ULongMember(bitSize: Int) : NumberMember<Long>(bitSize), ReadWriteProperty<StructBinding, Long> {
		override fun getValue(binding: StructBinding): Long {
			return getUnsigned(binding)
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Long) {
			setValue(value, thisRef)
		}
	}

	inner class IntMember(bitSize: Int) : NumberMember<Int>(bitSize), ReadWriteProperty<StructBinding, Int> {

		override fun getValue(binding: StructBinding): Int {
			return getSigned(binding).toInt()
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Int) {
			setValue(value.toLong(), thisRef)
		}
	}

	inner class UIntMember(bitSize: Int) : NumberMember<Int>(bitSize), ReadWriteProperty<StructBinding, Int> {
		override fun getValue(binding: StructBinding): Int {
			return getUnsigned(binding).toInt()
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Int) {
			setValue(value.toLong(), thisRef)
		}
	}

	inner class LinearFloatMember(val unsigned: Boolean, bitSize: Int, val scale: Double = 1.0, val shift: Double = 0.0) :
			NumberMember<Double>(bitSize), ReadWriteProperty<StructBinding, Double> {
		val raw = object : ReadWriteProperty<StructBinding, Long> {
			override operator fun getValue(thisRef: StructBinding, property: KProperty<*>): Long {
				return getRawValueUnsigned(thisRef)
			}

			override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Long) {
				setValue(value, thisRef)
			}
		}
		fun getRawValueUnsigned(binding:StructBinding) = getUnsigned(binding)
		fun getRawValue(binding:StructBinding) = if (unsigned) getUnsigned(binding) else getSigned(binding)

		override fun getValue(binding: StructBinding): Double {
			return lfget(scale, shift, getRawValue(binding))
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Double) {
			raw.setValue(thisRef, property, lfset(scale, shift, value))
		}

		val minDouble: Double = (if (unsigned) 0.0 else minSigned * scale) + shift
		val maxDouble: Double = (if (unsigned) maxUnsigned * scale else maxSigned * scale) + shift
	}

	inner class Float32Member(): NumberMember<Float>(32), ReadWriteProperty<StructBinding,Float> {
		override fun getValue(binding: StructBinding): Float {
			return java.lang.Float.intBitsToFloat(getUnsigned(binding).toInt())
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Float) {
			setValue(java.lang.Float.floatToIntBits(value).toLong(),thisRef)
		}

	}

	inner class Float64Member(): NumberMember<Double>(64), ReadWriteProperty<StructBinding,Double> {
		override fun getValue(binding: StructBinding): Double {
			return java.lang.Double.longBitsToDouble(getSigned(binding))
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Double) {
			setValue(java.lang.Double.doubleToLongBits(value),thisRef)
		}
	}

	inner class LinearLongMember(val unsigned: Boolean, bitSize: Int, val scale: Long = 1, val shift: Long = 0) :
			NumberMember<Long>(bitSize), ReadWriteProperty<StructBinding, Long> {
		val raw = object : ReadWriteProperty<StructBinding, Long> {
			override operator fun getValue(thisRef: StructBinding, property: KProperty<*>): Long {
				return getRawValue(thisRef)
			}

			override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Long) {
				setValue(value, thisRef)
			}

		}
		fun getRawValue(binding: StructBinding) = if (unsigned) getUnsigned(binding) else getSigned(binding)

		override fun getValue(binding:StructBinding): Long {
			return scale * getRawValue(binding) + shift
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Long) {
			raw.setValue(thisRef, property, (value - shift) / scale)
		}

		val minLong: Long = (if (unsigned) 0 else minSigned * scale) + shift
		val maxLong: Long = (if (unsigned) maxUnsigned * scale else maxSigned * scale) + shift
	}

	inner class BitMember : NumberMember<Boolean>(1), ReadWriteProperty<StructBinding, Boolean> {
		override fun getValue(binding: StructBinding): Boolean {
			return getUnsigned(binding) != 0L
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Boolean) {
			setValue(if (value) 1L else 0L, thisRef)
		}
	}

	abstract inner class BitMaskReturningMember() : Member<BitSet>(), ReadWriteProperty<StructBinding, BitSet> {
		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: BitSet) {
			setValue(thisRef,value)
		}
		abstract fun setValue(binding: StructBinding,value:BitSet)
	}

	inner class BitMaskMember(val bitSize: Int, val flushing:Boolean = false) : BitMaskReturningMember() {
		val asLong = object: ReadWriteProperty<StructBinding,Long> {
			override fun getValue(thisRef: StructBinding, property: KProperty<*>): Long {
				return readBits(thisRef.buffer,pos.start(thisRef),bitSize)
			}

			override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Long) {
				if (flushing) thisRef.clearCaches()
				writeBits(thisRef.buffer,value,pos.start(thisRef),bitSize)
			}

		}

		override val pos: BitRef = FixedSizeRef(prev?.pos, bitSize, mAlignment)

		override fun getValue(binding: StructBinding): BitSet {
			return readBitMask(binding.buffer, pos.start(binding), bitSize)
		}

		override fun setValue(binding: StructBinding, value: BitSet) {
			if (flushing) binding.clearCaches()
			writeBitMask(binding.buffer, pos.start(binding), bitSize, value)
		}

	}

	internal inner class ArraySizeRef(prev: BitRef?,
	                                  val itemBitSize: Int,
	                                  val count: StructDef<*>.NumberMember<*>,
	                                  val countShift: Int,
	                                  alignment: Int) : BitRef(prev, null, alignment) {
		override fun calcBitSize(binding: StructBinding): Int {
			return (countShift + count.getUnsigned(binding).toInt()) * itemBitSize
		}

		override fun toString(): String {
			return "" + (fixedStart ?: "?") +
					"+(" + count + (
					if (countShift > 0) "+" + countShift else
						if (countShift < 0) countShift else
							"") +
					")x" + itemBitSize
		}
	}

	inner class VarByteArrayMember(val countFn: (StructBinding)->Int) : Member<ByteArray>(), ReadWriteProperty<StructBinding, ByteArray> {
		constructor(count: NumberMember<*>, countShift: Int = 0) : this({count.getUnsigned(it).toInt() + countShift})
		val safe: ReadWriteProperty<StructBinding, ByteArray> by lazy {
			object : ReadWriteProperty<StructBinding, ByteArray> {
				override fun getValue(thisRef: StructBinding, property: KProperty<*>): ByteArray {
					val n = countFn(thisRef)
					val start = pos.start(thisRef)
					val max = (thisRef.buffer.limit() * 8 - start) / 8
					return readBytes(thisRef.buffer,
							start,
							Math.min(n, max) * 8)
				}

				override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: ByteArray) {
					val n1 = countFn(thisRef)
					val start = pos.start(thisRef)
					val n2 = (thisRef.buffer.limit()*8 - start) / 8
					val n = Math.min(n1,n2)
					writeBytes(thisRef.buffer, pos.start(thisRef), n*8, value.copyOf(n))
				}
			}
		}
		val ints: ReadWriteProperty<StructBinding, IntArray> by lazy {
			object : ReadWriteProperty<StructBinding, IntArray> {
				override fun getValue(thisRef: StructBinding, property: KProperty<*>): IntArray {
					val a = this@VarByteArrayMember.getValue(thisRef)
					return IntArray(a.size){a[it].toInt().and(0xff)}
				}

				override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: IntArray) {
					this@VarByteArrayMember.setValue(thisRef,property,ByteArray(value.size){value[it].toByte()})
				}
			}
		}
		override val pos: BitRef = FnSizePosRef(prev?.pos, 8, mAlignment, countFn)
				//ArraySizeRef(prev?.pos, 8, count, countShift, mAlignment)
		override fun getValue(binding: StructBinding): ByteArray {
			val n = countFn(binding)
			return readBytes(binding.buffer,
					pos.start(binding),
					n*8)
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: ByteArray) {
			val n = countFn(thisRef)
			writeBytes(thisRef.buffer, pos.start(thisRef), n*8, value.copyOf(n))
		}
	}

	inner class FixedStringMember(val count: Int, val charset: Charset = Charsets.UTF_8):FixedSizeMember<String>(count*8),ReadWriteProperty<StructBinding, String> {
		override fun getValue(binding: StructBinding): String {
			return readBytes(binding.buffer, pos.start(binding), count*8).toString(charset)
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: String) {
			writeBytes(thisRef.buffer, pos.start(thisRef), count*8, value.toByteArray(charset).copyOf(count))
		}

	}

	inner class VarStringMember(val count: NumberMember<*>, val countShift: Int=0,val charset: Charset = Charsets.UTF_8) : Member<String>(), ReadWriteProperty<StructBinding, String> {
		override val pos: BitRef = ArraySizeRef(prev?.pos, 8, count, countShift,mAlignment)

		override fun getValue(binding: StructBinding): String {
			val n = countShift + count.getUnsigned(binding).toInt()
			return readBytes(binding.buffer, pos.start(binding), n*8).toString(charset)
		}

		override operator fun setValue(thisRef: StructBinding, property: KProperty<*>, value: String) {
			val n = countShift + count.getUnsigned(thisRef).toInt()
			writeBytes(thisRef.buffer, pos.start(thisRef), n*8, value.toByteArray(charset).copyOf(n))
		}

		fun setCounterFor(binding: StructBinding, value: String) {
			count.setValue(value.toByteArray(charset).size.toLong()-countShift, binding)
		}
	}

	internal inner class StructSizeRef(prev: BitRef?, val structDef: StructDef<*>, alignment: Int) : BitRef(prev, structDef.fixedSize(), alignment) {
		override fun toString(): String {
			return (fixedStart?.toString() ?: "?") + "+" + (structDef.fixedSize() ?: "?")
		}

		override fun calcBitSize(binding: StructBinding): Int = structDef.bitSize(binding)
	}

	inner class StructMember<out STRUCT : StructBinding>(val def: StructDef<STRUCT>) : Member<STRUCT>() {
		override val pos: BitRef = StructSizeRef(prev?.pos, def, mAlignment)
		override fun getValue(binding: StructBinding): STRUCT {
			val bb = binding.buffer
			val bitStart = pos.start(binding)
			return def.binding(bb.subByteBuffer(bitStart / 8), bitStart % 8)
		}
	}

	inner class StructArraySizeRef(prev: BitRef?, val structDef: StructDef<*>, val count: StructDef<*>.NumberMember<*>, alignment: Int) : BitRef(prev, null, alignment) {
		val structSize = structDef.fixedSize() ?: error("Unable to create array of variable size structs")
		override fun calcBitSize(binding: StructBinding): Int {
			return count.getUnsigned(binding).toInt() * structSize
		}

		override fun toString(): String {
			return "" + (fixedStart ?: "?") + "+" + count + "x" + structSize
		}
	}

	inner class StructArrayMember<out STRUCT : StructBinding>(val def: StructDef<STRUCT>, val count: NumberMember<*>) : Member<List<STRUCT>>() {
		override val pos: StructArraySizeRef = StructArraySizeRef(prev?.pos, def, count, mAlignment)
		fun getItem(binding: StructBinding, index: Int): STRUCT {
			val bb = binding.buffer
			val bitStart = pos.start(binding) + pos.structSize * index
			return def.binding(bb.subByteBuffer(bitStart / 8), bitStart % 8)
		}

		override fun getValue(binding: StructBinding): List<STRUCT> {
			return (0..count.getUnsigned(binding) - 1).map { getItem(binding, it.toInt()) }
		}
	}

	fun bitSize(binding: StructBinding) = tailMembers.lastOrNull()?.pos?.end(binding) ?:
			frontMembers.lastOrNull()?.pos?.end(binding) ?: 0

	fun fixedSize(): Int? = frontMembers.fold(0 as Int?, { s, m -> s?.plus(m.pos.fixedSize ?: return@fold null) })

	fun minFixedSize(): Int = (
			frontMembers.fold(0, { s, m -> s + (m.pos.fixedSize ?: 0) }) +
					tailMembers.fold(0, { s, m -> s + (m.pos.fixedSize ?: 0) }) + 7
			) / 8

	fun byteSize(binding:StructBinding) = (bitSize(binding) + 7) / 8

	abstract fun binding(bb: ByteBuffer, structOffset: Int): BINDING

	inline fun withBuffer(bb: ByteBuffer, structOffset: Int, code: BINDING.() -> Unit) {
		code.invoke(binding(bb, structOffset))
	}

	override fun toString(): String {
		return "StructDef$members"
	}
}
