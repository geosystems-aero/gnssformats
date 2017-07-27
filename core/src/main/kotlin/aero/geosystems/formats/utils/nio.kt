package aero.geosystems.formats.utils

import java.math.BigInteger
import java.nio.ByteBuffer

/**
 * Created by aimozg on 10.05.2016.
 * Confidential.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun minimum(a:Int, b:Int) = if (a < b) a else b
@Suppress("NOTHING_TO_INLINE")
inline fun minimum(a:Long, b:Long) = if (a < b) a else b

fun ByteBuffer.subByteBuffer(pos:Int) = duplicate()!!.apply {
	position(pos)
}.slice()!!
fun ByteBuffer.subByteBuffer(pos:Int, len:Int) = duplicate()!!.apply {
	position(pos)
	limit(pos + len)
}.slice()!!
fun ByteBuffer.put(src: ByteBuffer, len:Int): ByteBuffer {
	put(src.slice().limit(len) as ByteBuffer)
	src.position(src.position()+len)
	return this
}
fun ByteBuffer.flipDuplicate() = duplicate()!!.apply {
	flip()
}
fun ByteArray.asByteBuffer() = ByteBuffer.wrap(this)!!
fun ByteBuffer.copyToArray(srcpos:Int=0,srclen:Int=limit()):ByteArray {
	val arr = ByteArray(srclen)
	duplicate().apply { position(srcpos) }.get(arr,0,srclen)
	return arr
}

fun String.parseAsHex() = BigInteger(this,16).toByteArray().let { arr ->
	val n = length/2
	val m = arr.size
	if (m == n) arr else arr.sliceArray(m-n..m-1)
}!!
