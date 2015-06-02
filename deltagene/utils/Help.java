package deltagene.utils;

import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import deltagene.main.DeltaGene;



/**
 * @author ArjanDraisma
 * The HelpClass manages and shows the help window, with the help pages
 * in the /Help/ directory.
 */
public class Help implements HyperlinkListener {
	JFrame helpFrame = new JFrame("Help"); 		// This JFrame is the help window JFrame 
	JEditorPane content = new JEditorPane();	// The content EditorPane will contain the help HTML
	JScrollPane contentScrollPane;							// This is the scrollpane for the 'content' editorpane
	
	public Help(String page){
		//helpFrame.setPreferredSize(mainWindow.getSize()); 	// this sets the size of the help window JFrame, which will be the same as the main window
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