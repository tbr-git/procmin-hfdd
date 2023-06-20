package org.processmining.hfddbackend.service;

import java.util.Optional;
import java.util.UUID;

import org.processmining.hfddbackend.model.HFDDRun;

public interface HFDDRunRepo {
	
	public Optional<HFDDRun> getHFDDRun(UUID hfddRunId);

	public void saveHFDDRun(UUID hfddRunId, HFDDRun hfddRun);

}
