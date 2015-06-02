package deltagene.io;

import deltagene.utils.Error;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.WindowConstants;

public class HPOFileHandler {
	private File hpoFile;
	private File associationFile;
	private File directory = new File(".\\HPO\\");
	private int downloaded = 0;
	public final static int STATE_FAIL = -1;
	public final static int STATE_INIT = 0;
	public final static int STATE_DOWNLOAD_HPO = 1;
	public final static int STATE_DOWNLOAD_ASSOC = 2;
	public final static int STATE_READY = 3;
	private int state;
	
	
	public HPOFileHandler() {
		state = STATE_INIT;
	}
	
	public void LoadFiles() {
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
			
			// This URL points to the JSON file used to retrieve the timestamp
			jsonurl = new URL("http://compbio.charite.de/"
					+ "hudson/job/hpo/lastStableBuild/api/json");
			
			// convertStreamToString loads the JSON in a string.
			// the JSON file is ~1100 characters long.
			json = convertStreamToString(jsonurl.openStream());
			int tsindex = json.indexOf("timestamp\":")+"timestamp\":".length();
			timestamp = json.substring(tsindex,  json.indexOf(",\"url"));
			
			// check if HPO folder exists
			if (!directory.exists()) {
				// If not, Try to create the HPO directory in the applets' folder
				if (!directory.mkdir()) {
					// show error to user in case something goes wrong. Should not happen.
					new Error("Could not make HPO files directory.\n"
							+ "Try launching the application as administrator.", 
							"IO Error",
							WindowConstants.EXIT_ON_CLOSE);
					return;
				}
			}
			hpoFile = new File(".\\HPO\\override.obo");
			
			// check if override HPO file exists
			if (!hpoFile.exists()) {
				hpoFile = new File(".\\HPO\\"+timestamp+".obo");
				// check if HPO file with this timestamp already exists
				if (!hpoFile.exists()) {
					state = STATE_DOWNLOAD_HPO;
					// oldhpo will contain the filenames for all files in the HPO directory 
					oldfiles = directory.listFiles();
					
					for (File file : oldfiles) {
						if (file.getName().endsWith(".obo")) 
							if (!file.getName().startsWith(timestamp))
								file.delete();
					}
					
					// Create the file if it does not exist
					hpoFile.createNewFile();
					
					/* 
					 * From here, the method will download the HPO number database from
					 * the file pointed to by 'hpourl' and put it in 'hpofile'
					 */
					fileurl = new URL("http://compbio.charite.de/hudson/job/"
							+ "hpo/lastStableBuild/artifact/hp/hp.obo");
					out = new FileWriter(hpoFile);
					istream =	fileurl.openConnection().getInputStream(); 
					in = new BufferedReader(new InputStreamReader(istream));
					while ((buffer = in.readLine()) != null) { 
						out.write(buffer+"\n");
						downloaded += buffer.length();
					}
					out.close();
				}
			}
			stop = System.currentTimeMillis();
			time = stop - start;
			System.out.println("(Down)loading the HPO file took "+time+" millis");
			
			start = System.currentTimeMillis();
			
			jsonurl = new URL("http://compbio.charite.de/hudson/"
					+ "job/hpo.annotations.monthly/lastStableBuild/api/json");
			json = convertStreamToString(jsonurl.openStream());
			tsindex = json.indexOf("timestamp\":")+"timestamp\":".length();
			timestamp = json.substring(tsindex, json.indexOf(",\"url"));
			
			associationFile = new File(".\\HPO\\override.assoc");
			
			// check if override association file exists
			if (!associationFile.exists()) {
				// check if association file with this timestamp already exists
				associationFile = new File(".\\HPO\\"+timestamp+".assoc");
				if (!associationFile.exists()) {
					state = STATE_DOWNLOAD_ASSOC;
					/* 
					 * dggui.down contains the number of bytes that have been downloaded
					 * and will be displayed on the applet when it is downloading the files,
					 * to indicate some progress is being made.
					 */
					downloaded = 0;
					
					// oldfiles will contain the filenames for all files in the HPO directory 
					oldfiles = directory.listFiles();
					
					for (File file : oldfiles) {
						if (file.getName().endsWith(".assoc")) 
							if (!file.getName().startsWith(timestamp))
								file.delete();
					}
					
					// Create the file if it does not exist
	
					associationFile.createNewFile();
					fileurl = new URL("http://compbio.charite.de/hudson/job/"
							+ "hpo.annotations.monthly/lastStableBuild/artifact/"
							+ "annotation/ALL_SOURCES_ALL_FREQUENCIES_"
							+ "diseases_to_genes_to_phenotypes.txt");
					out = new FileWriter(associationFile);
					istream = fileurl.openConnection().getInputStream(); 
					in = new BufferedReader(
							new InputStreamReader(istream));
					while ((buffer = in.readLine()) != null) { 
						out.write(buffer+"\n");
						downloaded += buffer.length();
					}
					out.close();
				}
				stop = System.currentTimeMillis();
				time = stop - start;
				System.out.println("(Down)loading association files took "+time+"millis");
			}
			state = STATE_READY;
			return;
		}catch (IOException e){
			state = STATE_FAIL;
			e.printStackTrace();
			new Error(Error.UNDEF_ERROR, Error.UNDEF_ERROR_T,
					WindowConstants.EXIT_ON_CLOSE);
		}
	}
	
	/**
	 * This function returns a string with the contents of an inputstream
	 * 
	 * @param in an inputstream with the text to be copied to a string
	 * @return a string with the complete transcript from the inputstream
	 * @throws IOException
	 */
	public String convertStreamToString(InputStream in) 
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
	
	public File getAssocFile() {
		return associationFile;
	}
	
	public int getDown() {
		return downloaded;
	}
	
	public File getHPOFile() {
		return hpoFile;
	}
	
	/**
	 * returns the state of the HPOFILES object as an int
	 * @return
	 * <OL start=-1>
	 * <LI>FAIL</LI>
	 * <LI>INIT</LI>
	 * <LI>DOWNLOAD_HPO</LI>
	 * <LI>DOWNLOAD_ASSOC</LI>
	 * <LI>READY</LI>
	 * </OL>
	 * @return
	 */
	public int getState() {
		return state;
	}
	
	public boolean isReady () {
		return state == STATE_READY;
	}
}