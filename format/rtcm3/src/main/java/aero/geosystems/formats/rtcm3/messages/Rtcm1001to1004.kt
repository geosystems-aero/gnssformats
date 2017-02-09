package aero.geosystems.formats.rtcm3.messages

import aero.geosystems.formats.StructBinding
import aero.geosystems.formats.rtcm3.Rtcm3Message
import aero.geosystems.formats.rtcm3.Rtcm3MessageDef
import aero.geosystems.formats.rtcm3.Rtcm3StructDef
import aero.geosystems.formats.rtcm3.Rtcm3StructDef.Companion.LIGHTMS
import aero.geosystems.gnss.GnssConstants
import java.nio.ByteBuffer
import java.util.*

abstract class RtcmCommonDef_1001_1004<BINDING : RtcmCommon_1001_1004<SB>,SB:RtcmSatCommon_1001_1004>(
		satdef:RtcmSatCommonDef_1001_1004<SB>,mid_const: Int) : Rtcm3MessageDef<BINDING>(mid_const) {
	val refstn_id_def = DF003()
	val gps_epoch_def = DF004()
	val synch_gnss_def = DF005()
	val num_sat_def = DF006()
	val gps_div_smooth_def = DF007()
	val gps_smooth_int_def = DF008()
	val sats_def = StructArrayMember(satdef, num_sat_def)
}

abstract class RtcmCommon_1001_1004<SB:RtcmSatCommon_1001_1004>(def: RtcmCommonDef_1001_1004<*,SB>, bb: ByteBuffer, offset: Int) : Rtcm3Message(def, bb, offset) {
	var refstn_id: Int by def.refstn_id_def
	var gps_epoch: Long by def.gps_epoch_def
	var synch_gnss: Boolean by def.synch_gnss_def
	var num_sat: Int by def.num_sat_def
	var gps_div_smooth: Boolean by def.gps_div_smooth_def
	var gps_smooth_int: Int by def.gps_smooth_int_def

	val sats: List<SB> by def.sats_def
	fun getSat(index: Int) = (def as RtcmCommonDef_1001_1004<*,*>).sats_def.getItem(this, index)

	override fun bodyToString(): String {
		return "$refstn_id,$gps_epoch,$synch_gnss,$num_sat,$gps_div_smooth,$gps_smooth_int" +
				if (num_sat == 0) "" else ";${sats.joinToString(";")}"
	}
}

abstract class RtcmSatCommonDef_1001_1004<SB : RtcmSatCommon_1001_1004>(val hasamb: Boolean, val hasL2: Boolean) : Rtcm3StructDef<SB>() {
	val hascnr = hasamb
	val sat_id_def = DF009()
	val l1code_ind_def = DF010()
	val l1psr_def = DF011()
	val l1phrl1psr_def = DF012()
	val l1locktime_def = DF013()
	val l1psr_amb_def = if (hasamb) DF014() else null
	val l1cnr_def = if (hascnr) DF015() else null
	val l2code_ind_def = if (hasL2) DF016() else null
	val l2l1psrdiff_def = if (hasL2) DF017() else null
	val l2phrl1psr_def = if (hasL2) DF018() else null
	val l2locktime_def = if (hasL2) DF019() else null
	val l2cnr_def = if (hascnr && hasL2) DF020() else null
}

