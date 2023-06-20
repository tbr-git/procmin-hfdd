package org.processmining.emdapplications.hfdd.data.csgraph.visualization.diffdecompgraph;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.AbstTraceCC;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.BasicTraceCC;
import org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors.TraceDescriptor;
import org.processmining.emdapplications.hfdd.data.hfddgraph.HFDDVertexInfo;

public class DDGActivityBuilder {
	
	public static DDGActivity activityFrom(TraceDescriptor descTrace, int index, 
			Optional<Function<String, String>> itemAbbreviator) {
		// Array of trace element's string representation
		String activity = descTrace.toString(index);

		// Initialize category
		Optional<Integer> actCategory = Optional.empty();
		if (descTrace instanceof BasicTraceCC descCat) {
			actCategory = Optional.of(descCat.getTraceCategories()[index]);
		}
		else if (descTrace instanceof AbstTraceCC descAbstCat) {
			actCategory = Optional.of(descAbstCat.getTrace()[index]);
		}
		
		return new DDGActivity(activity, actCategory, 
				abbreviate(activity, itemAbbreviator));

	}
	
	/**
	 * Convert the activities associated with the vertex. 
	 * @param hfddVInfo Reference to the (activity itemset-based) vertex
	 * @param itemAbbreviator Activity abbreviation function that can be 
	 * 		applied to the full activity label string.
	 * @return Set of activities for visualization.
	 */
	public static Collection<DDGActivity> activitiesFrom(HFDDVertexInfo hfddVInfo, 
			Optional<Function<String, String>> itemAbbreviator) {
		
		final CategoryMapper categoryMapper = hfddVInfo.getCategoryMapper();

		return hfddVInfo.getActivities().stream()
			.mapToObj(c -> 
				activityFrom(c, categoryMapper, itemAbbreviator)) // Convert each code
			.collect(Collectors.toSet()); // collect into set
	}
	
	/**
	 * Create it from a categorical code and category mapper.
	 * 
	 * Every activity will be build freshly. 
	 * 
	 * Note:
	 * For simplicity and assuming that this method is not
	 * called very frequently, activity instances are not cashed.
	 * @param actCode Activity categorical code
	 * @param categoryMapper Mapper translating categorical codes into readable activity names.
	 * @param itemAbbreviator Activity abbreviation function that can be 
	 * 		applied to the full activity label string.
	 * @return 
	 */
	public static DDGActivity activityFrom(int actCode, CategoryMapper categoryMapper, 
			Optional<Function<String, String>> itemAbbreviator) {

		// Convert categorical code to activity label
		String activityLabel = categoryMapper.getActivity4Category(actCode);
		
		// Abbreviate
		Optional<String> activityAbbrev = abbreviate(activityLabel, itemAbbreviator);
		
		return new DDGActivity(activityLabel, Optional.of(actCode), activityAbbrev);
		
	}
	
	/**
	 * Abbreviate the activity.
	 * @param activity Activity to abbreviate
	 * @param itemAbbreviator Abbreviator mapping
	 * @return Abbreviated activity
	 */
	private static Optional<String> abbreviate(String activity, 
			Optional<Function<String, String>> itemAbbreviator)  {
		// Initialize activity abbreviation
		Optional<String> activityAbbrev = Optional.empty();

		// Abbreviation function provided?
		if (itemAbbreviator.isPresent()) {
			activityAbbrev = Optional.of(itemAbbreviator.get().apply(activity));
		}
		
		return activityAbbrev;
	}

}
