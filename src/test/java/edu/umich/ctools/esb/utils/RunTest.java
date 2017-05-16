package edu.umich.ctools.esb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mashape.unirest.http.exceptions.UnirestException;

public class RunTest {
	
	public RunTest() {
		super();
	}

	private static Log M_log = LogFactory.getLog(RunTest.class);
	
	public Properties getProps() throws IOException{
		Properties props = new Properties();
		InputStream in = getClass().getResourceAsStream("test.properties");
		props.load(in);
		in.close();
		return props;
	}

//	// TODO: extract out method to pull in grouped properties and place in map.
//	// should be property file independent
//	//take input of Props, list of property names, name of group, 
//	//loop through and return as hashmap
//			
//	public static HashMap<String,String> getPropertiesGroup	 (Properties props, String group, List<String> propertyNames) {
//		HashMap<String, String> value = new HashMap<String, String>();
//		String key = "tokenServer";
//		propertyNames.add(key);
//		
//		value.put(key, props.getProperty(group + key));
//		return value;
//	}
//	
	public static void main(String [] args) throws UnirestException, IOException{
		RunTest runTest = new RunTest();
		
		Properties props = runTest.getProps();
		String env = "qa.";
		
		HashMap<String, String> value = new HashMap<String, String>();
		value.put("tokenServer", props.getProperty(env + "tokenServer"));
		value.put("apiPrefix", props.getProperty(env + "apiPrefix"));
		value.put("key", props.getProperty(env + "key"));
		value.put("secret", props.getProperty(env + "secret"));
		String call = props.getProperty(env + "call");
		WAPI wapi = new WAPI(value);
		WAPIResultWrapper wrappedResult = wapi.getRequest(wapi.getApiPrefix() + call);
		M_log.info(wrappedResult.toJson());
	}
}