abstract class RtcmSatCommon_1001_1004(def: RtcmSatCommonDef_1001_1004<*>, bb: ByteBuffer, offset: Int) :
		StructBinding(def, bb, offset) {
	val hasL2 = def.hasL2
	val hasamb = def.hasamb
	val hascnr = def.hascnr

	var sat_id: Int by def.sat_id_def
	var l1code_ind: Int by def.l1code_ind_def
	var l1psr: Double by def.l1psr_def
	var l1psr_raw: Long by def.l1psr_def.raw
	var l1phrl1psr: Double by def.l1phrl1psr_def
	var l1phrl1psr_raw: Long by def.l1phrl1psr_def.raw
	var l1locktime: Int by def.l1locktime_def
	var l1psr_amb: Double by def.l1psr_amb_def ?: errDoubleAccessor
	var l1psr_amb_raw: Long by def.l1psr_amb_def?.raw ?: errLongAccessor
	var l1cnr: Double by def.l1cnr_def ?: errDoubleAccessor
	var l1cnr_raw: Long by def.l1cnr_def?.raw ?: errLongAccessor
	var l2code_ind: Int by def.l2code_ind_def ?: errIntAccessor
	var l2l1psrdiff: Double by def.l2l1psrdiff_def ?: errDoubleAccessor
	var l2l1psrdiff_raw: Long by def.l2l1psrdiff_def?.raw ?: errLongAccessor
	var l2phrl1psr: Double by def.l2phrl1psr_def ?: errDoubleAccessor
	var l2phrl1psr_raw: Long by def.l2phrl1psr_def?.raw ?: errLongAccessor
	var l2locktime: Int by def.l2locktime_def ?: errIntAccessor
	var l2cnr: Double by def.l2cnr_def ?: errDoubleAccessor
	var l2cnr_raw: Long by def.l2cnr_def?.raw ?: errLongAccessor

	fun getL1Pseudorange(l1psr_amb: Double): Double = l1psr + l1psr_amb
	fun setL1Pseudorange(@Suppress("UNUSED_PARAMETER") l1psr_amb: Double, value: Double) {
		l1psr = value % LIGHTMS
		if (hasamb) l1psr_amb_raw = (value / LIGHTMS).toLong()
	}

	fun getL1Phaserange(l1psr_amb: Double): Double = getL1Pseudorange(l1psr_amb) + l1phrl1psr
	fun setL1Phaserange(l1psr_amb: Double, value: Double) {
		var diff = value - getL1Pseudorange(l1psr_amb)
		if (diff < (def as RtcmSatCommonDef_1001_1004).l1phrl1psr_def.minDouble || diff > def.l1phrl1psr_def.maxDouble) {
			diff %= (1500 * GnssConstants.GPS_L1_WAVELENGTH)
			if (diff < 0) diff += 1500 * GnssConstants.GPS_L1_WAVELENGTH
		}
		l1phrl1psr = diff
	}

	fun getL1Phase(l1psr_amb: Double) = getL1Phaserange(l1psr_amb) / GnssConstants.GPS_L1_WAVELENGTH
	fun setL1Phase(l1psr_amb: Double, value: Double) {
		setL1Phaserange(l1psr_amb, value * GnssConstants.GPS_L1_WAVELENGTH);
	}

	fun isL1valid() = l1phrl1psr_raw != 0x80000L

	fun setL1invalid() {
		l1phrl1psr_raw = 0x80000L
	}

	fun getL2Pseudorange(l1psr_amb: Double) = getL1Pseudorange(l1psr_amb) + l2l1psrdiff
	fun setL2Pseudorange(l1psr_amb: Double, value: Double) {
		l2l1psrdiff = value - getL1Pseudorange(l1psr_amb)
	}


	fun getL2Phaserange(l1psr_amb: Double) = getL1Pseudorange(l1psr_amb) + l2phrl1psr
	fun setL2Phaserange(l1psr_amb: Double, value: Double) {
		var diff = value - getL1Pseudorange(l1psr_amb)
		val phrdef = (def as RtcmSatCommonDef_1001_1004).l2phrl1psr_def!!
		if (diff < phrdef.minDouble || diff > phrdef.maxDouble) {
			diff %= (1500 * GnssConstants.GPS_L2_WAVELENGTH)
			if (diff < 0) diff += 1500 * GnssConstants.GPS_L2_WAVELENGTH
		}
		l2phrl1psr = diff
	}


	fun getL2Phase(l1psr_amb: Double) = getL1Phaserange(l1psr_amb) / GnssConstants.GPS_L2_WAVELENGTH
	fun setL2Phase(l1psr_amb: Double, value: Double) {
		setL2Phaserange(l1psr_amb, value * GnssConstants.GPS_L2_WAVELENGTH)
	}

	fun isL2codeValid() = l2l1psrdiff_raw != 0x2000L

	fun isL2phaseValid() = l2phrl1psr_raw != 0x80000L

	fun setL2codeInvalid() {
		l2l1psrdiff_raw = 0x2000L
	}

	fun setL2phaseInvalid() {
		l2phrl1psr_raw = 0x80000L
	}

	override fun toString(): String {
		var s = String.format(Locale.ENGLISH, "%d,%d,%.2f,%.4f,%d", sat_id, l1code_ind, l1psr, l1phrl1psr, l1locktime)
		if (hasamb) s+= String.format(Locale.ENGLISH, ",%.3f,%.2f", l1psr_amb, l1cnr)
		if (hasL2) {
			s += String.format(Locale.ENGLISH, ",%d,%.2f,%.4f,%d", l2code_ind, l2l1psrdiff, l2phrl1psr, l2locktime)
			if (hasamb) s += String.format(Locale.ENGLISH, ",%.2f", l2cnr)
		}
		return s

	}
}

