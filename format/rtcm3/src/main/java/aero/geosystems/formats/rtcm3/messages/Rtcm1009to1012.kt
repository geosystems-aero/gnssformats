package aero.geosystems.formats.rtcm3.messages

import aero.geosystems.formats.StructBinding
import aero.geosystems.formats.rtcm3.Rtcm3Message
import aero.geosystems.formats.rtcm3.Rtcm3MessageDef
import aero.geosystems.formats.rtcm3.Rtcm3StructDef
import java.nio.ByteBuffer
import java.util.*

abstract class RtcmCommonDef_1009_1012<BINDING : RtcmCommon_1009_1012>(mid_const:Int) : Rtcm3MessageDef<BINDING>(mid_const) {
	val refstn_id_def = DF003()
	val glo_epoch_def = DF034()
	val synch_glo_def = DF005()
	val num_sat_def = DF035()
	val glo_div_smooth_def = DF036()
	val glo_smooth_int_def = DF037()
}

abstract class RtcmCommon_1009_1012(def: RtcmCommonDef_1009_1012<*>, bb: ByteBuffer, offset: Int) : Rtcm3Message(def, bb, offset) {
	val refstn_id: Int by def.refstn_id_def
	val glo_epoch: Int by def.glo_epoch_def
	val synch_glo: Boolean by def.synch_glo_def
	val num_sat: Int by def.num_sat_def
	val glo_div_smooth: Boolean by def.glo_div_smooth_def
	val glo_smooth_int: Int by def.glo_smooth_int_def
}

