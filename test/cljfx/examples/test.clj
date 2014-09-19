(ns cljfx.examples.test
  (:use cljfx.core))

(let [f1 (fn [])
      f2 (fn [])

      h1 (listener :event f1)
      h2 (listener :event f2)]
  (prn (= (listener :event f1) h1))
  (prn (= (listener :event f2) h2)))