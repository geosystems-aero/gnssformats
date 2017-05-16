package aero.geosystems.gnss

fun Long.gpstimeWithGuessedWeeks(refGpstime:Long) = GnssUtils.addGuessedWeek(refGpstime,this)
fun Long.gloms2gpstime(refGpsTime:Long) = GnssUtils.gloms2gpstime(this,refGpsTime)