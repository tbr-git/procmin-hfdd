package org.processmining.emdapplications.hfdd.data.hfddgraph.serialization;

import java.io.IOException;

import org.processmining.emdapplications.emdconceptdrift.diagnostics.view.PerspectiveDescriptor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class PerspectiveDescriptorSerializer extends StdSerializer<PerspectiveDescriptor> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3466069010021740107L;

	public PerspectiveDescriptorSerializer() {
		this(null);
	}

	protected PerspectiveDescriptorSerializer(Class<PerspectiveDescriptor> t) {
		super(t);
	}

	@Override
	public void serialize(PerspectiveDescriptor value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		gen.writeFieldName(value.getID());
	}

}
