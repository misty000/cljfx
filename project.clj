(defproject cljfx "0.1.14-SNAPSHOT"
            :description "JavaFX Clojure ラッパー"
            :url "https://github.com/shinmuro/cljfx"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           ;[local.oracle/javafxrt "2.2.45"]
                           ]

            :codox {:exclude cljfx.deprecated.bind}
            :profiles {:dev {:resource-paths ["sample-resources"]}}
            :global-vars {*warn-on-reflection* true}
            :aot [cljfx.primary])
