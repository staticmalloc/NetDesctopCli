package client.Utils

class Location(var latitude: Float = 0f, var longtitude: Float = 0f) {
    override fun toString(): String {
        return "[ Lat/Lon $latitude, $longtitude ]"
    }
}