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
		JFrame helpFrame = new JFrame("Help"); 		// This JFrame is the help window JFrame 
		JEditorPane content = new JEditorPane();	// The content EditorPane will contain the help HTML
		JScrollPane contentScrollPane;							// This is the scrollpane for the 'content' editorpane
		
		HelpClass(String page){
			helpFrame.setPreferredSize(mainWindow.getSize()); 	// this sets the size of the help window JFrame, which will be the same as the main window
			contentScrollPane = new JScrollPane(content);	// this creates the JScrollPane and sets the content EditorPane as its viewing pane
			content.setEditable(false);						// we do not want the user to be able to edit the EditorPane
			content.addHyperlinkListener(this);				// this listens for click on hyperlinks, which are used as navigation in the help pages
			helpFrame.add(contentScrollPane);								// this adds the jscrollpane, which 'contains' the EditorPane to the help JFrame
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
				new Error(Error.IO_ERROR, Error.IO_ERROR_T, WindowConstants.DISPOSE_ON_CLOSE);
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
				helpFrame.pack();		
				helpFrame.setLocationRelativeTo(null); 	// this centers the help/about window
				helpFrame.setVisible(true);
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
	
	public static JFrame mainWindow; 
	public static Container contentContainer;	
	public static Container inputContainer;
	public int down;
	public boolean downloading = true;	
	private SpringLayout contentContainerLayout;
	private BoxLayout inputContainerLayout;
	private static Input inputInstance;
	//JMenuItem browserMenuItem;
	
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
					inputInstance.getResults();
					return null;
				}
			};
			resworker.execute();
		}if (e.getActionCommand().equals("clear")) {
			inputInstance.clearInputs();
		}if (e.getActionCommand().equals("add")) {
			inputInstance.addInput(1,1);
		}if (e.getActionCommand().equals("rem")) {
			inputInstance.removeInput();
		}if (e.getActionCommand().equals("format")) {
			inputInstance.formatGenes();
		}if (e.getActionCommand().equals("help")) {
			new HelpClass("index");
		}if (e.getActionCommand().equals("browser")) {
			DeltaGene.THREADPOOL.submit(new Runnable() {
				@Override
				public void run() {
					while (inputInstance.getData().getState() < Input.HPOObject.STATE_LOAD_ASSOC) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							new Error(Error.UNDEF_ERROR, Error.UNDEF_ERROR_T,
									WindowConstants.DISPOSE_ON_CLOSE);
							e.printStackTrace();
						}
					}
					inputInstance.getData().getBrowser().show("HP:0000001 - All",
							inputInstance.getData());
				}
			});
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
		mainWindow = new JFrame("DeltaGene");
		/* The menubar will contain a number of dropdown menus
		 * for opening specific HPO and association files, opening
		 * the HPO browser and opening the help files.
		 */
		JMenuBar mainMenuBar = new JMenuBar();
		// The filemenu contains the open and exit button
		JMenu fileMenu = new JMenu("File");
		JMenuItem exitMenuItem = new JMenuItem("Open");
		JMenuItem openMenuItem = new JMenuItem("Exit");
		// The tools menu will contain the HPO Browser button
		JMenu toolsMenuItem = new JMenu("Tools");
		JMenuItem browserMenuItem = new JMenuItem("HPO Browser");
		JMenuItem formatMenuItem = new JMenuItem("Format gene list");
		// The helpmenu will contain the controls to open help and about
		JMenu helpMenu = new JMenu("Help");
		JMenuItem helpMenuItem = new JMenuItem("Help");
		JMenuItem aboutMenuItem = new JMenuItem("About");
		

		mainWindow.setIconImage(new ImageIcon(DeltaGene.class.getResource("/icon.png")).getImage());
		
		/* the content container will contain the controls container and 
		 * input container. 
		 */
		contentContainer = new Container();
		
		// This adds all menus and their items to the menubar
		mainMenuBar.add(fileMenu);						
		fileMenu.add(exitMenuItem);	
		fileMenu.add(openMenuItem);
		mainMenuBar.add(toolsMenuItem);
		toolsMenuItem.add(browserMenuItem);
		toolsMenuItem.add(formatMenuItem);
		mainMenuBar.add(helpMenu);
		helpMenu.add(aboutMenuItem);
		helpMenu.add(helpMenuItem);
		
		/* The buttons work through the use of actioncommands, and
		 * are handled in actionPerformed
		 */
		exitMenuItem.addActionListener(this);
		exitMenuItem.setActionCommand("exit");
		browserMenuItem.addActionListener(this);
		browserMenuItem.setActionCommand("browser");
		//browser.setEnabled(false);
		formatMenuItem.addActionListener(this);
		formatMenuItem.setActionCommand("format");
		helpMenuItem.addActionListener(this);
		helpMenuItem.setActionCommand("help");
		aboutMenuItem.addActionListener(this);
		aboutMenuItem.setActionCommand("about");
		
		// GUI housekeeping
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setPreferredSize(new Dimension(700,600));
		mainWindow.setJMenuBar(mainMenuBar);
		mainWindow.setContentPane(contentContainer);
		mainWindow.pack();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);
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
				inputInstance.setOperator(Input.DEFAULT);
			}
			if (op.equals("AND")) {
				inputInstance.setOperator(Input.AND);
			}
			if (op.equals("NOT")) {
				inputInstance.setOperator(Input.NOT);
			}
			if (op.equals("XOR")) {
				inputInstance.setOperator(Input.XOR);
			}
		}
	}
	
	private void showInputs() {
		try {
			// Before initializing
			// Create an instance of the input class
			inputInstance = new Input();
			// we use a springlayout for content
			contentContainerLayout = new SpringLayout();
			contentContainer.setLayout(contentContainerLayout);
			GridBagConstraints c = new GridBagConstraints();
			JPanel controlPanel = new JPanel(new FlowLayout());
			inputContainer = new Container();
			inputContainerLayout = new BoxLayout(inputContainer, BoxLayout.PAGE_AXIS);
			
			// initialize 
			DeltaGene.THREADPOOL.submit(new Runnable() {
				@Override
				public void run() {
					inputInstance.initialize(mainWindow, inputContainer);
				}
			});
			
			// do while initializing
			inputContainer.setLayout(inputContainerLayout);
			JScrollPane inputScrollPane = new JScrollPane(inputContainer);
			final Button submitButton = new Button("Compare");
			final Button clearButton = new Button("Clear fields");
			final Button addButton = new Button("Add input");
			Button removeButton = new Button("Remove input");
			JComboBox<String> operandSelectionBox;
			String[] operators = {"Default", "AND", "NOT", "XOR"};
			c.insets = new Insets(2,2,2,2);
			
			
			submitButton.addActionListener(this);
			submitButton.setActionCommand("submit");
			submitButton.setEnabled(false);
			controlPanel.add(submitButton,c);
			
			clearButton.addActionListener(this);
			clearButton.setActionCommand("clear");
			clearButton.setEnabled(false);
			controlPanel.add(clearButton,c);
			
			addButton.addActionListener(this);
			addButton.setActionCommand("add");
			addButton.setEnabled(false);
			controlPanel.add(addButton,c);
			
			removeButton.addActionListener(this);
			removeButton.setActionCommand("rem");
			removeButton.setEnabled(false);
			controlPanel.add(removeButton,c);
			
			operandSelectionBox = new JComboBox<String>(operators);
			operandSelectionBox.addItemListener(this);
			controlPanel.add(operandSelectionBox);
			c.insets.set(5, 5, 5, 5);
			c.weighty = 0;
			c.weightx = 1;
			c.anchor = GridBagConstraints.NORTH;
			c.fill = GridBagConstraints.HORIZONTAL;
			controlPanel.setBorder(BorderFactory.createDashedBorder(Color.gray));
			controlPanel.setPreferredSize(new Dimension(
					mainWindow.getContentPane().getWidth()-10, 40));
			
			contentContainerLayout.putConstraint(SpringLayout.WEST, controlPanel, 5,
					SpringLayout.WEST, mainWindow.getContentPane());
			contentContainerLayout.putConstraint(SpringLayout.NORTH, controlPanel, 5,
					SpringLayout.NORTH, mainWindow.getContentPane());
			mainWindow.getContentPane().add(controlPanel);
			inputScrollPane.setPreferredSize(new Dimension(
					mainWindow.getContentPane().getWidth()-10,
					mainWindow.getContentPane().getHeight()-controlPanel.getHeight()
					-50));
			inputScrollPane.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			inputScrollPane.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			contentContainerLayout.putConstraint(SpringLayout.WEST, inputScrollPane, 5, 
					SpringLayout.WEST, mainWindow.getContentPane());
			contentContainerLayout.putConstraint(SpringLayout.NORTH, inputScrollPane, 5,
					SpringLayout.SOUTH, controlPanel);
			mainWindow.getContentPane().add(inputScrollPane);
			mainWindow.revalidate();
			mainWindow.repaint();
			
			// 
			DeltaGene.THREADPOOL.submit(new Runnable() {
				@Override
				public void run() {
					while (inputInstance.getState() < Input.STATE_BUILDING) {
						try {
							Thread.sleep(50);
						}catch (InterruptedException e) {
							new Error(Error.CRIT_ERROR, Error.CRIT_ERROR_T,
									WindowConstants.EXIT_ON_CLOSE, e);
							e.printStackTrace();
						}
					}
					addButton.setEnabled(true);
					clearButton.setEnabled(true);
					submitButton.setEnabled(true);
					inputInstance.addInput(1,0);
					inputInstance.addInput(1,1);
				}
			});
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
