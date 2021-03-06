package eu.vital.discoverer.inputJSON;

import org.json.simple.JSONObject;

public class Discover_Systems_JSON_Object implements RequestJSONObjectInterface{

	private String type, serviceArea;
	private boolean hasType, hasServiceArea;

	public String getType() {
		return type;
	}

	public void setType(String type){
		this.type=type;
	}

	public String getServiceArea() {
		return serviceArea;
	}

	public void setServiceArea(String serviceArea) {
		this.serviceArea = serviceArea;
	}

	public boolean hasType(){
		return this.hasType;
	}

	public boolean hasServiseArea(){
		return this.hasServiceArea;
	}

	public void setIncludedKeys(JSONObject inputObject) {
		this.hasServiceArea=inputObject.containsKey("serviceArea");
		this.hasType=inputObject.containsKey("type");
	}

}
