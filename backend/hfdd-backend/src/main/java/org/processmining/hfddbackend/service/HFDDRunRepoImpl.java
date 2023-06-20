package org.processmining.hfddbackend.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.processmining.hfddbackend.model.HFDDRun;
import org.springframework.stereotype.Component;

@Component
public class HFDDRunRepoImpl implements HFDDRunRepo {
	
	Map<UUID, HFDDRun> runs;
	
	public HFDDRunRepoImpl() {
		runs = new HashMap<>();
	}

	@Override
	public Optional<HFDDRun> getHFDDRun(UUID hfddRunId) {
		HFDDRun res = runs.get(hfddRunId);
		if (res == null) {
			return Optional.empty();
		}
		else {
			return Optional.of(res);
		}
	}

	@Override
	public void saveHFDDRun(UUID hfddRunId, HFDDRun hfddRun) {
		runs.put(hfddRunId, hfddRun);
	}

}
