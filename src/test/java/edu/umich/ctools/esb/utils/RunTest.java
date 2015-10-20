package edu.umich.ctools.esb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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

	public static void main(String [] args) throws UnirestException, IOException{
		RunTest runTest = new RunTest();
		
		Properties props = runTest.getProps();
		
		HashMap<String, String> value = new HashMap<String, String>();
		value.put("tokenServer", props.getProperty("tokenServer"));
		value.put("apiPrefix", props.getProperty("apiPrefix"));
		value.put("key", props.getProperty("key"));
		value.put("secret", props.getProperty("secret"));
		String call = props.getProperty("call");
		WAPI wapi = new WAPI(value);
		WAPIResultWrapper wrappedResult = wapi.getRequest(wapi.getApiPrefix() + call);
		M_log.info(wrappedResult.toJson());
	}
}
