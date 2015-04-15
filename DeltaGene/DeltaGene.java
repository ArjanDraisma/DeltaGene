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
	
	private static boolean getLatestFiles() {
		try {
			File[] oldfiles;
			String json;
			String buffer;
			URL jsonurl;
			URL fileurl;
			BufferedReader in;
			InputStream istream;
			FileWriter out;
			/* 	Since we will be searching for the timestamp in filenames and text,
			we will not be using a Long or Date variable. */
			String timestamp;
			long start, stop, time;
			
			start = System.currentTimeMillis();
			// We inform the user that the application is downloading the HPO file
			dggui.setUpdateLabelText("Downloading HPO file, please wait...");
			
			// This URL points to the JSON file used to retrieve the timestamp
			jsonurl = new URL("http://compbio.charite.de/"
					+ "hudson/job/hpo/lastStableBuild/api/json");
			
			// convertStreamToString loads the JSON in a string.
			// the JSON file is ~1100 characters long.
			json = convertStreamToString(jsonurl.openStream());
			int tsindex = json.indexOf("timestamp\":")+"timestamp\":".length();
			timestamp = json.substring(tsindex,  json.indexOf(",\"url"));
			
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
				oldfiles = dir.listFiles();
				
				for (File file : oldfiles) {
					if (file.getName().endsWith(".obo")) 
						if (!file.getName().startsWith(timestamp))
							file.delete();
				}
				
				// Create the file if it does not exist
				hpofile.createNewFile();
				
				/* 
				 * From here, the method will download the HPO number database from
				 * the file pointed to by 'hpourl' and put it in 'hpofile'
				 */
				fileurl = new URL("http://compbio.charite.de/hudson/job/"
						+ "hpo/lastStableBuild/artifact/hp/hp.obo");
				out = new FileWriter(hpofile);
				istream =	fileurl.openConnection().getInputStream(); 
				in = new BufferedReader(new InputStreamReader(istream));
				while ((buffer = in.readLine()) != null) { 
					out.write(buffer+"\n");
					dggui.down += buffer.length();
				}
				out.close();
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			System.out.println("(Down)loading the HPO file took "+time+" millis");
			
			start = System.currentTimeMillis();
			dggui.setUpdateLabelText("Downloading association files. "
					+ "Please wait...");
			
			jsonurl = new URL("http://compbio.charite.de/hudson/"
					+ "job/hpo.annotations.monthly/lastStableBuild/api/json");
			json = convertStreamToString(jsonurl.openStream());
			tsindex = json.indexOf("timestamp\":")+"timestamp\":".length();
			timestamp = json.substring(tsindex, json.indexOf(",\"url"));
			
			assocfile = new File(".\\HPO\\"+timestamp+".assoc");
			
			// check if HPO file with this timestamp already exists
			if (!assocfile.exists()) {
				/* 
				 * dggui.down contains the number of bytes that have been downloaded
				 * and will be displayed on the applet when it is downloading the files,
				 * to indicate some progress is being made.
				 */
				dggui.down = 0;
				
				// oldfiles will contain the filenames for all files in the HPO directory 
				oldfiles = dir.listFiles();
				
				for (File file : oldfiles) {
					if (file.getName().endsWith(".obo")) 
						if (!file.getName().startsWith(timestamp))
							file.delete();
				}
				
				// Create the file if it does not exist

				assocfile.createNewFile();
				fileurl = new URL("http://compbio.charite.de/hudson/job/"
						+ "hpo.annotations.monthly/lastStableBuild/artifact/"
						+ "annotation/ALL_SOURCES_ALL_FREQUENCIES_"
						+ "diseases_to_genes_to_phenotypes.txt");
				out = new FileWriter(assocfile);
				istream = fileurl.openConnection().getInputStream(); 
				in = new BufferedReader(
						new InputStreamReader(istream));
				while ((buffer = in.readLine()) != null) { 
					out.write(buffer+"\n");
					dggui.down += buffer.length();
				}
				dggui.downloading = false;
				out.close();
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			System.out.println("(Down)loading association files took "+time+"millis");
			return true;	
		}catch (IOException e){
			e.printStackTrace();
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
			getLatestFiles();
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
