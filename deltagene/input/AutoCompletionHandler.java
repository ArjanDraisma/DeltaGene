package deltagene.input;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.TreeMap;
import java.util.concurrent.Future;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import deltagene.input.UserInput;

/**
 * This class handles the autocompletion for the textareas
 */
class Autocomplete implements DocumentListener, ActionListener, FocusListener {
	/**
	 * @author ArjanDraisma
	 * acWindow implements a custom version of show, tailored to the
	 * amount of autocomplete items that have been found.
	 */
	class AutocompleteWindow extends JPopupMenu {
		private static final long serialVersionUID = 1L;
		public void show(int num) {
			setPreferredSize(new Dimension(acInvoker.getSize().width,
					num*22));
			setLocation(acInvoker.getInputBoxLocationOnScreen().x,
					acInvoker.getInputBoxLocationOnScreen().y
					+ acInvoker.getSize().height);
			pack();
			revalidate();
			repaint();
			if (!isVisible()) {
				setVisible(true);
			}	
		}
	}
	
	class keyHandler implements KeyListener {

		private void highlightselection (int index) {
			for (int i = 0; i < acDropdownBox.getComponentCount(); i++) {
				if (i == index) {
					acDropdownBox.getComponent(i).setBackground(
							UIManager.getColor(
									"MenuItem.selectionBackground"));
					acDropdownBox.getComponent(i).setForeground(
							UIManager.getColor(
									"MenuItem.selectionForeground"));
				}else{
					acDropdownBox.getComponent(i).setBackground(
							UIManager.getColor(
									"MenuItem.background"));
					acDropdownBox.getComponent(i).setForeground(
							UIManager.getColor(
									"MenuItem.foreground"));
				}
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if ((key == KeyEvent.VK_DOWN||key == KeyEvent.VK_UP||key == KeyEvent.VK_ENTER)&&acDropdownBox.isVisible()) {
				e.consume();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			int key = e.getKeyCode();
			int index = acDropdownBox.getSelectionModel().getSelectedIndex();
			if (key == KeyEvent.VK_DOWN&&acDropdownBox.isVisible()) {
				if (index+1>acDropdownBox.getComponentCount()-1) {
					acDropdownBox.getSelectionModel().setSelectedIndex(0);
				}else{
					acDropdownBox.getSelectionModel().setSelectedIndex(index+1);
				}
			}
			if (key == KeyEvent.VK_UP&&acDropdownBox.isVisible()) {
				if (index-1<0) {
					acDropdownBox.getSelectionModel().setSelectedIndex(acDropdownBox.getComponentCount()-1);
				}else{
					acDropdownBox.getSelectionModel().setSelectedIndex(index-1);
				}
			}
			highlightselection(acDropdownBox.getSelectionModel().getSelectedIndex());
			if (key == KeyEvent.VK_ENTER&&acDropdownBox.isVisible()) {
				if (index > -1) {
					ActionEvent ae = new ActionEvent(acDropdownBox.getComponent(index), ActionEvent.ACTION_PERFORMED, ((JMenuItem)acDropdownBox.getComponent(index)).getActionCommand());
					actionPerformed(ae);
					acDropdownBox.setVisible(false);
				}
			}
			if (key == KeyEvent.VK_ESCAPE&&acDropdownBox.isVisible()) {
				acDropdownBox.setVisible(false);
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}
	
	AutocompleteWindow acDropdownBox;
	keyHandler keyHandlerInstance;
	UserInput acInvoker;
	Future<TreeMap<String, String>> futureACList;
	TreeMap<String,String> ACList;
	InputHandler inputHandler;
	
	Autocomplete (Future<TreeMap<String,String>> futureAcKeywordList, InputHandler inputHandler) {
		futureACList = futureAcKeywordList;
		this.inputHandler = inputHandler;
		keyHandlerInstance = new keyHandler();
		acDropdownBox = new AutocompleteWindow();
		acDropdownBox.addFocusListener(this);
		acDropdownBox.setVisible(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String hpo = e.getActionCommand();
		((JTextArea)acDropdownBox.getInvoker()).setText(hpo);
	}
	
	public void add (UserInput input) {
		input.getDocument().addDocumentListener(this);
		input.getInputBox().addFocusListener(this);
		input.getInputBox().addKeyListener(keyHandlerInstance);
	}
	
	public void remove(UserInput input) {
		input.getDocument().removeDocumentListener(this);
		input.getInputBox().removeFocusListener(this);
		input.getInputBox().removeKeyListener(keyHandlerInstance);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (acInvoker == null||!e.getComponent().equals(
				acInvoker.getInputBox())) {
			acInvoker = inputHandler.getUserInputFromComponent(e.getSource());
			acDropdownBox.setInvoker(acInvoker.getInputBox());
		}
	}
	
	@Override
	public void focusLost(FocusEvent arg0) {
	}
	
	// changedupdate is not used on plainDocuments, as used by
	// JTextArea
	@Override
	public void changedUpdate(DocumentEvent e) {}
	
	@Override
	public void insertUpdate (DocumentEvent e) {
		int length;
		String s;
		String hpo;
		Document invokerDocument;
		JMenuItem acListItem;
		int num = 0;
		long timeOut = System.currentTimeMillis()+5000;
		
		invokerDocument = acInvoker.getDocument();
		acInvoker.getInputBox().setComponentPopupMenu(acDropdownBox);
		acDropdownBox.removeAll();
		acDropdownBox.setInvoker(acInvoker.getInputBox());
		length = invokerDocument.getLength();
		if (length > 2) {
			/* Checks if ACList is not null, the DocumentEvent is not
			 * null, or whether the future ACList is done.
			 * If the ACList is not null, the data is already available
			 * for the dropdown menu.
			 * 
			 * If the DocumentEvent is null, this event was fired
			 * manually. This occurs when the dropdown is visible when
			 * the futureAC is about to be ready.
			 * 
			 * If futureACList is done, we can populate ACList.
			 */
			if (ACList != null || e == null || futureACList.isDone()) {
				/* If ACList == null it still needs to be gotten from
				 * the future object
				 */
				if (ACList == null) {
					/* we will wait for ~5 seconds to get the ACList
					 * when this event fires and the future object
					 * is not done. This should only be relevant
					 * when the event is forced to fire just before
					 * the future object is done.
					 */
					while (!futureACList.isDone()) {
						if (timeOut < System.currentTimeMillis()) {
							acDropdownBox.setVisible(false);
							return;
						}
					}
					try {
						// Retrieve the actual ACList
						ACList = futureACList.get();
						//Collections.sort
					}catch (Exception exception) {
						/* Something will have gone horribly wrong
						 * if this exception is ever reached.
						 */
						exception.printStackTrace();
					}
				}
				s = acInvoker.getInputText();
				if (acInvoker.getInputType() == 0) {
					for (String search : ACList.keySet()) {
						if (search.toLowerCase().contains(s.toLowerCase())&&num<10) {
							hpo = ACList.get(search);
							acListItem = new JMenuItem(search+" ("+hpo+")");
							acListItem.setComponentPopupMenu(acDropdownBox);
							acListItem.addActionListener(this);
							acListItem.addKeyListener(keyHandlerInstance);
							acListItem.setActionCommand(hpo);
							acListItem.setEnabled(true);
							acListItem.setOpaque(true);
							acDropdownBox.add(acListItem);
							num++;
						}
					}
					acDropdownBox.show(num);
					acInvoker.getInputBox().requestFocusInWindow();
				}
			}else{
				acDropdownBox.add(new JMenuItem("Please wait..."));
				acDropdownBox.show(3);
				acInvoker.getInputBox().requestFocusInWindow();
			}
		}else{
			acDropdownBox.setVisible(false);
		}
	}

	@Override
	public void removeUpdate (DocumentEvent e) {
		if (e.getLength() == e.getDocument().getLength()) {
			return;
		}else{
			insertUpdate(e);
		}
	}

	public boolean isVisible() {
		return acDropdownBox.isVisible();
	}
}