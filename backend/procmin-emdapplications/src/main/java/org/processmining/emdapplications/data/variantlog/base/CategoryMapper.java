package org.processmining.emdapplications.data.variantlog.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface that defines the contract for mapping between category codes and activity names.
 * @author brockhoff
 *
 */
public interface CategoryMapper {

	public Integer getCategory4Activity(String activity); 
	
	public String getActivity4Category(int category); 
	
	@JsonIgnore
	public int getMaxCategoryCode(); 

	@JsonIgnore
	public int getCategoryCount(); 

	public String getId(); 

	public void setId(String id); 
}
