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
object NopGnssConsumer: IGnssDataConsumer<Any?> {
	override fun consume(message: Any?, buffer: ByteBuffer, timestamp: Long?, type: Int) { }
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

abstract class GnssDecoder<T>(var sink: IGnssDataConsumer<T> = NopGnssConsumer) {
	abstract fun consume(data: ByteBuffer)
	abstract val currentBufferSize: Int
	abstract fun flush()
}

abstract class AbstractGnssDecoder<T>(
		minHeaderLength: Int,
		val syncLength: Int,
		sink: IGnssDataConsumer<T> = NopGnssConsumer
) : GnssDecoder<T>(sink) {
	open val byteOrder: ByteOrder get() = ByteOrder.LITTLE_ENDIAN
	var currentOffset: Long = 0L

	@Suppress("LeakingThis")
	protected val headerBuffer = ByteBuffer.allocateDirect(minHeaderLength).order(byteOrder)!!
	@Suppress("LeakingThis")
	protected var messageBuffer = ByteBuffer.allocate(0).order(byteOrder)!!


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
		var pp = data.position()
		val state0 = "data@$pp/${data.limit()}, HB=${headerBuffer.position()}/${headerBuffer.limit()}, MB=${messageBuffer.position()}/${messageBuffer.limit()}"
		while(data.hasRemaining()) {
			if (headerBuffer.hasRemaining()) {
				// SYNC
				while (headerBuffer.position() < syncLength && data.hasRemaining()) {
					val b = data.get()
					currentOffset++
					if (syncByte(headerBuffer.position()) != b) {
						glen++
						resetHeader()
					} else {
						headerBuffer.put(b)
					}
				}
				// MINIMAL HEADER
				if (headerBuffer.position() >= syncLength && data.hasRemaining()) {
					if (glen > 0) {
						sink.consumeGarbage(data.subByteBuffer(gpos, glen))
						glen = 0
						gpos = 0
					}
					val n = minimum(headerBuffer.remaining(), data.remaining())
					if (n > 0) {
						currentOffset += n
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
			if (!headerBuffer.hasRemaining() && messageBuffer.hasRemaining() && data.hasRemaining()) {
				val n = minimum(messageBuffer.remaining(),data.remaining())
				currentOffset += n
				messageBuffer.put(data,n)
			}
			// MESSAGE IS COMPLETE, CHECK CRC
			if (!headerBuffer.hasRemaining() && !messageBuffer.hasRemaining()) {
				messageBuffer.flip()
				if (crcGood()) {
					resetHeader()
					try {
						completeAndConsumeMessage()
					} catch (e:Exception) {
						glen += syncLength
						sink.consumeGarbage(messageBuffer.subByteBuffer(gpos,glen))
						throw e
					}
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
			if (data.position() == pp) {
				error("${this.javaClass.simpleName} re-read " +state0+ "  " +
						"data@$pp/${data.limit()}, HB=${headerBuffer.position()}/${headerBuffer.limit()}, MB=${messageBuffer.position()}/${messageBuffer.limit()}"
				)
			}
			pp = data.position()
		}
		if (glen > 0) {
			sink.consumeGarbage(data.subByteBuffer(gpos,glen))
		}
	}
}