/*
 * TODO(Reyna) license, adapted from code by viq
*/


package consensus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Spanning tree of the phylogenetic constraint network
 * 
 * @autor viq
 */
public class PHYTree implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public ArrayList<PHYNode> treeNodes;
	public HashMap<PHYNode, ArrayList<PHYNode>> treeEdges;
	protected double weight = 1;
	
	public PHYTree() {
		treeNodes = new ArrayList<PHYNode>();
		treeEdges = new HashMap<PHYNode, ArrayList<PHYNode>>();
	}
	
	public void addNode(PHYNode n) {
		if(!treeNodes.contains(n)) {
			treeNodes.add(n);
		}
	}
	
	public void addEdge(PHYNode from, PHYNode to) {
		ArrayList<PHYNode> nbrs = treeEdges.get(from);
		if(nbrs == null) {
			treeEdges.put(from, new ArrayList<PHYNode>());
		}
		if(!treeEdges.get(from).contains(to)) {
			treeEdges.get(from).add(to);
		}
	}
	
	public void removeEdge(PHYNode from, PHYNode to) {
		ArrayList<PHYNode> nbrs = treeEdges.get(from);
		if(nbrs != null) {
			for(PHYNode n : nbrs) {
				if(n.equals(to)) {
					nbrs.remove(n);
					break;
				}
			}
		}
		
		// remove the node if no edge points to it
		boolean connected = false;
		for(PHYNode n : treeEdges.keySet()) {
			for(PHYNode n2 : treeEdges.get(n)) {
				if(to.equals(n2)) {
					connected = true;
					break;
				}
			}
		}
		if(!connected) {
			treeNodes.remove(to);
		}			
	}
	
	public boolean containsNode(PHYNode v) {
		for(PHYNode n : treeNodes) {
			if(n.equals(v)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsEdge(PHYNode from, PHYNode to) {
		if(treeEdges.get(from) == null) return false;
		for(PHYNode n : treeEdges.get(from)) {
			if(n.equals(to)) {
				return true;
			}
		}
		return false;
	}
	
	public PHYNode getRoot() {
		return treeNodes.get(0);
	}
	
	/** 
	 * Returns a copy of the tree
	 */
	public PHYTree clone() {
		PHYTree copy = new PHYTree();
		copy.treeNodes.addAll(this.treeNodes);
		for(PHYNode n : this.treeEdges.keySet()) {
			ArrayList<PHYNode> nbrs = new ArrayList<PHYNode>();
			nbrs.addAll(this.treeEdges.get(n));
			copy.treeEdges.put(n, nbrs);
		}
		return copy;
	}
	
	/**
	 * Returns true if w is a descendant of v in this tree
	 */
	public boolean isDescendent(PHYNode v, PHYNode w) {
		ArrayList<PHYNode> nbrs = treeEdges.get(v);
		if(nbrs == null) {
			return false;
		}
		ArrayList<PHYNode> q = new ArrayList<PHYNode>(nbrs);
		while(q.size() > 0) {
			PHYNode n = q.remove(0);
			if(n.equals(w)) {
				return true;
			}
			if(treeEdges.get(n) != null) {
				q.addAll(treeEdges.get(n));
			}
		}
		return false;
	}
	
	public String toString() {
		String graph = "";
		for(PHYNode n1 : treeEdges.keySet()) {
			ArrayList<PHYNode> nbrs = treeEdges.get(n1);
			for(PHYNode n2 : nbrs) {
				graph += n1.getNodeId() + " -> " + n2.getNodeId() + "\n";
			}
		}
		return graph;
	}
	
	public String getNodeSNVString() {
		String s = "";
		for(PHYNode n : treeNodes) {
			if(n.getSampleProfile() == null) continue;
    		ArrayList<SNVEntry> snvs = n.getSNVs();
    		s += n.getNodeId();
    		s += "\t" + n.getSampleProfile().getTag();
    		for(SNVEntry snv : snvs) {
    			s += "\t" + snv.getDescription();
        	}
    		s += "\n";
		}
		return s;
	}
	
	/**
	 * Returns the sub-populations of a given sample
	 */
	public String getLineage(int sampleId, String sampleName) {
		StringBuilder lineage = new StringBuilder();
		lineage.append("\tSample lineage decomposition: ");
		lineage.append(sampleName + "\n");
		lineage.append("GL\n");
		
		// traverse the tree starting from the root in DFS order
		String indent = "";
		for(PHYNode n : treeEdges.get(treeNodes.get(0))) {
			getLineageHelper(lineage, indent, n, sampleId);
		}
		return lineage.toString();
	}
	
	private void getLineageHelper(StringBuilder lineage, String indent, PHYNode n, int sampleId) {
		//indent += "     ";	
		indent += ".....";	
		
		if(n.getSampleProfile().containsSample(sampleId)) {
			lineage.append(indent + n.toString() + "\n");
		}
		if(treeEdges.get(n) != null) {
			for(PHYNode nbr : treeEdges.get(n)) {
				getLineageHelper(lineage, indent, nbr, sampleId);
			}
		}
	}
	
	public void getLineageClusters(ArrayList<PHYNode> path, ArrayList<ArrayList<PHYNode>> clones, PHYNode n, int sampleId) {
		if(n.getSampleProfile() != null && n.getSampleProfile().containsSample(sampleId)) {
			path.add(n);
		} else if(n.getSampleProfile() != null && !n.getSampleProfile().containsSample(sampleId)) {
			return;
		}
		if(treeEdges.get(n) != null) {
			for(PHYNode nbr : treeEdges.get(n)) {
				int size1 = clones.size();
				ArrayList<PHYNode> clone = new ArrayList<PHYNode>(path);
				getLineageClusters(new ArrayList<PHYNode>(path), clones, nbr, sampleId);
				int size2 = clones.size();
				if(size1 == size2) {
					if(nbr.getSampleProfile() != null && nbr.getSampleProfile().containsSample(sampleId)) {
						clone.add(nbr);
						clones.add(clone);
					}
				}
			}
		} else {
			clones.add(new ArrayList<PHYNode>(path));
		}
	}
}	
