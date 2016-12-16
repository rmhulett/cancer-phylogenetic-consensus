/*
 * TODO(Reyna) license
*/


package consensus;

import java.util.ArrayList;


/**
 * Edge in the phylogenetic constraint network
 */
public class CNNode {
	private int nodeId; // unique ID, corresponding to index in cluster and node arrays
	private ArrayList<PHYNode> label = new ArrayList<PHYNode>();
	private double confidence = 1;
	
	public CNNode(int id) {
		this.nodeId = id;
	}

	public CNNode(int id, ArrayList<PHYNode> nodes) {
		this.nodeId	= id;
		this.label = nodes;
	}

	public CNNode(int id, double confidence) {
		this.nodeId = id;
		this.confidence = confidence;
	}

	public CNNode(int id, ArrayList<PHYNode> nodes, double confidence) {
		this.nodeId = id;
		this.label = nodes;
		this.confidence = confidence;
	}

	public int getId() {
		return nodeId;
	}

	public void setLabel(ArrayList<PHYNode> nodes) {
		this.label = nodes;
	}

	public String toString() {
		String ret = nodeId + " (" + confidence + "): ";
		for (PHYNode n : label) {
			ret += n.toString();
		}
		return ret;
	}
}