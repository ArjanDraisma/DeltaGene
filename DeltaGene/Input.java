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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.TreeUI;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.sun.org.apache.xerces.internal.impl.xs.SubstitutionGroupHandler;

//import DeltaGene.input.Browser.HPOTree;

/**
 * This class handles input from the user, generating headers and generating 
 * results.
 * @author ArjanDraisma
 * 
 */
class Input {
	class Autocomplete implements DocumentListener, ActionListener {
		/**
		 * @author ArjanDraisma
		 * acWindow implements a custom version of show, tailored to the
		 * amount of autocomplete items that have been found.
		 */
		class AutocompleteWindow extends JPopupMenu {
			private static final long serialVersionUID = 1L;
			public void show(int num) {
				setPreferredSize(new Dimension(acInvoker.getSize().width,
						num*22));
				setLocation(acInvoker.getInputBoxLocationOnScreen().x,
						acInvoker.getInputBoxLocationOnScreen().y
						+ acInvoker.getSize().height);
				pack();
				revalidate();
				repaint();
				if (!isVisible()) {
					setVisible(true);
				}	
			}
		}
		
		class keyHandler implements KeyListener, FocusListener {
			@Override
			public void focusGained(FocusEvent e) {
				if (acInvoker == null||!e.getComponent().equals(
						acInvoker.getInputBox())) {
					acInvoker = getInputboxObject((JTextArea)e.getSource());
					acDropdownBox.setInvoker(acInvoker.getInputBox());
				}
			}
			
			@Override
			public void focusLost(FocusEvent arg0) {
			}
			

