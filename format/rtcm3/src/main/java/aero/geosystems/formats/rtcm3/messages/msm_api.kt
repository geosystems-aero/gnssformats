package aero.geosystems.formats.rtcm3.messages

import aero.geosystems.formats.StructBinding
import aero.geosystems.formats.rtcm3.Rtcm3Message
import aero.geosystems.formats.rtcm3.Rtcm3MessageDef
import aero.geosystems.formats.rtcm3.Rtcm3StructDef.Companion.LIGHTMS
import aero.geosystems.formats.rtcm3.formatAs
import aero.geosystems.formats.rtcm3.joinFormatted
import aero.geosystems.gnss.SatSystem
import java.nio.ByteBuffer
import kotlin.properties.ReadWriteProperty

/**
 * Created by aimozg on 09.01.2017.
 * Confidential.
 */

abstract class RtcmMsmCommonDef<ET, BINDING : RtcmMsmCommon<ET, BINDING>>(
		val gnss: SatSystem,
		msmtype: Int,
		mid_const: Int
) : Rtcm3MessageDef<BINDING>(mid_const) {
	val nophr: Boolean = msmtype == 1
	val nopsr: Boolean = msmtype == 2
	val has_cms: Boolean = msmtype >= 4
	val has_cnr: Boolean = msmtype >= 4
	val has_rates: Boolean = msmtype == 5 || msmtype == 7
	val has_extinfo: Boolean = msmtype == 5 || msmtype == 7
	val has_hd: Boolean = msmtype >= 6

	val refstn_id_def = DF003()
	@Suppress("LeakingThis")
	val gnss_epoch_def: ReadWriteProperty<StructBinding, ET> = gnss_epoch_def_gen()
	val multiple_msg_bit_def = DF393()
	val iods_def = DF409()
	val reserved_def = DF001(7)
	val clock_steering_def = DF411()
	val ext_clock_def = DF412()
	val gnss_div_smooth_def = DF417()
	val gnss_smooth_int_def = DF418()
	val gnss_sat_mask_def = DF394()
	val gnss_sig_mask_def = DF395()
	val gnss_cell_mask_def = DF396(gnss_sat_mask_def, gnss_sig_mask_def)

	abstract protected fun gnss_epoch_def_gen(): ReadWriteProperty<StructBinding, ET>
	abstract fun getGpstime(epoch_time: ET, ref_gpstime: Long):Long

	val psr_rough_cms_def = if (has_cms) DF397array(gnss_sat_mask_def) else null
	@Suppress("LeakingThis")
	val ext_sat_info_def: ReadWriteProperty<StructBinding, IntArray>? = if (has_extinfo) ext_sta_info_def_gen(gnss_sat_mask_def) else null
	val psr_rough_mod_def = DF398array(gnss_sat_mask_def)
	val phr_rate_rough_def = if (has_rates) DF399array(gnss_sat_mask_def) else null

	protected open fun ext_sta_info_def_gen(gnss_sat_mask_def: BitMaskMember): ReadWriteProperty<StructBinding, IntArray> = MsmMaskUIntArray(gnss_sat_mask_def, 4)

	val psr_fine_def = if (has_hd) DF405array(gnss_cell_mask_def) else if (!nopsr) DF400array(gnss_cell_mask_def) else null
	val phr_fine_def = if (has_hd) DF406array(gnss_cell_mask_def) else if (!nophr) DF401array(gnss_cell_mask_def) else null
	val phr_lock_def = if (has_hd) DF407array(gnss_cell_mask_def) else if (!nophr) DF402array(gnss_cell_mask_def) else null
	val halfcy_ambind_def = if (!nophr) DF420array(gnss_cell_mask_def) else null
	val cnr_def = if (has_hd) DF408array(gnss_cell_mask_def) else if (has_cnr) DF403array(gnss_cell_mask_def) else null
	val phr_rate_fine_def = if (has_rates) DF404array(gnss_cell_mask_def) else null
}

