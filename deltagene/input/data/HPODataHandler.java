package deltagene.input.data;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import deltagene.input.InputHandler;
import deltagene.input.browser.HPOBrowser;
import deltagene.io.HPOFileHandler;
import deltagene.utils.Error;

/**
 * The HPOObject subclass contains many methods related to looking up sets of HPONumbers,
 * and contains the HPONumber collection and the HPONumbers class itself.
 */
public class HPODataHandler implements TreeModel {
	/*
	 * The data HashMap contains all instances of HPONumbers objects.
	 * The key to each HPONumber is it's hpo id (HP:#######)
	 */
	private static HPOFileHandler hpoFileHandler;
	private static HashMap<String, HPONumber> data = 
			new HashMap<String, HPONumber>();
	public final static int STATE_INIT = 0;
	public final static int STATE_WAIT = 1;
	public final static int STATE_LOAD_HPO = 2;
	public final static int STATE_LOAD_ASSOC = 3;
	public final static int STATE_READY = 4;
	private static int state;
	private static HPONumber rootNode;
	public HPOBrowser browser;
	
	public HPODataHandler(HPOFileHandler hpoFileHandler) {
		state = STATE_INIT;
		this.hpoFileHandler = hpoFileHandler;
		rootNode = new HPONumber("HP:0000000", "Root",
				"Root node for the Human Phenotype Ontology");
		data.put("HP:0000000", rootNode);
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		// TODO Auto-generated method stub
		
	}

	public void build() {
		try {
			if (hpoFileHandler.getState() != HPOFileHandler.STATE_READY) {
				state = STATE_WAIT;
				wait();
			}
			state = STATE_LOAD_HPO;
			populateHPOObject(hpoFileHandler.getHPOFile());
			//browser = new HPOBrowser(this);
			state = STATE_LOAD_ASSOC;
			populateHPOGenes(hpoFileHandler.getAssocFile());
			state = STATE_READY;
			rootNode.addChild(data.get("HP:0000001"));
			rootNode.setExpandedByDefault(true);
			data.get("HP:0000001").setExpandedByDefault(true);
		}catch (InterruptedException e) {
			e.printStackTrace();
			new Error("A critical error occured! Try running the application as administrator.\n"
					+ "If the issue persists, report the issue with the data below: \n\n"
					+ e.getStackTrace(), "Critical error", WindowConstants.EXIT_ON_CLOSE);
			// TODO: Cleanup for these kinds of problems
		}
	}
	
	public void findHPOGenes(String hpo, ArrayList<String> inAL) {
		findHPOGenes(hpo, inAL, true);
	}
	
	/**
	 * This function finds the unique genes associated with a specified
	 * hpo number. If <code>findchildren == true</code> it will also return
	 * genes of this hpo number's children.
	 * @param hpo the HPO of which the genes to return
	 * @param inAL the ArrayList to append the genes onto
	 * @param findchildren true if the genes for this hpo's children are also to be added
	 */
	public void findHPOGenes(String hpo, ArrayList<String> inAL, boolean findchildren) {
		String[] hpolist = hpo.toUpperCase().split("([^A-Z0-9:]+)");
		findHPOGenes(hpolist, inAL, findchildren);
	}
	
	public void findHPOGenes(String[] hpolist, ArrayList<String> inAL, boolean findchildren) {
		HashSet<String> completehpolist = new HashSet<String>();
		HashSet<String> set = new HashSet<String>();
		for (String hpoid : hpolist) {
			if (findchildren) {
				data.get(hpoid).getIDCollection(completehpolist);
			}else{
				completehpolist.add(hpoid);
			}
		}
		if (completehpolist.size() == 0) {
			return;
		}
		for (String hpo : completehpolist) {
			if (data.containsKey(hpo)) {
				data.get(hpo).getGenes(inAL, set);
			}
		}
	}
	/**
	 * This function returns a full list of phenotypes, to be used in the
	 * autocompletion of input boxes. The list of phenotypes will also
	 * contain meta information, such as age of onset.
	 * @return a full ArrayList of phenotypes.
	 */
	public TreeMap<String, String> getACList() {
		
		TreeMap<String, String> out = new TreeMap<String, String>();
		HashSet<String> set = new HashSet<String>();
		for (HPONumber entry : data.values()) {
			if (!set.contains(entry.phenotype())) {
				set.add(entry.phenotype());
				out.put(entry.phenotype(), entry.hpo());
			}
		}
		return out;
	}
	
	public HPOBrowser getBrowser() {
		return browser;
	}
	
	@Override
	public Object getChild(Object node, int i) {
		return ((HPONumber)node).getChildAt(i);
	}
	
