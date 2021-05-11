;; (ns anomaly-detection.core
;;   (:gen-class)
;;   (:require [ubergraph.core :as uber]
;;             (bigml.sketchy [count-min :as count-min])
;;             [org.httpkit.server :as server]
;;             [compojure.core :refer :all]
;;             [compojure.route :as route]
;;             [ring.middleware.defaults :refer :all]
;;             [clojure.pprint :as pp]
;;             [clojure.string :as str]
;;             [clojure.data.json :as json]
;;             (anomaly-detection [[alg :as alg]
;;                                 [queries :as queries]
;;                                 [reporting :as reporting]]))

;; ;; Want Count-Min structures (for nodes and edges) and list of seen nodes and edges for each relation type
;; ;; Transfer: based on the simulation data
;; ;; Debit: based on the simulation data
;; ;; CashIn: based on the simulation data
;; ;; CashOut: based on the simulation data
;; ;; Payment: based on the simulation data

;; ;; Upon call to API, will insert into count-mins and then the database.

;; ;; Time Management
;; (def time-interval 1)
;; (def time-period (atom 0))

;; ;; Counters
;; (def num-hash-bits 9)
;; (def cm-transfer-edge (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-transfer-node (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-debit-edge (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-debit-node (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-cash-in-edge (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-cash-in-node (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-cash-out-edge (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-cash-out-node (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-payment-edge (atom (count-min/create :hash-bits num-hash-bits)))
;; (def cm-payment-node (atom (count-min/create :hash-bits num-hash-bits)))

;; (def seen-transfer-edge (atom #{}))
;; (def seen-transfer-node (atom #{}))
;; (def seen-debit-edge (atom #{}))
;; (def seen-debit-node (atom #{}))
;; (def seen-cash-in-edge (atom #{}))
;; (def seen-cash-in-node (atom #{}))
;; (def seen-cash-out-edge (atom #{}))
;; (def seen-cash-out-node (atom #{}))
;; (def seen-payment-edge (atom #{}))
;; (def seen-payment-node (atom #{}))

;; (defn update-counters
;;   [cm-edges cm-nodes edge-map]
;;   (let [first-node (:idFrom edge-map)
;;         second-node (:idTo edge-map)
;;         weight (:weight edge-map)
;;         new-cm-edges (alg/weighted-insert cm-edges edge-map weight)
;;         new-cm-nodes (alg/weighted-insert (alg/weighted-insert cm-nodes first-node weight) second-node weight)]
;;     [new-cm-edges new-cm-nodes edge-map [first-node second-node]]))

;; (defn new-time-period?
;;   "Checks if still in the same time period")

;; (defn increase-increment
;;   "Checks if still in the same time period")

;; ; Simple Body Page
;; (defn simple-body-page [req]
;;   {:status  200
;;    :headers {"Content-Type" "text/html"}
;;    :body    "Hello World"})

;; ; request-example
;; (defn request-example [req]
;;   {:status  200
;;    :headers {"Content-Type" "text/html"}
;;    :body    (->>
;;              (pp/pprint req)
;;              (str "Request Object: " req))})

;; (defn add [req]
;;   {:status  200
;;    :headers {"Content-Type" "text/html"}
;;    :body (let [{:keys [idFrom idTo amount time relation]} req
;;                edge-map (assoc {} 
;;                                :idFrom idFrom 
;;                                :idTo idTo 
;;                                :weight weight 
;;                                :time time 
;;                                :relation relation)]
;;            (update-counters edge-map))})

;; (defroutes app-routes
;;   (GET "/add" [] add)
;;   (route/not-found "Error, page not found!"))

;; (defn -main
;;   "This is our main entry point"
;;   [& args]
;;   (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
;;     ; Run the server with Ring.defaults middleware
;;     (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
;;     ; Run the server without ring defaults
;;     ;(server/run-server #'app-routes {:port port})
;;     (println (str "Running webserver at http:/127.0.0.1:" port "/"))))