package aero.geosystems.formats.rtcm3.messages

import aero.geosystems.formats.StructBinding
import aero.geosystems.gnss.RawSignal
import aero.geosystems.gnss.SatSystem
import java.nio.ByteBuffer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

enum class MsmGpsSignalCode(val code: Int, val signal: RawSignal?, val df016: Int?) {
	GPS_L1C(2, RawSignal.GPS_L1_CA, null),
	GPS_L1P(3, RawSignal.GPS_L1_PY, null),
	GPS_L1W(4, null/*RawSignal.GPS_L1_W*/, null),
	GPS_L2C(8, RawSignal.GPS_L2_C, 0),
	GPS_L2P(9, RawSignal.GPS_L2_PY_DIRECT, 1),
	GPS_L2W(10, RawSignal.GPS_L2_PY_CROSS, 2),
	GPS_L2S(15, RawSignal.GPS_L2C_M, 0),
	GPS_L2L(16, RawSignal.GPS_L2C_L, 0),
	GPS_L2X(17, RawSignal.GPS_L2C_ML, 0),
	GPS_L5I(22, /*RawSignal.GPS_L5_I*/null, null),
	GPS_L5Q(23, RawSignal.GPS_L5_Q, null),
	GPS_L5X(24, RawSignal.GPS_L5_IQ, null),
	GPS_L1_C_D(30, /*RawSignal.GPS_L1_C_D*/null, null),
	GPS_L1_C_P(31, /*RawSignal.GPS_L1_C_P*/null, null),
	GPS_L1_C_DP(32, /*RawSignal.GPS_L1_C_DP*/null, null),
	// TODO BDS QZS GAL
	GLO_L1C(2, RawSignal.GLO_L1_CA, null),
	GLO_L1P(3, RawSignal.GLO_L1_P, null),
	GLO_L2C(8, RawSignal.GLO_L2_CA, null),
	GLO_L2P(9, RawSignal.GLO_L2_P, null),

	GAL_E1C(2, RawSignal.GAL_E1C, null),
	GAL_E1A(3, null/*RawSignal.GAL_E1A*/, null),
	GAL_E1B(4, RawSignal.GAL_E1B, null),
	GAL_E1BC(5, RawSignal.GAL_E1BC, null),
	GAL_E1ABC(6, null/*RawSignal.GAL_E1CABC*/, null),
	GAL_E6C(8, null/*RawSignal.GAL_E6C*/, null),
	GAL_E6A(9, null/*RawSignal.GAL_E6A*/, null),
	GAL_E6B(10, null/*RawSignal.GAL_E6B*/, null),
	GAL_E6BC(11, null/*RawSignal.GAL_E6BC*/, null),
	GAL_E6ABC(12, null/*RawSignal.GAL_E6ABC*/, null),
	GAL_E5BI(14, RawSignal.GAL_E5b_I, null),
	GAL_E5BQ(15, RawSignal.GAL_E5b_Q, null),
	GAL_E5BIQ(16, RawSignal.GAL_E5b_IQ, null),
	GAL_E5ABI(18, null/*RawSignal.GAL_E5ABI*/, null),
	GAL_E5ABQ(19, null/*RawSignal.GAL_E5ABQ*/, null),
	GAL_E5ABIQ(20, null/*RawSignal.GAL_E5ABIQ*/, null),
	GAL_E5AI(22, RawSignal.GAL_E5a_IQ, null),
	GAL_E5AQ(23, RawSignal.GAL_E5a_Q, null),
	GAL_E5AIQ(24, null/*RawSignal.GAL_E5AIQ*/, null),

