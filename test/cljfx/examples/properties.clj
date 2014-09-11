(ns cljfx.examples.properties
  (:use cljfx.core
        cljfx.util)
  (:import [javafx.scene.control Button]))

(set! *warn-on-reflection* true)

;; lein run -m cljfx.examples.listener/change-listener

(defn properties []
  (run-later
    (debug (time (getter-fn Button :cancel-button)))
    (debug (time (getter-fn Button :default-button)))
    (debug (time (getter-fn Button :text)))
    (debug (time (getter-fn Button :text)))
    (let [btn (Button. "A Button")
          get-text (getter-fn Button :text)]
      (debug (time (get-text btn)))
      (debug (time (.getText btn))))
    ))

(properties)