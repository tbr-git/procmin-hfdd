package org.processmining.hfddbackend.model;

import java.util.Optional;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagement;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagementBuilder;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagementBuilderMinSupport;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagementBuilderTimeout;
import org.processmining.emdapplications.hfdd.algorithm.iteration.HFDDIterationManagementBuildingException;

/**
 * Class that holds information on a hfdd run (logs, hfdd iteration management).
 * 
 * @author brockhoff
 *
 */
public class HFDDRun {
	
	/**
	 * Event classifier to be used
	 */
	public static XEventClassifier classifier = XLogInfoImpl.NAME_CLASSIFIER;

	/**
	 * Left log.
	 */
	private Optional<XLog> logLeft = Optional.empty();
	
	/**
	 * Right log.
	 */
	private Optional<XLog> logRight = Optional.empty();
	
	/**
	 * Iteration management.
	 */
	private Optional<HFDDIterationManagement> hfddItMan = Optional.empty();

	public void setLeftLog(XLog log) {
		// TODO reset hfddItMan
		logLeft = Optional.of(log);
	}

	public void setRightLog(XLog log) {
		logRight = Optional.of(log);
	}
	
	/**
	 * Create the iteration management instance.
	 * @throws HFDDIterationManagementBuildingException 
	 */
	public void initHFDDRun() throws HFDDIterationManagementBuildingException {
		HFDDIterationManagementBuilder builder = new HFDDIterationManagementBuilderMinSupport();
		hfddItMan = Optional.of(builder.setClassifier(classifier)
			.setXlogL(logLeft.orElseThrow())
			.setXlogR(logRight.orElseThrow())
			.build());
	}
	
	/**
	 * Create the iteration management instance where initial itemsets are found by searching.
	 * 
	 * @param freqActMiningTimeMs
	 * @param targetActISNbr
	 * @param targetActISMargin
	 * @param maxLoopUnrolling Maximum number of loops iterations that will be unrolled. If absent, no unrolling.
	 * @throws HFDDIterationManagementBuildingException
	 */
	public void initHFDDRun(Integer freqActMiningTimeMs, Integer targetActISNbr, Double targetActISMargin, 
			Optional<Integer> maxLoopUnrolling) 
			throws HFDDIterationManagementBuildingException {
		HFDDIterationManagementBuilderTimeout builder = new HFDDIterationManagementBuilderTimeout();

		builder.setMaxMiningTime(freqActMiningTimeMs)
			.setTargetItemsetNumber(targetActISNbr)
			.setTargetItemsetMargin(targetActISMargin)
			.setClassifier(classifier)
			.setXlogL(logLeft.orElseThrow())
			.setXlogR(logRight.orElseThrow());
		// Apply loop unrolling if provided
		maxLoopUnrolling.ifPresent(builder::setMaxUnroll);
		hfddItMan = Optional.of(builder.build());
	}

	public Optional<XLog> getLogLeft() {
		return logLeft;
	}

	public Optional<XLog> getLogRight() {
		return logRight;
	}

	public Optional<HFDDIterationManagement> getHfddItMan() {
		return hfddItMan;
	}
	
}
