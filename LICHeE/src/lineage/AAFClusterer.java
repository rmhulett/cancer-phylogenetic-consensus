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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Simple implementation of a few clustering techniques
 * for allele frequency values of sample SNVs.
 * 
 * @autor viq
 */
public class AAFClusterer implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Cluster of observation points
	 * Each cluster has an associated centroid point and a list of members
	 */
	public class Cluster implements Serializable {
		
		private static final long serialVersionUID = 1L;

		/** Cluster id, unique per group */
		private int id;
		
		/** Cluster centroid */
		private double[] centroid;
		
		/** Cluster standard deviation */
		private double[] stdDev = null;
		
		/** List of observations assigned to this cluster */
		private ArrayList<Integer> members;
		
		private boolean robust = false;
		
		public Cluster(double[] clusterCentroid, int clusterId) {
			centroid = clusterCentroid;
			members = new ArrayList<Integer>();
			id = clusterId;
		}
		
		public Cluster(double[] clusterCentroid, ArrayList<Integer> assignments, int clusterId) {
			centroid = clusterCentroid;
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
		
		/**
		 * Returns the cluster centroid (mean) per sample
		 */
		public double[] getCentroid() {
			return centroid;
		}
		
		/**
		 * Returns the standard deviation per sample
		 * @requires setStdDev() method to have been called (currently implemented for EM only),
		 * will return null otherwise
		 */
		public double[] getStdDev() {
			return stdDev;
		}
		
		public void setStdDev(double[] dev) {
			stdDev = dev;
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
			DecimalFormat df = new DecimalFormat("#.##");
			c += "VAF Mean: [";
			for(int i = 0; i < centroid.length; i++) {
				c += " " + df.format(centroid[i]) + " ";
			}
			c += "] \n";
			c += "       Stdev:";
			if(stdDev != null) {
				c += " [";
				for(int i = 0; i < stdDev.length; i++) {
					c += " " + df.format(stdDev[i]) + " ";
				}
				c += "]";
			}
			return c;
		}
	}
	
}
