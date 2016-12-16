/*
 * TODO(Reyna) license, adapted from code by huson
*/


package consensus;

import java.util.*;

/**
 * constructs a cluster network from splits
 * Daniel Huson, 9.2007
 */
public class ClusterNetwork {
	private ArrayList<CNNode> nodes;
	private ArrayList<ArrayList<CNEdge>> outEdges; // stored per from node
	private ArrayList<ArrayList<CNEdge>> inEdges; // stored per to node

	public ClusterNetwork() {
		nodes = new ArrayList<CNNode>();
		outEdges = new ArrayList<ArrayList<CNEdge>>();
		inEdges = new ArrayList<ArrayList<CNEdge>>();
	}

	public CNNode addNode() {
		int id = nodes.size(); // ensure unique, corresponds to position in nodes
		CNNode n = new CNNode(id);
		nodes.add(n);
		return n;
	}

	public CNNode addNode(double confidence) {
		int id = nodes.size(); // ensure unique, corresponds to position in nodes
		CNNode n = new CNNode(id, confidence);
		nodes.add(n);
		outEdges.add(new ArrayList<CNEdge>());
		inEdges.add(new ArrayList<CNEdge>());
		return n;
	}

	/**
	 * Adds an edge to the network.
	 * Assumes the to and from nodes are already in nodes.
	 */
	public CNEdge addEdge(CNNode from, CNNode to) {
		CNEdge e = new CNEdge(from, to);
		outEdges.get(from.getId()).add(e);
		inEdges.get(to.getId()).add(e);
		return e;
	}

	public void removeEdge(CNEdge e) {
		outEdges.get(e.getSource().getId()).remove(e);
		inEdges.get(e.getTarget().getId()).remove(e);
	}

	public CNNode getRoot() {
		return nodes.get(0);
	}

	public ArrayList<CNEdge> getOutEdges(CNNode v) {
		return outEdges.get(v.getId());
	}

	public ArrayList<CNEdge> getInEdges(CNNode v) {
		return inEdges.get(v.getId());
	}

	public int getInDegree(CNNode v) {
		return getInEdges(v).size();
	}

	// TODO(Reyna) method to construct from... input trees? clusters & wts?
	// at some point need to get clusters from a set of trees
	// also will need to display and take input and crap
	// TODO(Reyna) add support for samples/leaves?? they probably shouldn't be in the clusters

	/**
	 * Computes the cluster network.
	 */
	public void constructNetwork(ArrayList<PHYNode> mutationGroups, ArrayList<Cluster> clusters) {
		System.err.println("Constructing cluster network (mutation groups=" + mutationGroups.size()
			+ ", clusters=" + clusters.size() + "):");

		constructHasse(mutationGroups, clusters);
		convertHasseToClusterNetwork();
		computeConfidenceOnReticulate();
	}

	/**
	 * Construct the Hasse diagram for a set of clusters.
	 * Assumes clusters contains the cluster of all mutation groups (largest cardinality)
	 */
	public void constructHasse(ArrayList<PHYNode> mutationGroups, ArrayList<Cluster> clusters) {
		// initialize nodes, one per cluster, with ids corresponding to index in clusters array
		clusters = Cluster.getClustersSortedByDecreasingCardinality(clusters);
		for (Cluster c : clusters) {
			CNNode newNode = addNode(c.getConfidence());
		}

		CNNode root = getRoot();

		// add edges indicating subset containment
		Stack<CNNode> stack = new Stack<>();
		Set<CNNode> visited = new HashSet<CNNode>();
		for (int i = 1; i < clusters.size(); i++) {
			CNNode node = nodes.get(i);
			Cluster cluster = clusters.get(i);

			// starting from the root, find all supersets of cluster
			// and add edges from the lowest supersets to node
			visited.clear();
			stack.push(root);
			visited.add(root);
			while (stack.size() > 0) {
				CNNode v = stack.pop();
				boolean isBelowAChildOfV = false;
				for (CNEdge e : getOutEdges(v)) {
					CNNode w = e.getTarget();
					int j = w.getId();
					if (Cluster.contains(clusters.get(j), cluster)) {
						isBelowAChildOfV = true;
						if (!visited.contains(w)) {
							visited.add(w);
							stack.push(w);
						}
					}
				}
				if (!isBelowAChildOfV) {
					addEdge(v, node);
				}
			}
		}

		// set labels
		for (int i = 0; i < clusters.size(); i++) {
			CNNode v = nodes.get(i);

			// calculate the set of mutation groups represented by v but none of its children
			BitSet cluster = (BitSet) clusters.get(v.getId()).clone();
			for (CNEdge e : getOutEdges(v)) {
				CNNode w = e.getTarget();
				cluster = Cluster.setminus(cluster, clusters.get(w.getId()));
			}

			// apply the label
			ArrayList<PHYNode> label = new ArrayList<PHYNode>();
			for (int t = cluster.nextSetBit(0); t != -1; t = cluster.nextSetBit(t + 1)) {
				label.add(mutationGroups.get(t));
			}
			v.setLabel(label);
		}
	}



