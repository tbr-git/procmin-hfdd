package org.processmining.emdapplications.hfdd.algorithm.iteration.diffcandidates;

import java.util.Collection;
import java.util.Optional;

import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public record DiffCandidate(HFDDVertex v, Optional<Collection<HFDDVertex>> condContext) {

}
