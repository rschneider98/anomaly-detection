(ns anomaly-detection.queries
  "Contains all of the internal algorithms needed to manage detection algorithms"
  (:gen-class)
  (:require [neo4j-clj.core :as db]))

;; Schema
;; Nodes
;; Entity: holds money; Properties: AccountNumber
;; Cash: node for the cash transactions for an account to go to; Properties: AccountNumber

;; Relations
;; Transfer: based on the simulation data
;; Debit: based on the simulation data
;; CashIn: based on the simulation data
;; CashOut: based on the simulation data
;; Payment: based on the simulation data
;; Map contains: idFrom, idTo, weight, time

(def connection
  (db/connect (URI. "bolt://localhost:7687")
              "neo4j"
              "YA4jI)Y}D9a+y0sAj]T5s|C5qX!w.T0#u<be5w6X[p"))

(db/defquery check-node
  "MATCH (e:Entity {AccountNumber: $id})
   WITH count(*) as count
   CALL apoc.when (count > 0,
   'RETURN true AS bool',     // one or more users with user_id = 1
   'RETURN false AS bool',    // no users with user_id = 1
   {count:count}
   ) YIELD value
   return value.bool")

(db/defquery create-entity
  "CREATE (e:Entity {AccountNumber: $id})")

(db/defquery create-transfer
  "MATCH (from: Entity {AccountNumber: $idFrom}), (to: Entity {AccountNumber: $idTo})
   CREATE (from)-[:Transfer {amount: $weight, time: $time}]->(to)")

(db/defquery create-debit
  "MATCH (from: Entity {AccountNumber: $idFrom}), (to: Entity {AccountNumber: $idTo})
   CREATE (from)-[:Debit {amount: $weight, time: $time}]->(to)")

(db/defquery create-cash-in
  "MATCH (from: Entity {AccountNumber: $idFrom}), (to: Cash {AccountNumber: $idTo})
   CREATE (from)-[:CashIn {amount: $weight, time: $time}]->(to)")

(db/defquery create-cash-out
  "MATCH (from: Entity {AccountNumber: $idFrom}), (to: Cash {AccountNumber: $idTo})
   CREATE (from)-[:CashOut {amount: $weight, time: $time}]->(to)")

(db/defquery create-payment
  "MATCH (from: Entity {AccountNumber: $idFrom}), (to: Entity {AccountNumber: $idTo})
   CREATE (from)-[:Payment {amount: $weight, time: $time}]->(to)")

(defn add-edge
  "Given an edge map, add the edge to the database
   (add-edge db-session edge-map)"
  [session edge-map]
  (with-open [session]
    ((case (:relation edge-map)
      "TRANSFER" create-transfer
      "DEBIT" create-debit
      "CASH-IN" create-cash-in
      "CASH-OUT" create-cash-out
      "PAYMENT" create-payment) edge-map)))

(defn add-node
  "Given a node and properties, add the node to the database
   (add-node node)"
  [session node-map]
  (with-open [session]
    (create-entity node-map)))

(defn exists?
  "Check if a node exists"
  [session account-number]
  (with-open [session]
    (check-node account-number)))