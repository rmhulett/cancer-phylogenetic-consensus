/*
 * Program LICHeE for multi-sample cancer phylogeny reconstruction
 * by Victoria Popic (viq@stanford.edu) 2014
 *
 * MIT License
 *
 * Copyright (c) 2014 Victoria Popic.
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/


package lineage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import lineage.AAFClusterer.Cluster;

/**
 * An SNV group is a set of SNVs occurring in a given subset of samples.
 * All SNVs are partitioned into SNV groups based on their occurrence across samples.
 * Given S samples, there can be at most 2^S different groups.
 * An SNV group is uniquely identified by an S-bit binary tag (each bit corresponding to
 * a given sample), where a bit is set if that sample contains the SNVs in this group.
 *
 * @autor viq
 */
public class SNVGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Binary tag identifying the group 
	 * (the length of the tag is equal to the number of input samples) */
	private String tag;
	
	/** Number of samples represented by this group */
	private int numSamples;
	
	/** Indices of the samples represented in this group (from 0 to |tag|-1 MSF order) */
	private int[] sampleIndex;
	
	/** Alternative allele frequency data matrix (numSNVs x numSamples) */
	protected transient double[][] alleleFreqBySample;
	
	/** SubPopulation clusters */
	protected Cluster[] subPopulations;
	
	/** SNVs assigned to this group */
	private transient ArrayList<SNVEntry> snvs;
	
	/** Flag indicating whether this group is robust */
	private transient boolean isRobust;
	
	private static Logger logger = LineageEngine.logger;
	
	public SNVGroup(String groupTag, ArrayList<SNVEntry> groupSNVs, boolean isGroupRobust) {
		tag = groupTag;
		isRobust = isGroupRobust;
		numSamples = 0;		
		sampleIndex = new int[tag.length()];
		for(int i = 0; i < tag.length(); i++) {
			if(tag.charAt(i) == '1') {
				sampleIndex[numSamples] = i;
				numSamples++;
			}
		}
		snvs = groupSNVs;
		alleleFreqBySample = new double[snvs.size()][numSamples];
		for(int i = 0; i < snvs.size(); i++) {
			SNVEntry snv = snvs.get(i);
			for(int j = 0; j < numSamples; j++) {
				alleleFreqBySample[i][j] = snv.getVAF(sampleIndex[j]);
			}
		}
	}

	public SNVGroup(String groupTag, double[] centroid, int size) {
		tag = groupTag;
		isRobust = true;
		numSamples = 0;		
		sampleIndex = new int[tag.length()];
		
		for(int i = 0; i < tag.length(); i++) {
			if(tag.charAt(i) == '1') {
				sampleIndex[numSamples] = i;
				numSamples++;
			}
		}
		double[] c = new double[numSamples];
		int idx = 0;
		for(int i = 0; i < tag.length(); i++) {
			if(tag.charAt(i) == '1') {
				c[idx] = centroid[i];
				idx++;
			}
		}
		
		snvs = new ArrayList<SNVEntry>();
		alleleFreqBySample = new double[snvs.size()][numSamples];
		subPopulations = new Cluster[1];
		AAFClusterer aafc = new AAFClusterer();
		subPopulations[0] = aafc.new Cluster(c, 0);
		
		
	}
	
	// Getters/Setters
	
	public ArrayList<SNVEntry> getSNVs() {
		return snvs;
	}
	
	public double[][] getAlleleFreqBySample() {
		return alleleFreqBySample;
	}
	
	public int getNumSamples() {
		return numSamples;
	}
	
	public int getNumSamplesTotal() {
		return tag.length();
	}
	
	public int[] getSampleIds() {
		return sampleIndex;
	}
	
	public int getNumSNVs() {
		return snvs.size();
	}
	
	public Cluster[] getSubPopulations() {
		return subPopulations;
	}
	
	public String getTag() {
		return tag;
	}
	
	public boolean isRobust() {
		return isRobust;
	}
	
	/**
	 * Returns the index of this sample in the centroid/AAF data of the group
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
		if(!(o instanceof SNVGroup)) {
			return false;
		}
		SNVGroup g = (SNVGroup) o;
		if(this.tag == g.tag) {
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {
		String group = "";
		group += "(tag = " + this.tag + ", ";
		group += "numSamples = " + this.numSamples + ", ";
		group += "numSNVs = " + this.snvs.size() + ") ";
		if(this.subPopulations != null) group += "numSubPopulations = " + this.subPopulations.length;
		return group;
	}
	
	public void removeCluster(Cluster c) {
		if(subPopulations == null || subPopulations.length == 0) {
			return;
		}
		Cluster[] clusters = new Cluster[subPopulations.length - 1];
		int j = 0;
		for(int i = 0; i < subPopulations.length; i++) {
			if(!subPopulations[i].equals(c)) {
				clusters[j] = subPopulations[i];
				j++;
			} 
		}
		subPopulations = clusters;
	}
	
	public void addCluster(Cluster c) {
		if(subPopulations == null) {
			return;
		}
		Cluster[] clusters = new Cluster[subPopulations.length + 1];
		int j = 0;
		for(int i = 0; i < subPopulations.length; i++) {
			clusters[j] = subPopulations[i];
			j++; 
		}
		clusters[subPopulations.length] = c;
		subPopulations = clusters;
	}
}
