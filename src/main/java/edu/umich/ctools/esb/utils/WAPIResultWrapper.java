package edu.umich.ctools.esb.utils;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class WAPIResultWrapper {

	private static Log M_log = LogFactory.getLog(WAPIResultWrapper.class);
	
	private HashMap<String, Object> value;
	private int status;
	private String message;
	private JSONObject result;
	
	public WAPIResultWrapper(int status, String msg, JSONObject result){
		this.status = status;
		this.message = msg;
		this.result = result;
		HashMap<String, Object> value = new HashMap<String, Object>();
		HashMap<String, Object> meta = new HashMap<String, Object>();
		meta.put("httpStatus", status);
		meta.put("Message", msg);
		value.put("Meta", meta);
		value.put("Result", result);
		this.value = value;
	}
		
	public HashMap<String, Object> getValue() {
		return value;
	}

	public void setValue(HashMap<String, Object> value) {
		this.value = value;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JSONObject getResult() {
		return result;
	}

	public void setResult(JSONObject result) {
		this.result = result;
	}

	public String toJson(){
		JSONObject jsonObject = new JSONObject(this.value);
		return jsonObject.toString();
	}	
}
