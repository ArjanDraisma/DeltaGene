package deltagene.input.browser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import deltagene.input.UserInput;
import deltagene.input.data.HPODataHandler;
import deltagene.input.data.HPONumber;
import deltagene.main.DeltaGene;
import deltagene.utils.Error;

public abstract class AbstractBrowser extends BrowserGui implements KeyListener, 
MouseListener, ActionListener {
	/**
	 * HPOTree is an extended JTree that fixes a bug where 
	 * expanding a large number of nodes takes a long time
	 * to process
	 */
	
	
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
	public enum State {
		STATE_READY, STATE_INIT;
	}
	State state;
	
	public AbstractBrowser (Component callingComponent, final HPODataHandler hpodata, String title) {
		super(callingComponent);
		this.state = State.STATE_INIT;
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
		hpoBrowserTree = new HPOTree((HPONumber)hpodata.getRoot());
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
				state = State.STATE_READY;
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
			// TODO resultobject.generate(sb.toString());
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
	
	private ArrayList<TreePath> find(HPONumber parent, String hpo) {
		ArrayList<TreePath> outAL = new ArrayList<TreePath>();
		for (HPONumber child : parent.getChildren()) {
			if (child.hpo().equals(hpo)) {
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
	
	public void showHPOHeirarchy(String hpo, HPODataHandler hpoData) {
		addButton.setEnabled(false);
		showHPOHeirarchy(hpo, hpoData, null);
	}

	public void showHPOHeirarchy(final String hpo, final HPODataHandler hpoData, final UserInput input) {
		browserWindow.pack();
		browserWindow.setLocationRelativeTo(null);
		browserWindow.setVisible(true);
		DeltaGene.THREADPOOL.submit(new Runnable() {
			@Override
			public void run() {
				while (!isReady()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (input != null) {
					addButton.setEnabled(true);
					addButton.setActionCommand("add:"+input.getID());
				}
				hpoBrowserTree.clearSelection();
				for (TreePath path : find(rootNode, hpo)) {
					hpoBrowserTree.expandPath(path);
					hpoBrowserTree.addSelectionPath(path);
				}
			}
		});
	}
	
	boolean isReady()
	{
		return state == State.STATE_READY;
	}
}