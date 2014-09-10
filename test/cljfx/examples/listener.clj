(ns cljfx.examples.listener
  (:use cljfx.core))

(set! *warn-on-reflection* true)

;; lein run -m cljfx.examples.listener/change-listener

(defn change-listener []
  (let [root (load-fxml "change-listener.fxml")
        _ (prn "==" root)
        txt (fseek root "#txt")
        rect (fseek root "#rect")
        hover-listener (listener :invalidated
                                 (fn [_ ov]
                                   (if (v rect :hover)
                                     (v! txt :text "hovered.")
                                     (v! txt :text "not hovered."))))]

    (v! (fseek root "#add")
        :on-action (listened [_ e]
                             (.addListener (p rect :hover) hover-listener)
                             (v! txt :text "listener added.")))

    (v! (fseek root "#remove")
        :on-action (listened [_ e]
                             (.removeListener (p rect :hover) hover-listener)
                             (v! txt :text "listener removed.")))

    (launch root)
    (run-later (prn (.getScene (fseek root "#add"))))))