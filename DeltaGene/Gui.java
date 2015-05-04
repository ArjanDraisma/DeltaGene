/*
 * Gui class for DeltaGene
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

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 * @author ArjanDraisma
 * The Gui class handles the creation of the main window
 * and the instantiation of other classes.
 */
public class Gui extends Thread implements ActionListener, ItemListener {
	/**
	 * @author ArjanDraisma
	 * The HelpClass manages and shows the help window, with the help pages
	 * in the /Help/ directory.
	 */
	class HelpClass implements HyperlinkListener {
		JFrame helpframe = new JFrame("Help"); 		// This JFrame is the help window JFrame 
		JEditorPane content = new JEditorPane();	// The content EditorPane will contain the help HTML
		JScrollPane jsp;							// This is the scrollpane for the 'content' editorpane
		
		HelpClass(String page){
			helpframe.setPreferredSize(window.getSize()); 	// this sets the size of the help window JFrame, which will be the same as the main window
			jsp = new JScrollPane(content);				  	// this creates the JScrollPane and sets the content EditorPane as its viewing pane
			content.setEditable(false);						// we do not want the user to be able to edit the EditorPane
			content.addHyperlinkListener(this);				// this listens for click on hyperlinks, which are used as navigation in the help pages
			helpframe.add(jsp);								// this adds the jscrollpane, which 'contains' the EditorPane to the help JFrame
			show(page);
		}
		
