package edu.umich.ctools.esb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
		String properties_file = "test.properties";
		Properties props = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream(properties_file);
			props.load(in);
			in.close();
		}
		catch (Exception e) {
			M_log.error("getProps: properties file not opened : "+properties_file+ " "+e);
		}
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

		Properties props = getProps();
		String group = "dev";
			
		//Arrays.asList is fixed length, so use that to initialize a mutable array.
		List<String> keys = new ArrayList<>(Arrays.asList("tokenServer","apiPrefix","key","secret","scope"));

		HashMap<String,String> value = WAPI.getPropertiesInGroup(props, group,keys);
		
	//	value.put("ignore_ssl_check", "true");
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
	
	
//	GET https://apigw-tst.it.umich.edu:444/aa/sandbox/aa/Unizin/data/CourseId/159923/AssignmentTitle/Spanish Placement Exam
//		Authorization: Bearer ∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙∙
//		X-IBM-Client-Id: ac54652b-5f59-4a9c-a39f-33f76567597b
//		accept: application/json
//		gradedAfterTime: 2017-04-01 18:00:00

	
	//private  void getGrades(RunTestIBM runTest) throws IOException {
	public WAPIResultWrapper getGrades() throws IOException {

		Properties props = getProps();
		String group = "dev";

		//Arrays.asList is fixed length, so use that to initialize a mutable array.
		List<String> keys = new ArrayList<>(Arrays.asList("tokenServer","apiPrefix","key","secret","scope"));
		keys.add("gradedaftertime");
		keys.add("COURSEID");
		keys.add("ASSIGNMENTTITLE");
		keys.add("x-ibm-client-id");

		HashMap<String,String> value = WAPI.getPropertiesInGroup(props, group,keys);
		
		HashMap<String,String> headers = new HashMap<String,String>();
		//headers.put("Accept", "json");
		headers.put("gradedaftertime",value.get("gradedaftertime"));
		headers.put("x-ibm-client-id",value.get("x-ibm-client-id"));
		//INFO: doRequest: https://apigw-tst.it.umich.edu:444/aa/sandbox/aa/Unizin/data/CourseId/159923/AssignmentTitle/'Spanish%20Placement%20Exam' headers: {x-ibm-client-id=ac54652b-5f59-4a9c-a39f-33f76567597b, GRADEDAFTERTIME=null}

		  //--url "${URL_PREFIX}/Unizin/data/CourseId/${COURSEID}/AssignmentTitle/${ASSIGNMENTTITLE}" 
		
		StringBuilder url = new StringBuilder();
		url.append(value.get("apiPrefix"))
		.append("/Unizin/data/CourseId/")
		.append(value.get("COURSEID"))
		.append("/AssignmentTitle/")
		.append(value.get("ASSIGNMENTTITLE"));

		M_log.info("getGrades: value:["+value.toString()+"]");
		M_log.info("getGrades: request url: ["+url.toString()+"]");
		M_log.info("getGrades: headers: ["+headers.toString()+"]");

		
		WAPI wapi = new WAPI(value);
		
		WAPIResultWrapper wrappedResult = wapi.doRequest(url.toString(),headers);

		M_log.info(wrappedResult.toJson());
		return wrappedResult;
	}
	
}
