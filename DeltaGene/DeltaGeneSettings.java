package DeltaGene;

import DeltaGene.Error;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class DeltaGeneSettings implements ActionListener, DocumentListener {
	private JDialog fileWindow;
	JRadioButton latestButton;
	JRadioButton versionButton;
	LinkedHashMap<String, String> hpoBuildMap;
	LinkedHashMap<String, String> assocBuildMap;
	JPanel selectionContainer;
	JTextField hposearch;
	JList<Object> hpolist;
	JTextField assocsearch;
	JList<Object> assoclist;
	JCheckBox saveCheckbox;
	private String selectedHPO;
	private String selectedAssoc;
	File settingsFile;
	
	DeltaGeneSettings(JFrame owner) {
		fileWindow = new JDialog(owner, "Choose HPO version", Dialog.ModalityType.APPLICATION_MODAL);
		settingsFile = new File(".\\settings.txt");
		if (!settingsExist()) {
			showFileWindow(WindowConstants.DISPOSE_ON_CLOSE);
		}else{
			selectedHPO = getSetting("hpofile");
			selectedAssoc = getSetting("assocfile");
		}
	}
	
	public boolean settingsExist() {
		return settingsFile.exists();
	}
	
	public void showFileWindow(int actionOnClose) {
		fileWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		fileWindow.setPreferredSize(new Dimension(400,600));
		fileWindow.setContentPane(new JPanel(new GridBagLayout()));
		fileWindow.setUndecorated(false);
		fileWindow.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		ButtonGroup buttonGroup;
		JScrollPane hpojsp;
		JScrollPane assocjsp = new JScrollPane();
		GridBagConstraints c = new GridBagConstraints();
		JButton confirmButton;
		
		buttonGroup = new ButtonGroup();
		latestButton = new JRadioButton("Use latest version");
		latestButton.setActionCommand("latestRadio");
		latestButton.addActionListener(this);
		latestButton.setSelected(true);
		versionButton = new JRadioButton("Select version:");
		versionButton.setActionCommand("versionRadio");
		versionButton.addActionListener(this);
		buttonGroup.add(latestButton);
		buttonGroup.add(versionButton);
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.05;
		fileWindow.getContentPane().add(latestButton, c);
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0.05;
		fileWindow.getContentPane().add(versionButton, c);
		
		selectionContainer = new JPanel(new GridBagLayout());
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 0.9;
		fileWindow.getContentPane().add(selectionContainer, c);
		
		c.gridy = 0;
		selectionContainer.add(new JLabel("HPO version:"), c);
		
		hposearch = new JTextField();
		hposearch.getDocument().addDocumentListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0.05;
		selectionContainer.add(hposearch,c);
		
		hpoBuildMap = getHpoBuildList("");
		hpolist = new JList<Object>(hpoBuildMap.keySet().toArray());
		hpolist.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		hpojsp = new JScrollPane();
		hpojsp.setViewportView(hpolist);
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 0.45;
		selectionContainer.add(hpojsp, c);

		c.gridy = 3;
		selectionContainer.add(new JLabel("Annotation version:"), c);
		
		assocsearch = new JTextField();
		assocsearch.getDocument().addDocumentListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 4;
		c.weightx = 1;
		c.weighty = 0.05;
		selectionContainer.add(assocsearch,c);
		
		assocBuildMap = getAssocBuildList("");
		assoclist = new JList<Object>(assocBuildMap.keySet().toArray());
		assoclist.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		assocjsp = new JScrollPane();
		assocjsp.setViewportView(assoclist);
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 5;
		c.weightx = 1;
		c.weighty = 0.45;
		selectionContainer.add(assocjsp, c);
		
		enableComponents(selectionContainer, false);
		
		saveCheckbox = new JCheckBox("Save these settings");
		c.gridy = 3;
		c.weightx = 1;
		c.weighty = 0.05;
		fileWindow.getContentPane().add(saveCheckbox, c);
		confirmButton = new JButton("Ok");
		confirmButton.setActionCommand("confirm");
		confirmButton.addActionListener(this);
		c.gridy = 4;
		c.weightx = 1;
		c.weighty = 0.05;
		fileWindow.getContentPane().add(confirmButton, c);
		fileWindow.pack();
		fileWindow.setLocationRelativeTo(null);
		fileWindow.revalidate();
		fileWindow.repaint();
		fileWindow.setVisible(true);
	}
	
	private LinkedHashMap<String, String> getHpoBuildList(String search) {
		URL url;
		String[] json;
		String buildnum;
		Date timestamp;
		String revision;
		DateFormat datetime = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy"); 
		Pattern regx = Pattern.compile(".*number\":(\\d*).*timestamp\":(\\d*).*revision\":(\\d*).*");
		Matcher matcher;
		LinkedHashMap<String, String> out = new LinkedHashMap<String, String>();
		
		try {
			url = new URL("http://compbio.charite.de/hudson/job/hpo/api/json?depth=1&tree=builds[number,timestamp,result,changeSet[revisions[revision]]]");
			json = convertStreamToString(url.openStream()).split("(\\},\\{)");
			for (String build : json) {
				if (build.contains("SUCCESS")) {
					matcher = regx.matcher(build);
					if (matcher.matches()) {
						buildnum = matcher.group(1);
						timestamp = new Date(Long.parseLong(matcher.group(2)));
						revision = matcher.group(3);
						// filtering any build below # 980 is a hack that I promise is temporary
						// any builds below this number do not have any artifacts
						if (Integer.parseInt(buildnum) > 979) {
							out.put("Revision "+revision+" - "+datetime.format(timestamp), buildnum);
						}
					}
				} 
			}
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private LinkedHashMap<String, String> getAssocBuildList(String search) {
		URL url;
		String[] json;
		String buildnum;
		Date timestamp;
		String revision;
		DateFormat datetime = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy"); 
		Pattern regx = Pattern.compile(".*number\":(\\d*).*timestamp\":(\\d*).*revision\":(\\d*).*");
		Matcher matcher;
		LinkedHashMap<String, String> out = new LinkedHashMap<String, String>();
		
		try {
			url = new URL("http://compbio.charite.de/hudson/job/hpo.annotations.monthly/api/json?depth=1&tree=builds[number,timestamp,result,changeSet[revisions[revision]]]");
			json = convertStreamToString(url.openStream()).split("(\\},\\{)");
			for (String build : json) {
				if (build.contains("SUCCESS")) {
					matcher = regx.matcher(build);
					if (matcher.matches()) {
						buildnum = matcher.group(1);
						timestamp = new Date(Long.parseLong(matcher.group(2)));
						revision = matcher.group(3);
						out.put("Revision "+revision+" - "+datetime.format(timestamp), buildnum);
					}
				} 
			}
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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

	
	public void enableComponents(Container container, boolean enable) {
		Component[] components = container.getComponents();
		for (Component component : components) {
			component.setEnabled(enable);
			if (component instanceof Container) {
			    enableComponents((Container)component, enable);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "versionRadio") {
			enableComponents(selectionContainer, true);
		}
		if (e.getActionCommand() == "latestRadio") {
			enableComponents(selectionContainer, false);
		}
		if (e.getActionCommand() == "confirm") {
			if (versionButton.isSelected()) {
				if (hpolist.getSelectedIndex() == -1 || assoclist.getSelectedIndex() == -1) {
					new Error(fileWindow, "You must select both a HPO file and an annotation file.",
							"Source file selection error",
							WindowConstants.DISPOSE_ON_CLOSE);
					return;
				}
				selectedAssoc = assocBuildMap.get(assoclist.getSelectedValue());
				selectedHPO = hpoBuildMap.get(hpolist.getSelectedValue());
			}if (latestButton.isSelected()) {
				selectedAssoc = "latest";
				selectedHPO = "latest";
			}
			if (saveCheckbox.isSelected()) {
				createSettingsFile();
			}
			fileWindow.dispose();
		}
	}
	
	private String getSetting(String key) {
		try (BufferedReader br = new BufferedReader(new FileReader(settingsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if (!line.startsWith("#")) {
		    	  String[] properties = line.split("=");
		    	  if (properties[0].equals(key)) {
		    		  return properties[1];
		    	  }
		       }
		    }
		    return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void createSettingsFile() {
		if (settingsFile.exists()) {
			settingsFile.delete();
		}
		try {
			settingsFile.createNewFile();
			System.out.println("happening");
			FileWriter fw = new FileWriter(settingsFile);
			fw.append("#DeltaGene settings file\n\r");
			if (versionButton.isSelected()) {
				fw.append("hpofile="+selectedHPO+"\n\r");
				fw.append("assocfile="+selectedAssoc+"\n\r");
			}else{
				fw.append("hpofile=latest\n\r");
				fw.append("assocfile=latest\n\r");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void changeSettings() {
		if (settingsFile.exists()) {
			settingsFile.delete();
		}
		showFileWindow(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public String getHPOVersion() {
		return selectedHPO;
	}
	
	public String getAssocVersion() {
		return selectedAssoc;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument() == hposearch.getDocument()) {
			hpolist.setListData(filterList(hpoBuildMap, hposearch.getText()).toArray());
		}if (e.getDocument() == assocsearch.getDocument()) {
			assoclist.setListData(filterList(assocBuildMap, assocsearch.getText()).toArray());
		}
	}

	private List<String> filterList(LinkedHashMap<String, String> map, String search) {
		List<String> out = new ArrayList<String>();
		for (String key : map.keySet()) {
			if (key.startsWith("Revision "+search)) {
				out.add(key);
			}
		}
		return out;
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		insertUpdate(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}
}