		/**
		 * load a new page if the user clicks a link in the help window.
		 */
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			try {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					content.setPage(e.getURL());
				}
			}catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		/**
		 * This function shows the help window and it's specified page, as
		 * passed by hyperlinkUpdate() above
		 * @param page the page to load, without directory or extension
		 */
		public void show(String page) {
			try{
				/* This sets a URL to a specified html file
				 * Should be either 'index' or 'about'. Directory and
				 * extension will be added.
				 */
				URL u = DeltaGene.class.getResource("/Help/"+page+".html");
				content.setPage(u);		// this sets the html file as the editorpane's content
				helpframe.pack();		
				helpframe.setLocationRelativeTo(null); 	// this centers the help/about window
				helpframe.setVisible(true);
			}catch (IOException e){
				/* Throw an error if the page files are missing or cannot be opened.
				 * Considering the help files are packed into the .jar, this really
				 * should not happen.
				 */
				new Error("Could not open help files.", 
						"Help error",
						WindowConstants.DISPOSE_ON_CLOSE);
				e.printStackTrace();
			}
		}
	}
	
	public static JFrame window;		// This is the main window. 
	public static Container content;	// This is the root JPanel
	public static Container inputcontainer;	// This is the container that will hold the input fields and information boxes
	public int down;					// The amount of bytes downloaded, to be used in the downloadlabel.
	public boolean downloading = true;	// When this is set to false, the program will remove the downloadLabel	
	private SpringLayout contentlayout;	// This will contain content layout
	private BoxLayout inputslayout;		// this will contain inputs layout
	private static input dgi;			// This will contain an instance of the input object
	JMenuItem browser;					// this is the menu item for the browser window, which has to be disabled until the HPO database has been compiled
	
	// the constructor class builds the main window and adds the inputs
	Gui () {
		createAndShowGUI();
		showInputs();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		/* When the user submits the inputs for processing,
		 * a new thread will process and show the results
		 */
		if (e.getActionCommand().equals("submit")) {
			SwingWorker<Void,Void> resworker = new SwingWorker<Void,Void>() {
				public Void doInBackground() {
					dgi.getResults();
					return null;
				}
			};
			resworker.execute();
		}if (e.getActionCommand().equals("clear")) {
			dgi.clearInputs();
		}if (e.getActionCommand().equals("add")) {
			dgi.addInput(1,1);
		}if (e.getActionCommand().equals("rem")) {
			dgi.removeInput();
		}if (e.getActionCommand().equals("format")) {
			dgi.formatGenes();
		}if (e.getActionCommand().equals("help")) {
			new HelpClass("index");
		}if (e.getActionCommand().equals("browser")) {
			dgi.getData().getBrowser().show("HP:0000001 - All");
		}if (e.getActionCommand().equals("about")) {
			new HelpClass("about");
		}if (e.getActionCommand().equals("exit")) {
			System.exit(0);
		}
	}
	/**
	 * This function creates and shows the main gui window
	 */
	public void createAndShowGUI() {
		// Creates the window for DeltaGene, titled 'DeltaGene'
		window = new JFrame("DeltaGene");
		window.setIconImage(new ImageIcon(DeltaGene.class.getResource("/icon.png")).getImage());
		/* The menubar will contain a number of dropdown menus
		 * for opening specific HPO and association files, opening
		 * the HPO browser and opening the help files.
		 */
		JMenuBar menuBar = new JMenuBar();
		// The filemenu contains the open and exit button
		JMenu fileMenu = new JMenu("File");
		JMenuItem exit = new JMenuItem("Open");
		JMenuItem open = new JMenuItem("Exit");
		// The tools menu will contain the HPO Browser button
		JMenu toolsMenu = new JMenu("Tools");
		browser = new JMenuItem("HPO Browser");
		JMenuItem format = new JMenuItem("Format gene list");
		// The helpmenu will contain the controls to open help and about
		JMenu helpMenu = new JMenu("Help");
		JMenuItem help = new JMenuItem("Help");
		JMenuItem about = new JMenuItem("About");
		
		/* the content container will contain the controls container and 
		 * input container. 
		 */
		content = new Container();
		// We use a boxlayout to align elements vertically using the PAGE_AXIS
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		
		// This adds all menus and their items to the menubar
		menuBar.add(fileMenu);						
		fileMenu.add(exit);	
		fileMenu.add(open);
		menuBar.add(toolsMenu);				
		toolsMenu.add(browser);
		toolsMenu.add(format);
		menuBar.add(helpMenu);
		helpMenu.add(about);
		helpMenu.add(help);
		
		/* The buttons work through the use of actioncommands, and
		 * are handled in actionPerformed
		 */
		exit.addActionListener(this);
		exit.setActionCommand("exit");
		browser.addActionListener(this);
		browser.setActionCommand("browser");
		//browser.setEnabled(false);
		format.addActionListener(this);
		format.setActionCommand("format");
		help.addActionListener(this);
		help.setActionCommand("help");
		about.addActionListener(this);
		about.setActionCommand("about");
		
		// GUI housekeeping
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setPreferredSize(new Dimension(700,600));
		window.setJMenuBar(menuBar);
		window.setContentPane(content);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setResizable(false);
		window.setVisible(true);
	}
	
	/* 
	 * (non-Javadoc) 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		String op = (String)e.getItem();
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (op.equals("Default")) {
				dgi.setOperator(input.DEFAULT);
			}
			if (op.equals("AND")) {
				dgi.setOperator(input.AND);
			}
			if (op.equals("NOT")) {
				dgi.setOperator(input.NOT);
			}
			if (op.equals("XOR")) {
				dgi.setOperator(input.XOR);
			}
		}
	}
	
	private void showInputs() {
		try {
			// Before initializing
			// Create an instance of the input class
			dgi = new input();
			contentlayout = new SpringLayout();	// create the layout manager for the content container
			content.setLayout(contentlayout);
			GridBagConstraints c = new GridBagConstraints();
			JPanel controls = new JPanel(new FlowLayout());
			inputcontainer = new Container();
			inputslayout = new BoxLayout(inputcontainer, BoxLayout.PAGE_AXIS);
			
			// initialize 
			DeltaGene.pool.submit(new Runnable() {
				@Override
				public void run() {
					dgi.initialize(window, inputcontainer);
				}
			});
			
			// do while initializing
			inputcontainer.setLayout(inputslayout);
			JScrollPane inputssp = new JScrollPane(inputcontainer);
			Button submitButton = new Button("Compare");
			Button clearButton = new Button("Clear fields");
			Button addButton = new Button("Add input");
			Button remButton = new Button("Remove input");
			JComboBox<String> operand;
			String[] operators = {"Default", "AND", "NOT", "XOR"};
			c.insets = new Insets(2,2,2,2);
			
			
			submitButton.addActionListener(this);
			clearButton.addActionListener(this);
			addButton.addActionListener(this);
			remButton.addActionListener(this);
			submitButton.setActionCommand("submit");
			clearButton.setActionCommand("clear");
			addButton.setActionCommand("add");
			remButton.setActionCommand("rem");
			controls.add(submitButton,c);
			controls.add(clearButton,c);
			controls.add(addButton,c);
			controls.add(remButton,c);
			operand = new JComboBox<String>(operators);
			operand.addItemListener(this);
			controls.add(operand);
			c.insets.set(5, 5, 5, 5);
			c.weighty = 0;
			c.weightx = 1;
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;
			controls.setBorder(BorderFactory.createDashedBorder(Color.gray));
			controls.setPreferredSize(new Dimension(
					window.getContentPane().getWidth()-10, 40));
			
			contentlayout.putConstraint(SpringLayout.WEST, controls, 5,
					SpringLayout.WEST, window.getContentPane());
			contentlayout.putConstraint(SpringLayout.NORTH, controls, 5,
					SpringLayout.NORTH, window.getContentPane());
			window.getContentPane().add(controls);
			inputssp.setPreferredSize(new Dimension(
					window.getContentPane().getWidth()-10,
					window.getContentPane().getHeight()-controls.getHeight()-50));
			inputssp.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			inputssp.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			contentlayout.putConstraint(SpringLayout.WEST, inputssp, 5, 
					SpringLayout.WEST, window.getContentPane());
			contentlayout.putConstraint(SpringLayout.NORTH, inputssp, 5,
					SpringLayout.SOUTH, controls);
			window.getContentPane().add(inputssp);
			window.revalidate();
			window.repaint();
			
			dgi.addInput(1,0);
			dgi.addInput(1,1);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
