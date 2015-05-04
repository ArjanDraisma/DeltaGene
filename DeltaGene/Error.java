package DeltaGene;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * @author ArjanDraisma
 * The error class simply creates a window with a 
 * title, description of error and OK button.
 */
public class Error implements ActionListener{
	private JFrame errframe;		// This is the error window
	
	/**
	 * Shows an error window with the specified error message, title
	 * and the consequence of the user pressing the OK button.
	 * @param errmsg the message of the error
	 * @param errtitle the error title
	 * @param consequence from WindowConstants
	 */
	Error(String errmsg, String errtitle, int consequence) {
		errframe = new JFrame(errtitle);
		Container errcnt = new Container();
		JTextArea errtxt = new JTextArea();
		Button errbtn = new Button ("OK");
		errframe.setContentPane(errcnt);
		errframe.setPreferredSize(new Dimension(500, 250));
		errframe.setDefaultCloseOperation(consequence);
		errcnt.setLayout(new BoxLayout(errcnt, BoxLayout.PAGE_AXIS));
		errtxt.setText(errmsg);
		errtxt.setEditable(false);
		errtxt.setPreferredSize(new Dimension(300,50));
		errtxt.setBackground(errframe.getContentPane().getBackground());
		errtxt.setLineWrap(true);
		errtxt.setWrapStyleWord(true);
		errframe.getContentPane().add(errtxt);
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
		errframe.setVisible(true);;
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
