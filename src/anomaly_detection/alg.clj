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