			private void highlightselection (int index) {
				for (int i = 0; i < acDropdownBox.getComponentCount(); i++) {
					if (i == index) {
						acDropdownBox.getComponent(i).setBackground(
								UIManager.getColor(
										"MenuItem.selectionBackground"));
						acDropdownBox.getComponent(i).setForeground(
								UIManager.getColor(
										"MenuItem.selectionForeground"));
					}else{
						acDropdownBox.getComponent(i).setBackground(
								UIManager.getColor(
										"MenuItem.background"));
						acDropdownBox.getComponent(i).setForeground(
								UIManager.getColor(
										"MenuItem.foreground"));
					}
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if ((key == KeyEvent.VK_DOWN||key == KeyEvent.VK_UP||key == KeyEvent.VK_ENTER)&&acDropdownBox.isVisible()) {
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				int index = acDropdownBox.getSelectionModel().getSelectedIndex();
				if (key == KeyEvent.VK_DOWN&&acDropdownBox.isVisible()) {
					if (index+1>acDropdownBox.getComponentCount()-1) {
						acDropdownBox.getSelectionModel().setSelectedIndex(0);
					}else{
						acDropdownBox.getSelectionModel().setSelectedIndex(index+1);
					}
				}
				if (key == KeyEvent.VK_UP&&acDropdownBox.isVisible()) {
					if (index-1<0) {
						acDropdownBox.getSelectionModel().setSelectedIndex(acDropdownBox.getComponentCount()-1);
					}else{
						acDropdownBox.getSelectionModel().setSelectedIndex(index-1);
					}
				}
				highlightselection(acDropdownBox.getSelectionModel().getSelectedIndex());
				if (key == KeyEvent.VK_ENTER&&acDropdownBox.isVisible()) {
					if (index > -1) {
						ActionEvent ae = new ActionEvent(acDropdownBox.getComponent(index), ActionEvent.ACTION_PERFORMED, ((JMenuItem)acDropdownBox.getComponent(index)).getActionCommand());
						actionPerformed(ae);
						acDropdownBox.setVisible(false);
					}
				}
				if (key == KeyEvent.VK_ESCAPE&&acDropdownBox.isVisible()) {
					acDropdownBox.setVisible(false);
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		}
		
		AutocompleteWindow acDropdownBox;
		keyHandler keyHandlerInstance;
		
		userinput acInvoker;
		Future<TreeMap<String, String>> futureACList;
		TreeMap<String,String> ACList;
		
		Autocomplete (Future<TreeMap<String,String>> futureAcKeywordList) {
			futureACList = futureAcKeywordList;
			keyHandlerInstance = new keyHandler();
			acDropdownBox = new AutocompleteWindow();
			acDropdownBox.addFocusListener(keyHandlerInstance);
			acDropdownBox.setVisible(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String hpo = e.getActionCommand();
			((JTextArea)acDropdownBox.getInvoker()).setText(hpo);
		}
		
		public void add (userinput input) {
			input.getDocument().addDocumentListener(this);
			input.getInputBox().addFocusListener(keyHandlerInstance);
			input.getInputBox().addKeyListener(keyHandlerInstance);
		}
		
		// changedupdate is not used on plainDocuments, as used by
		// JTextArea
		@Override
		public void changedUpdate(DocumentEvent e) {}
		
		@Override
		public void insertUpdate (DocumentEvent e) {
			int length;
			String s;
			String hpo;
			Document invokerDocument;
			JMenuItem acListItem;
			int num = 0;
			long timeOut = System.currentTimeMillis()+5000;
			
			invokerDocument = acInvoker.getDocument();
			acInvoker.getInputBox().setComponentPopupMenu(acDropdownBox);
			acDropdownBox.removeAll();
			acDropdownBox.setInvoker(acInvoker.getInputBox());
			length = invokerDocument.getLength();
			if (length > 2) {
				/* Checks if ACList is not null, the DocumentEvent is not
				 * null, or whether the future ACList is done.
				 * If the ACList is not null, the data is already available
				 * for the dropdown menu.
				 * 
				 * If the DocumentEvent is null, this event was fired
				 * manually. This occurs when the dropdown is visible when
				 * the futureAC is about to be ready.
				 * 
				 * If futureACList is done, we can populate ACList.
				 */
				if (ACList != null || e == null || futureACList.isDone()) {
					/* If ACList == null it still needs to be gotten from
					 * the future object
					 */
					if (ACList == null) {
						/* we will wait for ~5 seconds to get the ACList
						 * when this event fires and the future object
						 * is not done. This should only be relevant
						 * when the event is forced to fire just before
						 * the future object is done.
						 */
						while (!futureACList.isDone()) {
							if (timeOut < System.currentTimeMillis()) {
								acDropdownBox.setVisible(false);
								return;
							}
						}
						try {
							// Retrieve the actual ACList
							ACList = futureACList.get();
							//Collections.sort
						}catch (Exception exception) {
							/* Something will have gone horribly wrong
							 * if this exception is ever reached.
							 */
							exception.printStackTrace();
						}
					}
					s = acInvoker.getInputText();
					if (acInvoker.getInputType() == 0) {
						for (String search : ACList.keySet()) {
							if (search.toLowerCase().contains(s.toLowerCase())&&num<10) {
								hpo = ACList.get(search);
								acListItem = new JMenuItem(search+" ("+hpo+")");
								acListItem.setComponentPopupMenu(acDropdownBox);
								acListItem.addActionListener(this);
								acListItem.addKeyListener(keyHandlerInstance);
								acListItem.setActionCommand(hpo);
								acListItem.setEnabled(true);
								acListItem.setOpaque(true);
								acDropdownBox.add(acListItem);
								num++;
							}
						}
						acDropdownBox.show(num);
						acInvoker.getInputBox().requestFocusInWindow();
					}
				}else{
					acDropdownBox.add(new JMenuItem("Please wait..."));
					acDropdownBox.show(3);
					acInvoker.getInputBox().requestFocusInWindow();
				}
			}else{
				acDropdownBox.setVisible(false);
			}
		}

		@Override
		public void removeUpdate (DocumentEvent e) {
			if (e.getLength() == e.getDocument().getLength()) {
				return;
			}else{
				insertUpdate(e);
			}
		}

		public boolean isVisible() {
			return acDropdownBox.isVisible();
		}
	}
	
	class HPOFile {
		private File hpoFile;
		private File associationFile;
		private File directory = new File(".\\HPO\\");
		private int downloaded = 0;
		public final int STATE_FAIL = -1;
		public final int STATE_INIT = 0;
		public final int STATE_DOWNLOAD_HPO = 1;
		public final int STATE_DOWNLOAD_ASSOC = 2;
		public final int STATE_READY = 3;
		private int state;
		
		
		HPOFile() {
			state = STATE_INIT;
		}
		
		public void LoadFiles() {
			try {
				File[] oldfiles;
				String json;
				String buffer;
				URL jsonurl;
				URL fileurl;
				BufferedReader in;
				InputStream istream;
				FileWriter out;
				/* 	Since we will be searching for the timestamp in filenames and text,
				we will not be using a Long or Date variable. */
				String timestamp;
				long start, stop, time;
				
				start = System.currentTimeMillis();
				
				// This URL points to the JSON file used to retrieve the timestamp
				jsonurl = new URL("http://compbio.charite.de/"
						+ "hudson/job/hpo/lastStableBuild/api/json");
				
				// convertStreamToString loads the JSON in a string.
				// the JSON file is ~1100 characters long.
				json = convertStreamToString(jsonurl.openStream());
				int tsindex = json.indexOf("timestamp\":")+"timestamp\":".length();
				timestamp = json.substring(tsindex,  json.indexOf(",\"url"));
				
				// check if HPO folder exists
				if (!directory.exists()) {
					// If not, Try to create the HPO directory in the applets' folder
					if (!directory.mkdir()) {
						// show error to user in case something goes wrong. Should not happen.
						new Error("Could not make HPO files directory.\n"
								+ "Try launching the application as administrator.", 
								"IO Error",
								JFrame.EXIT_ON_CLOSE);
						return;
					}
				}
				hpoFile = new File(".\\HPO\\override.obo");
				
				// check if override HPO file exists
				if (!hpoFile.exists()) {
					hpoFile = new File(".\\HPO\\"+timestamp+".obo");
					// check if HPO file with this timestamp already exists
					if (!hpoFile.exists()) {
						state = STATE_DOWNLOAD_HPO;
						// oldhpo will contain the filenames for all files in the HPO directory 
						oldfiles = directory.listFiles();
						
						for (File file : oldfiles) {
							if (file.getName().endsWith(".obo")) 
								if (!file.getName().startsWith(timestamp))
									file.delete();
						}
						
						// Create the file if it does not exist
						hpoFile.createNewFile();
						
						/* 
						 * From here, the method will download the HPO number database from
						 * the file pointed to by 'hpourl' and put it in 'hpofile'
						 */
						fileurl = new URL("http://compbio.charite.de/hudson/job/"
								+ "hpo/lastStableBuild/artifact/hp/hp.obo");
						out = new FileWriter(hpoFile);
						istream =	fileurl.openConnection().getInputStream(); 
						in = new BufferedReader(new InputStreamReader(istream));
						while ((buffer = in.readLine()) != null) { 
							out.write(buffer+"\n");
							downloaded += buffer.length();
						}
						out.close();
					}
				}
				stop = System.currentTimeMillis();
				time = stop - start;
				System.out.println("(Down)loading the HPO file took "+time+" millis");
				
				start = System.currentTimeMillis();
				
				jsonurl = new URL("http://compbio.charite.de/hudson/"
						+ "job/hpo.annotations.monthly/lastStableBuild/api/json");
				json = convertStreamToString(jsonurl.openStream());
				tsindex = json.indexOf("timestamp\":")+"timestamp\":".length();
				timestamp = json.substring(tsindex, json.indexOf(",\"url"));
				
				associationFile = new File(".\\HPO\\override.assoc");
				
				// check if override association file exists
				if (!associationFile.exists()) {
					// check if association file with this timestamp already exists
					associationFile = new File(".\\HPO\\"+timestamp+".assoc");
					if (!associationFile.exists()) {
						state = STATE_DOWNLOAD_ASSOC;
						/* 
						 * dggui.down contains the number of bytes that have been downloaded
						 * and will be displayed on the applet when it is downloading the files,
						 * to indicate some progress is being made.
						 */
						downloaded = 0;
						
						// oldfiles will contain the filenames for all files in the HPO directory 
						oldfiles = directory.listFiles();
						
						for (File file : oldfiles) {
							if (file.getName().endsWith(".assoc")) 
								if (!file.getName().startsWith(timestamp))
									file.delete();
						}
						
						// Create the file if it does not exist
		
						associationFile.createNewFile();
						fileurl = new URL("http://compbio.charite.de/hudson/job/"
								+ "hpo.annotations.monthly/lastStableBuild/artifact/"
								+ "annotation/ALL_SOURCES_ALL_FREQUENCIES_"
								+ "diseases_to_genes_to_phenotypes.txt");
						out = new FileWriter(associationFile);
						istream = fileurl.openConnection().getInputStream(); 
						in = new BufferedReader(
								new InputStreamReader(istream));
						while ((buffer = in.readLine()) != null) { 
							out.write(buffer+"\n");
							downloaded += buffer.length();
						}
						out.close();
					}
					stop = System.currentTimeMillis();
					time = stop - start;
					System.out.println("(Down)loading association files took "+time+"millis");
				}
				state = STATE_READY;
				return;
			}catch (IOException e){
				state = STATE_FAIL;
				e.printStackTrace();
				new Error(Error.UNDEF_ERROR, Error.UNDEF_ERROR_T,
						WindowConstants.EXIT_ON_CLOSE);
			}
		}
		
		/**
		 * This function returns a string with the contents of an inputstream
		 * 
		 * @param in an inputstream with the text to be copied to a string
		 * @return a string with the complete transcript from the inputstream
		 * @throws IOException
		 */
		public String convertStreamToString(InputStream in) 
		    throws IOException {
		    BufferedInputStream is = new BufferedInputStream(in);
		    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		    int result = is.read();
		    while(result != -1) {
		      byte b = (byte)result;
		      buffer.write(b);
		      result = is.read();
		    }        
		    in.close();
		    return buffer.toString();
		}
		
		public File getAssocFile() {
			return associationFile;
		}
		
		public int getDown() {
			return downloaded;
		}
		
		public File getHPOFile() {
			return hpoFile;
		}
		
		/**
		 * returns the state of the HPOFILES object as an int
		 * @return
		 * <OL start=-1>
		 * <LI>FAIL</LI>
		 * <LI>INIT</LI>
		 * <LI>DOWNLOAD_HPO</LI>
		 * <LI>DOWNLOAD_ASSOC</LI>
		 * <LI>READY</LI>
		 * </OL>
		 * @return
		 */
		public int getState() {
			return state;
		}
		
		public boolean isReady () {
			return state == STATE_READY;
		}
	}
	
	/**
	 * This class handles the autocompletion for the textareas
	 */
	

	/**
	 * The HPOObject subclass contains many methods related to looking up sets of HPONumbers,
	 * and contains the HPONumber collection and the HPONumbers class itself.
	 */
	static class HPOObject {
		class Browser implements KeyListener, 
		MouseListener, ActionListener {
			/**
			 * HPOTree is an extended JTree that fixes a bug where 
			 * expanding a large number of nodes takes a long time
			 * to process
			 */
			class HPOTree extends JTree {
				private static final long serialVersionUID = 1L;

		        Stack expandedStack = new Stack();
		        Hashtable expandedState = new Hashtable();
		        private static final int TEMP_STACK_SIZE = 11;

		        
				/**
				 * @param node the root node
				 */
				HPOTree(DefaultMutableTreeNode node) {
					super(node);
					
				}

				@Override
				protected void setExpandedState(TreePath path, boolean state) {
			        if(path != null) {
			            // Make sure all parents of path are expanded.
			            Stack stack;
			            TreePath parentPath = path.getParentPath();

			            if (expandedStack.size() == 0) {
			                stack = new Stack();
			            }
			            else {
			                stack = (Stack)expandedStack.pop();
			            }

			            try {
			                while(parentPath != null) {
			                    if(isExpanded(parentPath)) {
			                        parentPath = null;
			                    }
			                    else {
			                        stack.push(parentPath);
			                        parentPath = parentPath.getParentPath();
			                    }
			                }
			                for(int counter = stack.size() - 1; counter >= 0; counter--) {
			                    parentPath = (TreePath)stack.pop();
			                    if(!isExpanded(parentPath)) {
			                        try {
			                            fireTreeWillExpand(parentPath);
			                        } catch (ExpandVetoException eve) {
			                            // Expand vetoed!
			                            return;
			                        }
			                        expandedState.put(parentPath, Boolean.TRUE);
			                        fireTreeExpanded(parentPath);
			                        if (accessibleContext != null) {
			                            ((AccessibleJTree)accessibleContext).
			                                              fireVisibleDataPropertyChange();
			                        }
			                    }
			                }
			            }
			            finally {
			                if (expandedStack.size() < TEMP_STACK_SIZE) {
			                    stack.removeAllElements();
			                    expandedStack.push(stack);
			                }
			            }
			            if(!state) {
			                // collapse last path.
			                Object          cValue = expandedState.get(path);

			                if(cValue != null && ((Boolean)cValue).booleanValue()) {
			                    try {
			                        fireTreeWillCollapse(path);
			                    }
			                    catch (ExpandVetoException eve) {
			                        return;
			                    }
			                    expandedState.put(path, Boolean.FALSE);
			                    fireTreeCollapsed(path);
			                    if (removeDescendantSelectedPaths(path, false) &&
			                        !isPathSelected(path)) {
			                        // A descendant was selected, select the parent.
			                        addSelectionPath(path);
			                    }
			                    if (accessibleContext != null) {
			                        ((AccessibleJTree)accessibleContext).
			                                    fireVisibleDataPropertyChange();
			                    }
			                }
			            }
			            else {
			                // Expand last path.
			                Object          cValue = expandedState.get(path);

			                if(cValue == null || !((Boolean)cValue).booleanValue()) {
			                    try {
			                        fireTreeWillExpand(path);
			                    }
			                    catch (ExpandVetoException eve) {
			                        return;
			                    }
			                    expandedState.put(path, Boolean.TRUE);
			                    fireTreeExpanded(path);
			                    if (accessibleContext != null) {
			                        ((AccessibleJTree)accessibleContext).
			                                          fireVisibleDataPropertyChange();
			                    }
			                }
			            }
			        }
			    }
				
				@Override
				public void expandPath(TreePath path) {
					 // Only expand if not leaf!
					TreeModel  model = getModel();

					if(path != null && model != null &&
						!model.isLeaf(path.getLastPathComponent())
						&& !isExpanded(path)) {
						setExpandedState(path, true);
					}
				}
				
				/**
				 * Collapses all (opened) nodes.
				 */
				public void collapseAll() {
					System.out.println("coll");
					for (int i = this.getRowCount()-1; i > 0; i--) {
						this.collapseRow(i);
					}
				}
				
				public void expandAll(JTree tree, boolean expand) {
			        TreeNode root = (TreeNode)tree.getModel().getRoot();
			        if (root!=null) {
			            // Traverse tree from root
			            expandAll(tree, new TreePath(root), expand);
			        }
			    }

			    /**
			     * @return Whether an expandPath was called for the last node in the parent path
			     */
			    private boolean expandAll(JTree tree, TreePath parent, boolean expand) {
	            	TreeUI ui = this.getUI();
	            	this.setUI(null);
			        // Traverse children
			        TreeNode node = (TreeNode)parent.getLastPathComponent();
			        if (node.getChildCount() > 0) {
			            boolean childExpandCalled = false;
			            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
			                TreeNode n = (TreeNode)e.nextElement();
			                TreePath path = parent.pathByAddingChild(n);
			                childExpandCalled = expandAll(tree, path, expand) || childExpandCalled; // the OR order is important here, don't let childExpand first. func calls will be optimized out !
			            }

			            if (!childExpandCalled) { // only if one of the children hasn't called already expand
			                // Expansion or collapse must be done bottom-up, BUT only for non-leaf nodes
			                if (expand) {
			                    tree.expandPath(parent);
			                } else {
			                    tree.collapsePath(parent);
			                }
			            }
		                this.setUI(ui);
			            return true;
			        } else {
			            return false;
			        }
			    }
			}
			
			private DefaultMutableTreeNode rootNode = 
					new DefaultMutableTreeNode("Human Phenotype Ontology");
			private JFrame browserWindow;
			private JPanel browserContentPanel;
			private HPOTree hpoBrowserTree;
			private JPopupMenu browserMenu;
			final private JScrollPane treeScrollPane;
			private JPanel browserControlPanel;
			private JLabel searchlabel;
			private JTextField searchField;
			private JButton searchButton;
			private JButton listButton;
			private JMenuItem treeListButton;
			private JButton addButton;
			private int state;
			public final static int STATE_INIT = 0;
			public final static int STATE_READY = 1;
			
			Browser (final HPOObject hpodata) {
				
				state = STATE_INIT;
				browserWindow = new JFrame("HPO Browser");
				browserControlPanel = new JPanel();
				browserContentPanel = new JPanel();
				treeScrollPane = new JScrollPane();
				searchlabel = new JLabel("Search:");
				searchField = new JTextField();
				searchButton = new JButton("Find");
				listButton = new JButton("List genes");
				addButton = new JButton("Add to input");
				final JLabel waitlabel = new JLabel("Please wait until the HPO database"
						+ "has finished loading...");
				hpoBrowserTree = new HPOTree(rootNode);
				final GridBagConstraints c = new GridBagConstraints();
				
				browserWindow.setPreferredSize(new Dimension(700,600));
				browserWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				browserWindow.setLayout(new GridBagLayout());
				
				searchField.addKeyListener(this);
				browserControlPanel.setLayout(new FlowLayout());
				browserControlPanel.add(searchlabel);
				searchField.setPreferredSize(new Dimension(80, 20));
				browserControlPanel.add(searchField);
				searchButton.addActionListener(this);
				searchButton.addKeyListener(this);
				searchButton.setActionCommand("search");
				browserControlPanel.add(searchButton);
				listButton.addActionListener(this);
				listButton.setActionCommand("list");
				browserControlPanel.add(listButton);
				addButton.setEnabled(false);
				browserControlPanel.add(addButton);
				browserControlPanel.setMaximumSize(new Dimension(browserWindow.getWidth(), 30));
				
				browserContentPanel.setLayout(new GridBagLayout());
				c.fill = GridBagConstraints.BOTH;
				c.weightx = 1; c.weighty = 1;
				browserContentPanel.setMaximumSize(null);
				
				c.fill = GridBagConstraints.BOTH;
				c.gridy = 0; c.gridx = 0; c.weightx = 1; c.weighty = 0.05;
				browserWindow.add(browserControlPanel, c);
				c.gridy = 1; c.gridx = 0; c.weightx = 1; c.weighty = 0.95;
				browserWindow.add(browserContentPanel, c);
				
				treeScrollPane.add(waitlabel);
				browserContentPanel.add(treeScrollPane, c);
				
				hpoBrowserTree.addMouseListener(this);
				hpoBrowserTree.setExpandsSelectedPaths(true);
				hpoBrowserTree.setLargeModel(true);
			
				DeltaGene.THREADPOOL.submit(new Runnable() {
					@Override
					public void run() {
						while (!hpodata.isReady()) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								new Error(Error.CRIT_ERROR, Error.CRIT_ERROR_T,
								WindowConstants.EXIT_ON_CLOSE, e);
								e.printStackTrace();
							}
						}
						treeScrollPane.remove(waitlabel);
						treeScrollPane.setViewportView(hpoBrowserTree);
						rootNode.add(hpodata.getHPOHeirarchy("HP:0000001"));
						state = STATE_READY;
					}
				});
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				Long start, stop, time;
				if (e.getActionCommand().equals("search")&&searchField.getText().length()>0) {
					start = System.currentTimeMillis();
					hpoBrowserTree.clearSelection();
					hpoBrowserTree.expandAll(hpoBrowserTree, false);
					ArrayList<TreePath> AL = find(rootNode, searchField.getText());
					stop = System.currentTimeMillis();
					time = stop - start;
					System.out.println("find: "+time+" millis");
					for (TreePath path : AL) {
						hpoBrowserTree.makeVisible(path);
					}
					stop = System.currentTimeMillis();
					time = stop - start - time;
					System.out.println("makevisible loop: "+time+" millis");
					hpoBrowserTree.setSelectionPaths(AL.toArray(new TreePath[AL.size()]));
					stop = System.currentTimeMillis();
					time = stop - start - time;
					System.out.println("setselection: "+time+" millis");
					//TreePath[] paths = new TreePath[AL.size()];
					//paths = AL.toArray(paths);
				}if (e.getActionCommand().equals("list")) {
					TreePath[] paths = hpoBrowserTree.getSelectionPaths();
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
			
			private void contextMenu(Point p) {
				browserMenu = new JPopupMenu();
				browserMenu.setPreferredSize(new Dimension(150,30));
				treeListButton = new JMenuItem("List selected");
				treeListButton.addActionListener(this);
				treeListButton.setActionCommand("list");
				browserMenu.add(treeListButton);
				browserMenu.setLocation(p);
				browserMenu.setInvoker(browserWindow);
				browserMenu.pack();
				browserMenu.revalidate();
				browserMenu.repaint();
				browserMenu.setVisible(true);
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
			
			@Override
			public void keyPressed(KeyEvent e) {}
			/**
			 * This enables the user to press enter in the search menu,
			 * instead of having to click the search button explicitly
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getComponent().equals(searchField)) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						actionPerformed(new ActionEvent(searchField, ActionEvent.ACTION_PERFORMED, "search"));
					}
				}
			}
		
			@Override
			public void keyTyped(KeyEvent e) {}
		
			/**
			 * displays the right mouse button menu, containing a button
			 * to list the genes
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
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
			public void show(String hpo, HPOObject hpoData) {
				addButton.setEnabled(false);
				show(hpo, hpoData, null);
			}
		
			public void show(final String hpo, final HPOObject hpoData, final userinput input) {
				browserWindow.pack();
				browserWindow.setLocationRelativeTo(null);
				browserWindow.setVisible(true);
				browserWindow.revalidate();
				browserWindow.repaint();
				browserWindow.pack();
				DeltaGene.THREADPOOL.submit(new Runnable() {
					@Override
					public void run() {
						while (!isReady()) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (input != null) {
							addButton.setEnabled(true);
							addButton.setActionCommand("add:"+input.getID());
						}
						//rootNode.removeAllChildren();
						//DefaultMutableTreeNode search = hpoData.getReverseHPOHeirarchy(new DefaultMutableTreeNode(hpo));
						//hpoBrowserTree.expandAll();
						hpoBrowserTree.clearSelection();
						hpoBrowserTree.collapseAll();
						for (TreePath path : find(rootNode, hpo)) {
							hpoBrowserTree.makeVisible(path);
							hpoBrowserTree.addSelectionPath(path);
						}
					}
				});
			}
			
			boolean isReady() {
				return state == STATE_READY;
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
			private HashSet<String> geneSet = new HashSet<String>();
			private ArrayList<HPONumber> children = new ArrayList<HPONumber>();
			private ArrayList<HPONumber> parents = new ArrayList<HPONumber>();
			
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
				if (geneSet.contains(gene)) {
					return false;
				}
				geneSet.add(gene);
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
					for (String gene : geneSet) {
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
				if (geneSet.size() > 0) {
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
		}
		
		/*
		 * The data HashMap contains all instances of HPONumbers objects.
		 * The key to each HPONumber is it's hpo id (HP:#######)
		 */
		private static HPOFile files;
		private static HashMap<String, HPONumber> data = 
				new HashMap<String, HPONumber>();
		public Browser browser;
		final static int STATE_INIT = 0;
		final static int STATE_WAIT = 1;
		final static int STATE_LOAD_HPO = 2;
		final static int STATE_LOAD_ASSOC = 3;
		final static int STATE_READY = 4;
		private static int state;
		
		HPOObject(HPOFile hpofile) {
			state = STATE_INIT;
			files = hpofile;
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

		public void build(Input inputinstance) {
			try {
				if (files.state != HPOFILES.STATE_READY) {
					state = STATE_WAIT;
					wait();
				}
				state = STATE_LOAD_HPO;
				populateHPOObject(files.getHPOFile());
				browser = new Browser(this);
				state = STATE_LOAD_ASSOC;
				populateHPOGenes(files.getAssocFile());
				state = STATE_READY;
				inputinstance.notify();
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
		public Browser getBrowser() {
			return browser;
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
					/*hpo = line.substring(line.indexOf("HP:"), line.indexOf("\t",
							line.indexOf("HP:")));
					gene = line.substring(line.indexOf("\t")+1, line.indexOf("\t",
							line.indexOf("\t")+1));*/
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
	
	public void reloadFiles() {
		data.clear();
		populateHPOObject(files.getHPOFile());
		populateHPOGenes(files.getAssocFile());
	}

	public int getState() {
		return state;
	}
	
	public boolean isReady() {
		return state == STATE_READY;
	}
}
	
	/**
	 * The result class handles displaying, generating and exporting
	 * the results
	 */
	class result implements ActionListener {
		JFrame reswindow;
		JScrollPane respanelsp;
		JMenuBar menubar;
		JMenu filemenu;
		JMenuItem export; 
		Container controlcont;
		Container rescont;
		JTable restable;
		String[] headers; // contains the headers for the JTable
		String[][] results; // contains the data for the JTable
		int[][] stats;
		
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
								new Error("Could not create export file.", "Export error", WindowConstants.DISPOSE_ON_CLOSE);
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
						new Error("An error occurred when choosing an export file", "Export error", WindowConstants.DISPOSE_ON_CLOSE);
						return;
					}
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void generate(ArrayList<userinput> in) {
			headers = null;
			results = null;
			
			getHeaders(in);
			generateResults(in);
			if (results == null) {
				new Error(getInputError(), "Result error", WindowConstants.DISPOSE_ON_CLOSE);
				return;
			}
			showResults(results, headers);
		}
		
		/**
		 *  
		 * @param hpo
		 */
		public void generate(String hpo) {
			headers = null;
			results = null;
			headers = hpo.split("(,)");
			for (int i = 0; i < headers.length; i++) {
				headers[i] = headers[i]+" - "+HPODATA.getPhenotypeFromHPO(headers[i]);
			}
			generateResults(hpo);
			if (results == null) {
				new Error(getInputError(), "Result error", WindowConstants.DISPOSE_ON_CLOSE);
				return;
			}
			showResults(results, headers);
		}
		
		/**
		 * generateResults will compare all genes associated with or input in the inputboxes by the user.
		 * The size of the array that is returned depends on the amount of genes left at the end of the comparison,
		 * and the operator that is used.
		 * @param oA Single-dimensional list of genes from the A input
		 * @param oB Multi-dimensional list of genes from the B input(s)
		 * @param operator Operator to use.
		 * @return a multi-dimensional array where String[x][] is a gene and String[][x] is an input.
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
				if (operator == LIST) {
					setInputError("This phenotype has no associated genes");
				}else{
					setInputError("Input(s) B do not have any associated genes");
				}
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

		/**
		 * This function displays the results passed by results and headers
		 * @param results the results in the form of a two-dimensional string array
		 * @param headers the headers in the form of a string array
		 */
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
			reswindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			reswindow.setPreferredSize(parentWindow.getSize());
			reswindow.setJMenuBar(menubar);
			menubar.add(filemenu);
			filemenu.add(export);
			reswindow.add(respanelsp);
			reswindow.pack();
			reswindow.setLocationRelativeTo(null);
			reswindow.setVisible(true);
		}
	}
	
	/**
	 * The userinput class takes care of the user's input, including
	 * parsing the type of input, 
	 */
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
			inputjsp = new JScrollPane(inputbox,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			inputjsp.setPreferredSize(new Dimension(
					parentContainer.getWidth()-10, DeltaGene.INPUTH));
			inputdoc.addDocumentListener(this); 
			inputbox.setFocusTraversalKeysEnabled(false);
			infobox = new JEditorPane();
			infobox.setContentType("text/html");
			infobox.addHyperlinkListener(this);
			infobox.setBackground(Color.getHSBColor(0, 0, 
					(float)0.9));
			infojsp = new JScrollPane(infobox, 
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			infojsp.setPreferredSize(new Dimension(
					parentContainer.getWidth()-10, DeltaGene.INFOH));
			infobox.setEditable(false);
			parentContainer.add(inputjsp);
			parentContainer.add(infojsp);
			ac.add(this);
			updateInfoBox();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {}
		
		// clears the inputbox of text
		public void clear() {
			inputbox.setText("");
		}

		// gets the document associated with the inputbox
		public Document getDocument() {
			return inputdoc;
		}
		
		int getID() {
			return id;
		}
		
		JEditorPane getInfoBox() {
			return infobox;
		}
		
		/**
		 * returns the infoboxes' JScrollpane. used in removing an input
		 * @return the infoboxes' JScrollPane
		 */
		public JScrollPane getInfoScrollPane() {
			return infojsp;
		}
		
		JTextArea getInputBox() {
			return inputbox;
		}
		
		/**
		 * Returns the location of the inputbox relative
		 * to the screen
		 * @return location of this input's inputbox relative to the scren
		 */
		public Point getInputBoxLocationOnScreen() {
			return inputbox.getLocationOnScreen();
		}
		
		int getInputGroup() {
			return group;
		}
		
		/**
		 * returns the inputscrollpane. used in removing an input
		 * @return the inputboxes' JScrollPane
		 */
		public JScrollPane getInputScrollPane() {
			return inputjsp;
		}
		
		String getInputText () {
			return inputbox.getText();
		}
		
		/**
		 * Returns the type of input in the inputbox.
		 * <p>
		 * <ol start=0>
		 * <li>List of hpo numbers</li>
		 * <li>Single hpo number</li>
		 * <li>Gene symbols</li>
		 * <li>Digits</li>
		 * <li>Mixed input</li>
		 * <li>invalid</li>
		 * </ol>
		 * </p>
		 * @return An int signifying the type of input
		 */
		int getInputType() {
			String in = getInputText();
			/*
			 * The first if checks if HP: occurs at all, in which case 
			 * an HPO number is assumed to be the input. The second if
			 * checks if any other instances of HP: occur after the 
			 * first, which signifies a list of HPO numbers.*/
			if (in.toUpperCase().indexOf("HP:") > -1) {
				if (in.toUpperCase().substring(in.toUpperCase().
						indexOf("HP:")+1).contains("HP:")) { 
					return 1;
				}else{
					return 2;
				}
			}
			// split lines by newline and whitespace
			String[] lines = in.split("[\\n\\s]"); 
			for (String line : lines) {
				// any lowercase characters indicate no genelist or HPO num
				if (line.matches("[a-z]")) { 
					return 0;
					
					/* if the line starts with a capital letter
					 * and has a mixture of capital letters and numbers
					 * thereafter, return list of genes */
				}if (line.matches("[A-Z][A-Z0-9]+")) {
					return 3;
					// if the line contains only digits, return digits
				}if (line.matches("[\\d]+")) {
					return 4;
				}
			}
			// if all else fails, return invalid
			return 0;
		}
		
		// returns the size of this inputs' inputbox
		Dimension getSize() {
			return inputbox.getSize();
		}

		/**
		 * When a link in an infobox is clicked, this will activate an
		 * appropriate action based on it's prefix.
		 * <ul>
		 * <li>genes: List genes in table</li>
		 * <li>tree: Show HPO term in hpo browser</li>
		 * </ul>
		 */
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String desc = e.getDescription();
				if (desc.contains("genes:")) {
					resultobject.generate(desc.substring(6));
				}else if (desc.contains("tree:")) {
					HPODATA.browser.show(desc.substring(5), HPODATA);
				}
			}
		}
		
		/**
		 *  This updates the infobox with information on the user's input 
		 */
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateInfoBox(getInputType());
		}
		/**
		 * This makes a list of genes in this inputbox unique, removing
		 * any duplicate genes.
		 */
		public void makeGenelistUnique() {
			if (getInputType() == 3) {
				StringBuilder sb = new StringBuilder();
				String[] in = getInputText().split("([^A-Z0-9\\-]+)");
				HashSet<String> out = new HashSet<String>();
				for (String gene : in) {
					if (!out.contains(gene)) {
						out.add(gene);
						sb.append(gene);
						sb.append(System.lineSeparator());
					}
				}
				setInputText(sb.toString());
			}
		}
		
		/**
		 * @see insertUpdate
		 */
		@Override
		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e);
		}

		void setInputGroup(int newgroup) {
			group = newgroup;
		}
		
		
		void setInputInfo(String text) {
			infobox.setText(text);
		}
		
		void setInputText(String text) {
			inputbox.setText(text);
		}
		/**
		 * Updates the infobox based upon the type of input in the
		 * inputbox.
		 */
		public void updateInfoBox() {
			updateInfoBox(getInputType());
		}
		
		/**
		 * Updates the infobox based upon a particular type
		 * @param type the type to assume
		 */
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
				String[] hponums = getInputText().toUpperCase()
						.split("([^A-Z0-9:]+)");
				for (String hpo : hponums) {
					pheno = HPODATA.getPhenotypeFromHPO(hpo);
					if (pheno == null) {
						sb.append("<br>"+hpo+" - "
								+ "No associated phenotype.");
					}else{
						/* 
						 * When either of these links in the infobox is
						 * clicked, hyperlinkUpdate takes action based upon
						 * the tree: or genes:  prefix of a HPO number.
						 * tree: lists the HPO numbers in the HPO browser
						 * tree, genes: lists the genes associated with
						 * this HPO number in a table
						 */
						sb.append("<br>"+hpo+" - "+pheno+". "
								+ "<a href=\"genes:"+hpo+"\">List genes</a>"
								+ " | <a href=\"tree:"+hpo
								+"\">Show in browser</a>");
					}
				}
				infobox.setText(sb.toString());
				break;
			case 2:
				String text = getInputText().toUpperCase();
				if (text.length() == 10) {
					if (HPODATA.getPhenotypeFromHPO(text) != null) {
						infobox.setText("Type: Single HPO number:<br>"
					+text+" - "+HPODATA.getPhenotypeFromHPO(text)+
					" (<a href=\"genes:"+text
					+"\">List genes</a> | <a href=\"tree:"
					+text+"\">Show in browser</a>)");
					}else{
						infobox.setText("Type: Single HPO number:<br>"+
					text+" - no associated phenotype.");
					}
				}else{
					infobox.setText("Type: Single HPO number:<br>"
							+ "(Requires seven digits)");
				}
				break;
			case 3:
				infobox.setText("Type: List of genes");
				break;
			case 4:
				sb.append("Type: List of numbers (Parsed as HPO numbers):");
				for (String hpo : HPODATA.parseNumbersAsHPO(
						inputbox.getText())) {
					if (hpo == null) {
						hpo = "Invalid HPO number";
					}
					pheno = HPODATA.getPhenotypeFromHPO(hpo);
					if (pheno != null) {
						sb.append("<br>"+hpo+" - "+pheno
								+" (<a href=\"genes:"+hpo
								+"\">List genes</a> | <a href=\"tree:"
								+hpo+"\">Show in browser</a>)");
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
	}
	
	private static JFrame parentWindow;
	private static Container parentContainer;
	public static HPOObject HPODATA;
	public static HPOFile HPOFILES;
	public static ArrayList<userinput> inputs;
	public static result resultobject;
	Autocomplete ac;
	final static int DEFAULT = 0;
	final static int AND = 1;
	final static int NOT = 2;
	final static int XOR = 3;
	final static int LIST = 4;
	public final static int STATE_INSTANTIATED = 0;
	public final static int STATE_INIT = 1;
	public final static int STATE_BUILDING = 2;
	public final static int STATE_READY = 3;
	private static int OPERATOR = DEFAULT;
	private String error;
	private int state = STATE_INSTANTIATED;
	

	Input() {
		inputs = new ArrayList<userinput>();
		resultobject = new result();
		HPOFILES = new HPOFile();
		HPODATA = new HPOObject(HPOFILES);
	}
	
	void addInput(int count, int assignedgroup) {
		for(int i = 0; i < count; i++) {
			userinput input = new userinput(assignedgroup);
			inputs.add(input);
			resizeInputContainer();
		}
	}

	/**
	 * This function is called when an input is added or removed, and resizes 
	 * the input container accordingly.
	 */
	public void resizeInputContainer() {
		parentContainer.setPreferredSize(
				new Dimension((parentWindow.getContentPane().getWidth()-50),
						(DeltaGene.INPUTH+DeltaGene.INFOH+(DeltaGene.INPUTPAD*2))*getInputCount()));
		parentWindow.pack();
		parentWindow.revalidate();
		parentWindow.repaint();
	}
	
	public void clearInputs() {
		for (userinput input : inputs) {
			input.clear();
		}
	}
	
	public void formatGenes() {
		for (userinput input : inputs) {
			input.makeGenelistUnique();
		}
	}
	
	public HPOObject getData() {
		return HPODATA;
	}
	
	userinput getDocumentInputObject(Document doc) {
		for (userinput input : inputs) {
			if (input.getDocument().equals(doc)) {
				return input;
			}
		}
		return null;
	}
	
	HPOFile getFilesObject() {
		return HPOFILES;
	}
	
	userinput getInputboxObject(JTextArea ta) {
		for (userinput input : inputs) {
			if (input.getInputBox().equals(ta)) {
				return input;
			}
		}
		return null;
	}
	
	int getInputCount() {
		return inputs.size();
	}

	public String getInputError() {
		return error;
	}
	
	String getInputText(int index) {
		return inputs.get(index).getInputText();
	}
	
	int getOperator() {
		return OPERATOR;
	}
	
	public void getResults() {
		resultobject.generate(inputs);
	}
	
	public void initialize(JFrame pwindow, Container pcontainer) {
		state = STATE_INIT;
		parentWindow = pwindow;
		parentContainer = pcontainer;
		
		/* start thread that keeps track of the download progress.
		 * Swing is not thread safe, and i am sure this is a big no-no
		 */
		SwingWorker<Void, Void> updateWorker = new SwingWorker<Void,Void>() {
			public Void doInBackground() {
				try { 
					while (!HPOFILES.isReady()) {
						Thread.sleep(50);
						parentWindow.setTitle("DeltaGene - Downloading HPO/Association files ("+HPOFILES.getDown()+"kB)");
					}
					while (!HPODATA.isReady()) {
						if (HPODATA.getState() == HPOObject.STATE_LOAD_HPO) {
							parentWindow.setTitle("DeltaGene - Building HPO Database...");
						}else if (HPODATA.getState() == HPOObject.STATE_LOAD_ASSOC){
							parentWindow.setTitle("DeltaGene - Loading gene associations...");
						}
						Thread.sleep(50);
					}
				}catch (InterruptedException e) {
					/* May mess up the window title, but should not 
					 * do much else
					 */
					new Error(Error.UNDEF_ERROR, Error.UNDEF_ERROR_T, WindowConstants.DISPOSE_ON_CLOSE);
					e.printStackTrace();
				}
				parentWindow.setTitle("DeltaGene");
				return null;
			}
		};
		updateWorker.execute();
		
		/* instantialte the autocompletion class with a future treemap
		 * object. will be ready once the files have been downloaded 
		 * and the HPO database has been built.
		 */
		ac = new Autocomplete(DeltaGene.THREADPOOL.submit(new Callable<TreeMap<String,String>>() {
			public TreeMap<String,String> call () {
				try {
					// While the future object is not yet available, wait
					while (HPODATA.getState() < HPOObject.STATE_LOAD_ASSOC) {
						Thread.sleep(100);
					}
					/* When done loading, check if user has prompted the
					 * autocomplete window. if so, will call an insertUpdate()
					 * event on the input the user has started typing in with
					 * a null event. see input.Autocomplete.insertUpdate()
					 */
					if (ac.isVisible()) {
						SwingWorker<Void, Void> refreshWorker = 
								new SwingWorker<Void,Void>() {
							public Void doInBackground() {
								ac.insertUpdate(null);
								return null;
							}
						};
						/* call this on a seperate thread so the gui doesn't
						 * block
						 */
						refreshWorker.execute();
					}
					// return the promised object once it's ready
					return HPODATA.getACList();
				}catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
			}
		}));
		state = STATE_BUILDING;
		HPOFILES.LoadFiles();
		HPODATA.build(this);
		state = STATE_READY;
	}
	
	void removeInput() {
		int last = inputs.size()-1;
		if (last > 1) {
			parentContainer.remove(inputs.get(last).getInputBox());
			parentContainer.remove(inputs.get(last).getInputScrollPane());
			parentContainer.remove(inputs.get(last).getInfoBox());
			parentContainer.remove(inputs.get(last).getInfoScrollPane());
			inputs.remove(inputs.size()-1);
			resizeInputContainer();
		}
	}

	public void setInputError(String errmsg) {
		error = errmsg;
	}

	public void setOperator (int op){
		OPERATOR = op;
	}
	
	public boolean isReady() {
		return state == STATE_READY;
	}
	
	public int getState() {
		return state;
	}
}
