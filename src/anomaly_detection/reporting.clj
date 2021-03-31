(ns reporting
  "Functions to store out metric calculations and records"
  (:gen-class)
  (:require [ubergraph.core :as uber]))

;; ToDo: Handle persistent storage of metric calculations
;; by creating local graph database to store metrics