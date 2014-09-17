package cljfx.examples;

import com.sun.javafx.scene.NodeEventDispatcher;
import javafx.application.Application;
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
        VBox root = new VBox();
        Button button = new Button();
        root.getChildren().addAll(button);

        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();

        button.addEventHandler(ActionEvent.ACTION, System.out::println);
        button.setOnAction(System.out::println);

        EventDispatcher dispatcher = button.getEventDispatcher();
        System.out.println(dispatcher);

        NodeEventDispatcher dispatcher1 = (NodeEventDispatcher) dispatcher;
        EventHandler eventHandler = dispatcher1.getEventHandlerManager().getEventHandler(ActionEvent.ACTION);
        System.out.println(eventHandler);
    }

    public static void main(String[] args) {
//        launch(args);
        WeakHashMap<EventHandler, WeakReference<Object>> map = new WeakHashMap<>();

        EventHandler h1 = System.out::println;
        Object f1 = new Object();

        EventHandler h2 = System.out::print;
        Object f2 = new Object();

        map.put(h1, new WeakReference(f1));
        map.put(h2, new WeakReference(f2));

        System.out.println(map);

        map.forEach((k, v) -> {
            System.out.println(k + " --> " + v);
            Object o = v.get();
            System.out.println(o);
        });
    }
}
