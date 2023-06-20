package org.processmining.emdapplications.emdconceptdrift.io;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class JSONWriter {
	/**
	 * Logger
	 */
	private final static Logger logger = LogManager.getLogger( JSONWriter.class );

	public static void write_json(String filename, JSONObject jsonObject) {
		FileWriter file = null;
        try {
 
            // Constructs a FileWriter given a file name, using the platform's default charset
            file = new FileWriter(filename);
            file.write(jsonObject.toString());
            logger.info("Sucessfully written JSON Object to file: " + filename);
        } catch (IOException e) {
        	logger.error("Could not write JSON Object to file: " + filename);
            e.printStackTrace();
 
        } finally {
            try {
            	if(file != null) {
					file.flush();
					file.close();
            	}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
}