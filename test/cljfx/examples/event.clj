(ns cljfx.examples.event
  (:import [javafx.event EventHandler])
  (:use cljfx.core))

(set! *warn-on-reflection* true)

(defprotocol PHandler
  (inner [_]))

#_(let [f (fn [_ e] (println 1))
      eh (reify
           EventHandler
           (handle [_ e] (f _ e))
           PHandler
           (inner [_] f))]
  (prn eh)
  (prn (inner eh))
  ((inner eh) nil nil))

(defn hello-event []
  (let [root (load-fxml "event.fxml")
        c (fseek root "#count")
        txt (fseek root "#txt")
        increment (fseek root "#inc")
        add (fseek root "#add")
        remove (fseek root "#remove")

        n (atom 0)
        h (fn [_ e]
            (v! c :text (str (swap! n inc))))]
    (v! add
        :on-action (handler [_ e]
                            (add-handler! increment :action h)
                            (v! txt :text "handler added")))
    (v! remove
        :on-action (handler [_ e]
                            (remove-handler! increment :action h)
                            (v! txt :text "handler removed")))
    (launch root)))

(hello-event)