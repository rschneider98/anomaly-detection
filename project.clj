(defproject anomaly-detection "0.1.0-SNAPSHOT"
  :description "Clojure project to scan Neo4J graph database for anomalies"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [aysylu/loom "1.0.2"]
                 ; graph algorithms 
                 [ubergraph "0.8.1"]
                 ; probablistic data structures
                 [bigml/sketchy "0.4.2"]
                 ; JSON library
                 [org.clojure/data.json "2.2.2"]
                 ; interface for NEO4J queries
                 [gorillalabs/neo4j-clj "4.1.2"]
                 ; Compojure - A basic routing library
                 [compojure "1.6.1"]
                 ; Our Http library for client/server
                 [http-kit "2.3.0"]
                 ; Ring defaults - for query params etc
                 [ring/ring-defaults "0.3.2"]]
  :main ^:skip-aot anomaly-detection.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:eastwood {:only-modified true
                              :parallelism? :naive
                              :debug [:progress :time]}
                   :bikeshed {:max-line-length 80}
                   :plugins [[jonase/eastwood "0.3.14"]
                             [lein-bikeshed "0.5.2"]]}
             :kaocha {:dependencies [[lambdaisland/kaocha "1.0.829"]
                                     [lambdaisland/kaocha-cloverage "1.0.75"]]}}
  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]})