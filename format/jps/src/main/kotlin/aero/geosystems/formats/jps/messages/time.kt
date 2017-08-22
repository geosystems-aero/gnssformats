package aero.geosystems.formats.jps.messages

import aero.geosystems.formats.jps.JpsMessage
import aero.geosystems.formats.jps.JpsMessageDef
import aero.geosystems.gnss.Datetime
import aero.geosystems.gnss.GnssUtils
import java.nio.ByteBuffer

/*
 * Created by aimozg on 18.08.2017.
 * Confidential unless published on GitHub
 */
class ReceiverTime(bb: ByteBuffer, bitOffset:Int): JpsMessage(Companion,bb,bitOffset) {
	var tod:Long by tod_def
	var cs:Int by cs_def

	fun gpsTime(rd:ReceiverDate) = GnssUtils.unix2gps_leap(
			Datetime(rd.year,rd.month,rd.day).addMillis(tod).time
	)

	companion object : JpsMessageDef<ReceiverTime>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = ReceiverTime(bb,structOffset)
		val tod_def = ULongMember(32)
		val cs_def = UIntMember(8)
	}
}
class EpochTime(bb: ByteBuffer, bitOffset:Int): JpsMessage(Companion,bb,bitOffset) {
	var tod:Long by tod_def
	var cs:Int by cs_def

	fun gpsTime(rd:ReceiverDate) = GnssUtils.unix2gps_leap(
			Datetime(rd.year,rd.month,rd.day).addMillis(tod).time
	)

	companion object : JpsMessageDef<EpochTime>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = EpochTime(bb,structOffset)
		val tod_def = ULongMember(32)
		val cs_def = UIntMember(8)
	}
}
class ReceiverDate(bb: ByteBuffer, bitOffset:Int): JpsMessage(Companion,bb,bitOffset) {
	var year:Int by year_def
	var month:Int by month_def
	var day:Int by day_def
	var base:Int by base_def
	var cs:Int by cs_def
	companion object : JpsMessageDef<ReceiverDate>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = ReceiverDate(bb,structOffset)
		val year_def = UIntMember(16)
		val month_def = UIntMember(8)
		val day_def = UIntMember(8)
		val base_def = UIntMember(8)
		val cs_def = UIntMember(8)
	}
	enum class ReceiverReferenceTime {
		GPS,UTC_USNO,GLONASS,UTC_SU,RESERVED
	}
}

class GpsTimeMessage(bb: ByteBuffer, bitOffset:Int): JpsMessage(Companion,bb,bitOffset) {
	var tow:Long by tow_def
	var wn:Int by wn_def
	var cs:Int by cs_def

	companion object : JpsMessageDef<GpsTimeMessage>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = GpsTimeMessage(bb,structOffset)
		val tow_def = ULongMember(32)
		val wn_def = UIntMember(16)
		val cs_def = UIntMember(8)
	}
}
class UtcParameters(bb: ByteBuffer, bitOffset:Int): JpsMessage(Companion,bb,bitOffset) {
	var a0: Double by a0_def
	var a1: Float by a1_def
	var tot: Long by tot_def
	var wnt: Int by wnt_def
	var dtls: Int by dtls_def
	var dn: Int by dn_def
	var wnlsf: Int by wnlsf_def
	var dtlsf: Int by dtlsf_def
	var cs: Int by cs_def
	companion object : JpsMessageDef<UtcParameters>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = UtcParameters(bb,structOffset)
		val a0_def = Float64Member()
		val a1_def = Float32Member()
		val tot_def = ULongMember(32)
		val wnt_def = UIntMember(16)
		val dtls_def = IntMember(8)
		val dn_def = UIntMember(8)
		val wnlsf_def = UIntMember(16)
		val dtlsf_def = IntMember(8)
		val cs_def = UIntMember(8)
	}
}
