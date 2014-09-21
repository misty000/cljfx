package cljfx.examples;

import com.sun.javafx.scene.NodeEventDispatcher;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by misty on 2014/9/12.
 */
public class Test extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
    }

    public static void main(String[] args) {
        StringProperty s1 = new SimpleStringProperty() {
            @Override
            protected void invalidated() {
                super.invalidated();
                System.out.println("invalidated");
            }
        };
        StringProperty s2 = new SimpleStringProperty();
        s1.bind(s2);

        System.out.println("----------------------");
        s2.set("aaa");
        s1.unbind();

        // ==============================================
        s1.bindBidirectional(s2);
        s1.set("1");
        s2.set("2");
    }
}
