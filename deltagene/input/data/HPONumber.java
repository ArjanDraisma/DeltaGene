package deltagene.input.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.tree.TreeNode;

/**
 * The HPONumber (sub)subclass contains information about a particular HPO term,
 * such as it's HPO number, phenotype, definition, genes and it's children/parents
 */
public class HPONumber implements TreeNode {
	private String hpoid = "Undefined";
	private String phenotype = "Undefined";
	private String definition = "Undefined";
	private HashSet<String> geneSet = new HashSet<String>();
	private ArrayList<HPONumber> children = new ArrayList<HPONumber>();
	private ArrayList<HPONumber> parents = new ArrayList<HPONumber>();
	private boolean expandedByDefault = false;
	
	HPONumber (String hpo) {
		set(hpo);
	}
	public HPONumber(String hpo, HPONumber child) {
		hpoid = hpo;
		children.add(child);
	}
	
	HPONumber (String hpo, String pheno, String def) {
		set(hpo, pheno, def);
	}
	public void addChild(HPONumber child) {
		children.add(child);
	}
	public void addChildren(ArrayList<HPONumber> c) {
		children.addAll(c);
	}
	
	/**
	 * Adds a gene to this HPONumber
	 * @param gene 	gene to be added
	 * @return true if gene was added, false if gene already exists.
	 */
	public boolean addGene(String gene) {
		if (getGeneSet().contains(gene)) {
			return false;
		}
		getGeneSet().add(gene);
		return true;
	}
	
	public boolean containsChild(HPONumber hpo) {
		return containsChild(hpo, false);
	}
	/**
	 * This function finds a child matching hpo in this hponumber's
	 * children and, if recursive == true, this children's children. 
	 * @param hpo	The @link{HPONumber} to search for
	 * @param recursive	Recursively searches this HPO number's children
	 * @return true if hpo is a child of this HPO number.
	 * True if one of these children has hpo as a child and recursive = true
	 */
	public boolean containsChild(HPONumber hpo, boolean recursive) {
		if (recursive) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).containsChild(hpo, recursive)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns the definition of this phenotype. May be undefined for certain HPONumbers (such as HP:0000001 - All)
	 * @return the description as a string
	 */
	public String definition() {
		return definition;
	}
	
	/**
	 * returns the amount of parents above this HPO number
	 * @return the number of parents above this HPO number
	 */
	public int getParentCount() {
		return parents.size();
	}
	
	/**
	 * Returns the amount of children under this HPO number.
	 * @return The number of children under this HPO number.
	 */
	public int getChildCount() {
		return children.size();
	}
	public void addParent(HPONumber parent) {
		parents.add(parent);
	}
	public ArrayList<HPONumber> getParents() {
		return parents;
	}
	public ArrayList<HPONumber> getChildren() {
		return children;
	}
	
	/**
	 * Populates an ArrayList with this HPONumber's genes.
	 * Will not add duplicate genes
	 * @see collectGenes
	 * @return A collection of genes as strings
	 */
	public void getGenes(ArrayList<String> inAL,
			HashSet<String> set) {
			for (String gene : getGeneSet()) {
				if (!set.contains(gene)) {
					set.add(gene);
					inAL.add(gene);
				}
			}
	}
	
	/**
	 * The getIDCollection(ArrayList<String>) function populates an {@link ArrayList}<String> with this HPONumber's hpo number,
	 * and that of it's children (recursive).
	 * @param out the {@link ArrayList}<String> to be populated.
	 */
	public void getIDCollection(HashSet<String> out){
		getIDCollection(out, true);
	}
	
	
	/**
	 * The getIDCollection(ArrayList<String>, boolean) function 
	 * populates an {@link ArrayList} with this
	 * <code>HPONumber</code>'s hpo number. If <code>recursive == 
	 * true</code> this will also add the HPO numbers of this 
	 * HPONumber's children to the list. The altered {@link ArrayList}
	 * should not contain any duplicate hpo numbers.
	 * 
	 * @param out the {@link ArrayList} to be populated. Should be an ArrayList of type String.
	 * @param recursive whether to return the unique HPO numbers of children of this HPONumber
	 */
	public void getIDCollection(HashSet<String> out, boolean recursive) {
		if (recursive) {
			if (children.size() > 0) {
				for (int i = 0; i < children.size(); i++) {
					children.get(i).getIDCollection(out);
				}
			}
		}
		if (!out.contains(hpoid)) {
			out.add(hpoid);
		}
	}
	
	/**
	 * Returns true if this HPO number's gene list > 0
	 * @return True if gene list > 0, false if not.
	 */
	public boolean hasGenes() {
		if (getGeneSet().size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * returns the HPO number associated with this HPONumber object. No HPOObject should exist with an undefined HPO number 
	 * @return the HPO number as a string
	 */
	public String hpo() {
		return hpoid;
	}
	/**
	 * Returns the phenotype associated with this HPONumber. Should not be undefined.
	 * @return the phenotype as a string
	 */
	public String phenotype() {
		return phenotype;
	}
	
	/**
	 * Returns the amount of parents above this HPO number
	 * @return The amount of parents above this HPO number
	 */
	/*public int getParentCount() {
		return parents.size();
	}*/
	
	public void set(String hpo) {
		hpoid = hpo;
	}
	
	public void set(String hpo, String pheno, String def) {
		set(hpo);
		phenotype = pheno;
		definition = def;
	}
	
	public void set(String hpo, String pheno, String def, HPONumber child) {
		set(hpo, pheno, def);
		children.add(child);
	}
	public void setDefinition(String def) {
		definition = def;
	}
	public void setPhenotype(String pheno) {
		phenotype = pheno;
	}
	@Override
	public Enumeration<HPONumber> children() {
		return Collections.enumeration(children);
	}
	@Override
	public boolean getAllowsChildren() {
		return true;
	}
	@Override
	public TreeNode getChildAt(int i) {
		return children.get(i);
	}
	@Override
	public int getIndex(TreeNode node) {
		return children.indexOf(((HPONumber)node));
	}
	@Override
	public TreeNode getParent() {
		return parents.size() > 0 ? parents.get(0) : null;
	}
	@Override
	public boolean isLeaf() {
		return children.size() == 0;
	}
	public TreeNode[] getPath() {
		return getPathToRoot(this,0);
	}
	protected TreeNode[] getPathToRoot(TreeNode node, int depth) {
		if (node == null) {
			if (depth == 0) {
				return null;
			}
			return new TreeNode[depth];
		}
		TreeNode[] path = getPathToRoot(node.getParent(), depth+1);
		path[path.length - depth - 1] = node;
		return path;
	}
	public void setExpandedByDefault(boolean b) {
		expandedByDefault = b;
	}
	public boolean getExpandedByDefault() {
		return expandedByDefault;
	}
	public HashSet<String> getGeneSet() {
		return geneSet;
	}
	public void setGeneSet(HashSet<String> geneSet) {
		this.geneSet = geneSet;
	}
}