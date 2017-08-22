package aero.geosystems.formats.jps.messages

import aero.geosystems.formats.jps.JpsMessage
import aero.geosystems.formats.jps.JpsMessageDef
import java.nio.ByteBuffer

/*
 * Created by aimozg on 18.08.2017.
 * Confidential unless published on GitHub
 */

class JpsFileId(bb:ByteBuffer,bitOffset:Int): JpsMessage(Companion,bb,bitOffset) {
	var id:String by id_def
	var description:String by description_def
	companion object : JpsMessageDef<JpsFileId>(){
		override fun binding(bb: ByteBuffer, structOffset: Int)= JpsFileId(bb,structOffset)
		val id_def = FixedStringMember(5)
		val description_def = FixedStringMember(80)
	}
}

class MsgFormatAndId(bb:ByteBuffer,bitOffset:Int): JpsMessage(Companion,bb,bitOffset) {
	var jp_id:String by jp_id_def
	var major_version_raw:String by major_version_raw_def
	var minor_version_raw:String by minor_version_raw_def
	var order:Int by order_def
	var crc:Int by crc_def
	companion object : JpsMessageDef<MsgFormatAndId>() {
		override fun binding(bb:ByteBuffer,structOffset: Int) = MsgFormatAndId(bb,structOffset)
		val jp_id_def = FixedStringMember(2)
		val major_version_raw_def = FixedStringMember(2)
		val minor_version_raw_def = FixedStringMember(2)
		val order_def = UIntMember(8)
		val crc_def = UIntMember(16)
	}
}

