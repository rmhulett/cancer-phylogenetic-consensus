/**
 * TODO(Reyna) license, taken from Huson
*/

package consensus;

import java.util.*;

/**
 * a weighted cluster
 * Daniel Huson, 9.2007
 */
public class Cluster extends BitSet {
	int id;
	double weight;
	double confidence;
	final List<Integer> treeNumbers;

	public Cluster() {
		super();
		weight = 1;
		confidence = 1;
		this.treeNumbers = new LinkedList<>();
	}

	public Cluster(BitSet A) {
		super();
		or(A);
		this.treeNumbers = new LinkedList<>();
	}

	public Cluster(BitSet A, double weight) {
		super();
		or(A);
		this.weight = weight;
		this.treeNumbers = new LinkedList<>();
	}

	public Cluster(BitSet A, double weight, int id) {
		super();
		or(A);
		this.weight = weight;
		this.id = id;
		this.treeNumbers = new LinkedList<>();
	}

	public Cluster(BitSet A, double weight, double confidence, int id) {
		super();
		or(A);
		this.weight = weight;
		this.confidence = confidence;
		this.id = id;
		this.treeNumbers = new LinkedList<>();
	}

	public Cluster(BitSet A, double weight, double confidence, int id, List<Integer> treeNumbers) {
		super();
		or(A);
		this.weight = weight;
		this.confidence = confidence;
		this.id = id;
		this.treeNumbers = new LinkedList<>();
		this.treeNumbers.addAll(treeNumbers);
	}

	public Object clone() {
		return new Cluster(this, this.getWeight(), this.getConfidence(), this.getId(), this.getTreeNumbers());
	}

	public List<Integer> getTreeNumbers() {
		return treeNumbers;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	/*
	public String toString ()
	{
		return super.toString()+": "+weight;
	}
	*/


	/**
	 * compare clusters first by size and then lexicographically
	 */
	static public Comparator<Cluster> getComparator() {
		return new Comparator<Cluster>() {
			public int compare(Cluster cluster1, Cluster cluster2) {
				if (cluster1.cardinality() > cluster2.cardinality())
					return -1;
				else if (cluster1.cardinality() < cluster2.cardinality())
					return 1;

				int t1 = cluster1.nextSetBit(0);
				int t2 = cluster2.nextSetBit(0);
				while (true) {
					if (t1 < t2)
						return -1;
					else if (t1 > t2)
						return 1;
					t1 = cluster1.nextSetBit(t1 + 1);
					t2 = cluster2.nextSetBit(t2 + 1);
					if (t1 == -1 && t2 > -1)
						return -1;
					else if (t1 > -1 && t2 == -1)
						return 1;
					else if (t1 == -1 && t2 == -1)
						return 0;
				}
			}
		};
	}

	/**
	 * determines whether set A contains set B
	 *
	 * @param A
	 * @param B
	 * @return true, if A contains B
	 */
	public static boolean contains(BitSet A, BitSet B) {
		for (int i = B.nextSetBit(0); i != -1; i = B.nextSetBit(i + 1))
			if (!A.get(i))
				return false;
		return true;
	}

	/**
	 * computes the union of A and B
	 *
	 * @param A
	 * @param B
	 * @return union
	 */
	public static BitSet union(BitSet A, BitSet B) {
		BitSet result = (BitSet) A.clone();
		result.or(B);
		return result;
	}

	/**
	 * computes the intersection of A and B
	 *
	 * @param A
	 * @param B
	 * @return intersection
	 */
	public static BitSet intersection(BitSet A, BitSet B) {
		if (A == null || B == null)
			return new BitSet();
		BitSet result = (BitSet) A.clone();
		result.and(B);
		return result;
	}

	/**
	 * subtracts A from B
	 *
	 * @param A
	 * @param B
	 * @return A \ B
	 */
	public static BitSet setminus(BitSet A, BitSet B) {
		BitSet result = (BitSet) A.clone();
		result.andNot(B);
		return result;
	}

	/**
	 * are two given clusters incompatible?
	 *
	 * @param A
	 * @param B
	 * @return
	 */
	public static boolean incompatible(BitSet A, BitSet B) {
		return setminus(A, B).cardinality() > 0 && setminus(B, A).cardinality() > 0
				&& intersection(A, B).cardinality() > 0;
	}

	/**
	 * are two clusters equal?
	 *
	 * @param A
	 * @param B
	 * @return true, if equal
	 */
	public static boolean equals(BitSet A, BitSet B) {
		return A.equals(B);
	}

	/**
	 * are two clusters equal or all taxa upto max taxon id?
	 *
	 * @param A
	 * @param B
	 * @param maxTaxonId
	 * @return true, if equal upto maxTaxonId
	 */
	public static boolean equals(BitSet A, BitSet B, int maxTaxonId) {
		for (int t = 0; t <= maxTaxonId; t++)
			if (A.get(t) != B.get(t))
				return false;
		return true;
	}

	/**
	 * print an array of clusters
	 *
	 * @param clusters
	 */
	public static void print(Cluster[] clusters) {
		for (Cluster cluster : clusters) {
			//System.err.println(clusters[i] + ": " + clusters[i].getWeight());
			System.err.println(cluster);
		}
	}

	/**
	 * compare two bit sets
	 *
	 * @param A
	 * @param B
	 * @return
	 */
	public static int compare(BitSet A, BitSet B) {
		int a = A.nextSetBit(0);
		int b = B.nextSetBit(0);

		while (a != -1 && b != -1) {
			if (a < b)
				return -1;
			else if (a > b) return 1;
			a = A.nextSetBit(a + 1);
			b = B.nextSetBit(b + 1);
		}
		if (a < b)
			return -1;
		else if (a > b)
			return 1;
		else return 0;
	}

	/**
	 * extract the set of taxa present in an array of clusters
	 *
	 * @param clusters
	 * @return taxa
	 */
	public static BitSet extractTaxa(Cluster[] clusters) {
		BitSet taxa = new BitSet();
		for (Cluster cluster : clusters) taxa.or(cluster);
		return taxa;
	}

	/**
	 * get clusters sorted by decreasing cardinality
	 *
	 * @param clusters
	 * @return sorted clusters
	 */
	public static ArrayList<Cluster> getClustersSortedByDecreasingCardinality(ArrayList<Cluster> clusters) {
		Set<Cluster> sorted = new TreeSet<>(Cluster.getComparator());
		sorted.addAll(clusters);
		return new ArrayList<Cluster>(sorted);
	}

	/**
	 * gets the maximum element in a set, or -1, if the set is empty
	 *
	 * @param cluster
	 * @return max element or -1
	 */
	public static int getMaxElement(BitSet cluster) {
		for (int t = cluster.nextSetBit(0); t != -1; ) {
			int r = cluster.nextSetBit(t + 1);
			if (r == -1)
				return t;
			else
				t = r;
		}
		return -1;
	}

	/**
	 * get as string
	 *
	 * @return string
	 */
	public String toString() {
		final StringBuilder buf = new StringBuilder();

		int startRun = 0;
		int inRun = 0;
		boolean first = true;
		for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
			if (first) {
				first = false;
				buf.append(i);
				startRun = inRun = i;
			} else {
				if (i == inRun + 1) {
					inRun = i;
				} else if (i > inRun + 1) {
					if (inRun == startRun || i == startRun + 1)
						buf.append(",").append(i);
					else if (inRun == startRun + 1)
						buf.append(",").append(inRun).append(",").append(i);
					else
						buf.append("-").append(inRun).append(",").append(i);
					inRun = startRun = i;
				}
			}
		}
		// dump last:
		if (inRun == startRun + 1)
			buf.append(",").append(inRun);
		else if (inRun > startRun + 1)
			buf.append("-").append(inRun);
		return buf.toString();
	}

