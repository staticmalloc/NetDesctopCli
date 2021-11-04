package client.RequestEntities

import client.OSM.OSMMap

data class CoverageRequestEntity(
    val lat00: Double,
    val lon00: Double,
    val lat11: Double,
    val lon11: Double,
    val connectionType: String,
    val step: Int
) {
    constructor(map: OSMMap, connectionType: String, step: Int) : this(
        map.lat00,
        map.lon00,
        map.lat11,
        map.lon11,
        connectionType,
        step
    )
}