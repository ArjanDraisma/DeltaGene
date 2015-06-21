package deltagene.output;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;

import com.sun.xml.internal.ws.api.server.Container;

import deltagene.gui.AbstractWindow;
import deltagene.input.InputHandler;
import deltagene.input.UserInput;
import deltagene.input.data.HPODataHandler;
import deltagene.utils.Error;


/**
 * The result class handles displaying, generating and exporting
 * the results
 */
public class AbstractResult extends AbstractWindow implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HPODataHandler hpoDataHandler;
	private InputHandler inputHandler;

	JFrame reswindow;

	JScrollPane respanelsp;
	JMenuBar menubar;
	JMenu filemenu;
	JMenuItem export;
	JTable restable;
	String[] headers; // contains the headers for the JTable
	String[][] results; // contains the data for the JTable
	HashMap<String, Integer> stats;
	
	public AbstractResult(HPODataHandler hpoDataHandler, InputHandler inputHandler) {
		super("Results", null, 800, 600, false, true, WindowConstants.DISPOSE_ON_CLOSE);
		this.hpoDataHandler = hpoDataHandler;
		this.inputHandler = inputHandler;
	}
	
	private void generate() {
		if (inputHandler.getOperator() != InputHandler.LIST) 
			generateComparison();
		else
			generateList();
	}

	private void generateList() {
		
	}
	
	private void generateComparison() {
		
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
							new Error("Could not create export file.", "Export error", WindowConstants.DISPOSE_ON_CLOSE);
							return;
						}
					}
					FileWriter outfile = new FileWriter(file);
					for (int i = 0; i < headers.length; i++) {
						outfile.append(headers[i]+" - "+hpoDataHandler.getPhenotypeFromHPO(headers[i]));
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
	
	public void generate(ArrayList<UserInput> userInput) {
		headers = null;
		results = null;
		
		getHeaders(userInput);
		generateResults(userInput);
		if (results == null) {
			// TODO new Error(getInputError(), "Result error", WindowConstants.DISPOSE_ON_CLOSE);
			return;
		}
		//showResults(results, headers);
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
			headers[i] = headers[i]+" - "+hpoDataHandler.getPhenotypeFromHPO(headers[i]);
		}
		generateResults(hpo);
		if (results == null) {
			// TODO new Error(getInputError(), "Result error", WindowConstants.DISPOSE_ON_CLOSE);
			return;
		}
		//showResults(results, headers);
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

		if (operator != InputHandler.LIST) {
			if (oA.size() == 0) {
				// TODO setInputError("Input A has no associated genes.");
				return;
			}
			Collections.sort(oA);
		}
		
		for (ArrayList<String> B : oB) {
			if (B.size() > oBSize) {
				oBSize = B.size();
			}
			if (operator == InputHandler.LIST) {
				Collections.sort(B);
			}
		}
		System.out.println(oBSize);
		if (oBSize == 0) {
			if (operator == InputHandler.LIST) {
				// TODO setInputError("This phenotype has no associated genes");
			}else{
				// TODO setInputError("Input(s) B do not have any associated genes");
			}
			return;
		}
		
		switch (operator) {
		case InputHandler.DEFAULT:
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
		case InputHandler.AND:
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
				/*setInputError("The command \"A AND B\" returns an empty result!\n"
							+ "This means that there are no genes that occur in both A AND B.\n"
							+ "Use the 'Default' setting to confirm this.");*/
				results = null;
			}else{
				outArray = new String[oA.size()][1];
				for (int i = 0; i < oA.size(); i++) {
					outArray[i][0] = oA.get(i);
				}
				results =  outArray;
			}
		break;
		case InputHandler.NOT:
			for (int i = 0; i < oB.size(); i++) {
				for (int o = 0; o < oB.get(i).size(); o++) {
					if (oA.contains(oB.get(i).get(o))) {
						oA.remove(oB.get(i).get(o));
					}
				}
			}
			if (oA.size() == 0) {
				/*setInputError("The command \"A NOT B\" returns an empty result!\n"
									+ "This means that there are no unique genes in A.\n"
									+ "Use the 'Default' setting to confirm this.");*/
				results = null;
			}else{
				outArray = new String[oA.size()][1];
				for (int i = 0; i < oA.size(); i++) {
					outArray[i][0] = oA.get(i);
				}
				results = outArray;
			}
		break;
		case InputHandler.XOR:
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
				/*setInputError("The command \"A XOR B\" returns an empty result!\n"
							+ "This means that there are no genes unique to A or B.");*/
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
		case InputHandler.LIST:
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
	public void generateResults(ArrayList<UserInput> inputs) {
		ArrayList<String> oA = new ArrayList<String>();
		ArrayList<ArrayList<String>> oB = new ArrayList<ArrayList<String>>();
		String txt;
		int type;
		
		for (UserInput input : inputs) {
			if (input.getInputGroup() == 0) {
				txt = input.getInputText();
				type = input.getInputType();
				if (type == 0) {
					//setInputError("Input A is invalid!");
					return;
				}else if (type == 1||type == 2) {
					
					hpoDataHandler.findHPOGenes(txt, oA);
				}else if (type == 3) {
					hpoDataHandler.parseUniqueGene(txt, oA);
				}else if (type == 4) {
					hpoDataHandler.findHPOGenes(hpoDataHandler.parseNumbersAsHPO(txt), oA, true);
				}
			}else{
				oB.add(new ArrayList<String>());
				txt = input.getInputText();
				type = input.getInputType();
				if (type == 0) {
					//setInputError("One of the B inputs is invalid!");
					return;
				}else if (type == 1||type == 2) {
					hpoDataHandler.findHPOGenes(txt, oB.get(oB.size()-1));
				}else if (type == 3) {
					hpoDataHandler.parseUniqueGene(txt, oB.get(oB.size()-1));
				}else if (type == 4) {
					hpoDataHandler.findHPOGenes(hpoDataHandler.parseNumbersAsHPO(txt), oB.get(oB.size()-1), true);
				}
			}
		}
		generateResults(oA, oB, inputHandler.getOperator());
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
			hpoDataHandler.findHPOGenes(hpon, oB.get(oB.size()-1),true);
		}
		generateResults(null, oB, InputHandler.LIST);
	}
	
	public void getHeaders(ArrayList<UserInput> inputs) {
		getHeaders(inputs, inputHandler.getOperator());
	}
	
	public void getHeaders(ArrayList<UserInput> inputs, int operator) {
		ArrayList<String> outAL = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		if (operator == InputHandler.AND || operator == InputHandler.NOT) {
			for (UserInput input : inputs) {
				String txt = input.getInputText();
				//String hpo;
				switch (input.getInputType()) {
				case 1:
					for (String hpon : txt.split("([^A-Z0-9:]+)")) {
						sb.append(hpon+" - "+hpoDataHandler.getPhenotypeFromHPO(hpon));
					}
					break;
				case 2:
					sb.append(txt+" - "+hpoDataHandler.getPhenotypeFromHPO(txt));
					break;
				case 3:
					sb.append("List of genes");
					break;
				case 4:
					for (String hpon : hpoDataHandler.parseNumbersAsHPO(txt)) {
						sb.append(hpon+" - "+hpoDataHandler.getPhenotypeFromHPO(hpon));
					}
				}
				if (input.getInputGroup() == 0) {
					if (inputHandler.getOperator() == InputHandler.AND) {
						sb.append(" AND (");
					}else{
						sb.append(" NOT (");
					}
				}
				if (input.getID() == inputHandler.getInputCount()-1) {
					sb.append(")");
				}
			}
			outAL.add(sb.toString());
		}else if (operator == InputHandler.DEFAULT || operator == InputHandler.XOR) {
			for (UserInput input : inputs) {
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
								sb.append(hpolist[i]+" - "+hpoDataHandler.getPhenotypeFromHPO(hpolist[i]));
							}
						}
						outAL.add(sb.toString());
						break;
					case 3:
						sb.append("List of genes.");
						outAL.add(sb.toString());
						break;
					case 4:
						hpolist = hpoDataHandler.parseNumbersAsHPO(txt);
						for (int i = 0; i < hpolist.length; i++) {
							if (hpolist.length > 1) {
								sb.append(hpolist[i]);
								if (i < hpolist.length-1) {
									sb.append(", ");
								}
							}else{
								sb.append(hpolist[i]+" - "+hpoDataHandler.getPhenotypeFromHPO(hpolist[i]));
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
		}else if (operator == InputHandler.LIST) {
			for (UserInput input : inputs) {
				String txt = input.getInputText();
				String hpo;
				switch (input.getInputType()) {
					case 1:
						hpo = txt.subSequence(txt.indexOf("HP:"), txt.indexOf("HP:"+9)).toString(); 
						sb.append(hpo+" - "+hpoDataHandler.getPhenotypeFromHPO(hpo));
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
						for (String hpon : hpoDataHandler.parseNumbersAsHPO(txt)) {
							sb.append(hpon+" - "+hpoDataHandler.getPhenotypeFromHPO(hpon));
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
	public void showResults() {
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
		reswindow.setJMenuBar(menubar);
		menubar.add(filemenu);
		filemenu.add(export);
		getContentPanel().add(respanelsp);
		reswindow.pack();
		reswindow.setLocationRelativeTo(null);
		reswindow.setVisible(true);
	}
}