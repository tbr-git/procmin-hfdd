package org.processmining.emdapplications.emdconceptdrift.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.emdapplications.emdconceptdrift.config.EMDConceptDriftParameters;
import org.processmining.emdapplications.emdconceptdrift.config.WindowParameter;
import org.processmining.emdapplications.emdconceptdrift.grounddistances.TraceDescDistCalculator;
import org.processmining.emdapplications.emdconceptdrift.helperclasses.MultiDimSlidingEMDOutput;
import org.processmining.emdapplications.emdconceptdrift.helperclasses.SlidingEMDOutput;
import org.processmining.emdapplications.emdconceptdrift.language.FreqBasedStochasticLanguageImpl;
import org.processmining.emdapplications.emdconceptdrift.language.SlidingStochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.StochasticLanguage;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstractTraceDescriptorFactory;
import org.processmining.emdapplications.emdconceptdrift.solver.akp.core.AKPFactoryArraySimple;
import org.processmining.emdapplications.emdconceptdrift.solver.akp.core.AKPSolverArraySimple;
import org.processmining.emdapplications.emdconceptdrift.solver.lpsolve.EMDWithLPSolve;
import org.processmining.emdapplications.emdconceptdrift.util.LogSorter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;

import com.google.common.base.Stopwatch;

public class SWEMDComp {
	private final static Logger logger = LogManager.getLogger( SWEMDComp.class );
	private static final Marker MARKER_EMD = MarkerManager.getMarker("EMD");
	
	
	private static final Stopwatch time_solver = Stopwatch.createUnstarted();

	public static MultiDimSlidingEMDOutput multiDimSlidingEMDTraceDistrComp(PluginContext context, XLog xlog, EMDConceptDriftParameters para, ProMCanceller canceller) {
		logger.info("Starting drift detection with distance " + para.getParamTraceComparison().getDistance().toString() + 
				" and trace descriptor factory " + para.getParamTraceComparison().getTraceDescFactory().toString());
		MultiDimSlidingEMDOutput output = new MultiDimSlidingEMDOutput();
		logger.info("Computing number of comparisons");
		List<Integer> lWinSteps = new ArrayList<>(para.getNbrWindows());
		final int logSize = xlog.size();
		para.getWindowIterator().forEachRemaining(wPara -> lWinSteps.add(
				(int) Math.ceil((logSize - wPara.getStrideSize() - (int)(wPara.getWinSize() / 2)) / (float)wPara.getStrideSize())));
		int slidingSteps = lWinSteps.stream().mapToInt(Integer::intValue).sum();
		logger.info(String.format("%d comparisons will be required", slidingSteps));
		
		logger.info("Initializing Trace Descriptor factory...");
		para.getParamTraceComparison().getTraceDescFactory().init(xlog);
		
		context.getProgress().setMaximum(slidingSteps);
		logger.info("Sorting traces by start timestamp.");
		xlog = LogSorter.sortOnStartTimes(xlog);
		
		Iterator<WindowParameter> it_wPara = para.getWindowIterator();
		while(it_wPara.hasNext()) {
			WindowParameter wPara = it_wPara.next();
			logger.info("Running drift detection with " + wPara.toString());
			ArrayList<Double> arrSldEMD = runEMDTraceComp(context, xlog, wPara.getWinSize(), wPara.getStrideSize(), 
					para.getParamTraceComparison().getTraceDescFactory(), para.getParamTraceComparison().getDistance(), canceller);
			output.addResult(new SlidingEMDOutput(wPara, arrSldEMD));
		}
		return output;
	}

	
	private static <T> ArrayList<Double> runEMDTraceComp(PluginContext context, XLog xlog, int winSize, int strideSize, AbstractTraceDescriptorFactory langFac,
			TraceDescDistCalculator trDescDist, ProMCanceller canceller) {

		logger.info("Prefilling windows.");
		Iterator<XTrace> it = xlog.iterator();
		Queue<XTrace> win_l = new LinkedList<XTrace>();
		Queue<XTrace> win_r = new LinkedList<XTrace>();
			

		{
			int i = 0;
			for(; it.hasNext() && i < winSize; i++) {
				XTrace t = it.next();
				logger.trace("Adding trace to window: " + t.getAttributes().get("concept:name"));
				win_l.add(t);
//				win_l.add(it.next());
			}
			for(i = 0; it.hasNext() && i < winSize; i++) {
				XTrace t = it.next();
				logger.trace("Adding trace to window: " + t.getAttributes().get("concept:name"));
				win_r.add(t);
//				win_r.add(it.next());
			}
			if(i < winSize) {
				return new ArrayList<Double>();
			}
		}
		
		SlidingStochasticLanguage langL = FreqBasedStochasticLanguageImpl.convert(win_l.iterator(), canceller, langFac);
		SlidingStochasticLanguage langR = FreqBasedStochasticLanguageImpl.convert(win_r.iterator(), canceller, langFac);
		
		logger.info("Run sliding trace distribution comparison");
		ArrayList<Double> arrSldEMD = new ArrayList<>();
		int step = 0;
		do {
			logger.trace(String.format("Step %d:", step++));
			time_solver.start();
			double emd = compareWindows(langL, langR, trDescDist);
			time_solver.stop();
			logger.trace(String.format("Final EMD: %f", emd));
			arrSldEMD.add(emd);
			
			context.getProgress().inc();
			int i = 0;
			while(it.hasNext() && i < strideSize) {
				XTrace traceNext = it.next();
				logger.trace("Adding trace to window: " + traceNext.getAttributes().get("concept:name"));
				updateWindows(win_l, win_r, langL, langR, traceNext);
				i++;
			}
		} while(it.hasNext());
		logger.info("Total solver time: " + time_solver);
		return arrSldEMD;
	}
	
	public static void updateWindows(Queue<XTrace> win_l, Queue<XTrace> win_r, SlidingStochasticLanguage Ll, SlidingStochasticLanguage Lr, XTrace trace) {
		// Update window queues
		XTrace traceSldOutL = win_l.poll();
		XTrace traceSldOutR = win_r.poll();
		if(traceSldOutL == null || traceSldOutR == null) {
			logger.error("Window queues seems to be not properly filled.");
			return;
		}
		win_l.add(traceSldOutR);
		win_r.add(trace);
		
		// Update languages
		Ll.slideOut(traceSldOutL);
		Ll.slideIn(traceSldOutR);
		
		Lr.slideOut(traceSldOutR);
		Lr.slideIn(trace);
		
	}
	
	public static double compareWindows(StochasticLanguage Ll, StochasticLanguage Lr, TraceDescDistCalculator trDescDist) {
		boolean USEAKP = true;
		if(USEAKP) {
			AKPFactoryArraySimple fac = new AKPFactoryArraySimple(trDescDist);
			fac.setupNewSolver(Ll, Lr, null);
			AKPSolverArraySimple solver = fac.getSolver();
			
			double emd = solver.solve();
			return emd;
		}
		else {
			return EMDWithLPSolve.calculateEMD(Ll, Lr, trDescDist);
		}

	}
	
	
	
	
	
}
