package deltagene.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import deltagene.utils.DeltaGeneConstants;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
	private GridBagLayout mgr;
	
	public AbstractWindow(String title, Component callingComponent, int width, int height, boolean controls, boolean show)
	{
		super(title);
		
		rootContainer = new Container();
		mgr = new GridBagLayout();
		
		rootContainer.setLayout(mgr);
		GridBagConstraints c = new GridBagConstraints();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(width,height));
		
		setContentPane(rootContainer);
		
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
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1; c.weighty = 0.98;
		getContentPane().add(contentPanel, c);
		
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