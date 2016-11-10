/*
 * TODO license
*/


package consensus;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Cluster of observation points
 * Each cluster has a list of members
 */
public class Cluster implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** Cluster id, unique per group */
	private int id;
	
	/** List of observations assigned to this cluster */
	private ArrayList<Integer> members;
	
	private boolean robust = false;
	
	public Cluster(int clusterId) {
		members = new ArrayList<Integer>();
		id = clusterId;
	}
	
	public Cluster(ArrayList<Integer> assignments, int clusterId) {
		members = assignments;
		id = clusterId;
		
	}
	
	/**
	 * Add a new observation to the cluster
	 * @param obsId - Id of the observation (index in the data matrix)
	 */
	public void addMember(int obsId) {
		members.add(new Integer(obsId));
	}
	
	public ArrayList<Integer> getMembership() {
		return members;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isRobust() {
		return robust;
	}
	
	public void setRobust() {
		robust = true;
	}
	
	public String toString() {
		String c = "";
		c += "Size: " + members.size() + "\n";
		c += "Members: ["
		for(int i = 0; i < members.size(); i++) {
			c += members.get(i) + ", "
		}
		return c;
	}
}
