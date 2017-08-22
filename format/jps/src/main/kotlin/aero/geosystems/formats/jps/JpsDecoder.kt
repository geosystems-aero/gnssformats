package aero.geosystems.formats.jps

import aero.geosystems.formats.AbstractGnssDecoder
import aero.geosystems.formats.IGnssDataConsumer
import aero.geosystems.formats.NopGnssConsumer
import java.nio.ByteBuffer

/*
 * Created by aimozg on 14.08.2017.
 * Confidential unless published on GitHub
 */

class JpsDecoder(var refGpsTime: Long,
                 sink: IGnssDataConsumer<JpsMessage> = NopGnssConsumer) :
		AbstractGnssDecoder<JpsMessage>(5, 0, sink) {
	private var header = JpsUnknown(headerBuffer)

	override fun syncByte(idx: Int): Byte = 0

	override fun checkMinimalHeader(): Boolean {
		return header.message_id_raw.all { it in MID_MIN_VALID..MID_MAX_VALID } &&
				header.length_of_body_raw.all { it in '0'..'9' || it in 'A'..'F'}
	}

	override fun allocateMessageBuffer(): ByteBuffer {
		return ByteBuffer.allocate(header.totalMessageLength)
	}

	override fun crcGood(): Boolean {

		throw UnsupportedOperationException("not implemented") //TODO implement ${CLASS_NAME}.crcGood
	}

	override fun completeAndConsumeMessage() {
		val msg = (header.messageId?.def?:JpsUnknown.def).binding(messageBuffer,0)
		when (msg) {
			else -> {
				sink.consume(msg,msg.buffer,refGpsTime,IGnssDataConsumer.TYPE_OTHER_DATA)
			}
		}
	}

	companion object {
		val MID_MIN_VALID: Char = 48.toChar()
		val MID_MAX_VALID: Char = 126.toChar()
	}
}