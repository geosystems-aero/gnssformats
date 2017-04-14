package aero.geosystems.formats.rtcm3.messages

import aero.geosystems.formats.rtcm3.Rtcm3Message
import aero.geosystems.formats.rtcm3.Rtcm3MessageDef
import aero.geosystems.formats.rtcm3.Rtcm3StructDef
import aero.geosystems.formats.rtcm3.Rtcm3StructDef.Companion.LIGHTMS
import aero.geosystems.formats.rtcm3.formatAs
import aero.geosystems.gnss.GnssConstants
import java.nio.ByteBuffer
import java.util.*

abstract class RtcmCommonDef_1009_1012<BINDING : RtcmCommon_1009_1012<SB>, out SB : RtcmSatCommon_1009_1012>(satdef: RtcmSatCommonDef_1009_1012<SB>, mid_const: Int) : Rtcm3MessageDef<BINDING>(mid_const) {
	val refstn_id_def = DF003()
	val glo_epoch_def = DF034()
	val synch_glo_def = DF005()
	val num_sat_def = DF035()
	val glo_div_smooth_def = DF036()
	val glo_smooth_int_def = DF037()
	val sats_def = StructArrayMember(satdef, num_sat_def)
}

abstract class RtcmCommon_1009_1012<out SB : RtcmSatCommon_1009_1012>(final override val def: RtcmCommonDef_1009_1012<*, SB>,
                                                                      bb: ByteBuffer, offset: Int) : Rtcm3Message(def, bb, offset) {
	val refstn_id: Int by def.refstn_id_def
	val glo_epoch: Int by def.glo_epoch_def
	val synch_glo: Boolean by def.synch_glo_def
	val num_sat: Int by def.num_sat_def
	val glo_div_smooth: Boolean by def.glo_div_smooth_def
	val glo_smooth_int: Int by def.glo_smooth_int_def

	val sats: List<SB> by def.sats_def
	fun getSat(index: Int) = def.sats_def.getItem(this, index)

	override fun bodyToString(): String {
		return String.format(Locale.ENGLISH, ",%d,%s,%s,%d,%s,%d", refstn_id,
				glo_epoch, synch_glo, num_sat,
				glo_div_smooth, glo_smooth_int) +
				if (num_sat == 0) "" else ";${sats.joinToString(";")}"
	}
}

abstract class RtcmSatCommonDef_1009_1012<SB : RtcmSatCommon_1009_1012>(val hasamb: Boolean, val hasL2: Boolean) : Rtcm3StructDef<SB>() {
	val hascnr = hasamb

	val sat_id_def = DF038()
	val l1code_ind_def = DF039()
	val sat_freq_idx_def = DF040()
	val l1psr_def = DF041()
	val l1phrl1psr_def = DF042()
	val l1locktime_ind_def = DF043()
	val l1psr_amb_def = if (hasamb) DF044() else null
	val l1cnr_def = if (hascnr) DF045() else null
	val l2code_ind_def = if (hasL2) DF046() else null
	val l2l1psrdiff_def = if (hasL2) DF047() else null
	val l2phrl1psr_def = if (hasL2) DF048() else null
	val l2locktime_ind_def = if (hasL2) DF049() else null
	var l2cnr_def = if (hasL2 && hascnr) DF050() else null
}

