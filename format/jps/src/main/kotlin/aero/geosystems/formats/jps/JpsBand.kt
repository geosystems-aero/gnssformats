package aero.geosystems.formats.jps

import aero.geosystems.gnss.RawSignal

enum class JpsBand private constructor(
		val bandIdx: Int,
		val gpsSignal: RawSignal,
		val qzssSignal: RawSignal?,
		val sbasSignal: RawSignal?,
		val galSignal: RawSignal?,
		val gloSignal: RawSignal?,
		val bdsSignal: RawSignal?) {
	L1CA(0, RawSignal.GPS_L1_CA, RawSignal.QZSS_L1CA, RawSignal.SBAS_L1CA, RawSignal.GAL_E1BC, RawSignal.GLO_L1_CA, RawSignal.BDS_B1D1),
	L1P(0, RawSignal.GPS_L1_PY, RawSignal.QZSS_L1SAIF, null, RawSignal.GAL_AltBOC_Q, RawSignal.GLO_L1_P, null),
	L2P(1, RawSignal.GPS_L2_PY_DIRECT, null, null, RawSignal.GAL_E5b_IQ, RawSignal.GLO_L2_P, null),
	L2C(1, RawSignal.GPS_L2C_ML, RawSignal.QZSS_L2C_ML, null, null, RawSignal.GLO_L2_CA, null),
	L5(2, RawSignal.GPS_L5_IQ, RawSignal.QZSS_L5_IQ, RawSignal.SBAS_L5I, RawSignal.GAL_E5a_IQ, RawSignal.GLO_L3_IQ, RawSignal.BDS_B2D1),
	L1C(3, RawSignal.GPS_L1C_DP, RawSignal.QZSS_L1C_DP, null, null, null, null)
}
