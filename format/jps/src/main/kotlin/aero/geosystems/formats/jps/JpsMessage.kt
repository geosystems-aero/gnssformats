package aero.geosystems.formats.jps

import aero.geosystems.formats.StructBinding
import aero.geosystems.formats.StructDef
import java.nio.ByteBuffer

/**
 * Created by IntelliJ IDEA.
 * User: aimozg
 * Date: 19.04.13
 * Time: 14:46
 */
abstract class JpsMessageDef<out BINDING: StructBinding> : StructDef<BINDING>() {
	val message_id_raw_def = FixedStringMember(2)
	val length_of_body_raw_def = FixedStringMember(3)

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
		get() = length_of_body_raw.toInt()
		set(value) {
			length_of_body_raw = value.toString().padStart(3,'0')
		}

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
		var SPECIAL_I1: Byte = 0x7f
		var SPECIAL_U1: Short = 0xff
		var SPECIAL_I2: Short = 0x7fff
		var SPECIAL_U2 = 0xffff
		var SPECIAL_I4 = 0x7fff_ffff
		var SPECIAL_U4: Long = 0xffff_ffff
		var SPECIAL_F4 = java.lang.Float.intBitsToFloat(0x7fc0_0000)
		var SPECIAL_F8 = java.lang.Double.longBitsToDouble(0x7ff8_0000_0000_0000L)


		fun checksum(bb: ByteBuffer, start: Int, length: Int): Int {
			var res = 0
			bb.position(start)
			for (i in 0..length - 1) {
				val byt = bb.get().toInt() and 0xff
				res = res shl 2 or (res shr 6) and 0xff
				res = res xor byt
			}
			return res shl 2 or (res shr 6) and 0xff
		}
	}
}

