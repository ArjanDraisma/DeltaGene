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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileWriter;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.UIManager;


class DeltaGene {
	
	private static Gui dggui;
	private static File hpofile;
	private static File assocfile;
	private static File dir = new File(".\\HPO\\");
	
	public static File getHPOFile() {
		return hpofile;
	}
	
	public static File getAssocFile() {
		return assocfile;
	}
	
	/**
	 * This function returns a string with the contents of an inputstream
	 * 
	 * @param in an inputstream with the text to be copied to a string
	 * @return a string with the complete transcript from the inputstream
	 * @throws IOException
	 */
	public static String convertStreamToString(InputStream in) 
	    throws IOException {
	    BufferedInputStream is = new BufferedInputStream(in);
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    int result = is.read();
	    while(result != -1) {
	      byte b = (byte)result;
	      buffer.write(b);
	      result = is.read();
	    }        
	    in.close();
	    return buffer.toString();
	}
	
	/**
	 * GetLatestHPOFiles gets the latest HPO term list from http://compbio.charite.de/hudson/job/hpo/lastStableBuild/.
	 * It checks for the timestamp of the latest version by reading the associated JSON file retrieved from 
	 * http://compbio.charite.de/hudson/job/hpo/lastStableBuild/api/json. It will store hpo files as .obo files in a
	 * directory named HPO, which will be created if it does not exist in the same folder as the applet is run in.
	 * getLatestHPOFiles will delete .obo files with a timestamp that is lower than the timestamp of the latest file.
	 * It will only delete files in the HPO directory. It does not delete any files other than .obo files.
	 * 
	 * @return true on success, false on failure.
	 */
	
