package org.processmining.hfddbackend.service;

public class InvalidVertexIdException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7716949879426893896L;
	
	private final int vertexId;

	public InvalidVertexIdException() {
		super("A requsted vertex does not exist!");
		this.vertexId = -1;
	}
	
	public InvalidVertexIdException(int vertexId) {
		super("Vertex " + vertexId + " does not exist!");
		this.vertexId = vertexId;
	}

	public int getVertexId() {
		return vertexId;
	}

}
