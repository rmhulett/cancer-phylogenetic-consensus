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
	
}
