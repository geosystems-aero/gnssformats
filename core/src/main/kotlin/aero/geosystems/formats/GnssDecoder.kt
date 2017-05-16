package aero.geosystems.formats

import aero.geosystems.formats.utils.flipDuplicate
import aero.geosystems.formats.utils.minimum
import aero.geosystems.formats.utils.put
import aero.geosystems.formats.utils.subByteBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by aimozg on 05.04.2016.
 * Confidential.
 */

interface IGnssDataConsumer<in T> {
	fun consume(
			message: T?,
			buffer: ByteBuffer,
			timestamp: Long?,
			type: Int)
	fun consumeGarbage(buffer: ByteBuffer) {
		consume(null,buffer,null, TYPE_GARBAGE)
	}
	companion object {
		val TYPE_GARBAGE = 0
		val TYPE_OBSERVATIONS = 1
		val TYPE_EPHEMERIS = 2
		val TYPE_OTHER_DATA = 3

		val INSTANCE_NOOP = object: IGnssDataConsumer<Any?> {
			override fun consume(message: Any?, buffer: ByteBuffer, timestamp: Long?, type: Int) { }
		}
	}
}
abstract class GnssDataConsumer<in T> : IGnssDataConsumer<T> {
	override fun consume(message: T?, buffer: ByteBuffer, timestamp: Long?, type: Int) {
		when(type){
			IGnssDataConsumer.TYPE_GARBAGE ->
					handleGarbage(buffer)
			IGnssDataConsumer.TYPE_EPHEMERIS ->
					if (message != null && timestamp != null) handleEphemeris(message,buffer,timestamp)
					else handleOther(message,buffer,timestamp,type)
			IGnssDataConsumer.TYPE_OBSERVATIONS ->
				if (message != null && timestamp != null) handleObservations(message,buffer,timestamp)
				else handleOther(message,buffer,timestamp,type)
			// IGnssDataConsumer.TYPE_OTHER_DATA ->
			else ->
				handleOther(message,buffer,timestamp,type)
		}
	}

	open fun handleGarbage(buffer: ByteBuffer) { }

	open fun handleEphemeris(message: T, buffer: ByteBuffer, timestamp: Long) { }

	open fun handleObservations(message: T, buffer: ByteBuffer, timestamp: Long) { }

	open fun handleOther(message: T?, buffer: ByteBuffer, timestamp: Long?, type: Int) {}
}

abstract class GnssDecoder<T>(var sink: IGnssDataConsumer<T>) {
	abstract fun consume(data: ByteBuffer)
	abstract val currentBufferSize: Int
	abstract fun flush()
}

abstract class AbstractGnssDecoder<T>(
		sink: IGnssDataConsumer<T>,
		minHeaderLength:Int,
		val syncLength:Int
) : GnssDecoder<T>(sink) {
	open val byteOrder: ByteOrder get() = ByteOrder.LITTLE_ENDIAN

	@Suppress("LeakingThis")
	protected val headerBuffer = ByteBuffer.allocateDirect(minHeaderLength).order(byteOrder)
	@Suppress("LeakingThis")
	protected var messageBuffer = ByteBuffer.allocate(0).order(byteOrder)


	override val currentBufferSize: Int
		get() = if (headerBuffer.hasRemaining()) headerBuffer.position() else messageBuffer.position()

	override fun flush() {
		if (headerBuffer.position()>0) {
			if (headerBuffer.hasRemaining()) {
				sink.consumeGarbage(headerBuffer.flipDuplicate())
			} else {
				sink.consumeGarbage(messageBuffer.flipDuplicate())
			}
		}
	}
	open protected fun resetHeader() {
		headerBuffer.position(0)
	}

	abstract protected fun syncByte(idx:Int):Byte
	abstract fun checkMinimalHeader():Boolean
	abstract protected fun allocateMessageBuffer(): ByteBuffer
	abstract protected fun crcGood():Boolean
	abstract protected fun completeAndConsumeMessage()

	override fun consume(data: ByteBuffer) {
		var gpos = data.position()
		var glen = 0
		while(data.hasRemaining()) {
			if (headerBuffer.hasRemaining()) {
				// SYNC
				while (headerBuffer.position() < syncLength && data.hasRemaining()) {
					val b = data.get()
					if (syncByte(headerBuffer.position()) != b) {
						glen++
						resetHeader()
					} else {
						headerBuffer.put(b)
					}
				}
				// MINIMAL HEADER
				if (headerBuffer.position() == syncLength && data.hasRemaining()) {
					if (glen > 0) {
						sink.consumeGarbage(data.subByteBuffer(gpos, glen))
						glen = 0
						gpos = 0
					}
					val n = minimum(headerBuffer.remaining(), data.remaining())
					if (n > 0) {
						headerBuffer.put(data, n)
						if (!headerBuffer.hasRemaining()) {
							if (checkMinimalHeader()) {
								messageBuffer = allocateMessageBuffer()
								headerBuffer.rewind()
								messageBuffer.put(headerBuffer)
							} else {
								glen += headerBuffer.position()
								resetHeader()
							}
						}
					}
				}
			}
			// REST OF HEADER AND MESSAGE BODY
			if (!headerBuffer.hasRemaining() && data.hasRemaining()) {
				val n = minimum(messageBuffer.remaining(),data.remaining())
				messageBuffer.put(data,n)
			}
			// MESSAGE IS COMPLETE, CHECK CRC
			if (!headerBuffer.hasRemaining() && !messageBuffer.hasRemaining()) {
				messageBuffer.flip()
				if (crcGood()) {
					resetHeader()
					completeAndConsumeMessage()
					if (glen > 0) {
						sink.consumeGarbage(messageBuffer.subByteBuffer(gpos,glen))
						glen = 0
						gpos = 0
					}
				} else {
					glen += syncLength
					sink.consumeGarbage(messageBuffer.subByteBuffer(gpos,glen))
					glen = 0
					gpos = 0
					resetHeader()
					val subb = ByteBuffer.allocateDirect(messageBuffer.limit()-syncLength)
					messageBuffer.position(syncLength)
					subb.put(messageBuffer).flip()
					messageBuffer.position(0)
					consume(subb)
					//return;
				}
			}
		}
		if (glen > 0) {
			sink.consumeGarbage(data.subByteBuffer(gpos,glen))
		}
	}
}