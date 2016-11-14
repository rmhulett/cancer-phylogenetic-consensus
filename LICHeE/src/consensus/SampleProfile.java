/*
 * TODO(Reyna) license, adapted from code by viq
*/


package consensus;

import java.io.Serializable;


public class SampleProfile implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Binary tag identifying the group 
	 * (the length of the tag is equal to the number of input samples) */
	private String tag;
	
	/** Number of samples represented by this group */
	private int numSamples;
	
	/** Indices of the samples represented in this group (from 0 to |tag|-1 MSF order) */
	private int[] sampleIndex;
	
	public SampleProfile(String groupTag) {
		tag = groupTag;
		numSamples = 0;		
		sampleIndex = new int[tag.length()];
		for(int i = 0; i < tag.length(); i++) {
			if(tag.charAt(i) == '1') {
				sampleIndex[numSamples] = i;
				numSamples++;
			}
		}
	}
	
	// Getters/Setters
	
	public int getNumSamples() {
		return numSamples;
	}
	
	public int getNumSamplesTotal() {
		return tag.length();
	}
	
	public int[] getSampleIds() {
		return sampleIndex;
	}
	
	public String getTag() {
		return tag;
	}
	
	/**
	 * Returns the index of this sample of those represented by this profile
	 * @return -1 if this sample is not represented in the group
	 */
	public int getSampleIndex(int sampleId) {
		for(int i = 0; i < numSamples; i++) {
			if(sampleIndex[i] == sampleId) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns true if the given sample contains the mutations of this group
	 */
	public boolean containsSample(int sampleId) {
		return (getSampleIndex(sampleId) != -1);
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof SampleProfile)) {
			return false;
		}
		SampleProfile g = (SampleProfile) o;
		if(this.tag == g.tag) {
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {
		String group = "";
		group += "tag = " + this.tag + ", ";
		group += "numSamples = " + this.numSamples;
		return group;
	}
}