	QZS_L1C(2,RawSignal.QZSS_L1CA,null),
	QZS_LEXS(9,null/*RawSignal.QZSS_LEXS*/,null),
	QZS_LEXL(10,null/*RawSignal.QZSS_LEXL*/,null),
	QZS_LEXSL(11,null/*RawSignal.QZSS_LEXSL*/,null),
	QZS_L2CM(15,RawSignal.QZSS_L2C_M,null),
	QZS_L2CL(16,RawSignal.QZSS_L2C_L,null),
	QZS_L2CML(17,RawSignal.QZSS_L2C_ML,null),
	QZS_L5I(22,null/*RawSignal.QZSS_L5I*/,null),
	QZS_L5Q(23,RawSignal.QZSS_L5Q,null),
	QZS_L5IQ(24,RawSignal.QZSS_L5_IQ,null),
	QZS_L1CD(30,RawSignal.QZSS_L1C_DP,null),
	QZS_L1CP(31,null/*RawSignal.QZSS_L1CP*/,null),
	QZS_L1CDP(32,null/*RawSignal.QZSS_L1CDP*/,null),

	BDS_1I(2,RawSignal.BDS_B1D1,null),
	BDS_1Q(3,null/*RawSignal.BDS_B1Q,*/,null),
	BDS_1IQ(4,null/*RawSignal.BDS_B1IQ,*/,null),
	BDS_3I(8,RawSignal.BDS_B3D1,null),
	BDS_3Q(9,null/*RawSignal.BDS_B3Q,*/,null),
	BDS_3IQ(10,null/*RawSignal.BDS_B3IQ,*/,null),
	BDS_2I(14,RawSignal.BDS_B2D1,null),
	BDS_2Q(15,null/*RawSignal.BDS_B2Q*/,null),
	BDS_2IQ(16,null/*RawSignal.BDS_B2IQ*/,null);
	companion object {
		fun byCode(gnss: SatSystem,code: Int) = values().find { it.code==code && gnss == it.signal?.gnss }
	}
}

abstract class RtcmMsmGpsDef<BINDING : RtcmMsmCommon<Long, BINDING>>(mid_const: Int) :
		RtcmMsmCommonDef<Long, BINDING>(SatSystem.GPS, mid_const - 1070, mid_const) {
	override fun gnss_epoch_def_gen(): ReadWriteProperty<StructBinding, Long> = DF004()
}

class Rtcm1071(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1071>(Companion, bb, offset) {
	companion object : RtcmMsmGpsDef<Rtcm1071>(1071) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1071 = Rtcm1071(bb, structOffset)
	}
}

class Rtcm1072(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1072>(Companion, bb, offset) {
	companion object : RtcmMsmGpsDef<Rtcm1072>(1072) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1072 = Rtcm1072(bb, structOffset)
	}
}

class Rtcm1073(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1073>(Companion, bb, offset) {
	companion object : RtcmMsmGpsDef<Rtcm1073>(1073) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1073 = Rtcm1073(bb, structOffset)
	}
}

class Rtcm1074(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1074>(Companion, bb, offset) {
	companion object : RtcmMsmGpsDef<Rtcm1074>(1074) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1074 = Rtcm1074(bb, structOffset)
	}
}

class Rtcm1075(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1075>(Companion, bb, offset) {
	companion object : RtcmMsmGpsDef<Rtcm1075>(1075) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1075 = Rtcm1075(bb, structOffset)
	}
}

class Rtcm1076(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1076>(Companion, bb, offset) {
	companion object : RtcmMsmGpsDef<Rtcm1076>(1076) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1076 = Rtcm1076(bb, structOffset)
	}
}

class Rtcm1077(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1077>(Companion, bb, offset) {
	companion object : RtcmMsmGpsDef<Rtcm1077>(1077) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1077 = Rtcm1077(bb, structOffset)
	}
}

class GloMsmEpoch(val dow: Int, val epochTime: Long) {
	override fun toString(): String {
		return "${dow}d${epochTime}ms"
	}
}
abstract class RtcmMsmGlonassDef<BINDING : RtcmMsmCommon<GloMsmEpoch, BINDING>>(mid_const: Int) :
		RtcmMsmCommonDef<GloMsmEpoch, BINDING>(SatSystem.GLONASS, mid_const - 1080, mid_const) {
	inner class GloMsmEpochMember : NumberMember<GloMsmEpoch>(30), ReadWriteProperty<StructBinding, GloMsmEpoch> {
		override fun getValue(binding: StructBinding): GloMsmEpoch {
			val d = getUnsigned(binding)
			return GloMsmEpoch(d.shr(27).and(1.shl(3) - 1).toInt(), d.and(1.shl(27) - 1))
		}

		override fun setValue(thisRef: StructBinding, property: KProperty<*>, value: GloMsmEpoch) {
			setValue(value.epochTime.or(value.dow.shl(27).toLong()), thisRef)
		}
	}

	fun DF416_DF034(): GloMsmEpochMember = GloMsmEpochMember()

	override fun gnss_epoch_def_gen(): ReadWriteProperty<StructBinding, GloMsmEpoch> = DF416_DF034()
}

