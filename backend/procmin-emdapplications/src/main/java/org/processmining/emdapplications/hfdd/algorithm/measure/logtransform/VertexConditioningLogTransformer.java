package org.processmining.emdapplications.hfdd.algorithm.measure.logtransform;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformerBuildingException;
import org.processmining.emdapplications.data.stochlangdatasource.transform.selection.SLDSFilterMandatoryCategoryFactory;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexCondition;
import org.processmining.emdapplications.hfdd.algorithm.measure.VertexConditionType;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertex;

public class VertexConditioningLogTransformer<T extends CVariant> implements HFDDVertexLogTransformerOuterContext<T> {

	private final static Logger logger = LogManager.getLogger( VertexConditioningLogTransformer.class );

	private final Optional<ArrayList<Set<VertexCondition>>> aggDataCBase;
	
	public VertexConditioningLogTransformer(Optional<ArrayList<Set<VertexCondition>>> aggDataCBase) {
		this.aggDataCBase = aggDataCBase;
	}
	
	
	@Override
	public HFDDLogTransformStep<T> getDataSourceOuterContext(HFDDVertex v, 
			BiComparisonDataSource<T> biCompDS) throws SLDSTransformerBuildingException {

		Optional<BitSet> conditionActivities = getConditionActivities(aggDataCBase, v);
		if (conditionActivities.isPresent()) {

			// Build aggregated activity condition set (AND over condition vertices)
			// ONLY consider PROBABILISTIC conditioning 
			if (conditionActivities.get().cardinality() > 0) {
				// Create copy of datasource
				biCompDS = new BiComparisonDataSource<>(biCompDS);
				// Reduce to variants that contain condition activities  
				// Mandatory activity filtering -> Each trace must contain the vertex' activities
				SLDSFilterMandatoryCategoryFactory<T> factoryMandatory = 
						new SLDSFilterMandatoryCategoryFactory<>();
				factoryMandatory.setClassifier(biCompDS.getClassifier())
					.setCategoryMapper(v.getVertexInfo().getCategoryMapper())
					.setActivities(conditionActivities.get());
				biCompDS.applyTransformation(factoryMandatory);
			}
			return new HFDDLogTransformStep<T>(biCompDS, FilterTag.CONDITION);
		}
		else {
			return new HFDDLogTransformStep<T>(biCompDS, FilterTag.CONDITION_NONE);
		}
	}
	
	/**
	 * Build aggregated activity condition set (AND over condition vertices) 
	 * ONLY consider PROBABILISTIC conditioning 
	 * 
	 * @param s
	 * @return
	 */
	public static Optional<BitSet> getConditionActivities(Optional<ArrayList<Set<VertexCondition>>> aggDataCBase, 
			HFDDVertex v) { 
		
		if (aggDataCBase.isPresent() && aggDataCBase.get().get(v.getId()) != null) {
			Set<VertexCondition> s = aggDataCBase.get().get(v.getId());
			BitSet conditionActivities = s.stream()
					.filter(vertCon -> vertCon.type() == VertexConditionType.PROBCOND)
					.map(vertCon -> vertCon.condVertex().getVertexInfo().getActivities())
					.reduce(new BitSet(v.getVertexInfo().getCategoryMapper().getMaxCategoryCode()),
							(setUnion, u) -> {
								setUnion.or(u);
								return setUnion;
							});
			return Optional.of(conditionActivities);
		}
		else {
			return Optional.empty();
		}
		
	}
}
