package DeltaGene;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Error implements ActionListener{
	private JFrame errframe;		// This is the error window
	
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
		case JFrame.EXIT_ON_CLOSE:
			errbtn.setActionCommand("errexit");
			break;
		case JFrame.DO_NOTHING_ON_CLOSE:
			errbtn.setActionCommand("nothing");
			break;
		case JFrame.DISPOSE_ON_CLOSE:
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
