package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.processmining.emdapplications.data.variantlog.base.CVariant;
import org.processmining.emdapplications.data.variantlog.base.CategoryMapper;

public class BasicTraceCC extends TraceDescriptor {
	
	private final int[] traceCategories;
	
	private final CategoryMapper categoryMapper;

	public BasicTraceCC(CVariant variant, CategoryMapper categoryMapper) {
		this.traceCategories = variant.getTraceCategories();
		this.categoryMapper = categoryMapper;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.traceCategories);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicTraceCC other = (BasicTraceCC) obj;
		if (!Arrays.equals(this.traceCategories, other.traceCategories))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BasicTrace [sTrace=" + Arrays.toString(this.traceCategories) + "]";
	}

	@Override
	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray(Arrays.stream(traceCategories).mapToObj(
				i -> categoryMapper.getActivity4Category(i)).toArray(String[]::new));
		jo.put("Trace", ja);
		return jo;
	}

	@Override
	public String toString(int index) {
		return categoryMapper.getActivity4Category(traceCategories[index]);
	}
	
	public int[] getTraceCategories() {
		return this.traceCategories;
	}

	@Override
	public int length() {
		return this.traceCategories.length;
	}
	
	

}
