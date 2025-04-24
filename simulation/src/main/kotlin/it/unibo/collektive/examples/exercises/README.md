# Combining Spatial Computing Blocks:

1. Select a node identified as `source`, chosen by finding the node with minimum uid in the network, assuming that the diameter of the network is no more than 10 hops [-> `SearchSource.kt`](SearchSource.kt). 
2. Compute the distances between any node and the `source` using the adaptive bellman-ford algorithm [-> `DistanceToSource.kt`](DistanceToSource.kt).
3. Calculate in the source an estimate of the true diameter of the network (the maximum distance of a device in the network) [-> `NetworkDiameter.kt`](NetworkDiameter.kt).
4. Broadcast the diameter to every node in the network [-> `NetworkDiameter.kt`](NetworkDiameter.kt).