	/**
	 * Extend the Hasse diagram to obtain a cluster network.
	 */
	public void convertHasseToClusterNetwork() {
		// split every node that has indegree > 1 and outdegree != 1
		for (CNNode v : nodes) {
			if (getInDegree(v) > 1) {
				// add a dummy node with all of v's in edges, to be v's parent
				CNNode dummyNode = addNode();
				ArrayList<CNEdge> toDelete = new ArrayList<CNEdge>();
				for (CNEdge e : getInEdges(v)) {
					CNNode parent = e.getSource();
					CNEdge newEdge = addEdge(parent, dummyNode);
					newEdge.setReticulate();
					toDelete.add(e);
				}
				addEdge(dummyNode, v);

				for (CNEdge aToDelete : toDelete) {
					removeEdge(aToDelete);
				}
			}
		}
	}

	/**
	 * Compute confidence of reticulate edges as proportion of confidence in edges between the
	 * node and LSA going through the edge, scaled by the average confidence below the node.
	 */
	private void computeConfidenceOnReticulate() {
		ReticulationData data = new ReticulationData(nodes.size());
		computeReticulation2LSA(getRoot(), data);

		// compute the sum of non-reticulate confidence and count below each node
		double[] averageConfidenceBelow = new double[nodes.size()];
		int[] countBelow = new int[nodes.size()];
		Arrays.fill(countBelow, -1);
		computeConfidenceBelow(getRoot(), averageConfidenceBelow, countBelow);

		// convert sums into averages
		for (CNNode v : nodes) {
			if (countBelow[v.getId()] > 0) {
				double sum = averageConfidenceBelow[v.getId()];
				averageConfidenceBelow[v.getId()] = sum / countBelow[v.getId()];
			}
		}

		for (Map.Entry<CNNode, CNNode> entry : data.ret2LSA.entrySet()) {
			CNNode v = entry.getKey();
			CNNode lsa = entry.getValue();

			// compute the share of confidence for each of v's reticulate edges
			Map<CNEdge, Double> e2AverageConfidence = new HashMap<>();
			for (CNEdge e : getInEdges(v)) {

				// get all edges between e and LSA
				Stack<CNEdge> stack = new Stack<>();
				stack.push(e);
				Set<CNEdge> seen = new HashSet<>();
				seen.add(e);
				while (stack.size() > 0) {
					CNEdge f = stack.pop();
					CNNode w = f.getSource();
					if (w != lsa) {
						for (CNEdge g : getInEdges(w)) {
							if (!seen.contains(g)) {
								seen.add(g);
								stack.push(g);
							}
						}
					}
				}

				// store the average confidence for edges above e and below LSA
				e2AverageConfidence.put(e, computeAverageConfidence(seen));
			}

			// compute the total confidence among all of v's reticulate edges
			double sum = 0;
			for (Double x : e2AverageConfidence.values()) {
				sum += (x);
			}

			// give each edge its share of the average confidence below v
			for (CNEdge e : e2AverageConfidence.keySet()) {
				double share = (sum == 0 ? 0 : e2AverageConfidence.get(e) / sum);
				e.setConfidence(share * averageConfidenceBelow[e.getTarget().getId()]);
			}
		}
	}

	/**
	 * Nested class to hold the information for recursively calculating LSA nodes.
	 */
	private static class ReticulationData {
		Map<CNNode, BitSet> ret2PathSet;
		Map<CNNode, Map<CNEdge, BitSet>> ret2Edge2PathSet;
		Map<CNNode, CNNode> ret2LSA;
		ArrayList<Set<CNNode>> node2below; // set of reticulation nodes below a given node

		public ReticulationData(int numNodes) {
			ret2PathSet = new HashMap<>();
			ret2Edge2PathSet = new HashMap<>();
			ret2LSA = new HashMap<>();
			node2below = new ArrayList<Set<CNNode>>(Collections.nCopies(numNodes, new HashSet<>()));
		}	
	}

