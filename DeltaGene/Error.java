package DeltaGene;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * @author ArjanDraisma
 * The error class simply creates a window with a 
 * title, description of error and OK button.
 */
public class Error implements ActionListener{
	private JFrame errframe;		// This is the error window
	
	public final static String UNDEF_ERROR = "An undefined error has occured.\n"
			+ "Try running the program as an administrator and try again.";
	public final static String IO_ERROR = "An error has occured when loading"
			+ "or saving a file.\nPlease try running the program as "
			+ "an administrator and try again.";
	public final static String CRIT_ERROR = "A critical error has occured.\n"
			+ "The application will have to exit. Please try running the "
			+ "application again.\n\nIf possible, contact a developer with "
			+ "the following information: \n\n";
	public final static String DEV_ERROR = "A critical error has occured.\n"
			+ "The application will have to exit. Please try running the "
			+ "application again.\n\n"
			+ "Furthermore, a developer has forgotten to properly "
			+ "handle this error; No further information is available.";
	public final static String UNDEF_ERROR_T = "Undefined error";
	public final static String IO_ERROR_T = "Input error";
	public final static String CRIT_ERROR_T = "Critical error";
	public final static String DEV_ERROR_T = "Critical / Developer error";
	public final static String INPUT_ERROR_T = "Input error";
	
	private void show(String errmsg, String errtitle, int consequence) {
		errframe = new JFrame(errtitle);
		Container errcnt = new Container();
		JTextArea errtxt = new JTextArea();
		JScrollPane errtxtsp = new JScrollPane(errtxt);
		Button errbtn = new Button ("OK");
		
		
		errframe.setContentPane(errcnt);
		errframe.setPreferredSize(new Dimension(500, 250));
		errframe.setDefaultCloseOperation(consequence);
		errcnt.setLayout(new BoxLayout(errcnt, BoxLayout.PAGE_AXIS));
		errtxt.setText(errmsg);
		errtxt.setEditable(false);
		errtxt.setBackground(errframe.getContentPane().getBackground());
		errtxt.setLineWrap(true);
		errtxt.setWrapStyleWord(true);
		//errtxtsp.setPreferredSize(new Dimension(300,50));
		errtxtsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		errtxtsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		errframe.getContentPane().add(errtxtsp);
		errbtn.setPreferredSize(new Dimension(80, 30));
		errbtn.addActionListener(this);
		switch (consequence) {
		case WindowConstants.EXIT_ON_CLOSE:
			errbtn.setActionCommand("errexit");
			break;
		case WindowConstants.DO_NOTHING_ON_CLOSE:
			errbtn.setActionCommand("nothing");
			break;
		case WindowConstants.DISPOSE_ON_CLOSE:
			errbtn.setActionCommand("errdispose");
			break;
		}
		errframe.getContentPane().add(errbtn);
		errframe.setResizable(false);
		errframe.pack();
		errframe.setLocationRelativeTo(null);
		errframe.setVisible(true);
	}
	
	Error(String errmsg, String errtitle, int consequence, Exception e) {
		if (errmsg == CRIT_ERROR && errtitle == CRIT_ERROR_T) {
			errmsg+=e.getStackTrace();
		}
		show(errmsg, errtitle, consequence);
	}
	
	/**
	 * Shows an error window with the specified error message, title
	 * and the consequence of the user pressing the OK button.
	 * @param errmsg the message of the error
	 * @param errtitle the error title
	 * @param consequence from WindowConstants
	 */
	Error(String errmsg, String errtitle, int consequence) {
		if (errmsg == CRIT_ERROR || errtitle == CRIT_ERROR_T) {
			errmsg = DEV_ERROR;
			errtitle = DEV_ERROR_T;
		}
		show(errmsg, errtitle, consequence);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("errdispose")) {
			errframe.dispose();
		}if (e.getActionCommand().equals("errexit")) {
			System.exit(0);
		}
		
	}
}
