(ns cljfx.examples.cached-listener
  (:use cljfx.core)
  (:import (javafx.scene.control Button)
           (javafx.scene.layout VBox)
           (javafx.collections ObservableList)
           (java.util Collection)))

(set! *warn-on-reflection* true)

(defn debug [x] (prn x) x)

(defn on-click [_ e]
  (prn e))

(let [root (VBox.)
      btn (Button. "Abc")
      lsn (Button. "Add Listener")
      unlsn (Button. "Remove Listener")]
  (v! btn :on-action (debug (listener :event on-click)))
  (v! lsn :on-action (listener :event
                               (fn [_ e]
                                 (println "add listener")
                                 (v! btn :on-action (debug (listener :event on-click))))))
  (v! unlsn :on-action (listener :event
                                 (fn [_ e]
                                   (println "remove listener")
                                   (v! btn :on-action nil))))
  (-> root
      ^ObservableList .getChildren
      (.setAll [btn lsn unlsn]))
  (launch root))