class Rtcm1001(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1001_1004<Rtcm1001.Sat1001>(Companion, bb, offset) {
	class Sat1001(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1001_1004(Companion, bb, offset) {
		companion object : RtcmSatCommonDef_1001_1004<Sat1001>(false,false) {
			override fun binding(bb: ByteBuffer, structOffset: Int) = Sat1001(bb, structOffset)
		}
	}
	companion object : RtcmCommonDef_1001_1004<Rtcm1001,Sat1001>(Sat1001.Companion,1001) {
		override fun binding(bb: ByteBuffer, structOffset: Int) = Rtcm1001(bb, structOffset)
	}
}

class Rtcm1002(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1001_1004<Rtcm1002.Sat1002>(Companion, bb, offset) {
	class Sat1002(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1001_1004(Companion, bb, offset) {
		var l1Pseudorange: Double
			get() = l1psr + l1psr_amb
			set(value) {
				val ambig = (value / LIGHTMS).toLong()
				l1psr_amb_raw = ambig
				l1psr = value - ambig * LIGHTMS
			}

		var l1Phaserange: Double
			get() = l1Pseudorange + l1phrl1psr
			set(value) {
				var diff = value - l1Pseudorange
				if (diff < l1phrl1psr_def.minDouble || diff > l1phrl1psr_def.maxDouble) {
					diff %= (1500 * GnssConstants.GPS_L1_WAVELENGTH)
					if (diff < 0) diff += 1500 * GnssConstants.GPS_L1_WAVELENGTH
				}
				l1phrl1psr = diff
			}

		companion object : RtcmSatCommonDef_1001_1004<Sat1002>(true,false) {
			override fun binding(bb: ByteBuffer, structOffset: Int) = Sat1002(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1001_1004<Rtcm1002,Sat1002>(Sat1002.Companion,1002) {
		override fun binding(bb: ByteBuffer, structOffset: Int) = Rtcm1002(bb, structOffset)
	}
}

class Rtcm1003(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1001_1004<Rtcm1003.Sat1003>(Companion, bb, offset) {
	class Sat1003(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1001_1004(Companion, bb, offset) {
		companion object : RtcmSatCommonDef_1001_1004<Sat1003>(false,true) {
			override fun binding(bb: ByteBuffer, structOffset: Int) = Sat1003(bb, structOffset)
		}
	}
	companion object : RtcmCommonDef_1001_1004<Rtcm1003,Sat1003>(Sat1003.Companion,1003) {
		override fun binding(bb: ByteBuffer, structOffset: Int) = Rtcm1003(bb, structOffset)
	}
}


class Rtcm1004(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1001_1004<Rtcm1004.Sat1004>(Companion, bb, offset) {
	class Sat1004(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1001_1004(Companion, bb, offset) {
		var l1Pseudorange: Double
			get() = getL1Pseudorange(l1psr_amb)
			set(value) {
				val ambig = (value / LIGHTMS).toLong()
				l1psr_amb_raw = ambig
				l1psr = value - ambig * LIGHTMS
			}

		var l1Phaserange: Double
			get() = l1Pseudorange + l1phrl1psr
			set(value) {
				var diff = value - l1Pseudorange
				if (diff < l1phrl1psr_def.minDouble || diff > l1phrl1psr_def.maxDouble) {
					diff %= (1500 * GnssConstants.GPS_L1_WAVELENGTH)
					if (diff < 0) diff += 1500 * GnssConstants.GPS_L1_WAVELENGTH
				}
				l1phrl1psr = diff
			}

		var l2Pseudorange: Double
			get() = l1Pseudorange + l2l1psrdiff
			set(value) {
				l2l1psrdiff = value - l1Pseudorange
			}

		var l2Phaserange: Double
			get() = l1Pseudorange + l2phrl1psr
			set(value) {
				var diff = value - l1Pseudorange
				if (diff < l2phrl1psr_def!!.minDouble || diff > l2phrl1psr_def.maxDouble) {
					diff %= (1500 * GnssConstants.GPS_L2_WAVELENGTH)
					if (diff < 0) diff += 1500 * GnssConstants.GPS_L2_WAVELENGTH
				}
				l2phrl1psr = diff
			}

		var l1Phase: Double
			get() = l1Phaserange / GnssConstants.GPS_L1_WAVELENGTH
			set(value) {
				l1Phaserange = value * GnssConstants.GPS_L1_WAVELENGTH
			}

		var l2Phase: Double
			get() = l2Phaserange / GnssConstants.GPS_L2_WAVELENGTH
			set(value) {
				l2Phaserange = value * GnssConstants.GPS_L2_WAVELENGTH
			}

		companion object : RtcmSatCommonDef_1001_1004<Sat1004>(true, true) {
			override fun binding(bb: ByteBuffer, structOffset: Int) = Sat1004(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1001_1004<Rtcm1004,Sat1004>(Sat1004.Companion,1004) {
		override fun binding(bb: ByteBuffer, structOffset: Int) = Rtcm1004(bb, structOffset)
	}
}


