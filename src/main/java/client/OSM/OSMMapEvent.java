package client.OSM;

import javafx.event.Event;

public class OSMMapEvent extends Event {
    private double lat;
    private double lng;
    private double zoom;

    public OSMMapEvent(OSMMap map, double lat, double lng, double zoom) {
        super(map, Event.NULL_SOURCE_TARGET, Event.ANY);
        this.lat = lat;
        this.lng = lng;
        this.zoom = zoom;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getZoom(){
        return zoom;
    }
}