	private static boolean getLatestHPOFiles() {
		try {
			File[] oldhpo;
			String json;
			String buffer;
			URL jsonurl;
			URL  hpourl;
			Pattern _regx;
			Matcher _match;
			BufferedReader in;
			InputStream hpostream;
			
			// We inform the user that the application is downloading the HPO file
			dggui.setUpdateLabelText("Downloading HPO file, please wait...");
			
			// This URL points to the JSON file used to retrieve the timestamp
			jsonurl = new URL("http://compbio.charite.de/"
					+ "hudson/job/hpo/lastStableBuild/api/json");
			
			// convertStreamToString loads the JSON in a string.
			// the JSON file is ~1100 characters long.
			json = convertStreamToString(jsonurl.openStream());
			
			/* 	Since we will be searching for the timestamp in filenames and text,
				we will not be using a Long or Date variable. */
			String timestamp;
			
			// _regx will match the digits that occur between "timestamp": and ','
			_regx = Pattern.compile("(?:timestamp\":(\\d+),)");
			_match = _regx.matcher(json);
			
			// if a match is found we use it as the timestamp.
			if (_match.find()) {
				timestamp = _match.group(1);
			}else{
				/* 	This really should never happen; if the JSON does not exist, an
				 	exception will be thrown earlier.								*/
				return false;
			}
			
			// check if HPO folder exists
			if (!dir.exists()) {
				// If not, Try to create the HPO directory in the applets' folder
				if (!dir.mkdir()) {
					// show error to user in case something goes wrong. Should not happen.
					new Error("Could not make HPO files directory.\n"
							+ "Try launching the application as administrator.", 
							"IO Error",
							JFrame.EXIT_ON_CLOSE);
					return false;
				}
			}
			hpofile = new File(".\\HPO\\"+timestamp+".obo");
			
			// check if HPO file with this timestamp already exists
			if (!hpofile.exists()) {
				/* 
				 * dggui.down contains the number of bytes that have been downloaded
				 * and will be displayed on the applet when it is downloading the files,
				 * to indicate some progress is being made.
				 */
				dggui.down = 0;
				
				// oldhpo will contain the filenames for all files in the HPO directory 
				oldhpo = dir.listFiles();
				
				// This _regx will find any file named <digit>.hpo
				_regx = Pattern.compile("(\\d+.obo)");
				
				/* 
				 * this loop deletes any files with digits that do not equal the current timestamp
				 * digits
				 */
				for (int i = 0; i < oldhpo.length; i++) {
					_match = _regx.matcher(oldhpo[i].getName());
					if (_match.matches()) {
						oldhpo[i].delete();
					}
				}
				
				// Create the file if it does not exist
				hpofile.createNewFile();
				
				/* 
				 * From here, the method will download the HPO number database from
				 * the file pointed to by 'hpourl' and put it in 'hpofile'
				 */
				hpourl = new URL("http://compbio.charite.de/hudson/job/"
						+ "hpo/lastStableBuild/artifact/hp/hp.obo");
				FileWriter out = new FileWriter(hpofile);
				hpostream =	hpourl.openConnection().getInputStream(); 
				in = new BufferedReader(new InputStreamReader(hpostream));
				while ((buffer = in.readLine()) != null) { 
					out.write(buffer+"\n");
					dggui.down += buffer.length();
				}
				out.close();
			}
			return true;
		} catch (IOException e) {
			/* 
			 * if any of the file creation or opening fails, an error will be shown and the 
			 * program will quit.
			 */
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * getLatestAssocFiles is nearly identical to getLatestAssocFiles, except for
	 * the file that it downloads.
	 * 
	 * @return true on success, false on failure
	 */
	private static boolean getLatestAssocFiles() {
		try {
			File[] oldhpo;
			String json;
			String buffer;
			URL jsonurl;
			URL assocurl;
			Pattern _regx;
			Matcher _match;
			BufferedReader in;
			InputStream assocstream;
			String timestamp;
			FileWriter out;
			
			dggui.setUpdateLabelText("Downloading association files. "
					+ "Please wait...");
			
			jsonurl = new URL("http://compbio.charite.de/hudson/"
					+ "job/hpo.annotations.monthly/lastStableBuild/api/json");
			json = convertStreamToString(jsonurl.openStream());
			
			_regx = Pattern.compile("(?:timestamp\":(\\d+),)");
			_match = _regx.matcher(json);
			if (_match.find()) {
				timestamp = _match.group(1);
			}else{
				return false;
			}
			if (!dir.exists()) {
				if (!dir.mkdir()) {
					new Error("Could not make HPO files directory.\n"
							+ "Try launching the application as administrator.", 
							"IO Error",
							JFrame.EXIT_ON_CLOSE);
					return false;
				}
			}
			assocfile = new File(".\\HPO\\"+timestamp+".txt");
			if (!assocfile.exists()) {
				dggui.down = 0;
				oldhpo = dir.listFiles();
				_regx = Pattern.compile("(\\d+.txt)");
				for (int i = 0; i < oldhpo.length; i++) {
					_match = _regx.matcher(oldhpo[i].getName());
					if (_match.matches()) {
						oldhpo[i].delete();
					}
				}
				assocfile.createNewFile();
				assocurl = new URL("http://compbio.charite.de/hudson/job/"
						+ "hpo.annotations.monthly/lastStableBuild/artifact/"
						+ "annotation/ALL_SOURCES_ALL_FREQUENCIES_"
						+ "diseases_to_genes_to_phenotypes.txt");
				out = new FileWriter(assocfile);
				assocstream = assocurl.openConnection().getInputStream(); 
				in = new BufferedReader(
						new InputStreamReader(assocstream));
				while ((buffer = in.readLine()) != null) { 
					out.write(buffer+"\n");
					dggui.down += buffer.length();
				}
				dggui.downloading = false;
				out.close();
			}
			return true;
		} catch (IOException e) {
			System.out.println(e.getStackTrace());
			return false;
		}
	}
	
	public static void main(String[] args) {
		try {
			// We will try to use the system's native look and feel for UI
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Any updates to the UI must happen on the event dispatching thread.
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					dggui = new Gui();
					dggui.createAndShowGUI();
				}
			});
			
			// The applet will try to use an older version of the association file if it is available.
			if (!getLatestHPOFiles()) {
				getLastHPOFile();
				new Error("HPO file could not be dowloaded! Trying to use older version...",
						"Download error",
						JFrame.DISPOSE_ON_CLOSE);
			}if (!getLatestAssocFiles()) {
				getLastAssocFile();
				new Error("Association file could not be dowloaded! Trying to use older version", 
						"Download error",
						JFrame.DISPOSE_ON_CLOSE);
			}
			dggui.updateGUI(hpofile, assocfile);
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	/** getLastAssocFile will try to load the last assoc file that is still in
	 * the /HPO/ directory.
	 */
	private static void getLastAssocFile() {
		// TODO Auto-generated method stub
		
	}
	/** getLastAssocFile will try to load the last HPO (.obo) file that is still 
	 * in the /HPO/ directory.
	 */
	private static void getLastHPOFile() {
		// TODO Auto-generated method stub
		
	}
}
