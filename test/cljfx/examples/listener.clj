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
        hover-fn (fn [_ ov]
                   (if (v rect :hover)
                     (v! txt :text "hovered.")
                     (v! txt :text "not hovered.")))
        hover-listener (listener :invalidated hover-fn)]

    (v! (fseek root "#add")
        :on-action (handler [_ e]
                            #_(listen! (p rect :hover) hover-listener)
                            (listen! (p rect :hover) :invalidated hover-fn)
                            (v! txt :text "listener added.")))

    (v! (fseek root "#remove")
        :on-action (handler [_ e]
                            #_(unlisten! (p rect :hover) hover-listener)
                            (unlisten! (p rect :hover) :invalidated hover-fn)
                            (v! txt :text "listener removed.")))

    (launch root)
    (run-later (prn (.getScene (fseek root "#add"))))))

(change-listener)