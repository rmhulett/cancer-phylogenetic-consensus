/*
 * TODO license
*/


/**
 * Represents an SNV entry
 */
package consensus;

import java.util.ArrayList;

public class SNVEntry {
	
	/** SNV entry info */
	protected int id;
	protected int chr;
	protected int position;
	protected String description;
	protected String presenceProfile;
	protected String ambigPresenceProfile;
	protected double[] VAF;
	protected boolean isRobust;
	protected String snvEntryString;
	 
	public SNVEntry(String line, int lineId) {
		id = lineId;
		snvEntryString = line;
	}
	
	public int getId() {
		return id;
	}
	
	/** Returns the SNV chromosome */
	public int getChromosome() {
		return chr;
	}
	
	/** Returns the SNV position  */
	public int getPosition() {
		return position;
	}
	
	/** Returns the description field */
	public String getDescription() {
		return description;
	}
	
	/** Returns the VAF in sample i */
	public double getVAF(int i) {
		return VAF[i];
	}
	
	/** Returns true if the SNV was robustly called in all samples  */
	public boolean isRobust() {
		return isRobust;
	}
	
	/** Returns the sample presence-absence profile */
	public String getProfile() {
		return presenceProfile;
	}
	
	public String getAmbigProfile() {
		return ambigPresenceProfile;
	}
	
	/** Sets the sample presence-absence profile */
	public void updateGroup(String code) {
		presenceProfile = code;
	}
	
	/** Returns true if profile[sampleId] == 1 */
	public boolean isPresent(int sampleId) {
		return presenceProfile.charAt(sampleId) == '1';
	}
	
	public boolean evidenceOfPresence(int sample){
		return (VAF[sample] > Parameters.MAX_VAF_ABSENT );
	}
	
	public String toString() {
		return snvEntryString;
	}
}
