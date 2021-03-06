package client

import client.OSM.OSMMap
import client.RequestEntities.CoverageNetRequest
import client.RequestEntities.CoverageRequestEntity
import com.google.gson.Gson
import javafx.beans.value.ObservableValue
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

class Controller internal constructor(private val primaryStage: Stage) {
    private var scene: Scene? = null
    private var map: OSMMap? = null
    private val urlString = "http://localhost:1598/coverage" //37.192.189.42
    private var httpClient: HttpClient? = null
    private var covarageLayer: Canvas? = null
    private var selectedType = "LTE"

    @Throws(IOException::class)
    private fun init() {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/mainwindow.fxml"))
        scene = Scene(root, 1920.0, 950.0)
        primaryStage.title = "Mobile Network Optimization Client"
        primaryStage.scene = scene
        primaryStage.maxHeight = 950.0
        primaryStage.maxWidth = 1920.0
        primaryStage.minHeight = 480.0
        primaryStage.minWidth = 720.0
        map = OSMMap()
        httpClient = HttpClient.newHttpClient()
    }

    private fun requestNet(connectionType: String, step: Int): CoverageNetRequest {
        val body = Gson().toJson(
            CoverageRequestEntity(
                map!!,
                connectionType,
                step
            )
        )
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlString))
            .timeout(Duration.ofMinutes(1))
            .header("Content-Type", "application/json")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    body.toString(),
                    StandardCharsets.UTF_8
                )
            )
            .build()
        val response =
            httpClient!!.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.statusCode())
        println(response.body())
        val temp = Gson()
        return temp.fromJson(response.body(), CoverageNetRequest::class.java)
    }

    private fun setListeners() {
        val pane_for_map = scene!!.lookup("#pane_for_map") as StackPane
        primaryStage.widthProperty()
            .addListener { _, _, _ ->
                map!!.width = pane_for_map.width
            }
        primaryStage.heightProperty()
            .addListener { _, _, _ ->
                map!!.height = pane_for_map.height
            }
        primaryStage.fullScreenProperty()
            .addListener { _, _, _ ->
                map!!.width = pane_for_map.width
                map!!.height = pane_for_map.height
            }
        val update = scene!!.lookup("#update") as Button
        val lat = scene!!.lookup("#lat") as Label
        val longtitude = scene!!.lookup("#longtitude") as Label
        val zoom = scene!!.lookup("#zoom") as Label
        update.pressedProperty()
            .addListener { _, _, _ ->
                map!!.updateLongLatZoom()
                lat.text = map!!.lat00.toString()
                longtitude.text = map!!.lon00.toString()
                zoom.text = map!!.curZoom.toString()
            }
        val showNet = scene!!.lookup("#shownet") as Button
        showNet.pressedProperty().addListener { _, _, newVal ->
            if (newVal) {
                val coverageNetData = requestNet(selectedType, 25)
                println(coverageNetData.index_type)
                println("Cur Resolution: " + map!!.width + "x" + map!!.height)
                drawCoverageImage(coverageNetData, selectedType)
            }
        }
        val combo = scene!!.lookup("#cType") as ComboBox<String>
        combo.items.addAll("GSM", "CDMA", "WCDMA", "LTE")
        combo.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            selectedType = newValue ?: "LTE"
        }
    }

    private fun setCanvasOnClickListener(canvas: Canvas) {
        canvas.setOnMouseClicked {
            if (covarageLayer != null) {
                val pane_for_map = scene!!.lookup("#pane_for_map") as StackPane
                pane_for_map.children.remove(covarageLayer)
                covarageLayer = null
            }
        }
    }

    private fun getColorSelector(connectionType: String): (Int) -> Color =
        when (connectionType) {
            "WCDMA" -> { value: Int -> getWCDMAColor(value) }
            "GSM" -> { value: Int -> getRSSIColor(value) }
            "LTE" -> { value: Int -> getRSRPColor(value) }
            else -> { _: Int -> Color.TRANSPARENT }
        }

    private fun drawCoverageImage(netData: CoverageNetRequest, connectionType: String) {
        val mapWidth = map!!.width
        val mapHeight = map!!.height
        val canvas = Canvas(mapWidth, mapHeight)
        setCanvasOnClickListener(canvas)
        val gc = canvas.graphicsContext2D
        gc.stroke = Color.TRANSPARENT
        gc.fill = Color.TRANSPARENT
        canvas.opacity = 0.5
        val sizeX = mapWidth / netData.sizeX
        val sizeY = mapHeight / netData.sizeY
        for (j in 0 until netData.sizeY) {
            for (i in 0 until netData.sizeX) {
                val x = i * sizeX
                val y = j * sizeY
                val ss = netData.data[(netData.sizeY - 1 - j) * netData.sizeX + i]
                val colorGetter = getColorSelector(connectionType)
                drawRound(gc, colorGetter(ss), x, y, sizeX, sizeY)

            }
        }
        val pane_for_map = scene!!.lookup("#pane_for_map") as StackPane
        covarageLayer = canvas
        pane_for_map.children.add(canvas)
    }

    private fun drawRound(gc: GraphicsContext, color: Color, x0: Double, y0: Double, w: Double, h: Double) {
        gc.fill = color
        val scale = 1.5f
        gc.fillOval(x0, y0, w * scale, h * scale)
    }

    private fun drawLine(gc: GraphicsContext, x0: Int, y0: Int, x1: Int, y1: Int) {
        gc.stroke = Color.GRAY
        gc.lineWidth = 1.0
        gc.strokeLine(x0.toDouble(), y0.toDouble(), x1.toDouble(), y1.toDouble())
    }

    private fun drawRect(gc: GraphicsContext, color: Color, x0: Double, y0: Double, w: Double, h: Double) {
        gc.fill = color
        gc.fillRect(x0, y0, w, h)
    }

    private fun getRSSIColor(rssi: Int): Color {
        return when {
            rssi > -10 -> Color.TRANSPARENT
            rssi > -60 -> Color.DARKGREEN
            rssi > -70 -> Color.GREEN
            rssi > -80 -> Color.YELLOW
            rssi > -120 -> Color.RED
            else -> Color.DARKRED
        }
    }

    private fun getRSRPColor(rsrp: Int): Color {
        //TODO
        return getRSSIColor(rsrp)
    }

    private fun getWCDMAColor(ss: Int): Color {
        return when {
            ss > -10 -> Color.TRANSPARENT
            ss > -55 -> Color.DARKGREEN
            ss > -85 -> Color.GREEN
            ss > -100 -> Color.YELLOW
            ss > -110 -> Color.RED
            else -> Color.DARKRED
        }
    }

    fun show() {
        primaryStage.show()
        val pane_for_map = scene!!.lookup("#pane_for_map") as StackPane
        map!!.width = pane_for_map.width
        map!!.height = pane_for_map.height
        pane_for_map.children.add(map)
        primaryStage.show()
    }

    init {
        init()
        setListeners()
    }
}