abstract class RtcmSatCommon_1009_1012(
		override final val def: RtcmSatCommonDef_1009_1012<*>, bb: ByteBuffer, offset: Int) :
		Rtcm3v0RtkSatCommon(def.hasL2,def.hasamb,def.hascnr,
				def, bb, offset) {
	override val waveL1 get() = GnssConstants.gloWaveL1(sat_freq_idx - 7)
	override val waveL2 get() = GnssConstants.gloWaveL2(sat_freq_idx - 7)
	override val min_l1phrl1psrDouble: Double
		get() = def.l1phrl1psr_def.minDouble
	override val max_l1phrl1psrDouble: Double
		get() = def.l1phrl1psr_def.maxDouble
	override val min_l2phrl1psrDouble: Double
		get() = def.l2phrl1psr_def?.minDouble?: error("No L2")
	override val max_l2phrl1psrDouble: Double
		get() = def.l2phrl1psr_def?.maxDouble?: error("No L2")

	var sat_id by def.sat_id_def
	var l1code_ind by def.l1code_ind_def
	var sat_freq_idx by def.sat_freq_idx_def
	override var l1psr by def.l1psr_def
	var l1psr_raw by def.l1psr_def.raw
	override var l1phrl1psr by def.l1phrl1psr_def
	override var l1phrl1psr_raw by def.l1phrl1psr_def.raw
	override var l1locktime_ind by def.l1locktime_ind_def
	override var l1psr_amb by def.l1psr_amb_def ?: errDoubleAccessor
	override var l1psr_amb_raw: Long by def.l1psr_amb_def?.raw ?: errLongAccessor
	override var l1cnr by def.l1cnr_def ?: errDoubleAccessor
	var l1cnr_raw by def.l1cnr_def?.raw ?: errLongAccessor
	var l2code_ind by def.l2code_ind_def ?: errIntAccessor
	override var l2l1psrdiff by def.l2l1psrdiff_def ?: errDoubleAccessor
	override var l2l1psrdiff_raw by def.l2l1psrdiff_def?.raw ?: errLongAccessor
	override var l2phrl1psr by def.l2phrl1psr_def ?: errDoubleAccessor
	override var l2phrl1psr_raw by def.l2phrl1psr_def?.raw ?: errLongAccessor
	override var l2locktime_ind by def.l2locktime_ind_def ?: errIntAccessor
	override var l2cnr by def.l2cnr_def ?: errDoubleAccessor
	var l2cnr_raw by def.l2cnr_def?.raw ?: errLongAccessor

	override fun toString(): String {
		var s = String.format(Locale.ENGLISH, "%d,%d,%d,%.2f,%.4f,%d",
				sat_id, l1code_ind, sat_freq_idx, l1psr, l1phrl1psr, l1locktime_ind)
		if (hasamb) s += l1psr_amb.formatAs(",%.3f")
		if (hascnr) s += l1cnr.formatAs(",%.2f")
		if (hasL2) s += String.format(Locale.ENGLISH, ",%d,%.2f,%.4f,%d",
				l2code_ind, l2l1psrdiff, l2phrl1psr, l2locktime_ind)
		if (hasL2 && hascnr) s += l2cnr.formatAs(",%.2f")
		return s
	}

}


class Rtcm1009(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012<Rtcm1009.Sat1009>(Companion, bb, offset) {
	class Sat1009(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1009_1012(Companion, bb, offset) {
		companion object : RtcmSatCommonDef_1009_1012<Sat1009>(false, false) {
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1009 = Sat1009(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1009, Sat1009>(Sat1009.Companion, 1009) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1009 = Rtcm1009(bb, structOffset)
	}
}

class Rtcm1010(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012<Rtcm1010.Sat1010>(Companion, bb, offset) {
	class Sat1010(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1009_1012(Companion, bb, offset) {
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
					diff %= (1500 * waveL1)
					if (diff < 0) diff += 1500 * waveL1
				}
				l1phrl1psr = diff
			}

		var l1Phase: Double
			get() = l1Phaserange / waveL1
			set(value) {
				l1Phaserange = value * waveL1
			}

		companion object : RtcmSatCommonDef_1009_1012<Sat1010>(true, false) {
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1010 = Sat1010(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1010, Sat1010>(Sat1010.Companion, 1010) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1010 = Rtcm1010(bb, structOffset)
	}
}

class Rtcm1011(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012<Rtcm1011.Sat1011>(Companion, bb, offset) {
	class Sat1011(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1009_1012(Companion, bb, offset) {
		companion object : RtcmSatCommonDef_1009_1012<Sat1011>(false, true) {
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1011 = Sat1011(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1011, Sat1011>(Sat1011.Companion, 1011) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1011 = Rtcm1011(bb, structOffset)
	}
}

class Rtcm1012(bb: ByteBuffer, offset: Int = 0) : RtcmCommon_1009_1012<Rtcm1012.Sat1012>(Companion, bb, offset) {
	class Sat1012(bb: ByteBuffer, offset: Int) : RtcmSatCommon_1009_1012(Companion, bb, offset) {

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
					diff %= (1500 * waveL1)
					if (diff < 0) diff += 1500 * waveL1
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
					diff %= (1500 * waveL2)
					if (diff < 0) diff += 1500 * waveL2
				}
				l2phrl1psr = diff
			}

		var l1Phase: Double
			get() = l1Phaserange / waveL1
			set(value) {
				l1Phaserange = value * waveL1
			}

		var l2Phase: Double
			get() = l2Phaserange / waveL2
			set(value) {
				l2Phaserange = value * waveL2
			}

		companion object : RtcmSatCommonDef_1009_1012<Sat1012>(true, true) {
			override fun binding(bb: ByteBuffer, structOffset: Int): Sat1012 = Sat1012(bb, structOffset)
		}
	}

	companion object : RtcmCommonDef_1009_1012<Rtcm1012, Sat1012>(Sat1012.Companion, 1012) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1012 = Rtcm1012(bb, structOffset)
	}
}
