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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.tree.TreePath;

import deltagene.gui.AbstractWindow;
import deltagene.input.InputHandler;
import deltagene.input.UserInput;
import deltagene.input.data.HPODataHandler;
import deltagene.input.data.HPONumber;
import deltagene.main.DeltaGene;
import deltagene.utils.Error;

public class HPOBrowser extends AbstractWindow implements KeyListener, 
MouseListener, ActionListener {
	private static final long serialVersionUID = 1L;
	
	private HPOTree hpoBrowserTree;
	private JPopupMenu browserPopupMenu;
	final private JScrollPane treeScrollPane;
	private JLabel searchlabel;
	private JTextField searchField;
	private JButton searchButton;
	private JButton listButton;
	private JMenuItem treeListButton;
	private JButton addButton;
	public enum State {
		STATE_READY, STATE_INIT;
	}
	private InputHandler inputHandler;
	State state;
	
	/**
	 * 
	 * @param callingComponent
	 * @param hpoDataHandler
	 * @param hpo which HPO number to scroll to
	 */
	public HPOBrowser(Component callingComponent, final InputHandler inputHandler, String hpo) {
		super("HPO Browser", callingComponent, 800, 600, true, true, WindowConstants.DISPOSE_ON_CLOSE);
		this.inputHandler = inputHandler;
		this.state = State.STATE_INIT;
		
		treeScrollPane = new JScrollPane();
		searchlabel = new JLabel("Search:");
		searchField = new JTextField();
		searchButton = new JButton("Find");
		listButton = new JButton("List genes");
		addButton = new JButton("Add to input");
		
		final JLabel waitlabel = new JLabel("Please wait until the HPO database"
				+ "has finished loading...");
		
		getContentPanel().add(waitlabel);
		
		while (this.inputHandler.getState() != InputHandler.STATE_READY) {
			try {
				Thread.sleep(5);
			}catch (InterruptedException e) {
				new Error(Error.CRIT_ERROR, Error.CRIT_ERROR_T,
				WindowConstants.EXIT_ON_CLOSE, e);
				e.printStackTrace();
			}
		}
		
		getContentPanel().remove(waitlabel);
		
		hpoBrowserTree = new HPOTree((HPONumber)inputHandler.getDataHandler().getRoot());
		hpoBrowserTree.addMouseListener(this);
		hpoBrowserTree.setExpandsSelectedPaths(true);
		hpoBrowserTree.setLargeModel(true);
		treeScrollPane.setViewportView(hpoBrowserTree);
		getContentPanel().add(treeScrollPane);
		
		
		searchField.addKeyListener(this);
		getControlPanel().setLayout(new FlowLayout());
		getControlPanel().add(searchlabel);
		searchField.setPreferredSize(new Dimension(80, 20));
		getControlPanel().add(searchField);
		searchButton.addActionListener(this);
		searchButton.addKeyListener(this);
		searchButton.setActionCommand("search");
		getControlPanel().add(searchButton);
		listButton.addActionListener(this);
		listButton.setActionCommand("list");
		getControlPanel().add(listButton);
		addButton.setEnabled(false);
		getControlPanel().add(addButton);
		getControlPanel().setMaximumSize(new Dimension(this.getWidth(), 30));
		
		pack();
		revalidate();
		repaint();
		
		state = State.STATE_READY;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Long start, stop, time;
		if (e.getActionCommand().equals("search")&&searchField.getText().length()>0) {
			start = System.currentTimeMillis();
			hpoBrowserTree.clearSelection();
			hpoBrowserTree.expandAll(hpoBrowserTree, false);
			ArrayList<TreePath> AL = find((HPONumber) inputHandler.getDataHandler().getRoot(), searchField.getText());
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
		browserPopupMenu = new JPopupMenu();
		browserPopupMenu.setPreferredSize(new Dimension(150,30));
		treeListButton = new JMenuItem("List selected");
		treeListButton.addActionListener(this);
		treeListButton.setActionCommand("list");
		browserPopupMenu.add(treeListButton);
		browserPopupMenu.setLocation(p);
		browserPopupMenu.setInvoker(this);
		browserPopupMenu.pack();
		browserPopupMenu.revalidate();
		browserPopupMenu.repaint();
		browserPopupMenu.setVisible(true);
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
	
	boolean isReady()
	{
		return state == State.STATE_READY;
	}
}