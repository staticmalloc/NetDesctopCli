package client.Utils

import kotlin.math.*

interface GeoShape {
    fun distanceIfContains(point:GeoVec) : Double?
}

data class GeoCircle(
    val center:GeoVec,
    val radiusMeters:Double
) : GeoShape {
    override fun distanceIfContains(point:GeoVec) : Double? {
        val distance = GeoUtils.distanceCartesian(center, point)
        return if(distance < radiusMeters) { distance } else { null }
    }
}

class GeoConvexPolygon : GeoShape {
    private val center:GeoVec
    private val normals:Array<GeoVec>

    @Throws(IllegalArgumentException::class)
    constructor(points:List<GeoVec>) {
        center = GeoUtils.averageAndNormalize(points)

        normals = Array(points.size) { i1 ->
            // Нам нужно создать нормаль для всех соседних пар точек.
            // Берём остаток от деления, чтобы перебрать все пары.
            // (0, 1) ... (n-2, n-1), (n-1, 0)
            val i2 = (i1 + 1) % points.size

            val p1 = points[i1]
            val p2 = points[i2]

            val normal = GeoUtils.crossProduct(p1, p2)

            // Перебераем все точки, кроме i1 и i2
            for (testPointIndexOffset in 2 until points.size) {
                val testPoint = points[(i1 + testPointIndexOffset) % points.size]
                if (GeoUtils.dotProduct(testPoint, normal) > 0) {
                    throw IllegalArgumentException("Многоугольник не выпуклый")
                }
            }

            normal
        }
    }
    override fun distanceIfContains(point:GeoVec) : Double? {
        var contains = true
        // Если точка лежит по одну сторону для всех нормалей, то она внутри многоугольника
        for(normal in normals) {
            contains = contains.and(GeoUtils.dotProduct(point, normal) <= 0)
        }
        return if(contains) GeoUtils.distanceCartesian(point, center) else null
    }
}

class GeoVec {
    internal var x:Double
    internal var y:Double
    internal var z:Double
    internal constructor(x:Double, y:Double, z:Double) {
        this.x = x
        this.y = y
        this.z = z
    }
    constructor(location: Location) {
        val lat = Math.toRadians(location.latitude.toDouble())
        val lon = Math.toRadians(location.longtitude.toDouble())
        x = cos(lat) * cos(lon)
        y = cos(lat) * sin(lon)
        z = sin(lat)
    }
}

object GeoUtils {
    private const val EARTH_RADIUS_METERS = 6_378_100.0
    fun averageAndNormalize(points: Iterable<GeoVec>) : GeoVec {
        // Чтобы найти центр на сфере, находим центр между точками, считая среднее арифметическое
        // для каждой координаты и нормализуем получившийся вектор.

        val ret = GeoVec(0.0, 0.0, 0.0)

        for (p in points) {
            ret.x += p.x
            ret.y += p.y
            ret.z += p.z
        }

        val len = sqrt(ret.x*ret.x + ret.y*ret.y + ret.z*ret.z)
        ret.x /= len
        ret.y /= len
        ret.z /= len

        return ret
    }

    fun dotProduct(v1: GeoVec, v2: GeoVec): Double {
        return v1.x*v2.x + v1.y*v2.y + v1.z*v2.z
    }
    fun crossProduct(v1: GeoVec, v2: GeoVec): GeoVec {
        return GeoVec(
            v1.y*v2.z-v1.z*v2.y,
            v1.z*v2.x-v1.x*v2.z,
            v1.x*v2.y-v1.y*v2.x
        )
    }
    fun distanceHaversine(l1: Location, l2: Location): Double {
        // Haversine formula

        val latDistance = Math.toRadians((l1.latitude  .toDouble() - l2.latitude  .toDouble()))
        val lonDistance = Math.toRadians((l1.longtitude.toDouble() - l2.longtitude.toDouble()))
        var a = (sin(latDistance / 2) * sin(latDistance / 2)
                + (cos(Math.toRadians(l1.latitude.toDouble())) * cos(Math.toRadians(l2.longtitude.toDouble()))
                *  sin(lonDistance / 2) * sin(lonDistance / 2)))
        a = abs(a)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }
    fun distanceCartesian(v1: GeoVec, v2: GeoVec): Double {
        val dx = v1.x-v2.x
        val dy = v1.y-v2.y
        val dz = v1.z-v2.z
        return EARTH_RADIUS_METERS * sqrt(dx*dx + dy*dy + dz*dz)
    }
}