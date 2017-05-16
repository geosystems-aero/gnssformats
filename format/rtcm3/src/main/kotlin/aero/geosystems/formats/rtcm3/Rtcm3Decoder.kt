package aero.geosystems.formats.rtcm3

import aero.geosystems.formats.AbstractGnssDecoder
import aero.geosystems.formats.IGnssDataConsumer
import aero.geosystems.formats.NopGnssConsumer
import aero.geosystems.formats.rtcm3.messages.GloMsmEpoch
import aero.geosystems.formats.rtcm3.messages.RtcmCommon_1001_1004
import aero.geosystems.formats.rtcm3.messages.RtcmCommon_1009_1012
import aero.geosystems.formats.rtcm3.messages.RtcmMsmCommon
import aero.geosystems.gnss.gloms2gpstime
import aero.geosystems.gnss.gpstimeWithGuessedWeeks
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by aimozg on 30.01.2017.
 * Confidential.
 */
class Rtcm3Decoder(var refGpsTime: Long,
                   sink: IGnssDataConsumer<Rtcm3Message> = NopGnssConsumer) :
		AbstractGnssDecoder<Rtcm3Message>(6, 1, sink){
	private var header = Rtcm3UnknownMessage(headerBuffer)

	override fun resetHeader() {
		super.resetHeader()
		header.clearCaches()
	}

	override fun syncByte(idx: Int): Byte {
		return PREAMBLE_VALUE.toByte()
	}

	override fun checkMinimalHeader(): Boolean {
		return header.message_id>1000
	}

	override fun allocateMessageBuffer(): ByteBuffer {
		return ByteBuffer.allocateDirect(header.totalMessageSize).order(ByteOrder.BIG_ENDIAN);
	}

	override fun crcGood(): Boolean {
		val message = Rtcm3UnknownMessage(messageBuffer)
		if (messageBuffer.limit()<message.totalMessageSize) return false
		val has = message.crc
		val calc = message.calcCrc()
		return has == calc
	}

	override fun completeAndConsumeMessage() {
		var message: Rtcm3Message = Rtcm3UnknownMessage(messageBuffer)
		val mid = message.message_id
		val clazz = Rtcm3MessageIDs[mid]
		if (clazz != null) message = clazz.binding(messageBuffer,0)
		val gpstime = when(mid){
			1001,1002,1003,1004 ->
				(message as RtcmCommon_1001_1004<*>).gps_epoch.gpstimeWithGuessedWeeks(refGpsTime)
			1009,1010,1011,1012 ->
				(message as RtcmCommon_1009_1012<*>).glo_epoch.toLong().gloms2gpstime(refGpsTime)
			1071,1072,1073,1074,1075,1076,1077 ->
				((message as RtcmMsmCommon<*,*>).gnss_epoch as Long).gpstimeWithGuessedWeeks(refGpsTime)
			1081,1082,1083,1084,1085,1086,1087 ->
				((message as RtcmMsmCommon<*,*>).gnss_epoch as GloMsmEpoch).epochTime.gloms2gpstime(refGpsTime)
			else -> null
		}
		if (gpstime != null) refGpsTime = gpstime
		/*
		if (mid == 1013) {
			refGpstime = GnssUtils.addGuessedWeek(
					GnssUtils.mjdToGpstime((message.body as Body1013).mjd_number.get(),refGpstime%GnssUtils.MS_IN_DAY),
					GnssUtils.extractMs(refGpstime));
		}

		 */
		val type:Int = when {
			OBSERVATION_MIDS.contains(mid) -> IGnssDataConsumer.TYPE_OBSERVATIONS
			EPHEMERIS_MIDS.contains(mid) -> IGnssDataConsumer.TYPE_EPHEMERIS
			else -> IGnssDataConsumer.TYPE_OTHER_DATA
		}
		sink.consume(message,messageBuffer,gpstime,type)
	}

}