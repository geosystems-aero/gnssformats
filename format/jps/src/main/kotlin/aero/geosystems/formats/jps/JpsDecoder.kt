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
		throw UnsupportedOperationException("not implemented") //TODO implement ${CLASS_NAME}.checkMinimalHeader
	}

	override fun allocateMessageBuffer(): ByteBuffer {
		throw UnsupportedOperationException("not implemented") //TODO implement ${CLASS_NAME}.allocateMessageBuffer
	}

	override fun crcGood(): Boolean {
		throw UnsupportedOperationException("not implemented") //TODO implement ${CLASS_NAME}.crcGood
	}

	override fun completeAndConsumeMessage() {
		throw UnsupportedOperationException("not implemented") //TODO implement ${CLASS_NAME}.completeAndConsumeMessage
	}

}