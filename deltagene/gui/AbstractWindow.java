package deltagene.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * General gui class.
 * @author ArjanDraisma
 */
public abstract class AbstractWindow extends JFrame
{
	private static final long serialVersionUID = 1L;
	private Container rootContainer;
	private JPanel contentPanel;
	private JPanel controlPanel;
	private JScrollPane contentPanelScrollPane;
	private GridBagLayout mgr;
	
	public AbstractWindow(String title, Component callingComponent, int width, int height, boolean controls, boolean show, int operation)
	{
		super(title);
		
		rootContainer = new Container();
		mgr = new GridBagLayout();
		
		rootContainer.setLayout(mgr);
		GridBagConstraints c = new GridBagConstraints();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(width,height));
		
		setContentPane(rootContainer);
		
		setDefaultCloseOperation(operation);
		
		if (controls)
		{
			controlPanel = new JPanel();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0; c.weighty = 0.02;
			getContentPane().add(controlPanel, c);
		}	
		
		contentPanel = new JPanel();
		contentPanelScrollPane = new JScrollPane(contentPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1; c.weighty = 0.98;
		getContentPane().add(contentPanelScrollPane, c);
		
		pack();
		setLocationRelativeTo(callingComponent);
		this.setVisible(show);
	}
	
	public JPanel getContentPanel()
	{
		return contentPanel;
	}
	
	public JPanel getControlPanel()
	{
		return controlPanel;
	}
}