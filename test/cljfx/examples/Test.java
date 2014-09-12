package cljfx.examples;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

/**
 * Created by misty on 2014/9/12.
 */
public class Test extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BooleanProperty booleanProperty = new SimpleBooleanProperty(false);
        Bindings.when(booleanProperty)
                .then(1)
                .otherwise(2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
