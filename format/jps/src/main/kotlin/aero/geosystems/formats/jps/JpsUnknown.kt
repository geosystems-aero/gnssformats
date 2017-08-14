package aero.geosystems.formats.jps

import aero.geosystems.formats.StructBinding
import aero.geosystems.formats.readBytes
import aero.geosystems.formats.writeBytes
import java.nio.ByteBuffer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by IntelliJ IDEA.
 * User: aimozg
 * Date: 19.04.13
 * Time: 15:09
 */

class JpsUnknownDef: JpsMessageDef<JpsUnknown>() {

	val data_def: ReadWriteProperty<JpsUnknown,ByteArray> = object:Member<ByteArray>(),ReadWriteProperty<JpsUnknown,ByteArray> {
		override fun getValue(thisRef: JpsUnknown, property: KProperty<*>): ByteArray {
			return getValue(thisRef)
		}

		override fun setValue(thisRef: JpsUnknown, property: KProperty<*>, value: ByteArray) {
			val n = pos.bitSize(thisRef)
			writeBytes(thisRef.buffer,pos.start(thisRef), n, value.copyOf(n))
		}

		override fun getValue(binding: StructBinding): ByteArray {
			return readBytes(binding.buffer,
					pos.start(binding), pos.bitSize(binding))
		}

		override val pos = FnSizePosRef(prev?.pos) { length_of_body_raw_def.getValue(it).toInt() }

	}

	override fun binding(bb: ByteBuffer, structOffset: Int): JpsUnknown = JpsUnknown(bb,structOffset)
}

class JpsUnknown(bb:ByteBuffer,offset:Int=0) : JpsMessage(Companion.def,bb,offset) {
	companion object {
		val def = JpsUnknownDef()
	}

	var data: ByteArray by JpsUnknown.def.data_def

	override fun loaded() {
//		data = array(arrayOfNulls<Struct.Unsigned8>(getLengthOfBody()))
	}

	override fun toString(): String {
		val sb = StringBuilder()
		sb.append(super.toString())
		sb.append(" DATA=")
		for (b in data) {
			val byt = b.toChar()
			if (byt < ' ') sb.append(String.format("\\%02x", byt))
			else if (byt == '\\') sb.append("\\\\")
			else sb.append(byt)
		}
		return sb.toString()
	}
}
