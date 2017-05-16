package aero.geosystems.formats.utils

/**
 * Created by aimozg on 01.02.2017.
 * Confidential.
 */

inline fun<T:Any> Array<T?>.getOrPut(index:Int, generator:()->T):T {
	val v1 = this[index]
	if (v1 != null) return v1
	val v2 = generator()
	this[index] = v2
	return v2
}