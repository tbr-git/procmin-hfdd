package org.processmining.emdapplications.emdconceptdrift.headless;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.json.JSONObject;
import org.processmining.emdapplications.emdconceptdrift.algorithm.SWEMDComp;
import org.processmining.emdapplications.emdconceptdrift.config.EMDConceptDriftParameters;
import org.processmining.emdapplications.emdconceptdrift.config.EMDTraceCompParamBuilder;
import org.processmining.emdapplications.emdconceptdrift.config.ParameterBuilder;
import org.processmining.emdapplications.emdconceptdrift.dummies.PluginContextFactory;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.GroundDistances;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.LVSStatefullWithEdit;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TWDStateful;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TimeBinnedWeightedLevenshteinStateful;
import org.processmining.emdapplications.emdconceptdrift.helperclasses.MultiDimSlidingEMDOutput;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.EdgeCalculatorType;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TimeBinType;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.xeslite.parser.XesLiteXmlParser;

import com.google.common.collect.Streams;
import com.google.gson.Gson;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;


public class Main {
	
	private final static Logger logger = LogManager.getLogger( Main.class );

	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newFor("ENDConceptDrift Headless").build() .description("Configure concept drift detection parameters.");
		parser.addArgument("--logfile").type(String.class).help("Path to log file");
		parser.addArgument("--outfile").type(String.class).setDefault("").help("Path to log file");

		// Window specification
		ArgumentGroup groupWindow = parser.addArgumentGroup("Window Spec");
		groupWindow.addArgument("--w_sizes").type(Integer.class).nargs("+").setDefault(200).help("Window sizes");
		groupWindow.addArgument("--w_strides").type(Integer.class).nargs("+").setDefault(50).help("Window stride sizes");

		parser.addArgument("--distance").choices("LVS", "TBWLVS", "TWED").setDefault("LVS").help("Ground distance");
		// Arguments for TBWLVS
		ArgumentGroup groupTBWLVS = parser.addArgumentGroup("TBWLVS config");
		groupTBWLVS.addArgument("--timeBinType").type(TimeBinType.class).choices(TimeBinType.DURATION, TimeBinType.SOJOURN);
		groupTBWLVS.addArgument("--binType").type(EdgeCalculatorType.class).choices(EdgeCalculatorType.PERCENTILE, EdgeCalculatorType.KMEANS);
		// Percentile based clustering arguments
		groupTBWLVS.addArgument("--binQuantiles").type(Integer.class).nargs("+").setDefault(0.33, 0.66).help("Time quantile bin boundaries");
		// K-means++ based clustering arguments
		groupTBWLVS.addArgument("--k").type(Integer.class).setDefault(3).help("k-means++ k");
		// Weighted LVS cost factors
		groupTBWLVS.addArgument("--considerTime").action(Arguments.storeTrue());
		groupTBWLVS.addArgument("--costR").type(Double.class).setDefault(1.0).help("Cost for renaming for TBWLVS");
		groupTBWLVS.addArgument("--costID").type(Double.class).setDefault(1.0).help("Cost for insertion/deletion for TBWLVS");
		// Arguments for TWED
		ArgumentGroup groupTWED = parser.addArgumentGroup("TWED config");
		groupTWED.addArgument("--nu").type(Double.class).setDefault(0.1).help("Nu (TWED)");
		groupTWED.addArgument("--lambda").type(Double.class).setDefault(1.0).help("Lambda (TWED)");
		
		Namespace argsParsed = parser.parseArgsOrFail(args);
		
		// Read the log
		XLog log = readLog((String) argsParsed.get("logfile"));
		if(log == null) {
			logger.error("Could not read log. Terminating program.");
			return;
		}

		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;	
		// Create parameters
		ParameterBuilder builder = new ParameterBuilder();
		EMDTraceCompParamBuilder paramBuilderTrComp = builder.getTraceComparisonParamBuilder();
		paramBuilderTrComp.setClassifier(classifier);
		switch((String) argsParsed.get("distance")) {
		case "LVS":
			paramBuilderTrComp.setDistance(GroundDistances.LEVENSTHEIN);
			paramBuilderTrComp.setDistanceCalculator(new LVSStatefullWithEdit());
			break;
		case "TBWLVS":
			paramBuilderTrComp.setDistance(GroundDistances.TIMEBINNEDLVS);
			paramBuilderTrComp.setTimeBinType(argsParsed.get("timeBinType"));
			EdgeCalculatorType edgeCalcType = argsParsed.get("binType");
			int nbrBins = 0;
			switch(edgeCalcType) {
			case KMEANS:
				paramBuilderTrComp.setNbrClusters(argsParsed.getInt("k"));
				nbrBins = argsParsed.getInt("k");
				break;
			case PERCENTILE:
				@SuppressWarnings("unchecked") int[] binQuantiles = ((List<Integer>) argsParsed.get("binQuantiles")).stream().mapToInt(i->i).toArray();
				nbrBins = binQuantiles.length;
				paramBuilderTrComp.setBinQuantiles(binQuantiles);
				break;
			}
			paramBuilderTrComp.setDistanceCalculator(new TimeBinnedWeightedLevenshteinStateful(nbrBins, argsParsed.get("costR"), argsParsed.get("costID"), 
					argsParsed.get("considerTime")));
			break;
		case "TWED":
			paramBuilderTrComp.setDistance(GroundDistances.TWED);
			paramBuilderTrComp.setDistance(GroundDistances.TIMEBINNEDLVS);
			paramBuilderTrComp.setDistanceCalculator(new TWDStateful(argsParsed.get("nu"), argsParsed.get("lambda")));
			break;
		default:
			logger.error("No ditance specified. Terminating...");
			
		}
		
		Streams.forEachPair(argsParsed.getList("w_sizes").stream().mapToInt(i -> (Integer) i).mapToObj(i -> (Integer) i), 
				argsParsed.getList("w_strides").stream().mapToInt(i -> (Integer) i).mapToObj(i -> (Integer) i),
				(w_size, w_stride) -> builder.addWindow(w_size, w_stride));
		
		EMDConceptDriftParameters para = builder.build();
		
		// Setup Dummy context
		PluginContextFactory factory = new PluginContextFactory();
		PluginContext context = factory.getContext();
		ProMCanceller canceller = new ProMCanceller() {
			
			@Override
			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}
		};
		
		MultiDimSlidingEMDOutput res = SWEMDComp.multiDimSlidingEMDTraceDistrComp(context, log, para, canceller);
		saveResult(argsParsed.getString("outfile"), argsParsed, res);
	}
	
	
	public static XLog readLog(String pathLog) {
		File initialFile = new File(pathLog);
		InputStream inputStream = null;
		XLog log = null;
		XesLiteXmlParser parserLog = new XesLiteXmlParser(true);
		try {
			inputStream = new FileInputStream(initialFile);
			List<XLog> parsedLogs = null;
			try {
				parsedLogs = parserLog.parse(inputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (parsedLogs.size() > 0) {
				log = parsedLogs.get(0);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return log;
	}
	
	public static void saveResult(String path, Namespace args, MultiDimSlidingEMDOutput res) {
		if(path != "") {
			JSONObject jo = new JSONObject();
			jo.put("Config", new JSONObject(new Gson().toJson(args)));
			jo.put("Result", new JSONObject(res.toJson()));
			Writer fileWriter = null;
			try {
				fileWriter = new FileWriter(path);
			jo.write(fileWriter);
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if(fileWriter != null) {
					try {
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
//	private static String namespace2json(Namespace args) {
//	}

}
