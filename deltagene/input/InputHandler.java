/*
 * InputHandler class for DeltaGene
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

package deltagene.input;

import deltagene.input.data.HPODataHandler;
import deltagene.io.HPOFileHandler;
import deltagene.main.DeltaGene;
import deltagene.utils.Error;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.swing.event.TreeModelListener;
import javax.swing.plaf.TreeUI;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * This class handles input from the user, generating headers and generating 
 * results.
 * @author ArjanDraisma
 * 
 */
public class InputHandler {
	private static Container contentPane;
	public HPODataHandler HPOData;
	public HPOFileHandler HPOFiles;
	public static ArrayList<UserInput> inputs;
	//public static result resultobject;
	Autocomplete ac;
	public final static int DEFAULT = 0;
	public final static int AND = 1;
	public final static int NOT = 2;
	public final static int XOR = 3;
	public final static int LIST = 4;
	public final static int GENE = 5;
	public final static int STATE_INSTANTIATED = 0;
	public final static int STATE_INIT = 1;
	public final static int STATE_BUILDING = 2;
	public final static int STATE_READY = 3;
	private static int OPERATOR = DEFAULT;
	private String error;
	private int state = STATE_INSTANTIATED;
	

	public InputHandler(String aArg, String bArg, String e, String fh, String fa, JPanel contentPane, boolean verbose) {
		if (contentPane != null)
			this.contentPane = contentPane;
		inputs = new ArrayList<UserInput>();
	//	resultobject = new result();
		HPOFiles = new HPOFileHandler();
		HPOData = new HPODataHandler(HPOFiles);
	}
	
	// this constructor is called by the gui
	public InputHandler(JPanel contentPane)
	{
		this(null, null, null, null, null, contentPane, false);
	}
	
	public void addInput(int count, int assignedgroup) {
		for(int i = 0; i < count; i++) {
			UserInput input = new UserInput(assignedgroup, this, HPOData);
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
		for (UserInput input : inputs) {
			input.clear();
		}
	}
	
	public void formatGenes() {
		for (UserInput input : inputs) {
			input.makeGenelistUnique();
		}
	}
	
	public HPODataHandler getData() {
		return HPOData;
	}
	
	UserInput getDocumentInputObject(Document doc) {
		for (UserInput input : inputs) {
			if (input.getDocument().equals(doc)) {
				return input;
			}
		}
		return null;
	}
	
	HPOFileHandler getFileHandler() {
		return HPOFiles;
	}
	
	UserInput getInputboxObject(JTextArea ta) {
		for (UserInput input : inputs) {
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
		switch (OPERATOR) {
			case DEFAULT:
				//TODO
		}
			//resultobject.generate(inputs);
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
					while (!HPOFiles.isReady()) {
						Thread.sleep(50);
						parentWindow.setTitle("DeltaGene - Downloading HPO/Association files ("+HPOFiles.getDown()+"kB)");
					}
					while (!HPOData.isReady()) {
						if (HPOData.getState() == HPOData.STATE_LOAD_HPO) {
							parentWindow.setTitle("DeltaGene - Building HPO Database...");
						}else if (HPOData.getState() == HPOData.STATE_LOAD_ASSOC){
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
					while (HPOData.getState() < HPOData.STATE_LOAD_ASSOC) {
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
					return HPOData.getACList();
				}catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
			}
		}));
		state = STATE_BUILDING;
		HPOFiles.LoadFiles();
		HPOData.build(this);
		state = STATE_READY;
	}
	
	public void removeInput() {
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
