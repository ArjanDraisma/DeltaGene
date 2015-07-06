package DeltaGene;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Date;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

class DeltaGeneSettings implements ActionListener {
	private JDialog fileWindow;
	JRadioButton latestButton;
	JRadioButton versionButton;
	TreeMap<String, String> hpoBuildMap;
	JList<Object> jlist;
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
		fileWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		fileWindow.setPreferredSize(new Dimension(400,600));
		fileWindow.setContentPane(new JPanel(new GridBagLayout()));
		GridBagConstraints c = new GridBagConstraints();
		ButtonGroup buttonGroup;
		JScrollPane fwjsp = new JScrollPane();
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
		hpoBuildMap = getHpoBuildList();
		jlist = new JList<Object>(hpoBuildMap.keySet().toArray());
		jlist.setEnabled(false);
		fwjsp.setViewportView(jlist);
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		fileWindow.getContentPane().add(fwjsp, c);
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
	
	private TreeMap<String, String> getHpoBuildList() {
		URL url;
		String[] json;
		String buildnum;
		Date timestamp;
		String revision;
		DateFormat datetime = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy"); 
		Pattern regx = Pattern.compile(".*number\":(\\d*).*timestamp\":(\\d*).*revision\":(\\d*).*");
		Matcher matcher;
		TreeMap<String, String> out = new TreeMap<String, String>();
		
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "versionRadio") {
			jlist.setEnabled(true);
		}
		if (e.getActionCommand() == "latestRadio") {
			jlist.setEnabled(false);
		}
		if (e.getActionCommand() == "confirm") {
			if (versionButton.isSelected()) {
				selectedAssoc = "latest";
				selectedHPO = hpoBuildMap.get(jlist.getSelectedValue());
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
		showFileWindow(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public String getHPOVersion() {
		return selectedHPO;
	}
	
	public String getAssocVersion() {
		return selectedAssoc;
	}
}