	@Override
	public int getChildCount(Object node) {
		return ((HPONumber)node).getChildCount();
	}

	public String[] getHPOFromGene(String gene) {
		ArrayList<String> out = new ArrayList<String>();
		for (HPONumber hpo : data.values()) {
			if (hpo.getGeneSet().contains(gene)) {
				out.add(hpo.hpo());
			}
		}
		return out.toArray(new String[out.size()]);
	}
	
	/**
	 * This function returns a HPO number that belongs to a given phenotype.
	 * Phenotypes should be unique. Regardless, this function returns the 
	 * first HPO number found with the given phenotype 
	 * @param pheno the phenotype to look up
	 * @return the associated HPO number. null if no HPO number found
	 */
	public String getHPOFromPhenotype(String pheno) {
		for (HPONumber hpo : data.values()) {
			if (hpo.phenotype().equals(pheno)) {
				return hpo.hpo();
			}
		}
		return null;
	}
	
	/**
	 * This function returns a node of a given hpo and it's children
	 * @param hpo a string starting with a HPO number. 
	 * @return node of a given hpo and it's children
	 */
	public DefaultMutableTreeNode getHPOHeirarchy(String hpo) {
		if (hpo.equals("HP:0011687")) {
			System.out.println();
		}
		String hpoin = hpo.substring(0, 10);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(hpoin+
				" - "+data.get(hpoin).phenotype());
		for(HPONumber child : data.get(hpoin).getChildren()) {
			if (child.getChildCount() > 0) {
				node.add(getHPOHeirarchy(child.hpo()));
			}else{
				DefaultMutableTreeNode childnode = 
						new DefaultMutableTreeNode(child.hpo()+" - "+
				child.phenotype());
				node.add(childnode);
			}
		}
		return node;
	}

	@Override
	public int getIndexOfChild(Object node, Object child) {
		return ((HPONumber)node).getIndex((HPONumber)child);
	}
	
	/**
	 * This function returns a phenotype belonging to an HPO number.
	 * @param hpo the hpo number to look up
	 * @return the associated phenotype. Null if no phenotype found
	 */
	public String getPhenotypeFromHPO(String hpo) {
		if (data.containsKey(hpo)) {
			return data.get(hpo).phenotype();
		}
		return null;
	}
	
	// Finds the path of a given hpo number from bottom to top
	public DefaultMutableTreeNode getReverseHPOHeirarchy(DefaultMutableTreeNode hponode) {
		String hpo = hponode.getUserObject().toString().substring(0,10);
		DefaultMutableTreeNode metaNode = new DefaultMutableTreeNode();
		if (data.get(hpo).getParentCount() == 0) {
			return hponode;
		}
		for (HPONumber parent : data.get(hpo).getParents()) {
			metaNode.add(getReverseHPOHeirarchy(new DefaultMutableTreeNode(parent.hpo())));
		}
		return metaNode;
	}
	
	@Override
	public Object getRoot() {
		return rootNode;
	}
	
	public int getState() {
		return state;
	}
	
	public HPONumber getHpoData(String hpo) {
		if (data.containsKey(hpo)) 
			return data.get(hpo);
		return null;
	}
	
	@Override
	public boolean isLeaf(Object node) {
		return ((HPONumber)node).isLeaf();
	}

	public boolean isReady() {
		return state == STATE_READY;
	}

	/**
	 * This function parses a (list) number as a HPO number, padding it with
	 * zeroes and prefixing 'HP:'. This function does not check if
	 * the hpo number exists
	 * @param num the (list of) numbers to be parsed
	 * @return an array of syntactically valid HPO numbers
	 */
	public String[] parseNumbersAsHPO(String num) {
		ArrayList<String> outAL = new ArrayList<String>();
		for (String number : num.split("([^\\d]+)")) {
			
			StringBuilder sb = new StringBuilder("HP:");
			if (number.length() < 8) {
				for (int i = number.length(); i < 7; i++) {
					sb.append('0');
				}
				sb.append(number);
				outAL.add(sb.toString());
				sb.delete(0, sb.length());
			}else{
				outAL.add(null);
			}
		}
		return outAL.toArray(new String[outAL.size()]);
	}

	/**
	 * This function parses a list of genes input by the user and removes
	 * any duplicates.
	 * 
	 * @param genelist the list of genes, input by the user
	 * @param inAL the array list where the results are added to
	 */
	public void parseUniqueGene(String genelist, ArrayList<String> inAL) {
		String[] in = genelist.split("([^A-Z0-9\\-]+)");
		ArrayList<String> out = new ArrayList<String>();
		for (String gene : in) {
			if (!out.contains(gene)) {
				out.add(gene);
			}
		}
		for (int i = 0; i < out.size(); i++) {
			inAL.add(out.get(i));
		}
	}

