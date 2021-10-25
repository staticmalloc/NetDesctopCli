package client;

import client.OSM.OSMMap;
import com.google.gson.Gson;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

public class Controller {
    private Stage primaryStage;
    private Scene scene;
    private OSMMap map;
    private String urlString = "http://37.192.189.42:1598/coverage";
    private HttpClient httpClient;
    private CoverageNetData coverageNetData;

    Controller(Stage primaryStage) throws IOException{
        this.primaryStage = primaryStage;
        init();
        setListeners();
    }

    private void init() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/mainwindow.fxml"));
        scene = new Scene(root, 1600, 900);
        primaryStage.setTitle("Mobile Network Optimization Client");
        primaryStage.setScene(scene);
        primaryStage.setMaxHeight(900);
        primaryStage.setMaxWidth(1600);
        primaryStage.setMinHeight(480);
        primaryStage.setMinWidth(720);
        map = new OSMMap();
        httpClient = HttpClient.newHttpClient();
    }

    private void setListeners(){
        StackPane pane_for_map = (StackPane)scene.lookup("#pane_for_map");
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            map.setWidth(pane_for_map.getWidth());
        });
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            map.setHeight(pane_for_map.getHeight());
        });
        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            map.setWidth(pane_for_map.getWidth());
            map.setHeight(pane_for_map.getHeight());
        });
        Button update = (Button)scene.lookup("#update");
        Label lat = (Label)scene.lookup("#lat");
        Label longtitude = (Label)scene.lookup("#longtitude");
        Label zoom = (Label)scene.lookup("#zoom");
        update.pressedProperty().addListener((obs, oldVal, newVal) -> {
            map.updateLongLatZoom();
            lat.setText(String.valueOf(map.getCurLat()));
            longtitude.setText(String.valueOf(map.getCurLong()));
            zoom.setText(String.valueOf(map.getCurZoom()));
        });
        Button showNet = (Button)scene.lookup("#shownet");
        showNet.pressedProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("lat", map.getCurLat());
                    requestBody.put("longtitude", map.getCurLong());
                    requestBody.put("connectionType", "GSM");
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(urlString))
                            .timeout(Duration.ofMinutes(1))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                            .build();
                    HttpResponse<String> response =
                            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println(response.statusCode());
                    System.out.println(response.body());
                    //PrintWriter writer = new PrintWriter("CoverageNet5KM.txt", "UTF-8");
                    //writer.println(response.body());
                    Gson temp = new Gson();
                    coverageNetData = temp.fromJson(response.body(), CoverageNetData.class);
                    //writer.close();
