package aero.geosystems.formats.jps

import aero.geosystems.formats.StructBinding
import aero.geosystems.formats.StructDef
import aero.geosystems.formats.readBytes
import aero.geosystems.formats.writeBytes
import java.nio.ByteBuffer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by IntelliJ IDEA.
 * User: aimozg
 * Date: 19.04.13
 * Time: 14:46
 */
abstract class JpsMessageDef<out BINDING: StructBinding> : StructDef<BINDING>() {
	val message_id_raw_def = FixedStringMember(2)
	val length_of_body_raw_def = FixedStringMember(3)
	protected fun lengthOfBody(b:StructBinding) = length_of_body_raw_def.getValue(b).toInt(16)
	companion object {
		/*
	i1 127 7F
	u1 255 FF
	i2 32767 7FFF
	u2 65535 FFFF
	i4 2147483647 7FFF_FFFF
	u4 4294967295 FFFF_FFFF
	f4 quiet NaN 7FC0_0000
	f8 quiet NaN 7FF8_0000_0000_0000
*/
		const val SPECIAL_I1 = 0x7f
		const val SPECIAL_U1 = 0xff
		const val SPECIAL_I2 = 0x7fff
		const val SPECIAL_U2 = 0xffff
		const val SPECIAL_I4 = 0x7fff_ffff
		const val SPECIAL_U4: Long = 0xffff_ffff
		val SPECIAL_F4 = java.lang.Float.intBitsToFloat(0x7fc0_0000)
		val SPECIAL_F8 = java.lang.Double.longBitsToDouble(0x7ff8_0000_0000_0000L)
	}
	inner class JpsI1ArrayMember(private val countFn: (StructBinding)->Int): Member<Array<Int?>>(), ReadWriteProperty<StructBinding, Array<Int?>> {
		override val pos:BitRef = FnSizePosRef(prev?.pos,8, countFn =countFn)
		override fun getValue(binding:StructBinding):Array<Int?> {
			val n = countFn(binding)
			val a = readBytes(binding.buffer,pos.start(binding),n*8)
			return Array(a.size){
				a[it].toInt().takeIf { it!= SPECIAL_I1 }
			}
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Array<Int?>) {
			val n = countFn(thisRef)
			val a = ByteArray(n) {
				(if (it < value.size) value[it]?: SPECIAL_I1  else SPECIAL_I1).toByte()
			}
			writeBytes(thisRef.buffer,pos.start(thisRef),n*8,a)
		}
	}
	inner class JpsU1ArrayMember(private val countFn: (StructBinding)->Int): Member<Array<Int?>>(), ReadWriteProperty<StructBinding, Array<Int?>> {
		override val pos:BitRef = FnSizePosRef(prev?.pos,8, countFn =countFn)
		override fun getValue(binding:StructBinding):Array<Int?> {
			val n = countFn(binding)
			val a = readBytes(binding.buffer,pos.start(binding),n*8)
			return Array(a.size){
				a[it].toInt().and(0xff).takeIf { it!= SPECIAL_U1 }
			}
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Array<Int?>) {
			val n = countFn(thisRef)
			val a = ByteArray(n) {
				(if (it < value.size) value[it]?: SPECIAL_U1  else SPECIAL_U1).toByte()
			}
			writeBytes(thisRef.buffer,pos.start(thisRef),n*8,a)
		}
	}
	inner class JpsF8ArrayMember(private val countFn: (StructBinding)->Int): Member<Array<Double?>>(),
			ReadWriteProperty<StructBinding,Array<Double?>> {
		override val pos:BitRef = FnSizePosRef(prev?.pos,64, countFn =countFn)
		override fun getValue(binding: StructBinding): Array<Double?> {
			throw UnsupportedOperationException("not implemented") //TODO implement ${CLASS_NAME}.getValue
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: Array<Double?>) {
			throw UnsupportedOperationException("not implemented") //TODO implement ${CLASS_NAME}.setValue
		}
	}
}
abstract class JpsMessage(_def:JpsMessageDef<*>, bb:ByteBuffer, offset:Int) : StructBinding(_def, bb, offset) {
	var message_id_raw: String by _def.message_id_raw_def
	var length_of_body_raw: String by _def.length_of_body_raw_def

	var messageId: JpsMessageId?
		get() = JpsMessageId.byCode(message_id_raw)
		set(value) {
			message_id_raw = value?.code ?:""
		}
	var length_of_body: Int
		get() = length_of_body_raw.toInt(16)
		set(value) {
			length_of_body_raw = value.toString(16).toUpperCase().padStart(3,'0')
		}
	val totalMessageLength get() = length_of_body+5

	open fun loaded() {
		// default - do nothing
	}

	open fun valid(): Boolean {
		// default - no checkums
		return true
	}

	/**
	 * Set checksum, message id, and length of body.
	 *
	 *
	 * Overriding methods should setMessageId and setLengthOfBody
	 */
	open fun fin() {
		// default - find message id and set it
		for (mid in JpsMessageId.values()) {
			if (mid.def == javaClass) {
				messageId = mid
				break
			}
		}
		setLengthOfBody()
	}

	fun setLengthOfBody() {
		length_of_body = def.byteSize(this) - HEADER_LENGTH
	}

	override fun toString(): String {
		val mid = messageId
		return (mid ?: javaClass.simpleName).toString() + ": " + messageId +
				" LENGTH=" + length_of_body
	}

	companion object {

		var HEADER_LENGTH = 5
		var MAX_BODY_LENGTH = 0xFFF

		fun checksum(bb: ByteBuffer, start: Int, length: Int): Int {
			var res = 0
			bb.position(start)
			for (i in 0 until length) {
				val byt = bb.get().toInt() and 0xff
				res = res shl 2 or (res shr 6) and 0xff
				res = res xor byt
			}
			return res shl 2 or (res shr 6) and 0xff
		}
	}
}

