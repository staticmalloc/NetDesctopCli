package client.OSM

import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import netscape.javascript.JSObject

class OSMMap : Parent() {
    var lat00: Double = 0.0
    var lon00: Double = 0.0
    var lat11: Double = 0.0
    var lon11: Double = 0.0
    var curZoom = 15.0
    private var doc: JSObject? = null
    private var onMapLatLngZoomChanged: EventHandler<OSMMapEvent>? = null
    private var webView: WebView? = null
    private var ready = false
    private fun initMap() {
        webView = WebView()
        webView!!.engine.setJavaScriptEnabled(true)
        val path = "../../../../resources/main/evtTest.html"
        println(path)
        val resource = javaClass.getResource(path).toExternalForm()
        webView!!.engine.load(resource)
        children.add(webView)
        ready = false
        webView!!.engine.getLoadWorker().stateProperty()
            .addListener { observableValue: ObservableValue<out Worker.State>?, oldState: Worker.State?, newState: Worker.State ->
                if (newState == Worker.State.SUCCEEDED) {
                    ready = true
                }
            }
    }

    private fun initCommunication() {
        webView!!.engine.loadWorker.stateProperty()
            .addListener { observableValue: ObservableValue<out Worker.State>?, oldState: Worker.State?, newState: Worker.State ->
                if (newState == Worker.State.SUCCEEDED) {
                    doc = webView!!.engine.executeScript("window") as JSObject
                    doc!!.setMember("app", this@OSMMap)
                }
            }
    }

    private fun invokeJS(str: String) {
        webView!!.engine.executeScript(str)
        if (ready) {
            doc!!.eval(str)
        } else {
            webView!!.engine.loadWorker.stateProperty()
                .addListener { observableValue: ObservableValue<out Worker.State>?, oldState: Worker.State?, newState: Worker.State ->
                    if (newState == Worker.State.SUCCEEDED) {
                        doc!!.eval(str)
                    }
                }
        }
    }

    fun handle(
        lat00: Double,
        lon00: Double,
        lat11: Double,
        lon11: Double,
        zoom: Double
    ) {
        this.lat00 = lat00
        this.lon00 = lon00
        this.lat11 = lat11
        this.lon11 = lon11
        curZoom = zoom
    }

    fun setOnMapLatLngChanged(eventHandler: EventHandler<OSMMapEvent>?) {
        println("Current long: ")
        onMapLatLngZoomChanged = eventHandler
    }

    fun updateLongLatZoom() {
        invokeJS("getLongLatZoom()")
    }

    var width: Double
        get() = webView!!.width
        set(w) {
            webView!!.prefWidth = w
        }
    var height: Double
        get() = webView!!.height
        set(h) {
            webView!!.prefHeight = h
        }

    init {
        initMap()
        initCommunication()
    }
}