class Rtcm1009(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012(Companion, bb, offset) {
	val sats by sats_def
	fun getSat(index: Int) = sats_def.getItem(this, index)
	override fun bodyToString(): String {
		return String.format(Locale.ENGLISH,",%d,%s,%s,%d,%s,%d", refstn_id,
				glo_epoch, synch_glo, num_sat,
				glo_div_smooth, glo_smooth_int)+
				if (num_sat==0) "" else ";${sats.joinToString(";")}"
	}
	class Sat1009(bb: ByteBuffer, offset: Int) : StructBinding(Companion, bb, offset) {
		var sat_id by sat_id_def
		var l1code_ind by l1code_ind_def
		var sat_freq by sat_freq_def
		var l1psr by l1psr_def
		var l1phrl1psr by l1phrl1psr_def
		var l1locktime by l1locktime_def
		override fun toString(): String = String.format(Locale.ENGLISH,
				"%d," +
						"%d,%d,%.2f,%.4f,%d",
				sat_id,
				l1code_ind, sat_freq, l1psr, l1phrl1psr, l1locktime
		)
		companion object : Rtcm3StructDef<Sat1009>() {
			val sat_id_def = DF038()
			val l1code_ind_def = DF039()
			val sat_freq_def = DF040()
			val l1psr_def = DF041()
			val l1phrl1psr_def = DF042()
			val l1locktime_def = DF043()
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1009 = Sat1009(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1009>(1009) {
		val sats_def = StructArrayMember(Sat1009, num_sat_def)

		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1009 = Rtcm1009(bb, structOffset)
	}
}

class Rtcm1010(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012(Companion, bb, offset) {
	val sats by sats_def
	fun getSat(index: Int) = sats_def.getItem(this, index)
	override fun bodyToString(): String {
		return String.format(Locale.ENGLISH,",%d,%s,%s,%d,%s,%d", refstn_id,
				glo_epoch, synch_glo, num_sat,
				glo_div_smooth, glo_smooth_int)+
				if (num_sat==0) "" else ";${sats.joinToString(";")}"
	}
	class Sat1010(bb: ByteBuffer, offset: Int) : StructBinding(Companion, bb, offset) {
		var sat_id by sat_id_def
		var l1code_ind by l1code_ind_def
		var sat_freq by sat_freq_def
		var l1psr by l1psr_def
		var l1phrl1psr by l1phrl1psr_def
		var l1locktime by l1locktime_def
		var l1psr_amb by l1psr_amb_def
		var l1cnr by l1cnr_def

		override fun toString(): String = String.format(Locale.ENGLISH,
				"%d," +
						"%d,%d,%.2f,%.4f,%d," +
						"%.3f,%.2f",
				sat_id,
				l1code_ind, sat_freq, l1psr, l1phrl1psr, l1locktime,
				l1psr_amb, l1cnr
		)

		companion object : Rtcm3StructDef<Sat1010>() {
			val sat_id_def = DF038()
			val l1code_ind_def = DF039()
			val sat_freq_def = DF040()
			val l1psr_def = DF041()
			val l1phrl1psr_def = DF042()
			val l1locktime_def = DF043()
			val l1psr_amb_def = DF044()
			val l1cnr_def = DF045()
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1010 = Sat1010(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1010>(1010) {
		val sats_def = StructArrayMember(Sat1010, num_sat_def)

		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1010 = Rtcm1010(bb, structOffset)
	}
}

class Rtcm1011(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012(Companion, bb, offset) {
	val sats by sats_def
	fun getSat(index: Int) = sats_def.getItem(this, index)
	override fun bodyToString(): String {
		return String.format(Locale.ENGLISH,",%d,%s,%s,%d,%s,%d", refstn_id,
				glo_epoch, synch_glo, num_sat,
				glo_div_smooth, glo_smooth_int)+
				if (num_sat==0) "" else ";${sats.joinToString(";")}"
	}
	class Sat1011(bb: ByteBuffer, offset: Int) : StructBinding(Companion, bb, offset) {
		var sat_id by sat_id_def
		var l1code_ind by l1code_ind_def
		var sat_freq by sat_freq_def
		var l1psr by l1psr_def
		var l1phrl1psr by l1phrl1psr_def
		var l1locktime by l1locktime_def
		var l2code_ind by l2code_ind_def
		var l2l1psrdiff by l2l1psrdiff_def
		var l2phrl1psr by l2phrl1psr_def
		var l2locktime by l2locktime_def

		override fun toString(): String = String.format(Locale.ENGLISH,
				"%d," +
						"%d,%d,%.2f,%.4f,%d," +
						"%d,%.2f,%.4f,%d",
				sat_id,
				l1code_ind, sat_freq, l1psr, l1phrl1psr, l1locktime,
				l2code_ind, l2l1psrdiff, l2phrl1psr, l2locktime
		)

		companion object : Rtcm3StructDef<Sat1011>() {
			val sat_id_def = DF038()
			val l1code_ind_def = DF039()
			val sat_freq_def = DF040()
			val l1psr_def = DF041()
			val l1phrl1psr_def = DF042()
			val l1locktime_def = DF043()
			val l2code_ind_def = DF046()
			val l2l1psrdiff_def = DF047()
			val l2phrl1psr_def = DF048()
			val l2locktime_def = DF049()
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1011 = Sat1011(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1011>(1011) {
		val sats_def = StructArrayMember(Sat1011, num_sat_def)

		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1011 = Rtcm1011(bb, structOffset)
	}
}

class Rtcm1012(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012(Companion, bb, offset) {
	val sats by sats_def
	fun getSat(index: Int) = sats_def.getItem(this, index)

	override fun bodyToString(): String {
		return String.format(Locale.ENGLISH,",%d,%s,%s,%d,%s,%d", refstn_id,
				glo_epoch, synch_glo, num_sat,
				glo_div_smooth, glo_smooth_int)+
				if (num_sat==0) "" else ";${sats.joinToString(";")}"
	}

	class Sat1012(bb: ByteBuffer, offset: Int) : StructBinding(Companion, bb, offset) {
		var sat_id by sat_id_def
		var l1code_ind by l1code_ind_def
		var sat_freq by sat_freq_def
		var l1psr by l1psr_def
		var l1phrl1psr by l1phrl1psr_def
		var l1locktime by l1locktime_def
		var l1psr_amb by l1psr_amb_def
		var l1cnr by l1cnr_def
		var l2code_ind by l2code_ind_def
		var l2l1psrdiff by l2l1psrdiff_def
		var l2phrl1psr by l2phrl1psr_def
		var l2locktime by l2locktime_def
		var l2cnr by l2cnr_def

		override fun toString(): String = String.format(Locale.ENGLISH,
				"%d," +
						"%s,%d,%.2f,%.4f,%d," +
						"%.3f,%.2f," +
						"%d,%.2f,%.4f,%d," +
						"%.2f",
				sat_id,
				l1code_ind, sat_freq, l1psr, l1phrl1psr, l1locktime,
				l1psr_amb, l1cnr,
				l2code_ind, l2l1psrdiff, l2phrl1psr, l2locktime,
				l2cnr
		)

		companion object : Rtcm3StructDef<Sat1012>() {
			val sat_id_def = DF038()
			val l1code_ind_def = DF039()
			val sat_freq_def = DF040()
			val l1psr_def = DF041()
			val l1phrl1psr_def = DF042()
			val l1locktime_def = DF043()
			val l1psr_amb_def = DF044()
			val l1cnr_def = DF045()
			val l2code_ind_def = DF046()
			val l2l1psrdiff_def = DF047()
			val l2phrl1psr_def = DF048()
			val l2locktime_def = DF049()
			var l2cnr_def = DF050()
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1012 = Sat1012(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1012>(1012) {
		val sats_def = StructArrayMember(Sat1012, num_sat_def)

		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1012 = Rtcm1012(bb, structOffset)
	}
}
