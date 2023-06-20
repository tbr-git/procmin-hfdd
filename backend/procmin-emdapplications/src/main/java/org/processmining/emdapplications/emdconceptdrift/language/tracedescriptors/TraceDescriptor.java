package org.processmining.emdapplications.emdconceptdrift.language.tracedescriptors;

import org.json.JSONObject;

public abstract class TraceDescriptor {
	
	public abstract int hashCode();

	public abstract boolean equals(Object obj);
	
	public abstract String toString();
	
	public abstract JSONObject toJson();
	
	public abstract String toString(int index);
	
	public abstract int length();

}
