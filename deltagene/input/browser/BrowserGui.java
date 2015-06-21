package deltagene.input.browser;

import java.awt.Component;

import javax.swing.WindowConstants;

import deltagene.gui.AbstractWindow;

public abstract class BrowserGui extends AbstractWindow
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	BrowserGui (Component callingComponent) 
	{
		super("HPO Browser", callingComponent, 800, 600, true, true, WindowConstants.DISPOSE_ON_CLOSE);
	}
}