//                    try (Stream<String> lines = Files.lines(Paths.get("CoverageNet5KM.txt"))) {
//                        String content = lines.collect(Collectors.joining(System.lineSeparator()));
//                        System.out.println(content);
//                        Gson temp = new Gson();
//                        coverageNetData = temp.fromJson(content, CoverageNetData.class);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                System.out.println(coverageNetData.getIndex_type());
                System.out.println(coverageNetData.getSize());
                System.out.println(Arrays.toString(coverageNetData.getData()));
                System.out.println("Cur Resolution: " + map.getWidth() + "x" + map.getHeight());
                createCoverageImage();
            }
        });
    }

    private void createCoverageImage(){
        final int mapWidth = (int)map.getWidth();
        final int mapHeight = (int)map.getHeight();
        final double mapZeroLat = map.getCurZeroLat();
        final double mapZeroLong = map.getCurZeroLong();
        final double mapCenterLat = map.getCurLat();
        final double mapCenterLong = map.getCurLong();
        System.out.println("Map zero lat: " + mapZeroLat);
        System.out.println("Data start lat: " + coverageNetData.getStartlat());
        System.out.println("Map zero long: " + mapZeroLong);
        System.out.println("Data start long: " + coverageNetData.getStartlong());
        double dMapLat = Math.abs(mapZeroLat - mapCenterLat)*2;
        double dMapLong = Math.abs(mapZeroLong - mapCenterLong)*2;
        double pixelsInLong = mapWidth/dMapLong;
        double pixelsInLat = mapHeight/dMapLat;
        int zeroPixelNetY = (int) (0.6*(mapZeroLat - coverageNetData.getStartlat())/coverageNetData.getDlat())+12;
        int zeroPixelNetX = (int) (0.6*(mapZeroLong - coverageNetData.getStartlong())/coverageNetData.getDlong())-5;
        System.out.println("Zero id X: " + zeroPixelNetX);
        System.out.println("Zero id Y: " + zeroPixelNetY);
        Canvas canvas = new Canvas(mapWidth,mapHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOpacity(0.5);
        int sectorsX = (int) (dMapLong/coverageNetData.getDlong());
        int sectorsY = (int) (dMapLat/coverageNetData.getDlat());
        int sizeX = mapWidth/(sectorsX + 1);
        int sizeY = mapHeight/(sectorsY + 1);
        for(int i = sectorsY; i >= 0;  i--){
            for(int j = 0; j < sectorsX ; j++){
                double deltax = j*coverageNetData.getDlong();
                double deltay = i*coverageNetData.getDlat();
                int x0 = (int)(deltax*pixelsInLong);
                int y0 = (int)(deltay*pixelsInLat);
                int size = coverageNetData.getSize();
                int data[] = coverageNetData.getData();
                int x = (int)Math.round((zeroPixelNetX+j));
                int y = (int)((zeroPixelNetY - i));
                int rssi = 0;
                if(x < size && (y+1) < size) {
                    rssi += data[x + y * size];
                    rssi += data[x + (y+1) * size];
                    rssi += data[x + 1 + (y - 1) * size];
                    rssi += data[x + 1 + (y - 1) * size];
                    rssi/=4;
                }

                if(rssi!=0) {
                    System.out.println("RSSI: " + rssi);
                    System.out.println("X0|Y0: " + x0 + " | " + y0);
                    System.out.println("SIZEX|SIZEY: " + sizeX + " | " + sizeY);
                }
                drawRect(gc,getRSSIColor(rssi),x0,y0,sizeX,sizeY);
            }
        }

//        for(int j = 0; j < (int) (dMapLong/coverageNetData.getDlong()); j++){
//            System.out.println("J: " + j);
//            double deltax = Math.abs(((zeroPixelNetX+j)*coverageNetData.getDlong()+coverageNetData.getStartlong())-mapZeroLong);
//            int x = (int)Math.round(deltax*pixelsInLong);
//            System.out.println("X: " + x);
//            drawLine(gc,x,0,x,mapHeight-1);
//        }
//        for(int i = (int) (dMapLat/coverageNetData.getDlat()); i >= 0 ;  i--){
//            System.out.println("I: " + i);
//            double deltay = Math.abs(((zeroPixelNetY-i)*coverageNetData.getDlat()+coverageNetData.getStartlat())-mapZeroLat);
//            int y = (int)Math.round(deltay*pixelsInLat);
//            System.out.println("Y: " + y);
//            drawLine(gc,0,y,mapWidth-1,y);
//        }
        StackPane pane_for_map = (StackPane)scene.lookup("#pane_for_map");
        pane_for_map.getChildren().add(canvas);
    }
    private void drawLine(GraphicsContext gc, int x0, int y0, int x1, int y1){
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokeLine(x0, y0, x1, y1);
    }
    private void drawRect(GraphicsContext gc, Color color, int x0, int y0, int w, int h){
        gc.setFill(color);
        gc.fillRect(x0, y0, w, h);
    }

    private Color getRSSIColor(int rssi){
        if(rssi > -10)
            return Color.TRANSPARENT;
        if(rssi > -60)
            return Color.GREEN;
        if(rssi > -70)
            return Color.DARKGREEN;
        if(rssi > -80)
            return Color.YELLOW;
        if(rssi > -120)
            return Color.RED;
        return Color.DARKRED;
    }

    void show(){
        primaryStage.show();
        StackPane pane_for_map = (StackPane)scene.lookup("#pane_for_map");
        map.setWidth(pane_for_map.getWidth());
        map.setHeight(pane_for_map.getHeight());
        pane_for_map.getChildren().add(map);
        primaryStage.show();
    }


}
