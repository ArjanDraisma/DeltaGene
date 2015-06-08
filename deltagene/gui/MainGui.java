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

package deltagene.gui;

import deltagene.utils.Error;
import deltagene.utils.Help;
import deltagene.input.InputHandler;
import deltagene.input.UserInput;
import deltagene.input.data.HPODataHandler;
import deltagene.io.HPOFileHandler;
import deltagene.main.DeltaGene;

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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;


/**
 * @author ArjanDraisma
 * The Gui class handles the creation of the main window
 * and the instantiation of other classes.
 */
public class MainGui extends AbstractWindow implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;
	public int down;
	public boolean downloading = true;
	private InputHandler inputInstance;
	
	// the constructor class builds the main window and adds the inputs
	public MainGui () {
		super("DeltaGene",null, 800, 600, true, true);
		inputInstance = new InputHandler(null, null, null, null, null, this, false);
		this.setMinimumSize(new Dimension(500,500));
		addMenus();
		addControls();
		getContentPanel().setLayout(new BoxLayout(getContentPanel(), BoxLayout.PAGE_AXIS));
		inputInstance.addInput(0);
		inputInstance.addInput(1);
	}
	
	/**
	 * This function creates and shows the main gui window
	 */
	public void addMenus() {
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
		
	
		setIconImage(new ImageIcon(DeltaGene.class.getResource("/icon.png")).getImage());
				
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
		formatMenuItem.addActionListener(this);
		formatMenuItem.setActionCommand("format");
		helpMenuItem.addActionListener(this);
		helpMenuItem.setActionCommand("help");
		aboutMenuItem.addActionListener(this);
		aboutMenuItem.setActionCommand("about");
		
		setJMenuBar(mainMenuBar);
		pack();
		repaint();
	}

	private void addControls() {
		final Button submitButton = new Button("Compare");
		final Button clearButton = new Button("Clear fields");
		final Button addButton = new Button("Add input");
		Button removeButton = new Button("Remove input");
		JComboBox<String> operandSelectionBox;
		String[] operators = {"Default", "AND", "NOT", "XOR"};
		operandSelectionBox = new JComboBox<String>(operators);
		
		BoxLayout layout = new BoxLayout(getControlPanel(), BoxLayout.LINE_AXIS);
		
		submitButton.addActionListener(this);
		submitButton.setActionCommand("submit");
		submitButton.setEnabled(false);
		getControlPanel().add(submitButton);
		
		clearButton.addActionListener(this);
		clearButton.setActionCommand("clear");
		clearButton.setEnabled(false);
		getControlPanel().add(clearButton);
		
		addButton.addActionListener(this);
		addButton.setActionCommand("add");
		addButton.setEnabled(true);
		getControlPanel().add(addButton);
		
		removeButton.addActionListener(this);
		removeButton.setActionCommand("rem");
		removeButton.setEnabled(true);
		getControlPanel().add(removeButton);
		operandSelectionBox.addItemListener(this);
		getControlPanel().add(operandSelectionBox);
		getControlPanel().setBorder(BorderFactory.createDashedBorder(Color.gray));
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
				inputInstance.setOperator(InputHandler.DEFAULT);
			}
			if (op.equals("AND")) {
				inputInstance.setOperator(InputHandler.AND);
			}
			if (op.equals("NOT")) {
				inputInstance.setOperator(InputHandler.NOT);
			}
			if (op.equals("XOR")) {
				inputInstance.setOperator(InputHandler.XOR);
			}
		}
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
			inputInstance.addInput(1);
		}if (e.getActionCommand().equals("rem")) {
			inputInstance.removeLastInput();
		}if (e.getActionCommand().equals("format")) {
			inputInstance.formatGenes();
		}if (e.getActionCommand().equals("help")) {
			new Help("index");
		}if (e.getActionCommand().equals("browser")) {
			DeltaGene.THREADPOOL.submit(new Runnable() {
				@Override
				public void run() {
					while (inputInstance.getDataHandler().getState() < HPODataHandler.STATE_LOAD_ASSOC) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							new Error(Error.UNDEF_ERROR, Error.UNDEF_ERROR_T,
									WindowConstants.DISPOSE_ON_CLOSE);
							e.printStackTrace();
						}
					}
					inputInstance.getDataHandler().getBrowser().showHPOHeirarchy("HP:0000001",
							inputInstance.getDataHandler());
				}
			});
		}if (e.getActionCommand().equals("about")) {
			new Help("about");
		}if (e.getActionCommand().equals("exit")) {
			System.exit(0);
		}
	}

	public void startDownload() {
		final InputHandler inputInstance = this.inputInstance;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					while (inputInstance.getFileHandler().getState() < HPOFileHandler.STATE_READY) {
						Thread.sleep(5);
						switch (inputInstance.getFileHandler().getState()) {
							case HPOFileHandler.STATE_DOWNLOAD_HPO:
								setTitle("DeltaGene - Downloading HPO File - "+inputInstance.getFileHandler().getDown());
							case HPOFileHandler.STATE_DOWNLOAD_ASSOC:
								setTitle("DeltaGene - Downloading association File - "+inputInstance.getFileHandler().getDown());
						}
					}
					setTitle("DeltaGene - Building HPO data");
					while (inputInstance.getDataHandler().getState() < HPODataHandler.STATE_READY) {
						Thread.sleep(5);
					}
					setTitle("DeltaGene - Ready");
				}catch (InterruptedException e) {
					new Error(Error.UNDEF_ERROR, Error.UNDEF_ERROR_T, DO_NOTHING_ON_CLOSE);
				}
			}
		});	
	}
}