	/**
	 * Recursively compute the mapping of reticulate nodes to their LSA nodes.
	 */
	private void computeReticulation2LSA(CNNode v, ReticulationData data) {
		if (getInDegree(v) > 1) {
			// set up new paths for this node
			Map<CNEdge, BitSet> edge2PathSet = new HashMap<>();
			data.ret2Edge2PathSet.put(v, edge2PathSet);
			BitSet pathsForR = new BitSet();
			data.ret2PathSet.put(v, pathsForR);

			//  assign a different path number to each in-edge
			int pathNum = 0;
			for (CNEdge e : getInEdges(v)) {
				pathNum++;
				pathsForR.set(pathNum);
				BitSet pathsForEdge = new BitSet();
				pathsForEdge.set(pathNum);
				edge2PathSet.put(e, pathsForEdge);
			}
		}

		Set<CNNode> reticulationsBelow = new HashSet<>(); // set of all reticulate nodes below v
		data.node2below.set(v.getId(), reticulationsBelow);

		// visit all children and determine all reticulations below this node
		for (CNEdge f : getOutEdges(v)) {
			CNNode w = f.getTarget();
			if (data.node2below.get(w.getId()) == null) {
				// if haven't processed child yet, do it
				computeReticulation2LSA(w, data);
			}
			reticulationsBelow.addAll(data.node2below.get(w.getId()));
			if (getInDegree(w) > 1) {
				reticulationsBelow.add(w);
			}
		}

		// check whether this is the LSA for any of the reticulations below v
		List<CNNode> toDelete = new LinkedList<>();
		for (CNNode r : reticulationsBelow) {
			// determine which paths from the reticulation lead to this node
			Map<CNEdge, BitSet> edge2PathSet = data.ret2Edge2PathSet.get(r);
			BitSet paths = new BitSet();
			for (CNEdge f : getOutEdges(v)) {
				BitSet eSet = (BitSet) edge2PathSet.get(f);
				if (eSet != null) {
					paths.or(eSet);
				}
			}

			// if the set of paths equals all alive paths, v is the LSA of r
			BitSet alive = data.ret2PathSet.get(r);
			if (paths.equals(alive)) {
				data.ret2LSA.put(r, v);
				toDelete.add(r); // don't need to consider this reticulation any more
			}
		}

		// don't need to consider reticulations for which LSA has been found
		for (CNNode u : toDelete)
			reticulationsBelow.remove(u);

		// all paths are pulled up the first in-edge
		if (getInDegree(v) >= 1) {
			for (CNNode r : reticulationsBelow) {
				// determine which paths from the reticulation lead to this node
				Map<CNEdge, BitSet> edge2PathSet = data.ret2Edge2PathSet.get(r);
				BitSet newSet = new BitSet();
				for (CNEdge e : getOutEdges(v)) {
					BitSet pathSet = edge2PathSet.get(e);
					if (pathSet != null)
						newSet.or(pathSet);
				}
				edge2PathSet.put(getInEdges(v).get(0), newSet);
			}
		}

		// open new paths on all additional in-edges
		if (getInDegree(v) >= 2) {
			ArrayList<CNEdge> inEdgesOfV = getInEdges(v);

			for (CNNode r : reticulationsBelow) {
				BitSet existingPathsForR = data.ret2PathSet.get(r);
				Map<CNEdge, BitSet> edge2PathSet = data.ret2Edge2PathSet.get(r);

				// start with the second in-edge
				for (int i = 1; i < inEdgesOfV.size(); i++) {
					CNEdge e = inEdgesOfV.get(i);
					BitSet pathsForEdge = new BitSet();
					int pathNum = existingPathsForR.nextClearBit(1);
					existingPathsForR.set(pathNum);
					pathsForEdge.set(pathNum);
					edge2PathSet.put(e, pathsForEdge);
				}
			}
		}
	}

	/**
	 * Recursively compute the sum of non-reticulate confidences and count below each node.
	 */
	private void computeConfidenceBelow(CNNode v, double[] confidenceBelow, int[] countBelow) {
		double confidence = 0;
		int count = 0;
		for (CNEdge e : getOutEdges(v)) {
			if (!e.isReticulate()) {
				confidence += e.getConfidence();
				count++;
			}
			CNNode w = e.getTarget();
			if (countBelow[w.getId()] == -1) {
				System.out.println(nodes);
				System.out.println(outEdges);
				System.out.println(confidenceBelow);
				computeConfidenceBelow(w, confidenceBelow, countBelow);
			}
			confidence += confidenceBelow[w.getId()];
			count += countBelow[w.getId()];
		}
		confidenceBelow[v.getId()] = confidence;
		countBelow[v.getId()] = count;
	}

	/**
	 * Computes the average confidence of a collection of edges. Uses only non-reticulate edges.
	 */
	private double computeAverageConfidence(Collection<CNEdge> edges) {
		double sum = 0;
		int count = 0;
		for (CNEdge e : edges) {
			if (!e.isReticulate()) {
				sum += e.getConfidence();
				count++;
			}
		}
		return (count == 0 ? 0 : sum / count);
	}
}