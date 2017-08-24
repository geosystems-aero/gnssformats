package aero.geosystems.formats.jps.messages

import aero.geosystems.formats.jps.JpsBand
import aero.geosystems.formats.jps.JpsMessage
import aero.geosystems.formats.jps.JpsMessageDef
import aero.geosystems.formats.jps.JpsMessageId
import aero.geosystems.gnss.GnssConstants
import aero.geosystems.gnss.SatSystem
import java.nio.ByteBuffer

/*
 * Created by aimozg on 22.08.2017.
 * Confidential unless published on GitHub
 */

class SatelliteIndices(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var usi:IntArray by usi_def.ints
	var cs:Int by cs_def

	companion object : JpsMessageDef<SatelliteIndices>(){
		override fun binding(bb: ByteBuffer, structOffset: Int) =  SatelliteIndices(bb, structOffset)
		val usi_def = VarByteArrayMember { lengthOfBody(it) - 1}
		val cs_def = UIntMember(8)

		const val GPS_USI_MIN = 1
		const val GPS_USI_MAX = 37
		const val GLO_USI_MIN = 38
		const val GLO_USI_MAX = 70
		const val GAL_USI_MIN = 71
		const val GAL_USI_MAX = 119
		const val SBAS_USI_MIN = 120
		const val SBAS_USI_MAX = 142
		const val QZSS_USI_MIN = 193
		const val QZSS_USI_MAX = 197
		const val BDS_USI_MIN = 211
		const val BDS_USI_MAX = 240
		const val GLO_FREQ_OFFSET = 45

	}
}
class GlonassNumbers(bb: ByteBuffer,bitOffset: Int): JpsMessage(Companion, bb, bitOffset) {
	var number:IntArray by number_def.ints
	var cs:Int by cs_def

	companion object : JpsMessageDef<GlonassNumbers>(){
		override fun binding(bb: ByteBuffer, structOffset: Int) = GlonassNumbers(bb,structOffset)
		val number_def = VarByteArrayMember { lengthOfBody(it) - 1 }
		val cs_def = UIntMember(8)
	}
}

class SatelliteElevations(bb: ByteBuffer,bitOffset: Int): JpsMessage(Companion, bb, bitOffset) {
	var elev:IntArray by elev_def
	var cs:Int by cs_def
	companion object : JpsMessageDef<SatelliteElevations>(){
		override fun binding(bb: ByteBuffer, structOffset: Int) = SatelliteElevations(bb,structOffset)
		val elev_def = JpsI1ArrayMember { lengthOfBody(it) - 1 }
		val cs_def = UIntMember(8)
	}
}

class SatelliteAzimuths(bb: ByteBuffer,bitOffset: Int): JpsMessage(Companion, bb, bitOffset) {
	var azim:IntArray by azim_def
	var cs:Int by cs_def
	companion object : JpsMessageDef<SatelliteAzimuths>(){
		override fun binding(bb: ByteBuffer, structOffset: Int) = SatelliteAzimuths(bb,structOffset)
		val azim_def = JpsI1ArrayMember { lengthOfBody(it) - 1 }
		val cs_def = UIntMember(8)
	}
}

abstract class AbstractMeasurementMsg(_def: JpsMessageDef<*>, bb: ByteBuffer, offset: Int) : JpsMessage(_def, bb, offset) {
	abstract val myMessageId: JpsMessageId
	abstract val myBand: JpsBand
}

abstract class AbstractIndependentPsrMsg(_def: JpsMessageDef<*>, bb: ByteBuffer, offset: Int) : AbstractMeasurementMsg(_def, bb, offset) {
	abstract fun getPseudorangeMeters(idx: Int, gnss: SatSystem): Double?
	fun getPseudorangeSeconds(idx: Int, gnss: SatSystem): Double? {
		val m = getPseudorangeMeters(idx, gnss)
		return if (m == null) null else m / GnssConstants.C
	}
}

abstract class AbstractFullPsrMsgDef<out BINDING : AbstractFullPsrMsg>:JpsMessageDef<BINDING>() {
	val pr_def = JpsF8ArrayMember { lengthOfBody(it) - 1 }
	val cs_def = UIntMember(8)
}
abstract class AbstractFullPsrMsg(_def: AbstractFullPsrMsgDef<*>, bb: ByteBuffer, offset: Int) : AbstractIndependentPsrMsg(_def, bb, offset) {
	/**
	 * L1 C/A pseudoranges [s]
	 */
	var pr: Array<Double?> by _def.pr_def
	var cs: Int by _def.cs_def


	override fun getPseudorangeMeters(idx: Int, gnss: SatSystem): Double? {
		return pr[idx]?.times(GnssConstants.C)
	}
}
