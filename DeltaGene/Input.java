/*
 * Input class for DeltaGene
 * 
 * V1.0
 * 
 * 10-3-2015
 * 
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */

package DeltaGene;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;

//import DeltaGene.input.Browser.HPOTree;

/**
 * This class handles input from the user, generating headers and generating results.
 */
class input extends Gui {
	
	/**
	 * The HPOObject subclass contains many methods related to looking up sets of HPONumbers,
	 * and contains the HPONumber collection and the HPONumbers class itself.
	 */
	static class HPOObject {
		class Browser implements KeyListener, 
		MouseListener, ActionListener {
			/**
			 * JTreeplus extends JTree to add the collapseAll() method
			 */
			class HPOTree extends JTree {
				private static final long serialVersionUID = 1L;
				
				HPOTree(DefaultMutableTreeNode node) {
					super(node);
					DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
						private static final long serialVersionUID = -5119614872016462922L;
						
					};
					Icon hpoicon = null;
					renderer.setLeafIcon(hpoicon);
					renderer.setClosedIcon(hpoicon);
					renderer.setOpenIcon(hpoicon);
					
				}
				
				/**
				 * Custom function to expand selected paths.
				 * The only reason This function is
				 * used instead of JTree's native functions is that this is
				 * faster. With the native methods expanding 1500 elements
				 * would take about a minute.
				 */
				public void expandPaths () {
					
				}
				
				public void collapseAll() {
					for (int i = this.getRowCount()-1; i > 0; i--) {
						this.collapseRow(i);
					}
				}
				
				public void expandAll() {
					for (int i = this.getRowCount()-1; i > 0; i--) {
						this.expandRow(i);
					}
				}
			}
			
			private DefaultMutableTreeNode root = 
					new DefaultMutableTreeNode("Human Phenotype Ontology");
			private JFrame bwindow;
			private JPanel content;
			private HPOTree tree;
			private JPopupMenu treemenu;
			private JScrollPane jsp;
			private JPanel controls;
			private JLabel searchlabel;
			private JTextField search;
			private JButton searchbutton;
			private JButton listbutton;
			private JMenuItem treelistbtn;
			private JButton addbutton;
			
			Browser (HPOObject hpodata) {
				bwindow = new JFrame("HPO Browser");
				controls = new JPanel();
				content = new JPanel();
				jsp = new JScrollPane();
				searchlabel = new JLabel("Search:");
				search = new JTextField();
				searchbutton = new JButton("Find");
				listbutton = new JButton("List genes");
				addbutton = new JButton("Add to input");
				tree = new HPOTree(root);
				
				GridBagConstraints c = new GridBagConstraints();
				
				bwindow.setPreferredSize(new Dimension(700,600));
				bwindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				bwindow.setLayout(new GridBagLayout());
				
				search.addKeyListener(this);
				controls.setLayout(new FlowLayout());
				controls.add(searchlabel);
				search.setPreferredSize(new Dimension(80, 20));
				controls.add(search);
				searchbutton.addActionListener(this);
				searchbutton.addKeyListener(this);
				searchbutton.setActionCommand("search");
				controls.add(searchbutton);
				listbutton.addActionListener(this);
				listbutton.setActionCommand("list");
				controls.add(listbutton);
				addbutton.setEnabled(false);
				controls.add(addbutton);
				controls.setMaximumSize(new Dimension(bwindow.getWidth(), 30));
				
				content.setLayout(new GridBagLayout());
				c.fill = GridBagConstraints.BOTH;
				c.weightx = 1; c.weighty = 1;
				content.add(jsp, c);
				content.setMaximumSize(null);
				
				/*
				 * I'm putting these constraint sets one one line to make things
				 * a little tidier. I am aware this is probably not up to 
				 * standards or might hamper readability, but I am a bit tired
				 * of weird looking code with 14-character constraint sets 
				 * spread over 10 lines.
				 */
				c.fill = GridBagConstraints.BOTH;
				c.gridy = 0; c.gridx = 0; c.weightx = 1; c.weighty = 0.05;
				bwindow.add(controls, c);
				c.gridy = 1; c.gridx = 0; c.weightx = 1; c.weighty = 0.95;
				bwindow.add(content, c);
				
				tree.addMouseListener(this);
				//tree.setToggleClickCount(1);
				tree.setExpandsSelectedPaths(true);
				root.add(hpodata.getHPOHeirarchy("HP:0000001"));
				jsp.setViewportView(tree);
			}
			private ArrayList<TreePath> find(DefaultMutableTreeNode parent, String hpo) {
				ArrayList<TreePath> outAL = new ArrayList<TreePath>();
				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> e = parent.children();
				while (e.hasMoreElements()) {
					DefaultMutableTreeNode child = e.nextElement();
					if (child.getUserObject().toString().toLowerCase().contains(hpo.toLowerCase())) {
						outAL.add(new TreePath(child.getPath()));
					}
					if (child.getChildCount() > 0) {
						outAL.addAll(find(child, hpo));
					}
				}
				return outAL;
			}
			
			public void show(String hpo) {
				addbutton.setEnabled(false);
				show(hpo, null);
			}
			
			public void show(String hpo, userinput input) {
				//addbutton.setEnabled(true);
				//addbutton.setActionCommand("add:"+input.getID());
				tree.clearSelection();
				tree.collapseAll();
				for (TreePath path : find(root, hpo)) {
					tree.makeVisible(path);
					tree.addSelectionPath(path);
				}
				bwindow.pack();
				bwindow.setLocationRelativeTo(null);
				bwindow.setVisible(true);
				bwindow.revalidate();
				bwindow.repaint();
				bwindow.pack();
			}
			
