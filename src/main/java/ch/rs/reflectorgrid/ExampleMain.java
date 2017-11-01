package ch.rs.reflectorgrid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * A small Example main class...
 */
public class ExampleMain extends Application {


  @Override
  public void start(Stage primaryStage) throws Exception {
    ExampleObject exampleObject = new ExampleObject();
    ReflectorGridV2 reflectorGrid = new ReflectorGridV2(LabelDisplayOrder.SIDE_BY_SIDE, 300)
        .setFieldNamingStrategy(DefaultFieldNamingStrategy.SPLIT_TO_CAPITALIZED_WORDS);
    GridPane gridPane = reflectorGrid.transformObjectToGrid(exampleObject);

    primaryStage.setScene(new Scene(gridPane));
    primaryStage.sizeToScene();
    primaryStage.show();

    primaryStage.setOnCloseRequest(event -> System.out.println(exampleObject));
  }

  public static void main(String[] args) {
    launch(args);
  }
}
