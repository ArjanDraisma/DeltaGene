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

import deltagene.gui.MainGui;
import deltagene.input.data.HPODataHandler;
import deltagene.io.HPOFileHandler;
import deltagene.main.DeltaGene;
import deltagene.output.AbstractResult;
import deltagene.output.ComparisonResult;
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
 * @author ArjanDraisma
 * 
 */
public class InputHandler {
	public HPODataHandler hpoDataHandler;
	public HPOFileHandler hpoFileHandler;
	public static ArrayList<UserInput> inputs;
	public MainGui gui;
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
	

	public InputHandler(String aArg, String bArg, String e, String fh, String fa, MainGui gui, boolean verbose) {
		if (gui != null)
		{
			this.gui = gui;
		}
		inputs = new ArrayList<UserInput>();
		hpoFileHandler = new HPOFileHandler();
		hpoDataHandler = new HPODataHandler(hpoFileHandler);
		DeltaGene.THREADPOOL.submit(new Runnable() {
			@Override
			public void run() {
				while (hpoFileHandler.getState() != HPOFileHandler.STATE_READY) {
					gui.updateTitle("Downloading HPO/association files: "+Integer.toString(hpoFileHandler.getDown())+"Kb");
				}
				while (hpoDataHandler.getState() != HPODataHandler.STATE_READY) {
					gui.updateTitle("Building HPO/association data");
				}
				gui.updateTitle(null);
			}
		});
		DeltaGene.THREADPOOL.submit(new Runnable() {
			@Override
			public void run() {
				hpoFileHandler.LoadFiles();
				hpoDataHandler.build();
			}
		});
	}
	
	UserInput getUserInputFromComponent (Object object) {
		for (UserInput input : inputs) {
			if (input.getInputBox() == object) {
				return input;
			}
		}
		return null;
	}
	
	// this constructor is called by the gui
	public InputHandler(MainGui gui)
	{
		this(null, null, null, null, null, gui, false);
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
	
	public HPODataHandler getDataHandler() {
		return hpoDataHandler;
	}
	
	public HPOFileHandler getFileHandler() {
		return hpoFileHandler;
	}
	
	UserInput getDocumentInputObject(Document doc) {
		for (UserInput input : inputs) {
			if (input.getDocument().equals(doc)) {
				return input;
			}
		}
		return null;
	}
	
	UserInput getInputboxObject(JTextArea ta) {
		for (UserInput input : inputs) {
			if (input.getInputBox().equals(ta)) {
				return input;
			}
		}
		return null;
	}
	
	public int getInputCount() {
		return inputs.size();
	}

	public String getInputError() {
		return error;
	}
	
	String getInputText(int index) {
		return inputs.get(index).getInputText();
	}
	
	public int getOperator() {
		return OPERATOR;
	}
	
	public void getResults() {
		new ComparisonResult(hpoDataHandler, this);
	}
	
	public void initialize(MainGui gui) {
		state = STATE_INIT;
		
		/* instantiate the autocompletion class with a future treemap
		 * object. will be ready once the files have been downloaded 
		 * and the HPO database has been built.
		 */
		ac = new Autocomplete(DeltaGene.THREADPOOL.submit(new Callable<TreeMap<String,String>>() {
			public TreeMap<String,String> call () {
				try {
					// While the future object is not yet available, wait
					while (hpoDataHandler.getState() < HPODataHandler.STATE_LOAD_ASSOC) {
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
					return hpoDataHandler.getACList();
				}catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
			}
		}), this);
		state = STATE_BUILDING;
		state = STATE_READY;
	}
	
	public void removeInput(UserInput input) {
		inputs.remove(input);
		ac.remove(input);
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

	public void addInput(int group) {
		UserInput input = new UserInput(group, this);
		inputs.add(input);
		gui.getContentPanel().add(input);
		ac.add(input);
		gui.revalidate();
		gui.repaint();
	}

	public void removeLastInput() {
		if (inputs.size() == 2)
			return;
		UserInput input = inputs.get(inputs.size()-1);
		inputs.remove(input);
		gui.getContentPanel().remove(input);
		ac.remove(input);
		gui.revalidate();
		gui.repaint();
	}
}
