package deltagene.utils;

import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import deltagene.gui.AbstractWindow;
import deltagene.main.DeltaGene;



/**
 * @author ArjanDraisma
 * The HelpClass manages and shows the help window, with the help pages
 * in the /Help/ directory.
 */
public class Help extends AbstractWindow implements HyperlinkListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JEditorPane helpContent = new JEditorPane();	// The content EditorPane will contain the help HTML
	JScrollPane helpContentScrollPane;							// This is the scrollpane for the 'content' editorpane
	
	public Help(JFrame window, String page){
		super("Help", window, 800, 600, false, true);
		helpContentScrollPane = new JScrollPane(helpContent);	// this creates the JScrollPane and sets the content EditorPane as its viewing pane
		helpContent.setEditable(false);						// we do not want the user to be able to edit the EditorPane
		helpContent.addHyperlinkListener(this);				// this listens for click on hyperlinks, which are used as navigation in the help pages
		getContentPanel().add(helpContentScrollPane);		// this adds the jscrollpane, which 'contains' the EditorPane to the help JFrame
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		show(page);
	}
	
	/**
	 * load a new page if the user clicks a link in the help window.
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				helpContent.setPage(e.getURL());
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
			helpContent.setPage(u);		// this sets the html file as the editorpane's content
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