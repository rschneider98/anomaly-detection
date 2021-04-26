(ns anomaly-detection.alg
  "Contains all of the internal algorithms needed to manage detection algorithms"
  (:gen-class)
  (:require [ubergraph.core :as uber]))


(defn pagerank_queued
  "Iterative PageRank, calculates rank for node on graph and updates queue
   (pagerank_queued graph node threshold dampening) will compute the PageRank for the node and add connected nodes to queue if change in PageRank is greater than threshold."
  ;; If we have a page A, dampening factor d, and set of pages that direct to A, 
  ;; PR(A) = (1-d) + d * (PR(T_1) / C(T_1) + ... + PR(T_n) / C(T_n)), 
  ;; where PR(page) = PageRank, C(page) = number (or total weight) of outbound edges
  [graph node threshold dampening]
  (let [other_nodes (uber/neighbors graph node)
        pagerank
        ;; use threading so previous output is passed as last argument of next function
        ;; get distinct list of nodes with their attributes
        (->> other_nodes 
             (map (fn [n] (uber/attrs graph n)))
             (map (fn [attrs] (let [node_pagerank (:pagerank attrs)
                                    node_degree (:degree attrs)]
                                (/ node_pagerank node_degree))))
             (reduce +)
             (* dampening)
             (+ (- 1 dampening)))
        prev_pagerank (:pagerank (uber/attrs graph node))
        difference (- pagerank prev_pagerank)]
    (cond
      (> difference threshold)
      [other_nodes
       (uber/add-attrs graph node :pagerank pagerank)
       {node [pagerank prev_pagerank]}]
      :else [{} graph {}])))
  

(defn pagerank
  "Iterative PageRank, calculates rank for node on graph and updates queue
   (pagerank graph node) will calcuate with defaults (threshold=0.01, dampening=0.85)
   (pagerank graph node threshold dampening) will compute the PageRank for the node and add connected nodes to queue if change in PageRank is greater than threshold."
  ;; If we have a page A, dampening factor d, and set of pages that direct to A, 
  ;; PR(A) = (1-d) + d * (PR(T_1) / C(T_1) + ... + PR(T_n) / C(T_n)), 
  ;; where PR(page) = PageRank, C(page) = number (or total weight) of outbound edges
  [graph queue & {:keys [threshold dampening changes] :or {threshold 0.01 dampening 0.85 changes '{}}}]
  (loop [g graph
         q queue 
         log changes ]
   (let [output (pmap (fn [n] (pagerank_queued g n threshold dampening)) q)
         new_queue (future (reduce into #{} (map (fn [tuple] (get tuple 0)) output)))
         new_graph (future (reduce into (map (fn [tuple] (get tuple 1)) output)))
         new_changes (future (reduce into log (map (fn [tuple] (get tuple 2)) output)))])
     (cond (== 0 (count (deref new_queue))) ;; exit condition
           [(deref new_graph) (deref new_changes)]
           :else
           (recur new_graph new_queue new_changes))))


(defn count-min-dampen 
  "Reduce the counts in the count-min sketch data structure. Used for weighted averages of struct
   (count-min-dampen count-min) will return modified count-min with factor=0.85
   (count-min-dampen count-min factor) will return modified count-min"
  ;; Count-min stores counters in vector of a map, modify this vector and remake map
  [cm]
  (count-min-dampen cm 0.85)
  [cm factor]
  (assoc cm :counters (map (fn [x] (* factor x)) (:counters cm))))


(defn count-edge
  "Given count-min structure and edge map, add into the struct and queue of 
   time-based seen objects
   (count-edge count-min edge-map)"
)


(defn count-node
  "Given a count-min structure and node id, add into the struct and queue of
   time-based seen objects
   (count-node count-min node-id)"
)


(defn add-edge
  "Given an edge map, add the edge to the database
   (add-edge edge-map)"
)


(defn add-node
  "Given a node and properties, add the node to the database
   (add-node node-id properties)"
)


(defn concern?
  "Given a count-min structure, value to check, and threshold, see if it raises a concern
   (concern? count-min value threshold)"
)


(defn report 
  "Output concerns to a file, takes collection of maps and filename, saves as CSV
   (report concerns filename)"
)
