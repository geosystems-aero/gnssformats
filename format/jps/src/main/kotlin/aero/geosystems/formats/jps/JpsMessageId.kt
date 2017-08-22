package aero.geosystems.formats.jps

import aero.geosystems.formats.jps.messages.*

/**
 * Created by IntelliJ IDEA.
 * User: aimozg
 * Date: 19.04.13
 * Time: 15:45
 */
enum class JpsMessageId {
	// Header Messages
	JPS_FILE_ID("JP", JpsFileId.Companion),
	MSG_FORMAT_AND_ID("MF", MsgFormatAndId.Companion),
	// Time Messages
	RECEIVER_TIME("~~", "RT", ReceiverTime.Companion),
	EPOCH_TIME("::", "ET", EpochTime.Companion),
	RECEIVER_DATE("RD", ReceiverDate.Companion),
	REF_TO_RECEIVER_TIME_OFFSET("TO", null),
	RECEIVER_TIME_DRIFT("DO", null),
	ROUGH_ACCURACY_OF_TIME("BP", null),
	GPS_TIME("GT", GpsTimeMessage.Companion),
	GPS_TO_RECEIVER_TIME_OFFSET("GO", null),
	GLONASS_TIME("NT", null),
	GLONASS_TO_RECEIVER_TIME_OFFSET("NO", null),
	GALILEO_TO_RECEIVER_TIME_OFFSET("EO", null),
	SBAS_TO_RECEIVER_TIME_OFFSET("WO", null),
	QZSS_TO_RECEIVER_TIME_OFFSET("QO", null),
	UTC_PARAMETERS("UO", UtcParameters.Companion),
	SBAS_UTC_PARAMETERS("WU", null),
	GALILEO_UTC_PARAMETERS("EU", null),
	QZSS_UTC_PARAMETERS("QU", null),
	GLONASS_UTC_PARAMETERS("NU", null),
	// Position and Velocity Messages
	SOLUTION_TIME("ST", SolutionTime.Companion),
	CARTESIAN_POSITION("PO", null),
	CARTESIAN_VELOCITY("VE", null),
	CARTESIAN_POSITION_AND_VELOCITY("PV", CartesianPosVel.Companion),
	GEODETIC_POSITION("PG", GeodeticPosition.Companion),
	GEODETIC_VELOCITY("VG", null),
	POS_VEL_RMS("SG", PosVelRmsErrors.Companion),
	DOP_PARAMETERS("DP", DopParameters.Companion),
	POSITION_COVARIANCE_MATRIX("SP", null),
	VELOCITY_COVARIANCE_MATRIX("SV", null),
	BASE_LINE("BL", BaseLine.Companion),
	POS_STATISTICS("PS", PositionStatistics.Companion),
	TIME_OF_CONT_POS("PT", null),
	BASE_INFORMATION("BI", BaseInformation.Companion),
	// Satellite Data Messages
	SATELLITE_INDICES("SI", null/*TODO SatelliteIndices.Companion*/),
	ANTENNA_NAMES("AN", null),
	GLONASS_NUMBERS("NN", null/*TODO GlonassNumbers.Companion*/),
	SATELLITE_ELEVATIONS("EL", null/*TODO SatelliteElevations.Companion*/),
	SATELLITE_AZIMUTHS("AZ", null/*TODO SatelliteAzimuths.Companion*/),
	L1CA_PSEUDORANGES("RC", null/*TODO L1CAPseudoranges.Companion*/),
	L1P_PSEUDORANGES("R1", null/*TODO L1PPseudoranges.Companion*/),
	L2P_PSEUDORANGES("R2", null/*TODO L2PPseudoranges.Companion*/),
	L2C_PSEUDORANGES("R3", null/*TODO L2CPseudoranges.Companion*/),
	L5_PSEUDORANGES("R5", null/*TODO L5Pseudoranges.Companion*/),
	//	L1C_PSEUDORANGES("RI", null /*TODO L1CPseudoranges.Companion*/),
	SHORT_L1CA_PSEUDORANGES("rc", null/*TODO ShortL1CAPseudoranges.Companion*/),
	SHORT_L1P_PSEUDORANGES("r1", null/*TODO ShortL1PPseudoranges.Companion*/),
	SHORT_L2P_PSEUDORANGES("r2", null/*TODO ShortL2PPseudoranges.Companion*/),
	SHORT_L2C_PSEUDORANGES("r3", null/*TODO ShortL2CPseudoranges.Companion*/),
	SHORT_L5_PSEUDORANGES("r5", null/*TODO ShortL5Pseudoranges.Companion*/),
	//	SHORT_L1C_PSEUDORANGES("rI", null /*TODO ShortL1CPseudoranges.Companion*/),
	RELATIVE_L1P_PSEUDORANGES("1R", null/*TODO RelativeL1PPseudoranges.Companion*/),
	RELATIVE_L2P_PSEUDORANGES("2R", null/*TODO RelativeL2PPseudoranges.Companion*/),
	RELATIVE_L2C_PSEUDORANGES("3R", null/*TODO RelativeL2CPseudoranges.Companion*/),
	RELATIVE_L5_PSEUDORANGES("5R", null/*TODO RelativeL5Pseudoranges.Companion*/),
	//	RELATIVE_L1C_PSEUDORANGES("IR", null /*TODO RelativeL1CPseudoranges.Companion*/),
	RELATIVE_SHORT_L1P_PSEUDORANGES("1r", null/*TODO RelativeShortL1PPseudoranges.Companion*/),
	RELATIVE_SHORT_L2P_PSEUDORANGES("2r", null/*TODO RelativeShortL2PPseudoranges.Companion*/),
	RELATIVE_SHORT_L2C_PSEUDORANGES("3r", null/*TODO RelativeShortL2CPseudoranges.Companion*/),
	RELATIVE_SHORT_L5_PSEUDORANGES("5r", null/*TODO RelativeShortL5Pseudoranges.Companion*/),
	//	RELATIVE_SHORT_L1C_PSEUDORANGES("Ir", null /*TODO RelativeShortL1CPPseudoranges.Companion*/),
	L1CA_PSEUDORANGE_SMOOTHING_CORRECTIONS("CC", null/*TODO L1CAPseudorangeSmoothingCorrections.Companion*/),
	L1P_PSEUDORANGE_SMOOTHING_CORRECTIONS("C1", null/*TODO L1PPseudorangeSmoothingCorrections.Companion*/),
	L2P_PSEUDORANGE_SMOOTHING_CORRECTIONS("C2", null/*TODO L2PPseudorangeSmoothingCorrections.Companion*/),
	L2C_PSEUDORANGE_SMOOTHING_CORRECTIONS("C3", null),
	L5_PSEUDORANGE_SMOOTHING_CORRECTIONS("C5", null),
	//	L1C_PSEUDORANGE_SMOOTHING_CORRECTIONS("CI", null /*TODO L1CPseudorangeSmoothingCorrections.Companion*/),
	SHORT_L1CA_PSEUDORANGE_SMOOTHING_CORRECTIONS("cc", null),
	SHORT_L1P_PSEUDORANGE_SMOOTHING_CORRECTIONS("c1", null),
	SHORT_L2P_PSEUDORANGE_SMOOTHING_CORRECTIONS("c2", null),
	SHORT_L2C_PSEUDORANGE_SMOOTHING_CORRECTIONS("c3", null),
	SHORT_L5_PSEUDORANGE_SMOOTHING_CORRECTIONS("c5", null),
	//	SHORT_L1C_PSEUDORANGE_SMOOTHING_CORRECTIONS("cI", null),
	L1CA_PHASES("PC", null/*TODO L1CAPhases.Companion*/),
	L1P_PHASES("P1", null/*TODO L1PPhases.Companion*/),
	L2P_PHASES("P2", null/*TODO L2PPhases.Companion*/),
	L2C_PHASES("P3", null/*TODO L2CPhases.Companion*/),
	L5_PHASES("P5", null/*TODO L5Phases.Companion*/),
	//	L1C_PHASES("PI",null),
	SHORT_L1CA_PHASES("pc", null/*TODO ShortL1CAPhases.Companion*/),
	SHORT_L1P_PHASES("p1", null/*TODO ShortL1PPhases.Companion*/),
	SHORT_L2P_PHASES("p2", null/*TODO ShortL2PPhases.Companion*/),
	SHORT_L2C_PHASES("p3", null/*TODO ShortL2CPhases.Companion*/),
	SHORT_L5_PHASES("p5", null/*TODO ShortL5Phases.Companion*/),
	//	SHORT_L1C_PHASES("pI", null),
	RELATIVE_L1CA_PHASES("CP", null/*TODO RelativeL1CAPhases.Companion*/),
	RELATIVE_L1P_PHASES("1P", null/*TODO RelativeL1PPhases.Companion*/),
	RELATIVE_L2P_PHASES("2P", null/*TODO RelativeL2PPhases.Companion*/),
	RELATIVE_L2C_PHASES("3P", null/*TODO RelativeL2CPhases.Companion*/),
	RELATIVE_L5_PHASES("5P", null/*TODO RelativeL5Phases.Companion*/),
	//	RELATIVE_L1C_PHASES("IP", null /*TODO RelativeL1CPhases.Companion*/),
	RELATIVE_SHORT_L1CA_PHASES("cp", null/*TODO RelativeShortL1CAPhases.Companion*/),
	RELATIVE_SHORT_L1P_PHASES("1p", null/*TODO RelativeShortL1PPhases.Companion*/),
	RELATIVE_SHORT_L2P_PHASES("2p", null/*TODO RelativeShortL2PPhases.Companion*/),
	RELATIVE_SHORT_L2C_PHASES("3p", null/*TODO RelativeShortL2CPhases.Companion*/),
	RELATIVE_SHORT_L5_PHASES("5p", null/*TODO RelativeShortL5Phases.Companion*/),
	//	RELATIVE_SHORT_L1C_PHASES("Ip",null),
	L1CA_DOPPLER("DC", null/*TODO L1CADoppler.Companion*/),
	L1P_DOPPLER("D1", null/*TODO L1PDoppler.Companion*/),
	L2P_DOPPLER("D2", null/*TODO L2PDoppler.Companion*/),
	L2C_DOPPLER("D3", null/*TODO L2CDoppler.Companion*/),
	L5_DOPPLER("D5", null/*TODO L5Doppler.Companion*/),
	//	L1C_DOPPLER("DI", null),
	RELATIVE_L1P_DOPPLER("1d", null),
	RELATIVE_L2P_DOPPLER("2d", null),
	RELATIVE_L2C_DOPPLER("3d", null),
	RELATIVE_L5_DOPPLER("5d", null),
	//	RELATIVE_L1C_DOPPLER("Id", null),
	L1CA_SNR("EC", null/*TODO L1CASignalToNoiseRatios.Companion*/),
	L1P_SNR("E1", null/*TODO L1PSignalToNoiseRatios.Companion*/),
	L2P_SNR("E2", null/*TODO L2PSignalToNoiseRatios.Companion*/),
	L2C_SNR("E3", null/*TODO L2CSignalToNoiseRatios.Companion*/),
	L5_SNR("E5", null/*TODO L5SignalToNoiseRatios.Companion*/),
	//	L1C_SNR("EI", null),
	L1CA_SNR_X4("CE", null/*TODO L1CASignalToNoiseRatiosX4.Companion*/),
	L1P_SNR_X4("1E", null/*TODO L1PSignalToNoiseRatiosX4.Companion*/),
	L2P_SNR_X4("2E", null/*TODO L2PSignalToNoiseRatiosX4.Companion*/),
	L2C_SNR_X4("3E", null/*TODO L2CSignalToNoiseRatiosX4.Companion*/),
	L5_SNR_X4("5E", null/*TODO L5SignalToNoiseRatiosX4.Companion*/),
	//	L1C_SNR_X4("IE", null),
	L1CA_SIGNAL_LOCK_LOOP_FLAGS("FC", null/*TODO L1CASignalLockLoopFlags.Companion*/),
	L1P_SIGNAL_LOCK_LOOP_FLAGS("F1", null),
	L2P_SIGNAL_LOCK_LOOP_FLAGS("F2", null),
	L2C_SIGNAL_LOCK_LOOP_FLAGS("F3", null),
	L5_SIGNAL_LOCK_LOOP_FLAGS("F5", null),
	//	L1C_SIGNAL_LOCK_LOOP_FLAGS("FI",null),
	L1CA_RAW_INPHASES("ec", null),
	L1P_RAW_INPHASES("e1", null),
	L2P_RAW_INPHASES("e2", null),
	L2C_RAW_INPHASES("e3", null),
	L5_RAW_INPHASES("e5", null),
	L1CA_RAW_QUADRATURES("qc", null),
	L1P_RAW_QUADRATURES("q1", null),
	L2P_RAW_QUADRATURES("q2", null),
	L2C_RAW_QUADRATURES("q3", null),
	L5_RAW_QUADRATURES("q5", null),
	TIME_SINCE_LOSS_OF_LOCK("TC", null/*TODO TimeSinceLossOfLock.Companion*/),
	SATELLITE_NAVIGATION_STATUS("SS", null/*TODO SatelliteNavigationStatus.Companion*/),
	IONO_DELAYS("ID", null),
	// Almanacs and Ephemeris
	GPS_ALMANAC("GA", null),
	GALILEO_ALMANAC("EA", null),
	QZSS_ALMANAC("QA", null),
	GLONASS_ALMANAC("NA", null),
	SBAS_ALMANAC("WA", null),
	GPS_EPHEMERIS("GE", null),
	GALILEO_EPHEMERIS("EN", null),
	QZSS_EPHEMERIS("QE", null),
	GLONASS_EPHEMERIS("NE", null),
	SBAS_EPHEMERIS("WE", null),
	// Raw Navigation Data
	//	GPS_RAW_NAV_DATA_OLD("GD",null),
	GPS_RAW_NAV_DATA("gd", null),
	//	QZSS_RAW_NAV_DATA_OLD("QD",null),
	QZSS_RAW_NAV_DATA("qd", null),
	//	GLONASS_RAW_NAV_DATA_OLD("LD",null),
	GLONASS_RAW_NAV_DATA("ID", null),
	SBAS_RAW_NAV_DATA("WD", null),
	GALILEO_RAW_NAV_DATA("ED", null),
	COMPASS_RAW_NAV_DATA("cd", null),
	// Spectrum messages
	SPECTRUM("sp", null),
	EXTENDED_SPECTRUM("sP", null),
	// Miscelanneous Messages
	GPS_IONO("IO", null),
	QZSS_IONO("QI", null),
	EVENT("==", "EV", null),
	WRAPPER(">>", null/*TODO Wrapper.Companion*/),
	EPOCH_END("||", "EE", null/*TODO EpochEnd.Companion*/),
	// Standard Text Messages
	RTK_PARAMETERS("RK", null/*TODO RtkParameters.Companion*/),
	// Command response messages
	ACK_REPLY("RE", null/*TODO AckReply.Companion*/),
	ERR_REPLY("ER", null);

	val code: String
	val requestCode: String
	val def: JpsMessageDef<JpsMessage>?

	val fullPath: String
		get() = "/msg/jps/" + requestCode

	val path: String
		get() = "jps/" + requestCode

	constructor(code: String, def: JpsMessageDef<JpsMessage>?) {
		this.code = code
		this.requestCode = code
		this.def = def
	}

	constructor(code: String, requestCode: String, def: JpsMessageDef<JpsMessage>?) {
		this.code = code
		this.requestCode = requestCode
		this.def = def
	}

	companion object {
		fun byCode(code:String): JpsMessageId? = values().firstOrNull { it.code == code }
	}
}
