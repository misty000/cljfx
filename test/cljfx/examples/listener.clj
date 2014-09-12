(ns cljfx.examples.listener
  (:use cljfx.core)
  (:import [javafx.scene Node]
           [javafx.beans InvalidationListener]
           [javafx.beans.value ObservableValue]))

(set! *warn-on-reflection* true)

;; lein run -m cljfx.examples.listener/change-listener

(defn change-listener []
  (let [root (load-fxml "change-listener.fxml")
        txt (fseek root "#txt")
        rect (fseek root "#rect")
        hover-listener (listener :invalidated
                                 (fn [_ ov]
                                   (if (v rect :hover)
                                     (v! txt :text "hovered.")
                                     (v! txt :text "not hovered."))))]

    (v! (fseek root "#add")
        :on-action (handler [_ e]
                             (listen! (p rect :hover) hover-listener)
                             (v! txt :text "listener added.")))

    (v! (fseek root "#remove")
        :on-action (handler [_ e]
                             (unlisten! (p rect :hover) hover-listener)
                             (v! txt :text "listener removed.")))

    (launch root)
    (run-later (prn (.getScene (fseek root "#add"))))))

(change-listener)