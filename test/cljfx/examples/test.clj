(ns cljfx.examples.test
  (:use cljfx.core))

(let [f1 (fn [])
      f2 (fn [])

      h1 (listener :event f1)
      h2 (listener :event f2)

      ;_ (cache-listener f1 h1)
      _ (cache-listener! f2 h2)
      ]
  (prn (= (get-cached-listener f1) h1))
  (prn (= (get-cached-listener f2) h2)))