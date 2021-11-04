package client.RequestEntities

data class CoverageNetRequest(
    val sizeX: Int,
    val sizeY: Int,
    val data: IntArray = intArrayOf(),
    val index_type: String?
)
