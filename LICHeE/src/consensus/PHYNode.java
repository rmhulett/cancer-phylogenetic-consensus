/*
 * TODO license
*/


package consensus;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Node in the phylogenetic graph
 * Represents a sub-population or sample (if leaf)
 * 
 * @autor viq
 */
public class PHYNode implements Serializable, Comparable<PHYNode> {
	private static final long serialVersionUID = 1L;

	/** Node/cluster id */
	private int nodeId;

	/** Sub-population cluster that the node represents */
	private ArrayList<SNVEntry> snvs;		
	
	/** The node's sample presence profile */
	protected SampleProfile sampleProfile;
	
	/** Flag indicating if the node is a sample leaf*/
	protected boolean isLeaf;
	
	/** Flag indicating if the node is the germline root */
	private boolean isRoot;
	
	/** The sample id if node is a leaf*/
	private int leafSampleId;
	
	/** 
	 * Internal node constructor
	 * @param g - SNV group the node belongs to
	 * @param nodeClusterId
	 */
	public PHYNode(SampleProfile p, ArrayList<SNVEntry> s, int uniqueId) {
		sampleProfile = p;
		snvs = s;
		isLeaf = false;
		nodeId = uniqueId;
	}
	
	/**
	 * Leaf node constructor - represents each tumor sample
	 * @param sampleId - ID of the represented tumor sample
	 */
	public PHYNode(int sampleId, int uniqueId) {
		isLeaf = true;
		leafSampleId = sampleId;
		nodeId = uniqueId;
	}
	
	/**
	 * Root node constructor
	 */
	public PHYNode(int uniqueId) {
		isRoot = true;
		nodeId = uniqueId;
	}
	
	/**
	 * Returns true if the node is a leaf
	 */
	public boolean isLeaf() {
		return isLeaf;
	}
	
	/**
	 * Returns true if the node is a root
	 */
	public boolean isRoot() {
		return isRoot;
	}
	
	/**
	 * Returns the ID of the sample 
	 * @requires node is a leaf
	 */
	public int getLeafSampleId() {
		return leafSampleId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
	
	public SampleProfile getSampleProfile() {
		return sampleProfile;
	}
	
	public int getSize() {
		if(snvs == null) return 0;
		return snvs.size();
	}

	/**
	 * Returns the SNV entries in the cluster
	 * corresponding to this node
	 */
	public ArrayList<SNVEntry> getSNVs() {
		return snvs;
	}
	
	public String toString() {
		String node = "Node " + nodeId + ": ";
		if(!isLeaf && !isRoot) {
			node += "group tag = " + sampleProfile.getTag() + ", ";
			node += "[";
			for (int i = 0; i < snvs.size(); i++) {
				node += snvs.get(i).toString() + ", ";
			}
			node += "]";
		} else if(isLeaf) {
			node += "leaf sample id = " + leafSampleId;
		} 
		return node;
	}
	
	public String getLabel() {
		String node = "";
		if(!isLeaf && !isRoot) {
			//node += nodeId + ": \n";
			node += sampleProfile.getTag() + "\n";
			node += "("+snvs.size()+")";
		} else if(isLeaf) {
			node += "sample " + leafSampleId;
		} else {
			node += "GL";
		}
		return node;
	}
	
	public String getLongLabel() {
		String node = "";
		if(!isLeaf && !isRoot) {
			node += "Group: " + sampleProfile.getTag() + "\n";
			node += "[";
			for (int i = 0; i < snvs.size(); i++) {
				node += snvs.get(i) + ", ";
			}
			node += "]";
		} else if(isLeaf) {
			node += "sample " + leafSampleId;
		} else {
			node += "GL";
		}
		return node;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof PHYNode)) {
			return false;
		}
		PHYNode n = (PHYNode) o;
		if(this.nodeId == n.nodeId) {
			return true;
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return nodeId;
	}

	@Override
	public int compareTo(PHYNode arg0) {
		if(arg0.getNodeId() < this.nodeId) {
			return -1;
		} else if(arg0.getNodeId() > this.nodeId) {
			return 1;
		}
		return 0;
	}
	
}