class Rtcm1081(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<GloMsmEpoch, Rtcm1081>(Companion, bb, offset) {
	companion object : RtcmMsmGlonassDef<Rtcm1081>(1081) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1081 = Rtcm1081(bb, structOffset)
	}
}

class Rtcm1082(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<GloMsmEpoch, Rtcm1082>(Companion, bb, offset) {
	companion object : RtcmMsmGlonassDef<Rtcm1082>(1082) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1082 = Rtcm1082(bb, structOffset)
	}
}

class Rtcm1083(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<GloMsmEpoch, Rtcm1083>(Companion, bb, offset) {
	companion object : RtcmMsmGlonassDef<Rtcm1083>(1083) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1083 = Rtcm1083(bb, structOffset)
	}
}

class Rtcm1084(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<GloMsmEpoch, Rtcm1084>(Companion, bb, offset) {
	companion object : RtcmMsmGlonassDef<Rtcm1084>(1084) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1084 = Rtcm1084(bb, structOffset)
	}
}

class Rtcm1085(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<GloMsmEpoch, Rtcm1085>(Companion, bb, offset) {
	companion object : RtcmMsmGlonassDef<Rtcm1085>(1085) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1085 = Rtcm1085(bb, structOffset)
	}
}

class Rtcm1086(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<GloMsmEpoch, Rtcm1086>(Companion, bb, offset) {
	companion object : RtcmMsmGlonassDef<Rtcm1086>(1086) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1086 = Rtcm1086(bb, structOffset)
	}
}

class Rtcm1087(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<GloMsmEpoch, Rtcm1087>(Companion, bb, offset) {
	companion object : RtcmMsmGlonassDef<Rtcm1087>(1087) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1087 = Rtcm1087(bb, structOffset)
	}
}

abstract class RtcmMsmGalileoDef<BINDING : RtcmMsmCommon<Long, BINDING>>(mid_const: Int) :
		RtcmMsmCommonDef<Long, BINDING>(SatSystem.GALILEO, mid_const - 1090, mid_const) {
	override fun gnss_epoch_def_gen(): ReadWriteProperty<StructBinding, Long> = DF004()
}

class Rtcm1091(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1091>(Companion, bb, offset) {
	companion object : RtcmMsmGalileoDef<Rtcm1091>(1091) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1091 = Rtcm1091(bb, structOffset)
	}
}

class Rtcm1092(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1092>(Companion, bb, offset) {
	companion object : RtcmMsmGalileoDef<Rtcm1092>(1092) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1092 = Rtcm1092(bb, structOffset)
	}
}

class Rtcm1093(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1093>(Companion, bb, offset) {
	companion object : RtcmMsmGalileoDef<Rtcm1093>(1093) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1093 = Rtcm1093(bb, structOffset)
	}
}

class Rtcm1094(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1094>(Companion, bb, offset) {
	companion object : RtcmMsmGalileoDef<Rtcm1094>(1094) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1094 = Rtcm1094(bb, structOffset)
	}
}

class Rtcm1095(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1095>(Companion, bb, offset) {
	companion object : RtcmMsmGalileoDef<Rtcm1095>(1095) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1095 = Rtcm1095(bb, structOffset)
	}
}

class Rtcm1096(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1096>(Companion, bb, offset) {
	companion object : RtcmMsmGalileoDef<Rtcm1096>(1096) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1096 = Rtcm1096(bb, structOffset)
	}
}

