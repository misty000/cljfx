(set! *warn-on-reflection* true)

(ns cljfx.examples.properties
  (:use cljfx.core
        cljfx.util)
  (:import [javafx.scene.control Button]))


;; lein run -m cljfx.examples.listener/change-listener

(defn properties []
  (run-later
    (let [btn (Button. "A Button")]
      (println "===== set text with java")
      (time (.setText btn "Anothor Button"))
      (time (.setText btn "Anothor Button"))
      (time (.setText btn "Anothor Button"))
      (time (.setText btn "Anothor Button"))

      (println "===== get text with java")
      (println (time (.getText btn)))
      (println (time (.getText btn)))
      (println (time (.getText btn)))
      (println (time (.getText btn)))

      (println "===== set text with v!")
      (time (v! btn :text "Third Button"))
      (time (v! btn :text "Third Button"))
      (time (v! btn :text "Third Button"))
      (time (v! btn :text "Third Button"))

      (println "===== get text with v")
      (println (time (v btn :text)))
      (println (time (v btn :text)))
      (println (time (v btn :text)))
      (println (time (v btn :text)))
      ))
  (exit))

(properties)