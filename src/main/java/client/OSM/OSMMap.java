package OSM;

import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class OSMMap extends Parent {


    private double curLat;
    private double curLong;
    private double curZoom;

    public double getCurZeroLat() {
        return curZeroLat;
    }

    public double getCurZeroLong() {
        return curZeroLong;
    }

    private double curZeroLat;
    private double curZeroLong;
    private JSObject doc;
    private EventHandler<OSMMapEvent> onMapLatLngZoomChanged;
    private WebView webView;
    private WebEngine webEngine;
    private boolean ready;

    public OSMMap(){
        initMap();
        initCommunication();
    }
    public double getCurLat() {
        return curLat;
    }

    public double getCurLong() {
        return curLong;
    }

    public double getCurZoom() {
        return curZoom;
    }

    private void initMap()
    {
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.load(getClass().getResource("/resources/evtTest.html").toExternalForm());
        getChildren().add(webView);
        ready = false;
        webEngine.getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                ready = true;
            }
        });
    }
    private void initCommunication() {
        webEngine.getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED)
            {
                doc = (JSObject) webEngine.executeScript("window");
                doc.setMember("app", OSMMap.this);
            }
        });
    }
    private void invokeJS(final String str) {
        webEngine.executeScript(str);
        if(ready) {
            doc.eval(str);
        }
        else {
            webEngine.getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    doc.eval(str);
                }
            });
        }
    }
    public void handle(double lat, double lng, double zoom, double zerolat, double zerolng) {
            curLat = lat;
            curZoom = zoom;
            curLong = lng;
            curZeroLat = zerolat;
            curZeroLong = zerolng;
            System.out.println("Current long: " + lng);
            System.out.println("Current lat: " + lat);
            System.out.println("Current zoom: " + zoom);
            System.out.println("Current ZeroLat: " + zerolat);
            System.out.println("Current ZeroLong: " + zerolng);
            OSMMapEvent event = new OSMMapEvent(this, lat, lng, zoom);
    }

    public void setOnMapLatLngChanged(EventHandler<OSMMapEvent> eventHandler) {
        System.out.println("Current long: ");
        onMapLatLngZoomChanged = eventHandler;
    }

    public void setHeight(double h) {
        webView.setPrefHeight(h);
    }

    public void setWidth(double w) {
        webView.setPrefWidth(w);
    }

    public void updateLongLatZoom(){
        invokeJS("getLongLatZoom()");
    }

    public double getWidth(){
        return webView.getWidth();
    }

    public double getHeight(){
        return webView.getHeight();
    }
}