class Rtcm1097(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1097>(Companion, bb, offset) {
	companion object : RtcmMsmGalileoDef<Rtcm1097>(1097) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1097 = Rtcm1097(bb, structOffset)
	}
}

abstract class RtcmMsmQzssDef<BINDING : RtcmMsmCommon<Long, BINDING>>(mid_const: Int) :
		RtcmMsmCommonDef<Long, BINDING>(SatSystem.QZSS, mid_const - 1110, mid_const) {
	override fun gnss_epoch_def_gen(): ReadWriteProperty<StructBinding, Long> = DF004()
}

class Rtcm1111(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1111>(Companion, bb, offset) {
	companion object : RtcmMsmQzssDef<Rtcm1111>(1111) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1111 = Rtcm1111(bb, structOffset)
	}
}

class Rtcm1112(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1112>(Companion, bb, offset) {
	companion object : RtcmMsmQzssDef<Rtcm1112>(1112) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1112 = Rtcm1112(bb, structOffset)
	}
}

class Rtcm1113(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1113>(Companion, bb, offset) {
	companion object : RtcmMsmQzssDef<Rtcm1113>(1113) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1113 = Rtcm1113(bb, structOffset)
	}
}

class Rtcm1114(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1114>(Companion, bb, offset) {
	companion object : RtcmMsmQzssDef<Rtcm1114>(1114) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1114 = Rtcm1114(bb, structOffset)
	}
}

class Rtcm1115(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1115>(Companion, bb, offset) {
	companion object : RtcmMsmQzssDef<Rtcm1115>(1115) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1115 = Rtcm1115(bb, structOffset)
	}
}

class Rtcm1116(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1116>(Companion, bb, offset) {
	companion object : RtcmMsmQzssDef<Rtcm1116>(1116) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1116 = Rtcm1116(bb, structOffset)
	}
}

class Rtcm1117(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1117>(Companion, bb, offset) {
	companion object : RtcmMsmQzssDef<Rtcm1117>(1117) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1117 = Rtcm1117(bb, structOffset)
	}
}

abstract class RtcmMsmBeidouDef<BINDING : RtcmMsmCommon<Long, BINDING>>(mid_const: Int) :
		RtcmMsmCommonDef<Long, BINDING>(SatSystem.BDS, mid_const - 1120, mid_const) {
	override fun gnss_epoch_def_gen(): ReadWriteProperty<StructBinding, Long> = DF004()
}

class Rtcm1121(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1121>(Companion, bb, offset) {
	companion object : RtcmMsmBeidouDef<Rtcm1121>(1121) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1121 = Rtcm1121(bb, structOffset)
	}
}

class Rtcm1122(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1122>(Companion, bb, offset) {
	companion object : RtcmMsmBeidouDef<Rtcm1122>(1122) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1122 = Rtcm1122(bb, structOffset)
	}
}

class Rtcm1123(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1123>(Companion, bb, offset) {
	companion object : RtcmMsmBeidouDef<Rtcm1123>(1123) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1123 = Rtcm1123(bb, structOffset)
	}
}

class Rtcm1124(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1124>(Companion, bb, offset) {
	companion object : RtcmMsmBeidouDef<Rtcm1124>(1124) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1124 = Rtcm1124(bb, structOffset)
	}
}

class Rtcm1125(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1125>(Companion, bb, offset) {
	companion object : RtcmMsmBeidouDef<Rtcm1125>(1125) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1125 = Rtcm1125(bb, structOffset)
	}
}

class Rtcm1126(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1126>(Companion, bb, offset) {
	companion object : RtcmMsmBeidouDef<Rtcm1126>(1126) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1126 = Rtcm1126(bb, structOffset)
	}
}

class Rtcm1127(bb: ByteBuffer, offset: Int=0) : RtcmMsmCommon<Long, Rtcm1127>(Companion, bb, offset) {
	companion object : RtcmMsmBeidouDef<Rtcm1127>(1127) {
		override fun binding(bb: ByteBuffer, structOffset: Int): Rtcm1127 = Rtcm1127(bb, structOffset)
	}
}