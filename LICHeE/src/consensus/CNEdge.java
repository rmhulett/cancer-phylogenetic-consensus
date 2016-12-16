/*
 * TODO(Reyna) license, code from viq
*/


package consensus;


/**
 * Edge in the phylogenetic constraint network
 */
public class CNEdge {
	protected CNNode from;
	protected CNNode to;
	private double confidence = 1;
	private boolean isReticulate = false;
	
	public CNEdge(CNNode from, CNNode to) {
		this.from = from;
		this.to = to;
	}

	public CNEdge(CNNode from, CNNode to, double confidence) {
		this.from = from;
		this.to = to;
		this.confidence = confidence;
	}

	public CNNode getSource() {
		return this.from;
	}

	public CNNode getTarget() {
		return this.to;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public boolean isReticulate() {
		return isReticulate;
	}

	public void setReticulate() {
		isReticulate = true;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof CNEdge)) {
			return false;
		}
		CNEdge e = (CNEdge) o;
		if(this.from.equals(e.from) && this.to.equals(e.to)) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return from.getId() + " to " + to.getId() + " (" + confidence + ", " + isReticulate + ")";
	}
}