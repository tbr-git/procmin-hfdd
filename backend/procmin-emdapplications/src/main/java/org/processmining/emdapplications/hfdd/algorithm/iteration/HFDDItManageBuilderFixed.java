package org.processmining.emdapplications.hfdd.algorithm.iteration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.processmining.emdapplications.data.stochlangdatasource.transform.SLDSTransformationError;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.emdconceptdrift.diagnostics.data.BiComparisonDataSource;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraph;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDGraphBuilderFromItemsets;

/**
 * Builder that can instantiate the session from a provided collection of activities.
 * 
 * @author brockhoff
 *
 */
public class HFDDItManageBuilderFixed extends HFDDIterationManagementBuilder {
	
	private Set<Collection<String>> itemsets;

	public HFDDItManageBuilderFixed() {
		this.itemsets = new HashSet<>();
	}
	

	@Override
	protected HFDDGraph mineHFDDGraph(BiComparisonDataSource<? extends CVariant> biCompDS)
			throws HFDDIterationManagementBuildingException {
		HFDDGraphBuilderFromItemsets hfddBuilder = new HFDDGraphBuilderFromItemsets();
	
		CategoryMapper cm;
		try {
			cm = biCompDS.getDataSourceLeftBase()
					.getVariantLog().getCategoryMapper();
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			throw new HFDDIterationManagementBuildingException(e.getMessage());
		}
		Set<List<Integer>> catItemsets = itemsets.stream()
			.map(s -> s.stream().map(cm::getCategory4Activity).toList())
			.collect(Collectors.toSet());
		hfddBuilder.setItemsets(catItemsets);
		
		try {
			return hfddBuilder.buildBaseHFDDGraph(biCompDS);
		} catch (SLDSTransformationError e) {
			e.printStackTrace();
			throw new HFDDIterationManagementBuildingException(e.getMessage());
		}
	}
	
	public HFDDItManageBuilderFixed addItemset(String[] itemset) {
		this.itemsets.add(Set.of(itemset));
		return this;
	}
	
	// Don't use String[] because it does not override equals and therefore becomes
	// more difficult to delete
	public HFDDItManageBuilderFixed addItemset(Collection<String> itemset) {
		this.itemsets.add(itemset);
		return this;
	}
	
	public HFDDItManageBuilderFixed removeItemset(Collection<String> itemset) {
		this.itemsets.remove(itemset);
		return this;
	}
}
