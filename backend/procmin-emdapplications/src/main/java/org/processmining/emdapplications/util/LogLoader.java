package org.processmining.emdapplications.util;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.xeslite.parser.XesLiteXmlParser;

public class LogLoader {
	private final static Logger logger = LogManager.getLogger( LogLoader.class );
	
	public static XLog loadLog(String pathLog) {
		logger.info("Loading log: {} ...", pathLog);
		File logFile = new File(pathLog);

		List<XLog> parsedLogs = null;
		XesLiteXmlParser parserLog = new XesLiteXmlParser(true);
		try {
			parsedLogs = parserLog.parse(logFile);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to load log");
			return null;
		}
		logger.info("Done loading log.");
		return parsedLogs.get(0);
	}
}