			private void contextMenu(Point p) {
				treemenu = new JPopupMenu();
				treemenu.setPreferredSize(new Dimension(150,30));
				treelistbtn = new JMenuItem("List selected");
				treelistbtn.addActionListener(this);
				treelistbtn.setActionCommand("list");
				treemenu.add(treelistbtn);
				treemenu.setLocation(p);
				treemenu.setInvoker(bwindow);
				treemenu.pack();
				treemenu.revalidate();
				treemenu.repaint();
				treemenu.setVisible(true);
			}
		
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO this function
				if (e.getButton() == MouseEvent.BUTTON3) {
					contextMenu(e.getLocationOnScreen());
				}
			}
		
			@Override
			public void mouseEntered(MouseEvent e) {
			}
		
			@Override
			public void mouseExited(MouseEvent e) {
			}
		
			@Override
			public void mousePressed(MouseEvent e) {
			}
		
			@Override
			public void mouseReleased(MouseEvent e) {
			}
		
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getComponent().equals(search)) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						actionPerformed(new ActionEvent(search, ActionEvent.ACTION_PERFORMED, "search"));
					}
				}
			}
		
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
		
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("search")&&search.getText().length()>0) {
					tree.clearSelection();
					tree.collapseAll();
					ArrayList<TreePath> AL = find(root, search.getText());
					for (TreePath path : AL) {
						tree.makeVisible(path);
					}
					tree.setSelectionPaths(AL.toArray(new TreePath[AL.size()]));
					//TreePath[] paths = new TreePath[AL.size()];
					//paths = AL.toArray(paths);
				}if (e.getActionCommand().equals("list")) {
					TreePath[] paths = tree.getSelectionPaths();
					StringBuilder sb = new StringBuilder();
					String node;
					for (TreePath path : paths) {
						node = path.getLastPathComponent().toString();
						if (node.contains("HP:")) {
							sb.append(node.subSequence(0, 10));
							sb.append(',');
						}
					}
					sb.deleteCharAt(sb.length()-1);
					resultobject.generate(sb.toString());
				}if (e.getActionCommand().startsWith("add:")) {
					int id = Integer.parseInt(e.getActionCommand().substring(4));
					System.out.println(id);
				}
			}
		}

		/**
		 * The HPONumber (sub)subclass contains information about a particular HPO term,
		 * such as it's HPO number, phenotype, definition, genes and it's children/parents
		 */
		class HPONumber {
			private String hpoid = "Undefined";
			private String phenotype = "Undefined";
			private String definition = "Undefined";
			private ArrayList<String> genes = new ArrayList<String>();
			private ArrayList<HPONumber> children = new ArrayList<HPONumber>();
			
			HPONumber (String hpo) {
				set(hpo);
			}
			HPONumber (String hpo, String pheno, String def) {
				set(hpo, pheno, def);
			}
			
			public HPONumber(String hpo, HPONumber child) {
				hpoid = hpo;
				children.add(child);
			}
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
			 * The getIDCollection(ArrayList<String>) function populates an {@link ArrayList}<String> with this HPONumber's hpo number,
			 * and that of it's children (recursive).
			 * @param out the {@link ArrayList}<String> to be populated.
			 */
			public void getIDCollection(HashSet<String> out){
				getIDCollection(out, true);
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
			 * Returns the definition of this phenotype. May be undefined for certain HPONumbers (such as HP:0000001 - All)
			 * @return the description as a string
			 */
			public String definition() {
				return definition;
			}
			
			public ArrayList<HPONumber> getChildren() {
				return children;
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
				if (genes.contains(gene)) {
					return false;
				}
				genes.add(gene);
				return true;
			}
			
			/**
			 * This function tries to find 
			 * @param hpo	The @link{HPONumber} to search for
			 * @param recursive	Recursively searches this HPO number's children for hpo 
			 * @return true if hpo is a child of this HPO number. True if one of these children has hpo as a child and recursive = true
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
			public boolean containsChild(HPONumber hpo) {
				return containsChild(hpo, false);
			}
			
			/**
			 * Returns the amount of parents above this HPO number
			 * @return The amount of parents above this HPO number
			 */
			/*public int getParentCount() {
				return parents.size();
			}*/
			
			/**
			 * Returns the amount of children under this HPO number.
			 * @return The number of children under this HPO number.
			 */
			public int getChildCount() {
				return children.size();
			}
			
			/**
			 * Returns true if this HPO number's gene list > 0
			 * @return True if gene list > 0, false if not.
			 */
			public boolean hasGenes() {
				if (genes.size() > 0) {
					return true;
				}
				return false;
			}
			
			/**
			 * Populates an ArrayList with this HPONumber's genes. Will not add duplicate genes
			 * @see collectGenes
			 * @return A collection of genes as strings
			 */
			public void getGenes(ArrayList<String> inAL, HashSet<String> set) {
					for (String gene : genes) {
						if (!set.contains(gene)) {
							set.add(gene);
							inAL.add(gene);
						}
					}
			}
			public void setPhenotype(String pheno) {
				phenotype = pheno;
			}
			public void setDefinition(String def) {
				definition = def;
			}
		}
		
		/*
		 * The data HashMap contains all instances of HPONumbers objects.
		 * The key to each HPONumber is it's hpo id (HP:#######)
		 */
		private static HashMap<String, HPONumber> data = new HashMap<String, HPONumber>();
		public Browser browser;
		
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
		
		/**
		 * This function returns a full list of phenotypes, to be used in the
		 * autocompletion of input boxes. The list of phenotypes will also
		 * contain meta information, such as age of onset.
		 * @return a full ArrayList of phenotypes.
		 */
		public ArrayList<String> getPhenosList() {
			ArrayList<String> out = new ArrayList<String>();
			HashSet<String> set = new HashSet<String>();
			String pheno;
			for (Entry<String, HPONumber> entry : data.entrySet()) {
				pheno = entry.getValue().phenotype();
				if (!set.contains(pheno)) {
					set.add(pheno);
					out.add(pheno);
				}
			}
			return out;
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
		public void findHPOGenes(String hpo, ArrayList<String> inAL) {
			findHPOGenes(hpo, inAL, true);
		}
		
		/**
		 * This function parses a list of genes input by the user and removes
		 * any duplicates.
		 * 
		 * @param genelist the list of genes, input by the user
		 * @param inAL the array list where the results are added to
		 */
		public void parseUniqueGene(String genelist, ArrayList<String> inAL) {
			String[] in = genelist.split("([\\s\\W\\n\\r]+)");
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
		 * This function returns a node of a given hpo and it's children
		 * @param hpo a string starting with a HPO number. 
		 * @return node of a given hpo and it's children
		 */
		public DefaultMutableTreeNode getHPOHeirarchy(String hpo) {
			if (hpo.equals("HP:0011687")) {
				System.out.println();
			}
			String hpoin = hpo.substring(0, 10);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(hpoin+" - "+data.get(hpoin).phenotype());
			for(HPONumber child : data.get(hpoin).getChildren()) {
				if (child.getChildCount() > 0) {
					node.add(getHPOHeirarchy(child.hpo()));
				}else{
					DefaultMutableTreeNode childnode = new DefaultMutableTreeNode(child.hpo()+" - "+child.phenotype());
					node.add(childnode);
				}
			}
			return node;
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
					}else{
						data.get(parent).addChild(_hpo);
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
	
	private void populateHPOGenes(File inassoc) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(inassoc));
			String hpo;
			String gene;
			String line;
			Boolean invalid = false;
			long start, stop, time;
			start = System.currentTimeMillis();
			while ((line = in.readLine()) != null) {
				if (line.contains("HP:")) {
					hpo = line.substring(line.indexOf("HP:"), line.indexOf("\t", line.indexOf("HP:")));
					gene = line.substring(line.indexOf("\t")+1, line.indexOf("\t", line.indexOf("\t")+1));
					if (data.containsKey(hpo)) {
						data.get(hpo).addGene(gene);
					}else{
						invalid = true;
					}
				}
			}
			if (invalid)
				new Error("Some genes were found with unknown HPO numbers,"
						+ " the HPO data might be outdated or invalid!\n"
						+ "Make sure your HPO files are up to date.",
						"Attention",
						JFrame.DISPOSE_ON_CLOSE);
			in.close();
			stop = System.currentTimeMillis();
			time = stop - start;
			System.out.println("Populating gene list took "+time+" millis");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	HPOObject(File inhpo, File inassoc) {
		populateHPOObject(inhpo);
		populateHPOGenes(inassoc);
		browser = new Browser(this);
	}

	public Browser getBrowser() {
		return browser;
	}
}
	
	/**
	 * This class handles the autocompletion for the textareas
	 */
	class Autocomplete extends input implements DocumentListener, ActionListener {
		class keyHandler implements KeyListener, FocusListener {
			private void highlightselection (int index) {
				for (int i = 0; i < dropdown.getComponentCount(); i++) {
					if (i == index) {
						dropdown.getComponent(i).setBackground(UIManager.getColor("MenuItem.selectionBackground"));
						dropdown.getComponent(i).setForeground(UIManager.getColor("MenuItem.selectionForeground"));
					}else{
						dropdown.getComponent(i).setBackground(UIManager.getColor("MenuItem.background"));
						dropdown.getComponent(i).setForeground(UIManager.getColor("MenuItem.foreground"));
					}
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				int index = dropdown.getSelectionModel().getSelectedIndex();
				if (key == KeyEvent.VK_DOWN&&dropdown.isVisible()) {
					if (index+1>dropdown.getComponentCount()-1) {
						dropdown.getSelectionModel().setSelectedIndex(0);
					}else{
						dropdown.getSelectionModel().setSelectedIndex(index+1);
					}
				}
				if (key == KeyEvent.VK_UP&&dropdown.isVisible()) {
					if (index-1<0) {
						dropdown.getSelectionModel().setSelectedIndex(dropdown.getComponentCount()-1);
					}else{
						dropdown.getSelectionModel().setSelectedIndex(index-1);
					}
				}
				highlightselection(dropdown.getSelectionModel().getSelectedIndex());
				if (key == KeyEvent.VK_ENTER&&dropdown.isVisible()) {
					if (index > -1) {
						ActionEvent ae = new ActionEvent(dropdown.getComponent(index), ActionEvent.ACTION_PERFORMED, ((JMenuItem)dropdown.getComponent(index)).getActionCommand());
						actionPerformed(ae);
						dropdown.setVisible(false);
					}
				}
				if (key == KeyEvent.VK_ESCAPE&&dropdown.isVisible()) {
					dropdown.setVisible(false);
				}
			}
			

			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if ((key == KeyEvent.VK_DOWN||key == KeyEvent.VK_UP||key == KeyEvent.VK_ENTER)&&dropdown.isVisible()) {
					e.consume();
				}
			}
			
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (invoker == null||!e.getComponent().equals(invoker.getInputBox())) {
					invoker = getInputboxObject((JTextArea)e.getSource());
					dropdown.setInvoker(invoker.getInputBox());
				}
			}

			@Override
			public void focusLost(FocusEvent arg0) {
			}
		}
		
		class acWindow extends JPopupMenu {
			private static final long serialVersionUID = 1L;
			public void show(int num) {
				setPreferredSize(new Dimension(invoker.getSize().width, num*22));
				setLocation(invoker.getLocationOnScreen().x, invoker.getLocationOnScreen().y+invoker.getSize().height);
				pack();
				revalidate();
				repaint();
				if (!isVisible()) {
					setVisible(true);
				}	
			}
		}
		
		acWindow dropdown;
		keyHandler kh;
		
		userinput invoker;
		ArrayList<String> phenos = new ArrayList<String>();
		
		public Autocomplete (ArrayList<String> keywords) {
			kh = new keyHandler();
			dropdown = new acWindow();
			dropdown.addFocusListener(kh);
			dropdown.setVisible(false);
			phenos.addAll(keywords);
			Collections.sort(phenos);
		}
		
		public void add (userinput input) {
			input.getDocument().addDocumentListener(this);
			input.getInputBox().addFocusListener(kh);
			input.getInputBox().addKeyListener(kh);
		}
		
		@Override
		public void insertUpdate (DocumentEvent e) {
			int len;
			String s;
			String hpo;
			Document doc = e.getDocument();
			JMenuItem jmitem;
			int num = 0;
			
			
			invoker.getInputBox().setComponentPopupMenu(dropdown);
			dropdown.removeAll();
			dropdown.setInvoker(invoker.getInputBox());
			len = doc.getLength();
			if (len > 2) {
				s = invoker.getText();
				if (invoker.getInputType() == 0) {
					for (String search : phenos) {
						if (search.toLowerCase().contains(s.toLowerCase())&&num<10) {
							hpo = HPODATA.getHPOFromPhenotype(search);
							jmitem = new JMenuItem(search+" ("+hpo+")");
							jmitem.setComponentPopupMenu(dropdown);
							jmitem.addActionListener(this);
							jmitem.addKeyListener(kh);
							jmitem.setActionCommand(hpo);
							jmitem.setEnabled(true);
							jmitem.setOpaque(true);
							dropdown.add(jmitem);
							num++;
						}
					}
				}else{
					return;
				}
			}
			dropdown.show(num);
			invoker.getInputBox().requestFocusInWindow();
		}
		
		@Override
		public void removeUpdate (DocumentEvent e) {
			insertUpdate(e);
		}
		
		public void actionPerformed(ActionEvent e) {
			String hpo = e.getActionCommand();
			((JTextArea)dropdown.getInvoker()).setText(hpo);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {}
	}
	
	class userinput implements DocumentListener, HyperlinkListener {
		private JTextArea inputbox;
		private JEditorPane infobox;
		private JScrollPane inputjsp;
		private JScrollPane infojsp;
		private Document inputdoc;
		private int group;
		private int id;
		
		userinput(int assignedgroup) {
			id = getInputCount();
			group = assignedgroup;
			inputbox = new JTextArea();
			inputdoc = inputbox.getDocument();
			inputjsp = new JScrollPane(inputbox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			inputjsp.setPreferredSize(new Dimension(inputcontainer.getWidth()-10, getInputh()));
			inputdoc.addDocumentListener(this);
			inputbox.setFocusTraversalKeysEnabled(false);
			infobox = new JEditorPane();
			infobox.setContentType("text/html");
			infobox.addHyperlinkListener(this);
			infobox.setBackground(Color.getHSBColor(0, 0, (float)0.9));
			infojsp = new JScrollPane(infobox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			infojsp.setPreferredSize(new Dimension(inputcontainer.getWidth()-10, getInfoh()));
			infobox.setEditable(false);
			inputcontainer.add(inputjsp);
			inputcontainer.add(infojsp);
			ac.add(this);
			updateInfoBox();
		}
		
		Dimension getSize() {
			return inputbox.getSize();
		}
		
		int getID() {
			return id;
		}

		void setInputGroup(int newgroup) {
			group = newgroup;
		}
		
		int getInputGroup() {
			return group;
		}
		
		String getInputText () {
			return inputbox.getText();
		}
		
		String getText() {
			return getInputText();
		}
		
		void setInputText(String text) {
			inputbox.setText(text);
		}
		
		void setInputInfo(String text) {
			infobox.setText(text);
		}
		
		JEditorPane getInfoBox() {
			return infobox;
		}
		
		JTextArea getInputBox() {
			return inputbox;
		}
		
		int getInputType() {
			String in = getInputText();
			/*
			 * The first if checks if HP: occurs at all, in which case an HPO
			 * number is assumed to be the input. The second if checks if
			 * any other instances of HP: occur after the first, which signifies
			 * a list of HPO numbers.
			 */
			if (in.toUpperCase().indexOf("HP:") > -1) { // check if HP: (case insensitive) occurs in the list
				if (in.toUpperCase().substring(in.toUpperCase().indexOf("HP:")+1).contains("HP:")) { // check if it occurs again after the first occurence
					return 1;
				}else{
					return 2;
				}
			}
			/*
			 *  This pattern will match either a (list of) digits,
			 *  gene symbols or anything else, 
			 *  in that order, whichever comes first.
			 */
			Pattern _regx = Pattern.compile("((?:\\W*\\d{1,7})+)|((?:[A-Z][A-Z0-9]+.*?\\n?)+)|(.*)");
			Matcher _match = _regx.matcher(in);
			if (_match.find()) {
				/* (list of) genes. Gene symbols always begin with an uppercase letter.
				 * Furthermore, Gene symbols only contain letters A-Z and numbers 0-9.
				 */
				if (_match.group(2) != null){ 		
					return 3;
				}
				
				/* Just digits. This will be parsed as an HPO number as
				 * long as the amount of digits in one number does not exceed 7
				 */
				if (_match.group(1) != null) {
					return 4;
				}
				
				/*  if all else fails, return 0. this is assumed to be invalid input.
				 *  Autocomplete tries to search when:
				 *  - the user is typing and this is returned
				 *  - the user's input is > 2 characters
				 */ 
				else{							
					return 0;
				}
			}else{
				return 0;
			}
		}
		
		public Document getDocument() {
			return inputdoc;
		}
		
		public Point getLocationOnScreen() {
			return inputbox.getLocationOnScreen();
		}
		
		public void clear() {
			inputbox.setText("");
		}

		@Override
		public void changedUpdate(DocumentEvent e) {}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateInfoBox(getInputType());
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e);
		}

		public JScrollPane getInputScrollPane() {
			return inputjsp;
		}

		public JScrollPane getInfoScrollPane() {
			return infojsp;
		}
		
		public void updateInfoBox() {
			updateInfoBox(getInputType());
		}
		
		public void updateInfoBox(int type) {
			String pheno;
			StringBuilder sb = new StringBuilder();
			switch(type) {
			case 0:		
				setInputInfo("Type Genes, HPO number, "
						+ "or start typing a phenotype in the field.");
				break;
			case 1:
				sb.append("Type: List of HPO numbers:");
				String[] hponums = getInputText().toUpperCase().split("([^A-Z0-9:]+)");
				for (String hpo : hponums) {
					pheno = HPODATA.getPhenotypeFromHPO(hpo);
					if (pheno == null) {
						sb.append("<br>"+hpo+" - No associated phenotype.");
					}else{
						sb.append("<br>"+hpo+" - "+pheno+". <a href=\"genes:"
					+hpo+"\">List genes</a> | <a href=\"tree:"+hpo
					+"\">Show in browser</a>");
					}
				}
				infobox.setText(sb.toString());
				break;
			case 2:
				String text = getInputText().toUpperCase();
				if (text.length() == 10) {
					if (HPODATA.getPhenotypeFromHPO(text) != null) {
						infobox.setText("Type: Single HPO number:<br>"+text+" - "
					+HPODATA.getPhenotypeFromHPO(text)+". <a href=\"genes:"+text
					+"\">List genes</a> | <a href=\"tree:"+text+"\">Show in browser</a>");
					}else{
						infobox.setText("Type: Single HPO number:<br>"+text+" - no associated phenotype.");
					}
				}else{
					infobox.setText("Type: Single HPO number:<br>(Requires seven digits)");
				}
				break;
			case 3:
				infobox.setText("Type: List of genes");
				break;
			case 4:
				sb.append("Type: List of numbers (Parsed as HPO numbers):");
				for (String hpo : HPODATA.parseNumbersAsHPO(inputbox.getText())) {
					if (hpo == null) {
						hpo = "Invalid HPO number";
					}
					pheno = HPODATA.getPhenotypeFromHPO(hpo);
					if (pheno != null) {
						sb.append("<br>"+hpo+" - "+pheno+". <a href=\"genes:"+hpo+"\">List genes</a> | <a href=\"tree:"+hpo+"\">Show in browser</a>");
					}else{
						sb.append("<br>"+hpo+" - Unknown phenotype.");
					}
				}
				infobox.setText(sb.toString());
				break;
			default:	
				infobox.setText("Invalid input");
				break;
			}
		}

		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String desc = e.getDescription();
				if (desc.contains("genes:")) {
					resultobject.generate(desc.substring(6));
				}else if (desc.contains("tree:")) {
					HPODATA.browser.show(desc.substring(5));
				}
			}
		}
	}
	
	class result implements ActionListener {
		JFrame reswindow;
		JScrollPane respanelsp;
		JMenuBar menubar;
		JMenu filemenu;
		JMenuItem export;
		Container controlcont;
		Container rescont;
		JTable restable;
		
		String[] headers;
		String[][] results;
		
		public void showResults(String[][] results, String[] headers) {
			reswindow = new JFrame("Results");
			restable = new JTable(results, headers);
			if (headers.length > 5) {
				restable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}
			respanelsp = new JScrollPane(restable);
			menubar = new JMenuBar();
			filemenu = new JMenu("File");
			export = new JMenuItem("Export to file...");
			export.addActionListener(this);
			export.setActionCommand("export");
			reswindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			reswindow.setPreferredSize(window.getSize());
			reswindow.setJMenuBar(menubar);
			menubar.add(filemenu);
			filemenu.add(export);
			reswindow.add(respanelsp);
			reswindow.pack();
			reswindow.setLocationRelativeTo(null);
			reswindow.setVisible(true);
		}
		
		/**
		 * This method will pass a list of genes associated with a given
		 * hpo number to generateResults(ArrayList<String> ArrayList<ArrayList<String>>, int)
		 * @param hpo a specific, valid hpo number.
		 */
		public void generateResults(String hpo) {
			ArrayList<ArrayList<String>> oB = new ArrayList<ArrayList<String>>();
			for (String hpon : hpo.split("(,)")) {
				oB.add(new ArrayList<String>());
				HPODATA.findHPOGenes(hpon, oB.get(oB.size()-1),true);
			}
			generateResults(null, oB, LIST);
		}
		
		/**
		 * This method will retrieve the genes associated with an hpo number input by the user
		 * and passes it to generateResults(ArrayList<String> ArrayList<ArrayList<String>>, int).
		 * @param inputs the userinputs available
		 */
		public void generateResults(ArrayList<userinput> inputs) {
			ArrayList<String> oA = new ArrayList<String>();
			ArrayList<ArrayList<String>> oB = new ArrayList<ArrayList<String>>();
			String txt;
			int type;
			
			for (userinput input : inputs) {
				if (input.getInputGroup() == 0) {
					txt = input.getInputText();
					type = input.getInputType();
					if (type == 0) {
						setInputError("Input A is invalid!");
						return;
					}else if (type == 1||type == 2) {
						
						HPODATA.findHPOGenes(txt, oA);
					}else if (type == 3) {
						HPODATA.parseUniqueGene(txt, oA);
					}else if (type == 4) {
						HPODATA.findHPOGenes(HPODATA.parseNumbersAsHPO(txt), oA, true);
					}
				}else{
					oB.add(new ArrayList<String>());
					txt = input.getInputText();
					type = input.getInputType();
					if (type == 0) {
						setInputError("One of the B inputs is invalid!");
						return;
					}else if (type == 1||type == 2) {
						HPODATA.findHPOGenes(txt, oB.get(oB.size()-1));
					}else if (type == 3) {
						HPODATA.parseUniqueGene(txt, oB.get(oB.size()-1));
					}else if (type == 4) {
						HPODATA.findHPOGenes(HPODATA.parseNumbersAsHPO(txt), oB.get(oB.size()-1), true);
					}
				}
			}
			generateResults(oA, oB, getOperator());
		}
		
		/**
		 * generateResults will compare all genes associated with or input in the inputboxes by the user.
		 * The size of the array that is returned depends on the amount of genes left at the end of the comparison,
		 * and the operator that is used.
		 * @return a multi-dimensional array where String[x] is a gene and String[][x] is an input.
		 */
		public void generateResults(ArrayList<String> oA, ArrayList<ArrayList<String>> oB, int operator) {
			String[][] outArray = null;
			int oBSize = 0;

			if (operator != LIST) {
				if (oA.size() == 0) {
					setInputError("Input A has no associated genes.");
					return;
				}
				Collections.sort(oA);
			}
			
			for (ArrayList<String> B : oB) {
				if (B.size() > oBSize) {
					oBSize = B.size();
				}
				if (operator == LIST) {
					Collections.sort(B);
				}
			}
			System.out.println(oBSize);
			if (oBSize == 0) {
				setInputError("Input(s) B do not have any associated genes");
				return;
			}
			
			switch (operator) {
			case DEFAULT:
				// creates a new array which will fit all genes in operand A and all inputs.
				outArray = new String[oA.size()][oB.size()+1];
				//fills the output array's [][0] cells with genes from operand A
				for (int i = 0; i < oA.size(); i++) {
					outArray[i][0] = oA.get(i);
				}
				for (int i = 0; i < oB.size(); i++) { // loop through each operand B input
					for (int o = 0; o < outArray.length; o++) { // loop through each gene in the outArray
						if (oB.get(i).contains(outArray[o][0])) {
							// if the gene is present in the currently selected operand B input, add it to outArray
							outArray[o][i+1] = outArray[o][0];
						}else{
							/* if gene is not present in currently selected operand B input, set the null value of that cell
							 * to an empty string.
							 */
							if (outArray[o][i] == null) {
								outArray[o][i] = "";
							}
						}
					}
				}
				results = outArray;
				break;
			case AND:
				// TODO show how many genes differ between input and output
				
				// loop through each input in operand B
				for (int i = 0; i < oB.size(); i++) {
					// loop through each element in operand A backwards
					for (int o = oA.size()-1; o >= 0; o--) {
						/*
						 *  remove every element in operand A that does not exist in
						 *  the currently selected operand B input
						 */
						if (!oB.get(i).contains(oA.get(o))) {
							oA.remove(o);
						}
					}
				}
				if (oA.size() == 0) {
					setInputError("The command \"A AND B\" returns an empty result!\n"
								+ "This means that there are no genes that occur in both A AND B.\n"
								+ "Use the 'Default' setting to confirm this.");
					results = null;
				}else{
					outArray = new String[oA.size()][1];
					for (int i = 0; i < oA.size(); i++) {
						outArray[i][0] = oA.get(i);
					}
					results =  outArray;
				}
			break;
			case NOT:
				for (int i = 0; i < oB.size(); i++) {
					for (int o = 0; o < oB.get(i).size(); o++) {
						if (oA.contains(oB.get(i).get(o))) {
							oA.remove(oB.get(i).get(o));
						}
					}
				}
				if (oA.size() == 0) {
					setInputError("The command \"A NOT B\" returns an empty result!\n"
										+ "This means that there are no unique genes in A.\n"
										+ "Use the 'Default' setting to confirm this.");
					results = null;
				}else{
					outArray = new String[oA.size()][1];
					for (int i = 0; i < oA.size(); i++) {
						outArray[i][0] = oA.get(i);
					}
					results = outArray;
				}
			break;
			case XOR:
				int maxSize = 0;
				oBSize = 0;
				for (int i = 0; i < oB.size(); i++) {
					for (int o = oB.get(i).size()-1; o >= 0; o--) {
						if (oA.contains(oB.get(i).get(o))) {
							oA.remove(oB.get(i).get(o));
							oB.get(i).remove(o);
						}
					}
					if (oB.get(i).size() > maxSize) {
						maxSize = oB.get(i).size();
					}
					oBSize += oB.get(i).size();
				}
				if (oA.size() == 0&&oBSize == 0) {
					setInputError("The command \"A XOR B\" returns an empty result!\n"
								+ "This means that there are no genes unique to A or B.");
					results = null;
				}else{
					if (oA.size() > maxSize) {
						maxSize = oA.size();
					}
					outArray = new String[maxSize][oB.size()+1];
					for (int i = 0; i < maxSize; i++) {
						if (i < oA.size()) {
							outArray[i][0] = oA.get(i);
						}else{
							outArray[i][0] = "";
						}
						for (int o = 0; o < oB.size(); o++) {
							if (i < oB.get(o).size()) {
								outArray[i][o+1] = oB.get(o).get(i);
							}else{
								outArray[i][o+1] = "";
							}
						}
					}
					results = outArray;
				}
			break;
			case LIST:
				outArray = new String[oBSize][oB.size()];
				for (int x = 0; x < oB.size(); x++) {
					for (int y = 0; y < oB.get(x).size(); y++) {
						outArray[y][x] = oB.get(x).get(y);
					}
				}
				results = outArray;
			break;
			}
		}

		public void getHeaders(ArrayList<userinput> inputs) {
			getHeaders(inputs, getOperator());
		}
		
		public void getHeaders(ArrayList<userinput> inputs, int operator) {
			ArrayList<String> outAL = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			if (operator == AND || operator == NOT) {
				for (userinput input : inputs) {
					String txt = input.getInputText();
					//String hpo;
					switch (input.getInputType()) {
					case 1:
						for (String hpon : txt.split("([^A-Z0-9:]+)")) {
							sb.append(hpon+" - "+HPODATA.getPhenotypeFromHPO(hpon));
						}
						break;
					case 2:
						sb.append(txt+" - "+HPODATA.getPhenotypeFromHPO(txt));
						break;
					case 3:
						sb.append("List of genes");
						break;
					case 4:
						for (String hpon : HPODATA.parseNumbersAsHPO(txt)) {
							sb.append(hpon+" - "+HPODATA.getPhenotypeFromHPO(hpon));
						}
					}
					if (input.getInputGroup() == 0) {
						if (getOperator() == AND) {
							sb.append(" AND (");
						}else{
							sb.append(" NOT (");
						}
					}
					if (input.getID() == getInputCount()-1) {
						sb.append(")");
					}
				}
				outAL.add(sb.toString());
			}else if (operator == DEFAULT || operator == XOR) {
				for (userinput input : inputs) {
					String txt = input.getInputText();
					String[] hpolist;
					int type = input.getInputType();
					if (type == 2) { type--; }
					switch (type) {
						case 1:
							hpolist = txt.split("([^A-Z0-9:]+)");
							for (int i = 0; i < hpolist.length; i++) {
								if (hpolist.length > 1) {
									sb.append(hpolist[i]);
									if (i < hpolist.length-1) {
										sb.append(", ");
									}
								}else{
									sb.append(hpolist[i]+" - "+HPODATA.getPhenotypeFromHPO(hpolist[i]));
								}
							}
							outAL.add(sb.toString());
							break;
						case 3:
							sb.append("List of genes.");
							outAL.add(sb.toString());
							break;
						case 4:
							hpolist = HPODATA.parseNumbersAsHPO(txt);
							for (int i = 0; i < hpolist.length; i++) {
								if (hpolist.length > 1) {
									sb.append(hpolist[i]);
									if (i < hpolist.length-1) {
										sb.append(", ");
									}
								}else{
									sb.append(hpolist[i]+" - "+HPODATA.getPhenotypeFromHPO(hpolist[i]));
								}
							}
							outAL.add(sb.toString());
							break;
						default:
							outAL.add("Undefined header");
							break;
					}
					sb.delete(0, sb.length());
				}
			}else if (operator == LIST) {
				for (userinput input : inputs) {
					String txt = input.getInputText();
					String hpo;
					switch (input.getInputType()) {
						case 1:
							hpo = txt.subSequence(txt.indexOf("HP:"), txt.indexOf("HP:"+9)).toString(); 
							sb.append(hpo+" - "+HPODATA.getPhenotypeFromHPO(hpo));
							outAL.add(sb.toString());
							break;
						case 2: 
							sb.append("List of HPO Numbers");
							outAL.add(sb.toString());
							break;
						case 3:
							sb.append("List of genes.");
							outAL.add(sb.toString());
							break;
						case 4:
							for (String hpon : HPODATA.parseNumbersAsHPO(txt)) {
								sb.append(hpon+" - "+HPODATA.getPhenotypeFromHPO(hpon));
							}
							outAL.add(sb.toString());
							break;
						default:
							outAL.add("Undefined header");
							break;
					}
					sb.delete(0, sb.length());
				}
			}
			
			headers = new String[outAL.size()];
			headers = outAL.toArray(headers);
		}
		/*
		public void generateList(String hpo) {
			headers = new String[1];
			headers[0] = hpo;
			generateResults(hpo);
			if (results == null) {
				new Error(getInputError(), "Results", JFrame.DISPOSE_ON_CLOSE);
				return;
			}
			showResults(results, headers);
		}*/
		
		public void generate(ArrayList<userinput> in) {
			headers = null;
			results = null;
			getHeaders(in);
			generateResults(in);
			if (results == null) {
				new Error(getInputError(), "Result error", JFrame.DISPOSE_ON_CLOSE);
				return;
			}
			showResults(results, headers);
		}
		
		public void generate(String hpo) {
			headers = null;
			results = null;
			headers = hpo.split("(,)");
			for (int i = 0; i < headers.length; i++) {
				headers[i] = headers[i]+" - "+HPODATA.getPhenotypeFromHPO(headers[i]);
			}
			generateResults(hpo);
			if (results == null) {
				new Error(getInputError(), "Result error", JFrame.DISPOSE_ON_CLOSE);
				return;
			}
			showResults(results, headers);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ae.getActionCommand().equals("export")) {
				try {
					/**
					 * This override of the approveSelection method of the JFileChooser class
					 * will ask the user whether they want to overwrite the
					 * selected file if it exists.
					 */
					JFileChooser filechooser = new JFileChooser() {
						private static final long serialVersionUID = 1L;

						@Override
						public void approveSelection() {
							File f = getSelectedFile();
							if (f.exists()&&getDialogType() == SAVE_DIALOG&&f.toString().toLowerCase().endsWith(".csv")) {
								int result = JOptionPane.showConfirmDialog(this,
										"Would you like to overwrite the existing file?",
										"Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION);
								switch (result) {
									case JOptionPane.YES_OPTION:
										super.approveSelection();
										break;
									case JOptionPane.NO_OPTION:
										break;
									case JOptionPane.CANCEL_OPTION:
										break;
									case JOptionPane.CLOSED_OPTION:
										break;
								}
							}else{
								super.approveSelection();
							}
						}
					};
					File file;
					String newline = System.getProperty("line.separator");
					int res = filechooser.showSaveDialog(reswindow);
					if (res == JFileChooser.APPROVE_OPTION) {
						file = filechooser.getSelectedFile();
						if (!file.toString().toLowerCase().endsWith(".csv")) {
							file = new File(file.getAbsolutePath()+".csv");
						}
						if (!file.createNewFile()) {
							file.delete();
							if (!file.createNewFile()) {
								new Error("Could not create export file.", "Export error", JFrame.DISPOSE_ON_CLOSE);
								return;
							}
						}
						FileWriter outfile = new FileWriter(file);
						for (int i = 0; i < headers.length; i++) {
							outfile.append(headers[i]+" - "+HPODATA.getPhenotypeFromHPO(headers[i]));
							if (i < headers.length-1) {
								outfile.append(",");
							}
						}
						outfile.append(newline);
						for (String[] resultsy : results) {
							for (int i = 0; i < resultsy.length; i++) {
								outfile.append(resultsy[i]);
								if (i < resultsy.length-1) {
									outfile.append(",");
								}
							}
							outfile.append(newline);
						}
						outfile.close();
					}else if (res == JFileChooser.ERROR_OPTION) {
						new Error("An error occurred when choosing an export file", "Export error", JFrame.DISPOSE_ON_CLOSE);
						return;
					}
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//public String[] headers = new String[getMaxInputs()];
	public static HPOObject HPODATA;
	public static ArrayList<userinput> inputs;
	public static result resultobject;
	Autocomplete ac;
	final static int DEFAULT = 0;
	final static int AND = 1;
	final static int NOT = 2;
	final static int XOR = 3;
	final static int LIST = 4;
	private static int OPERATOR = DEFAULT;
	private String error;

	public String getInputError() {
		return error;
	}
	
	public void setInputError(String errmsg) {
		error = errmsg;
	}
	
	int getOperator() {
		return OPERATOR;
	}
	
	public void setOperator (int op){
		OPERATOR = op;
	}
	
	void addInput(int count, int assignedgroup) {
		for(int i = 0; i < count; i++) {
			userinput input = new userinput(assignedgroup);
			inputs.add(input);
			resizeInputContainer();
		}
	}
	
	void removeInput() {
		int last = inputs.size()-1;
		if (last > 1) {
			inputcontainer.remove(inputs.get(last).getInputBox());
			inputcontainer.remove(inputs.get(last).getInputScrollPane());
			inputcontainer.remove(inputs.get(last).getInfoBox());
			inputcontainer.remove(inputs.get(last).getInfoScrollPane());
			inputs.remove(inputs.size()-1);
			resizeInputContainer();
		}
	}
	
	/**
	 * This function returns an integer which signifies the type of input
	 * the user has typed into the input box.
	 * <ol>
	 * <li>List of HPO numbers</li>
	 * <li>Single HPO number</li>
	 * <li>List of genes</li>
	 * <li>Digits to be parsed as HPO number</li>
	 * </ol>
	 * @param in the user's input to be parsed.
	 * @return an integer signifying the input type, 0 if unrecognized input.
	 */
	
	public void clearInputs() {
		for (userinput input : inputs) {
			input.clear();
		}
	}

	input() {
		inputs = new ArrayList<userinput>();
		resultobject = new result();
	}
	
	public void getResults() {
		resultobject.generate(inputs);
	}
	
	int getInputCount() {
		return inputs.size();
	}
	
	String getInputText(int index) {
		return inputs.get(index).getInputText();
	}
	
	userinput getDocumentInputObject(Document doc) {
		for (userinput input : inputs) {
			if (input.getDocument().equals(doc)) {
				return input;
			}
		}
		return null;
	}
	
	userinput getInputboxObject(JTextArea ta) {
		for (userinput input : inputs) {
			if (input.getInputBox().equals(ta)) {
				return input;
			}
		}
		return null;
	}
	
	public void initialize(File hpofile, File assocfile) {
		HPODATA = new HPOObject(hpofile, assocfile);
		ac = new Autocomplete(HPODATA.getPhenosList());
	}

	public HPOObject getData() {
		return HPODATA;
	}
}