	/**
	 * does the given set intersect all of the given clusters?
	 *
	 * @param set
	 * @param clusters
	 * @return true, if set intersects one of the clusters
	 */
	public static boolean intersectsAll(BitSet set, Cluster[] clusters) {
		for (Cluster cluster : clusters)
			if (!set.intersects(cluster))
				return false;
		return true;
	}

	/**
	 * does the set contain at least one of the given clusters
	 *
	 * @param set
	 * @param clusters
	 * @return true, if set contains at least one of the given clusters
	 */
	public static boolean containsAtLeastOne(BitSet set, Cluster[] clusters) {
		for (Cluster cluster : clusters)
			if (contains(set, cluster))
				return true;
		return false;
	}

	/**
	 * computes the number of taxa that are each not contained in all clusters
	 *
	 * @param taxa
	 * @param clusters
	 * @return number of taxa that are each not contained in all clusters
	 */
	public static int numberOfTaxaNotContainedInAllClusters(BitSet taxa, Cluster[] clusters) {
		BitSet result = new BitSet();
		for (Cluster cluster : clusters) {
			BitSet missing = setminus(taxa, cluster);
			result.or(missing);
			if (missing.cardinality() == taxa.cardinality())
				break;
		}
		return result.cardinality();
	}

	/**
	 * add a cluster to an array of clusters
	 *
	 * @param clusters
	 * @param additional
	 * @return new array of clusters
	 */
	public static Cluster[] addCluster(Cluster[] clusters, Cluster additional) {
		Cluster[] result = new Cluster[clusters.length + 1];
		System.arraycopy(clusters, 0, result, 0, clusters.length);
		result[result.length - 1] = additional;
		return result;
	}
}
