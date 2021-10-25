package client;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Controller controller = new Controller(primaryStage);
        controller.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
