package com.publictransportation.model;

/**
 * Instance of routes with three attributes (id, longName and shortName)
 * 
 * @author Henio
 * @since 2014/02
 */
public class Route {
	
	private String id;
	private String longName;
	private String shortName;

	
	public Route() {}
	
	public Route(String id, String longName, String shortName) {
		super();
		this.id = id;
		this.longName = longName;
		this.shortName = shortName;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
}
