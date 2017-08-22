package aero.geosystems.formats.jps.messages

import aero.geosystems.formats.jps.JpsMessage
import aero.geosystems.formats.jps.JpsMessageDef
import aero.geosystems.gnss.Datetime
import aero.geosystems.gnss.GnssUtils
import java.nio.ByteBuffer

/*
 * Created by aimozg on 22.08.2017.
 * Confidential unless published on GitHub
 */

class SolutionTime(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var time: Long by time_def
	var type: Int by type_def
	var cs: Int by cs_def

	fun gpsTime(receiverDate: ReceiverDate) = GnssUtils.unix2gps_leap(
			Datetime(receiverDate.year, receiverDate.month, receiverDate.day).
					addMillis(time).time)

	companion object : JpsMessageDef<SolutionTime>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = SolutionTime(bb, structOffset)
		val time_def = ULongMember(32)
		val type_def = UIntMember(8)
		val cs_def = UIntMember(8)
	}
}

class CartesianPosVel(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var x: Double by x_def
	var y: Double by y_def
	var z: Double by z_def
	var pSigma: Float by pSigma_def
	var vx: Float by vx_def
	var vy: Float by vy_def
	var vz: Float by vz_def
	var solType: Int by solType_def
	var cs: Int by cs_def

	companion object : JpsMessageDef<CartesianPosVel>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = CartesianPosVel(bb, structOffset)
		val x_def = Float64Member()
		val y_def = Float64Member()
		val z_def = Float64Member()
		val pSigma_def = Float32Member()
		val vx_def = Float32Member()
		val vy_def = Float32Member()
		val vz_def = Float32Member()
		val solType_def = UIntMember(8)
		val cs_def = UIntMember(8)
	}
}

class BaseInformation(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var x: Double by x_def
	var y: Double by y_def
	var z: Double by z_def
	var id: Int by id_def
	var solType: Int by solType_def
	var cs: Int by cs_def

	companion object : JpsMessageDef<BaseInformation>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = BaseInformation(bb, structOffset)
		val x_def = Float64Member()
		val y_def = Float64Member()
		val z_def = Float64Member()
		val id_def = UIntMember(16)
		val solType_def = UIntMember(8)
		val cs_def = UIntMember(8)
	}
}

class PositionStatistics(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var type: Int by type_def
	var gpsLocked: Int by gpsLocked_def
	var gloLocked: Int by gloLocked_def
	var gpsAvail: Int by gpsAvail_def
	var gloAvail: Int by gloAvail_def
	var gpsUsed: Int by gpsUsed_def
	var gloUsed: Int by gloUsed_def
	var fixProg: Int by fixProg_def
	var cs: Int by cs_def

	companion object : JpsMessageDef<PositionStatistics>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = PositionStatistics(bb, structOffset)
		val type_def = UIntMember(8)
		val gpsLocked_def = UIntMember(8)
		val gloLocked_def = UIntMember(8)
		val gpsAvail_def = UIntMember(8)
		val gloAvail_def = UIntMember(8)
		val gpsUsed_def = UIntMember(8)
		val gloUsed_def = UIntMember(8)
		val fixProg_def = UIntMember(8)
		val cs_def = UIntMember(8)
	}
}

class BaseLine(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var x: Double by x_def
	var y: Double by y_def
	var z: Double by z_def
	var sigma: Float by sigma_def
	var solType: Int by solType_def
	var id: Int by id_def
	var cs: Int by cs_def

	companion object : JpsMessageDef<BaseLine>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = BaseLine(bb, structOffset)
		val id_def = UIntMember(16)
		val x_def = Float64Member()
		val y_def = Float64Member()
		val z_def = Float64Member()
		val sigma_def = Float32Member()
		val solType_def = UIntMember(8)
		val cs_def = UIntMember(8)
	}
}

class DopParameters(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var hdop: Float by hdop_def
	var vdop: Float by vdop_def
	var tdop: Float by tdop_def
	var type: Int by type_def
	var cs: Int by cs_def

	val gdop: Double get() = Math.sqrt((hdop*hdop+vdop*vdop+tdop*tdop).toDouble())
	val pdop: Double get() = Math.sqrt((hdop*hdop+vdop*vdop).toDouble())
	companion object : JpsMessageDef<DopParameters>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = DopParameters(bb, structOffset)

		val hdop_def = Float32Member()
		val vdop_def = Float32Member()
		val tdop_def = Float32Member()
		val type_def = UIntMember(8)
		val cs_def = UIntMember(8)

	}
}

class PosVelRmsErrors(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var hpos: Float by hpos_def
	var vpos: Float by vpos_def
	var hvel: Float by hvel_def
	var vvel: Float by vvel_def
	var type: Int by type_def
	var cs: Int by cs_def
	companion object : JpsMessageDef<PosVelRmsErrors>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = PosVelRmsErrors(bb, structOffset)

		val hpos_def = Float32Member()
		val vpos_def = Float32Member()
		val hvel_def = Float32Member()
		val vvel_def = Float32Member()
		val type_def = UIntMember(8)
		val cs_def = UIntMember(8)
	}
}

class GeodeticPosition(bb: ByteBuffer, bitOffset: Int) : JpsMessage(Companion, bb, bitOffset) {
	var lat: Double by lat_def
	var lon: Double by lon_def
	var alt: Double by alt_def
	var pSigma: Float by pSigma_def
	var type: Int by type_def
	var cs: Int by cs_def
	companion object : JpsMessageDef<GeodeticPosition>() {
		override fun binding(bb: ByteBuffer, structOffset: Int) = GeodeticPosition(bb, structOffset)
		val lat_def = Float64Member()
		val lon_def = Float64Member()
		val alt_def = Float64Member()
		val pSigma_def = Float32Member()
		val type_def = UIntMember(8)
		val cs_def = UIntMember(8)

	}
}