	/**
	 * This function populates the hponumbers with their genes.
	 * Passes through the association file line-by-line and adds
	 * a gene to each HPO number it comes across
	 * @param inassoc the association file to be used
	 */
	private void populateHPOGenes(File inassoc) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(inassoc));
			String hpo;
			String gene;
			String line;
			String[] split;
			StringBuilder invalidhpo = new StringBuilder(); // array of HPO files that do not exist
			int HPOColumn = -1;
			int GeneColumn = -1;
			Boolean invalid = false;
			long start, stop, time;
			start = System.currentTimeMillis();
			
			while ((line = in.readLine()) != null) {
				// get column numbers for data
				if (line.contains("#Format: ")) {
					split = line.substring(9).split("<tab>");
						for (int i = 0; i < split.length; i++) {
							if (split[i].equals("HPO-ID")) {
								HPOColumn = i;
							}if (split[i].equals("gene-symbol")) {
								GeneColumn = i;
							}
						}
				}
				// get data
				if (line.contains("HP:") && HPOColumn > -1 && GeneColumn > -1) {
					split = line.split("\t");
					hpo = split[HPOColumn];
					gene = split[GeneColumn];
					if (data.containsKey(hpo)) {
						data.get(hpo).addGene(gene);
					}else{
						/* if an HPO number does not exist, add it to the invalid
						 * list and flag the association file for invalid
						 */
						invalidhpo.append(hpo+"\n");
						invalid = true;
					}
				}
				if (data.size() == 0) {
					/* if the entire data set is empty, something went wrong
					 * in parsing the association file.
					 */
					invalid = true;
				}
			}
			if (invalid)
				if (HPOColumn == -1 || GeneColumn == -1) {
					new Error("Error in parsing gene file. Please (re)move "
							+ " or replace the .assoc file in the /HPO/ "
							+ "folder and try again.",
							"Parse error",
							WindowConstants.EXIT_ON_CLOSE);
				}else{
					new Error("Some genes were found with unknown HPO numbers,"
							+ " the HPO data might be outdated or invalid.\n"
							+ "Make sure your HPO files are up to date.\n\n"
							+ "List of HPO numbers that occur in the .assoc "
							+ "file, but not in the .hpo file:\n\n"
							+ invalidhpo.toString(),
							"Attention",
							WindowConstants.DISPOSE_ON_CLOSE);
				}
			in.close();
			stop = System.currentTimeMillis();
			time = stop - start;
			System.out.println("Populating gene list took "+time+" millis");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void populateHPOObject (File inhpo) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(inhpo));
			String line;
			String hpo = null;
			String pheno = null;
			ArrayList<String> altid = new ArrayList<String>();
			String definition = null;
			long start, stop, time;
			HPONumber _hpo = null;
			HPONumber parentObject;
			String parent;
			start = System.currentTimeMillis();
			while ((line = in.readLine()) != null) {
				if (line.startsWith("id: ")) {
					hpo = line.substring(4);
					if (!data.containsKey(hpo)) {
						_hpo = new HPONumber(hpo);
					}else{
						_hpo = data.get(hpo);
					}
				}
				if (line.startsWith("name: ")) {
					pheno = line.substring(6);
					_hpo.setPhenotype(pheno);
				}
				if (line.startsWith("def: ")) {
					definition = line.substring(5);
					_hpo.setDefinition(definition);
				}
				if (line.startsWith("alt_id: ")) {
					altid.add(line.substring(8));
				}
				if (line.startsWith("is_a: ")) {
					parent = line.substring(6, line.indexOf(" ! "));
					if (!data.containsKey(parent)) {
						parentObject = new HPONumber(parent, _hpo);
						data.put(parent, parentObject);
						_hpo.addParent(parentObject);
					}else{
						data.get(parent).addChild(_hpo);
						_hpo.addParent(data.get(parent));
					}
				}
				if (line.isEmpty()) {
					if (hpo != null) {
						data.put(hpo, _hpo);
					}
					for (String alt : altid) {
						data.put(alt, _hpo);
					}
					hpo = null;
					pheno = null;
					altid.clear();
					definition = null;
				}
			}
			in.close();
			stop = System.currentTimeMillis();
			time = stop - start;
			System.out.println("Building the HPO data took "+time+" millis");
			data.remove(0);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean reloadFiles() {
		data.clear();
		populateHPOObject(hpoFileHandler.getHPOFile());
		populateHPOGenes(hpoFileHandler.getAssocFile());
		// TODO throw error if fail
		return true;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}