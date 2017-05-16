package aero.geosystems.formats.rtcm3.messages

import aero.geosystems.formats.rtcm3.Rtcm3Message
import aero.geosystems.formats.rtcm3.Rtcm3MessageDef
import java.nio.ByteBuffer

/**
 * Created by aimozg on 06.02.2017.
 * Confidential.
 */
class Rtcm1033(bb:ByteBuffer, offset:Int = 0): Rtcm3Message(Companion, bb, offset) {
	val refstn_id by refstn_id_def
	val antenna_descriptor_counter by antenna_descriptor_counter_def
	val antenna_descriptor by antenna_descriptor_def
	val antenna_setup_id by antenna_setup_id_def
	val antenna_serial_number_counter by antenna_serial_number_counter_def
	val antenna_serial_number by antenna_serial_number_def
	val receiver_type_descriptor_counter by receiver_type_descriptor_counter_def
	val receiver_type_descriptor by receiver_type_descriptor_def
	val receiver_firmware_version_counter by receiver_firmware_version_counter_def
	val receiver_firmware_version by receiver_firmware_version_def
	val receiver_serial_number_counter by receiver_serial_number_counter_def
	val receiver_serial_number by receiver_serial_number_def
	companion object : Rtcm3MessageDef<Rtcm1033>(1033){
		override fun binding(bb: ByteBuffer, structOffset: Int) = Rtcm1033(bb,structOffset)

		val refstn_id_def = DF003()
		val antenna_descriptor_counter_def = DF029()
		val antenna_descriptor_def = DF030(antenna_descriptor_counter_def)
		val antenna_setup_id_def = DF031()
		val antenna_serial_number_counter_def = DF032()
		val antenna_serial_number_def = DF033(antenna_serial_number_counter_def)
		val receiver_type_descriptor_counter_def = DF227()
		val receiver_type_descriptor_def = DF228(receiver_type_descriptor_counter_def)
		val receiver_firmware_version_counter_def = DF229()
		val receiver_firmware_version_def = DF230(receiver_firmware_version_counter_def)
		val receiver_serial_number_counter_def = DF231()
		val receiver_serial_number_def = DF232(receiver_serial_number_counter_def)
	}
}