abstract class RtcmMsmCommon<ET, BINDING : RtcmMsmCommon<ET, BINDING>>(
		override final val def: RtcmMsmCommonDef<ET, BINDING>, bb: ByteBuffer, offset: Int) : Rtcm3Message(def, bb, offset) {
	val gnss: SatSystem get() = def.gnss

	var refstn_id by def.refstn_id_def
	var gnss_epoch by def.gnss_epoch_def
	var multiple_msg_bit by def.multiple_msg_bit_def
	var iods by def.iods_def
	var reserved by def.reserved_def
	var clock_steering by def.clock_steering_def
	var ext_clock by def.ext_clock_def
	var gnss_div_smooth by def.gnss_div_smooth_def
	var gnss_smooth_int by def.gnss_smooth_int_def
	var gnss_sat_mask by def.gnss_sat_mask_def
	var gnss_sig_mask by def.gnss_sig_mask_def
	var gnss_cell_mask by def.gnss_cell_mask_def

	var psr_rough_cms by def.psr_rough_cms_def ?: errIntArrayAccessor
	var ext_sat_info by def.ext_sat_info_def ?: errIntArrayAccessor
	var psr_rough_mod by def.psr_rough_mod_def
	var phr_rate_rough by def.phr_rate_rough_def ?: errIntArrayAccessor

	var psr_fine by def.psr_fine_def ?: errDoubleArrayAccessor
	var phr_fine by def.phr_fine_def ?: errDoubleArrayAccessor
	var phr_lock by def.phr_lock_def ?: errIntArrayAccessor
	var halfcy_ambind by def.halfcy_ambind_def ?: errBoolArrayAccessor
	var cnr by def.cnr_def ?: errDoubleArrayAccessor
	var phr_rate_fine by def.phr_rate_fine_def ?: errDoubleArrayAccessor

	val numSats: Int get() = gnss_sat_mask.cardinality()
	val numSignals: Int get() = gnss_sig_mask.cardinality()
	val numCells: Int get() = gnss_cell_mask.cardinality()
	val satCodes: IntArray get() = gnss_sat_mask.stream().map { it + 1 }.toArray()
	val sigCodes: IntArray get() = gnss_sig_mask.stream().map { it + 1 }.toArray()

	fun getGpstime(ref_gpstime: Long):Long = def.getGpstime(gnss_epoch,ref_gpstime)
	data class Observation(
			val gnss: SatSystem,
			val satCode: Int,
			val sigCode: MsmSignalCode?,
			val cellIndex: Int,
			val psr_rough_cms: Int?,
			val ext_sat_info: Int?,
			val psr_rough_mod: Double,
			val phr_rate_rough: Int?,
			val psr_fine: Double?,
			val phr_fine: Double?,
			val phr_lock: Int?,
			val halfcy_ambind: Boolean?,
			val cnr: Double?,
			val phr_rate_fine: Double?
	) {
		val fqidx = if (gnss == SatSystem.GLONASS) ext_sat_info?.minus(7) else 0
		val wavelength = if (fqidx!=null && sigCode?.signal != null) sigCode.signal.wavelength(fqidx) else null
		val satGlobalIdx = gnss.indexToId(satCode)
		val psr_rough_m = ((psr_rough_cms?:0) + psr_rough_mod)* LIGHTMS
		val psr_m = psr_fine?.times(LIGHTMS)?.plus(psr_rough_m)
		val phr_m = phr_fine?.times(LIGHTMS)?.plus(psr_rough_m)
		val phr_cy = if (wavelength != null && phr_m != null) phr_m/wavelength else null
		val phr_rate_mps = phr_rate_rough?.toDouble()?.plus(phr_rate_fine?:0.0)?.times(LIGHTMS)
		val phr_rate_cyps = if (wavelength != null && phr_rate_mps != null) phr_rate_mps/wavelength else null
		override fun toString(): String {
			return "[$cellIndex]${gnss.charCode}$satCode,$sigCode" +
					(ext_sat_info?.formatAs(",xsi=%d") ?: "") +
					",psr=" + psr_rough_mod.times(LIGHTMS).formatAs("%.3f") +
					(psr_rough_cms?.times(LIGHTMS)?.formatAs("%+.3f") ?: "") +
					(psr_fine?.times(LIGHTMS)?.formatAs("%+.4f")?:"") +
					(psr_m?.formatAs("=%.4fm")?:"")+
					(phr_fine?.times(LIGHTMS)?.formatAs(",phr=psr%+.4f")?.plus(phr_m.formatAs("=%.4fm"))?:"")+
					(phr_rate_rough?.formatAs(",dop=%d")?:"")+
					(phr_rate_fine?.formatAs("%+.4f")?:"")+
					(cnr?.formatAs(",cnr=%.4f")?:"")+
					(when(halfcy_ambind){null->"";true->",hfcy+";false->",hfcy-"})
		}
	}

	fun buildObservations(ext_glofreqs:Array<Int?>?=null): List<Observation> {
		val def = def
		val psr_rough_cms = if (def.psr_rough_cms_def != null) psr_rough_cms else null
		val ext_sat_info = if (def.ext_sat_info_def != null) ext_sat_info else null
		val psr_rough_mod = psr_rough_mod
		val phr_rate_rough = if (def.phr_rate_rough_def != null) phr_rate_rough else null
		val psr_fine = if (def.psr_fine_def != null) psr_fine else null
		val phr_fine = if (def.phr_fine_def != null) phr_fine else null
		val phr_lock = if (def.phr_lock_def != null) phr_lock else null
		val halfcy_ambind = if (def.halfcy_ambind_def != null) halfcy_ambind else null
		val cnr = if (def.cnr_def != null) cnr else null
		val phr_rate_fine = if (def.phr_rate_fine_def != null) phr_rate_fine else null
		val sats = satCodes
		return cellSatSigCodes.mapIndexed { i, it ->
			val sat = sats.indexOf(it.first)
			Observation(gnss, it.first, MsmSignalCode.byCode(gnss, it.second), i,
					psr_rough_cms?.get(sat),
					ext_sat_info?.get(sat) ?:
							if (gnss==SatSystem.GLONASS && ext_glofreqs!=null) ext_glofreqs[sat-1]?.plus(7) else null,
					psr_rough_mod[sat],
					phr_rate_rough?.get(sat),
					psr_fine?.get(i),
					phr_fine?.get(i),
					phr_lock?.get(i),
					halfcy_ambind?.get(i),
					cnr?.get(i),
					phr_rate_fine?.get(i))
		}
	}

	val cellSatSigCodes: List<Pair<Int, Int>>
		get() {
			val sats = satCodes
			val sigs = sigCodes
			val numSigs = sigs.size
			return gnss_cell_mask.stream().mapToObj { sats[it / numSigs] to sigs[it % numSigs] }.iterator().asSequence().toList()
		}

	fun cellIndex(satIndex: Int, sigIndex: Int) = satIndex * numSignals + sigIndex

	override fun bodyToString(): String {

		//val profile = profile
		//var i = 0
		//profile[i] -= System.nanoTime()
		val cellSatSigCodes1 = cellSatSigCodes
		val def = def
		//val cellFreqs = cellSatSigCodes1.map { MsmSignalCode.byCode(it.second)?.signal?.frequency }
		val s0 = "$refstn_id,$gnss_epoch,$multiple_msg_bit,$iods,$reserved,$clock_steering,$ext_clock,$gnss_div_smooth,$gnss_smooth_int"
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val gnss = def.gnss
		val gnssc = gnss.charCode.toString()
		val s1 = satCodes.joinFormatted("%s", ",", prefix = ";") { "$gnssc$it" }
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s2 = sigCodes.joinFormatted("%s", ",", prefix = ";") { MsmSignalCode.byCode(gnss, it)?.name ?: "SIG#$it" }
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s3 = cellSatSigCodes1.joinFormatted("%s", ",", prefix = ";") { gnssc + it.first + "." + (MsmSignalCode.byCode(gnss, it.second)?.name ?: "SIG#$it") }
		return s0+s1+s2+s3+";"+buildObservations().joinToString(";")
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		/*val s4 = if (def.psr_rough_cms_def != null) psr_rough_cms.joinFormatted("%s", ",", ";psr=[", "]") { (it * LIGHTMS).formatAs("%.3f") } else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s5 = if (def.ext_sat_info_def != null) ext_sat_info.joinFormatted("%s", ",", ";xsi=[", "]") else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s6 = psr_rough_mod.joinFormatted("%s", ",", ";psr=[", "]") { (it * LIGHTMS).formatAs("%.4f") }
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s7 = if (def.phr_rate_rough_def != null) phr_rate_rough.joinFormatted("%s", ",", ";dop=[", "]") else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s8 = if (def.psr_fine_def != null) psr_fine.joinFormatted("%s", ",", ";psr=[", "]") { (it * LIGHTMS).formatAs("%.4f") } else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s9 = if (def.phr_fine_def != null) phr_fine.joinFormatted("%s", ",", ";phr=[", "]") {
			(it * LIGHTMS).formatAs("%.4f")
			//cellFreqs[it.index]?.div(GnssConstants.C)?.times(it.value)?.formatAs("%.5f")?:it.value.formatAs("%.5fms")
		} else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s10 = if (def.phr_lock_def != null) phr_lock.joinFormatted("%s", ",", ";lck=[", "]") else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s11 = if (def.halfcy_ambind_def != null) halfcy_ambind.joinFormatted("%s", "", ";1/2=[", "]") { if (it) "+" else "-" } else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s12 = if (def.cnr_def != null) cnr.joinFormatted(if (def.has_hd) "%.4f" else "%.0f", ",", ";cnr=[", "]") else ""
		//profile[i++] += System.nanoTime();profile[i] -= System.nanoTime()
		val s13 = if (def.phr_rate_fine_def != null) phr_rate_fine.joinFormatted("%.4f", ",", ";dop=[", "]") else ""
		//profile[i] += System.nanoTime()
		return s0 + s1 + s2 + s3 + s4 + s5 + s6 + s7 + s8 + s9 + s10 + s11 + s12 + s13*/
	}

	companion object {
		//val profile = LongArray(15)
	}

}

