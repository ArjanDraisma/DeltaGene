package deltagene.input;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.HashSet;
import java.util.Stack;

import javax.activation.DataHandler;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;














import deltagene.input.data.HPODataHandler;
import deltagene.main.DeltaGene;

/**
 * The userinput class takes care of the user's input, including
 * parsing the type of input, 
 */
public class UserInput extends JPanel implements DocumentListener, HyperlinkListener {
	private static final long serialVersionUID = 1L;
	private JTextArea inputbox;
	private JEditorPane infobox;
	private JScrollPane inputjsp;
	private JScrollPane infojsp;
	private Document inputdoc;
	private int group;
	private InputHandler input;
	private HPODataHandler data;
	
	public UserInput(int group, InputHandler input) {
		this.input = input;
		this.group = group;
		data = input.getDataHandler();
		inputbox = new JTextArea();
		inputdoc = inputbox.getDocument();
		inputjsp = new JScrollPane(inputbox,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints c = new GridBagConstraints();
		inputdoc.addDocumentListener(this); 
		inputbox.setFocusTraversalKeysEnabled(false);
		infobox = new JEditorPane();
		infobox.setContentType("text/html");
		infobox.addHyperlinkListener(this);
		infobox.setBackground(Color.getHSBColor(0, 0, 
				(float)0.9));
		infojsp = new JScrollPane(infobox, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		infobox.setEditable(false);
		this.setPreferredSize(new Dimension(0,250));
		this.setLayout(new GridBagLayout());
		c.insets.bottom = 5;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 0.5;
		this.add(inputjsp,c);
		c.gridx = 0; c.gridy = 1;
		c.weightx = 1; c.weighty = 0.5;
		this.add(infojsp,c);
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
	
	public int getID() {
		return InputHandler.inputs.indexOf(this);
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
	
	// returns the size of this inputs' inputbox
	Dimension getInputBoxSize() {
		return inputbox.getSize();
	}
	
	public int getInputGroup() {
		return group;
	}
	
	/**
	 * returns the inputscrollpane. used in removing an input
	 * @return the inputboxes' JScrollPane
	 */
	public JScrollPane getInputScrollPane() {
		return inputjsp;
	}
	
	public String getInputText () {
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
	public int getInputType() {
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
				// TODO resultobject.generate(desc.substring(6));
			}else if (desc.contains("tree:")) {
				// TODO HPOData.browser.showHPOHeirarchy(desc.substring(5), HPOData);
			}else if (desc.equals("hpogenes")) {
				// TODO HPOData.browser.showGenesUnderHPO(desc.substring(8), HPOData);
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
				pheno = data.getPhenotypeFromHPO(hpo);
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
				if (data.getPhenotypeFromHPO(text) != null) {
					infobox.setText("Type: Single HPO number:<br>"
				+text+" - "+data.getPhenotypeFromHPO(text)+
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
			infobox.setText("Type: List of genes. "
					+ "<a href=\"findhpo\">Find HPO numbers associated "
					+ "with genes</a>");
			break;
		case 4:
			sb.append("Type: List of numbers (Parsed as HPO numbers):");
			for (String hpo : data.parseNumbersAsHPO(
					inputbox.getText())) {
				if (hpo == null) {
					hpo = "Invalid HPO number";
				}
				pheno = data.getPhenotypeFromHPO(hpo);
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
	
	public void getGeneSet(HashSet<String> hashSet) {
		String[] hpoids;
		if (getInputType() == 1 || getInputType() == 2) {
			hpoids = getInputText().split("[^0-9HP:]");
			for (String hpoid : hpoids) {
				for (String gene : data.getHpoData(hpoid).getGeneSet()) {
					if (!hashSet.contains(gene))
						hashSet.add(gene);
				}
			}
		}if (getInputType() == 3) {
			hpoids = data.parseNumbersAsHPO(getInputText());
			for (String hpoid : hpoids) {
				for (String gene : data.getHpoData(hpoid).getGeneSet()) {
					if (!hashSet.contains(gene))
						hashSet.add(gene);
				}
			}
		}if (getInputType() == 4) {
			for (String gene : getInputText().split("[^A-Z0-9\\-]")) {
				hashSet.add(gene);
			}
		}
	}
}