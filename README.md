# Anomaly Detection of Time-based Graphs
This is a library to find anomalies in graphs with timestamps. This implements MIDAS and AnomRank algorithms (with minor modifications) to calculate the metrics on the probability that a node or edge is an anomaly. These metric computations rely on two parameters: time increment and threshold. The time increments are used to compare the change in the graph through the addition of edge, and the threshold is used to define what will be flagged. 


## Installation
This project is not currently part of any repositories, so the easiest way to use it would be to download the source. You can `git clone` this to your local machine. If you have Leiningen installed, within this directory, run `lein uberjar` to build the .jar file needed to run the functions here. This jar file can be copied to your project under `resources` and add the file as a resource in your package manager.


## Detection Algorithms
The detection algorithms used are meant to cover the types of graph anomalies: edge, node, event, and subgraph. MIDAS and AnomRank cover all of these by looking at the temporal behavior of edges and nodes with comparsions to previous intervals without assuming any probablity distribution. These profile the existing graph and look for behavior that is dissimilar. This will include changes in density, weights, and volume, but the case not yet covered is the comparison of subgraphs with itself. 

### MIDAS
Microcluster-Based Detector of Anomalies in Edge Streams (MIDAS) attempts to evaluate nodes and edges within a time increment. This is done by approximating the number of edges between nodes u,v within a period and comparing this behavior to the a dampened accumation of historical behavior using a hypothesis test. This blog post explains the mechanics and usage well: https://towardsdatascience.com/controlling-fake-news-using-graphs-and-statistics-31ed116a986f. Here, this is modified from the original context of network security, where the goal is to detect scanners, intrusions, and denial-of-service attacks, to a general case where the edges can be weighted. Rather than incremeneting the data structure by one, we use the edge's assigned weight. This algorithm also approximates the number of edges a node has within a time period and this is also used as a metric on the stability of a nodes' behavior.

See "Microcluster-Based Detector of Anomalies in Edge Streams" by Bhatia, Hooi, Yoon, Shin, and Faloutsos at https://arxiv.org/abs/1911.04464.

### AnomRank
Anomaly Ranking (AnomRank) iteratively calculates the PageRank score for each node whenever an edge is added. A node is flagged as anomalous based on its PageRank score's first and second derivates. PageRank is a measure of centrality that is recursively defined for a graph. If we have a page A, dampening factor d, and set of pages that direct to A, 
<center>PR(A) = (1-d) + d * (PR(T_1) / C(T_1) + ... + PR(T_n) / C(T_n)), </center>

where PR(page) = PageRank, C(page) = number (or total weight) of outbound edges. For this algorithm (with our modifications), this score is calculated iteratively, so when an edge is added, its PageRank is calculated. If the change in the node's PageRank is greater than some epsilon, then its connected neighbors are added to the queue to be recalcuated.

See "Fast and Accurate Anomaly Detection in Dynamic Graphs with a Two-Pronged Approach" by Yoon, Hooi, Shin, and Faloutsos at https://arxiv.org/abs/2011.13085.

### Subgraph Anomalies with k-cores
In an undirected graph, the k-core is the maximal subgraph where every vertex is adjacent to at least k vertices. This concept is applied here to find anomalies within our graph. Patterns:
- Mirror Pattern: the coreness of a vertex (maximum k such that the vertex is part of the k-core) is correlated with degree.
- Core-Triange Pattern: the degeneracy (maximum k where a k-core exists) and triangle count obey a power-law with slope 1/3.
- Structured Core Pattern: the degeneracy-cores are not cliques but have non-trivial structures.

Of these patterns, the most useful in finding anomalies is the mirror pattern, where an anomaly is more likely to have a higher degree than coreness with regards to other nodes. Calcualting "coreness" of a large database would be difficult since it would require multiple iterations for each candidate value for k (max k approx. 1/3 number of triangles). Therefore this project uses a substantive time period of data so that it is representative of a larger set of information, and then calculates the coreness of the nodes in this time period.

See "Patterns and anomalies in k-cores of real-world graphs with applications" by Shin, Eliassi-Rad, and Faloutsos at https://doi.org/10.1007/s10115-017-1077-6.


## To-Do
- Extend local storage abilities to allow for database queries larger than allocated RAM 
- Implement Onyx for usage in a distributed system
- More extensive testing framework?


## License

MIT License

Copyright (c) 2021 Richard Schneider

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
