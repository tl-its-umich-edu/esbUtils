package edu.umich.ctools.esb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mashape.unirest.http.exceptions.UnirestException;

public class RunTestIBM {

	public RunTestIBM() {
		super();
	}

	private static Log M_log = LogFactory.getLog(RunTestIBM.class);

	public Properties getProps() throws IOException{
		Properties props = new Properties();
		InputStream in = getClass().getResourceAsStream("test.properties");
		props.load(in);
		in.close();
		return props;
	}

	public static void main(String [] args) throws UnirestException, IOException{
		RunTestIBM runTest = new RunTestIBM();

		//runTest.renewToken(runTest);
		runTest.renewToken();
		//runTest.getGrades(runTest);
		runTest.getGrades();
		
	}

	//private  void renewToken(RunTestIBM runTest) throws IOException {
	public  WAPIResultWrapper renewToken() throws IOException {
		//Properties props = runTest.getProps();
		Properties props = getProps();
		String group = "dev";
		
		
		List<String> keys = (List<String>)Arrays.asList("tokenServer","apiPrefix","key","secret","scope");
		
//		HashMap<String, String> value = new HashMap<String, String>();
//		
//		value.put("tokenServer", props.getProperty(group + "tokenServer"));
//		value.put("apiPrefix", props.getProperty(group + "apiPrefix"));
//		value.put("key", props.getProperty(group + "key"));
//		value.put("secret", props.getProperty(group + "secret"));
//		value.put("scope",props.getProperty(group + "scope"));
//		
		HashMap<String,String> value = WAPI.getPropertiesInGroup(props, group,keys);
		
		value.put("ignore_ssl_check", "true");
		WAPI wapi = new WAPI(value);
		WAPIResultWrapper wrappedResult = wapi.renewToken();

		M_log.info(wrappedResult.toJson());
		return wrappedResult;
	}
	
//	function getSPEGrades {
//    #set -x
//    curl --request GET \
//         --url "${URL_PREFIX}/Unizin/data/CourseId/${COURSEID}/AssignmentTitle/${ASSIGNMENTTITLE}" \
//         --header 'accept: application/json' \
//         --header "authorization: Bearer ${ACCESS_TOKEN}" \
//         --header "gradedaftertime: ${GRADEAFTERTIME}" \
//         --header "x-ibm-client-id: ${IBM_CLIENT_ID}"
//}
	
	//private  void getGrades(RunTestIBM runTest) throws IOException {
	public WAPIResultWrapper getGrades() throws IOException {
		//Properties props = runTest.getProps();
		Properties props = getProps();
		String env = "dev.";
		
		HashMap<String, String> value = new HashMap<String, String>();

		value.put("tokenServer", props.getProperty(env + "tokenServer"));
		value.put("apiPrefix", props.getProperty(env + "apiPrefix"));
		value.put("key", props.getProperty(env + "key"));
		value.put("secret", props.getProperty(env + "secret"));
		value.put("scope",props.getProperty(env + "scope"));

		value.put("gradedaftertime", props.getProperty(env+"gradedaftertime"));
		value.put("COURSEID", props.getProperty(env+"COURSEID"));
		value.put("ASSIGNMENTTITLE", props.getProperty(env+"ASSIGNEMENTTITILE"));
		
		value.put("ignore_ssl_check", "true");
		
		WAPI wapi = new WAPI(value);
		String url = "";
		
		HashMap<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", "json");
		headers.put("gradedaftertime",value.get("gradedaftertime"));
		
		WAPIResultWrapper wrappedResult = wapi.doRequest(url,headers);

		M_log.info(wrappedResult.toJson());
		return wrappedResult;
	}
	
	
}
