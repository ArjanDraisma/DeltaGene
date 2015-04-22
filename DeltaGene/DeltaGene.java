/*
 * Main class for DeltaGene
 * 
 * V1.0
 * 
 * 10-3-2015
 * 
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 * 
 * Of course, this also means that you are free to post any snippets on
 * your favorite website mocking bad code. I know I am not a professional
 * programmer. Know that this program was written with the best intentions.
 */

package DeltaGene;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.UIManager;
import javax.swing.WindowConstants;


class DeltaGene {
	
	public static Gui dggui;
	public final static int INPUTH = 126;	// Height of the input box
	public final static int INFOH = 92;		// Height of the info box
	public final static int INPUTPAD = 10;	// padding between the inputbox and infobox
	public final static ExecutorService pool = Executors.newFixedThreadPool(5);
	
	public static void main(String[] args) {
		try {
			// We will try to use the system's native look and feel for UI
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Any updates to the UI must happen on the event dispatching thread.
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					dggui = new Gui();
					//dggui.createAndShowGUI();
					//dggui.updateGUI();
				}
			});
		}catch (Exception e) {
			e.printStackTrace();
			new Error("Unspecified error! Restart the program as administrator and try again.\n"
					+ "If this problem persists, report an issue with the following text:\n\n"
					+ "Stack trace:\n"
					+e.getStackTrace(),"Critical error!",WindowConstants.EXIT_ON_CLOSE);
		}
	}
}
