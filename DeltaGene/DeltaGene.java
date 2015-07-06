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
 * your favorite website mocking bad code.
 */

package DeltaGene;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * @author ArjanDraisma
 * The main class for deltagene. Does little else besides set the 
 * look and feel of swing, parse command line arguments and create the gui
 */
class DeltaGene {
	
	public static Gui guiInstance;				// instance of the Gui class
	// these constants are used throughout multiple classes
	public final static int INPUTH = 126;	// Height of the input box
	public final static int INFOH = 92;		// Height of the info box
	public final static int INPUTPAD = 10;	// padding between the inputbox and infobox
	
	public final static ExecutorService THREADPOOL = Executors.newFixedThreadPool(5);
	// in case of command line mode, we do not enable the gui
	
	public static void main(String[] args) {
		try {
			// We will try to use the system's native look and feel for UI
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			String a = null;
			String b = null;
			String e = null;
			String fh = null;
			String fa = null;
			boolean enableGui = true;
			boolean verbose = false;
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.equals("-help")||arg.equals("-h")) {
					System.out.println("DeltaGene is a simple program that"
							+ "compares a list of genes against a multitude"
							+ "of lists. Includes a connection to the Human"
							+ "Phenotype Ontology database.\n\n"
							+ "Usage: java -jar DeltaGene -a [\"input\"] "
							+ "-b [\"input\",\"input\"] "
							+ "-e <output path> -fh <hpo file path> "
							+ "-fa <association file path> -nogui -verbose\n\n"
							+ "input can be:"
							+ "HPO number(s): [HP:0000021,HP:0000012]\n"
							+ "Plain number(s): [12] for HP:0000012\n"
							+ "(list of) gene(s): [TTN,DSP]\n\n"
							+ "Options:\n"
							+ "-a: the A group input (singular) "
							+ "-export: Export a comma-seperated file with"
							+ "results to the specified location. implies "
							+ "-nogui\n"
							+ "-h, -help: Display this help text\n"
							+ "-nogui: disble gui display\n"
							+ "-op: operator. Can be DEFAULT,AND,NOT,XOR and LIST "
							+ "Note: List only handles the -b input."
							+ "-verbose: gives verbose output of the process");
					return;
				}if (arg.equals("-a")) {
					
				}if (arg.equals("-b")) {
					// results with b input
				}if (arg.equals("-e")) {
					// export to arg[i+1]
				}if (arg.equals("-fh")) {
					
				}if (arg.equals("-fa")) {
					
				}if (arg.equals("-nogui")) {
					enableGui = false;
				}if (arg.equals("-verbose")) {
					verbose = true;
				}
			}
			
			if (enableGui) {
				// Any updates to the UI must happen on the event dispatching thread
				javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						guiInstance = new Gui();
					}
				});
			}else{
				// TODO print command line output, export to file
				Input dgi = new Input(a, b, e, fh, fa, enableGui, verbose, null);
			}
		}catch (Exception e) {
			e.printStackTrace();
			/* Something went terribly wrong if this ever occurs. 
			 * Could happen when something interrupts invokeAndWait above,
			 * is not expected to ever happen.
			 */
			new Error(null, Error.UNDEF_ERROR,Error.UNDEF_ERROR_T, WindowConstants.EXIT_ON_CLOSE);
		}
	}
}
