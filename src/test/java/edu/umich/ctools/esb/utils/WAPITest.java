package edu.umich.ctools.esb.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;

import edu.umich.ctools.esb.utils.WAPI;
import edu.umich.ctools.esb.utils.WAPIResultWrapper;
import junit.framework.TestCase;

public class WAPITest extends TestCase {

	public void testBuidRenewal() {
		WAPI wapi = new WAPI();
		assertEquals(wapi.buildRenewal("AAA", "BBB"),"Basic QUFBOkJCQg==");
	}

	public void testBase64KeySecret() {
		WAPI wapi = new WAPI();
		assertEquals(wapi.base64KeySecret("AAA", "BBB"),"QUFBOkJCQg==");
	}

	public void testRunWAPIResultWrapper() {
		WAPI wapi = new WAPI();
		WAPIResultWrapper wrappedResult400 = new WAPIResultWrapper(400, "BAD REQUEST", new JSONObject("{error : " + "No results due to error. See meta for more details." + "}"));
		WAPIResultWrapper wrappedResult401 = new WAPIResultWrapper(401, "UNAUTHORIZED", new JSONObject("{error : " + "No results due to error. See meta for more details." + "}"));
		WAPIResultWrapper wrappedResult403 = new WAPIResultWrapper(403, "FORBIDDEN", new JSONObject("{error : " + "No results due to error. See meta for more details." + "}"));
		WAPIResultWrapper wrappedResult404 = new WAPIResultWrapper(404, "NOT FOUND", new JSONObject("{error : " + "No results due to error. See meta for more details." + "}"));
		WAPIResultWrapper wrappedResult504 = new WAPIResultWrapper(504, "GATEWAY TIME OUT", new JSONObject("{error : " + "No results due to error. See meta for more details." + "}"));
		WAPIResultWrapper wrappedResult666 = new WAPIResultWrapper(666, "UNKNOWN ERROR", new JSONObject("{error : " + "No results due to error. See meta for more details." + "}"));
		assertEquals(wapi.reportError(400).getStatus(),wrappedResult400.getStatus());
		assertEquals(wapi.reportError(401).getStatus(),wrappedResult401.getStatus());
		assertEquals(wapi.reportError(403).getStatus(),wrappedResult403.getStatus());
		assertEquals(wapi.reportError(404).getStatus(),wrappedResult404.getStatus());
		assertEquals(wapi.reportError(504).getStatus(),wrappedResult504.getStatus());
		assertEquals(wapi.reportError(666).getStatus(),wrappedResult666.getStatus());
		assertEquals(wapi.reportError(777).getStatus(),wrappedResult666.getStatus());
	}
	
	
	//public static HashMap<String,String> getPropertiesGroup	 (Properties props, String group, List<String> propertyNames) {
		public void testGetPropertiesDevGroupSimple() 
		{
			Properties props = new Properties();
			props.setProperty("dev.HOWDY", "DUTY");
			props.setProperty("QA.howbouty", "SURE");

			HashMap<String,String> p = WAPI.getPropertiesInGroup(props,"dev",(List<String>)Arrays.asList("tokenserver","HOWDY"));
			
			assertEquals("dev HOWDY value","DUTY",p.get("HOWDY"));
			assertNull("dev howbouty value",p.get("SURE"));	
			assertNull("dev tokenserver value",p.get("tokenserver"));	
		}


		//public static HashMap<String,String> getPropertiesGroup	 (Properties props, String group, List<String> propertyNames) {
		public void testGetPropertiesDevGroupBig() 
		{
			Properties props = new Properties();
			
			props.setProperty("dev.HOWDY", "DUTY");
			props.setProperty("dev.tokenserver", "GIVEITTOME");
			
			props.setProperty("qa.HOWDY", "HELLO GOV'NUR");
			props.setProperty("qa.tokenserver", "takeitfromthem");
			props.setProperty("qa.TURING", "cool");
			props.setProperty("qa.GRANT_TYPE", "CARY_GRANT");

			HashMap<String,String> p = null;
			List<String> keys = (List<String>)Arrays.asList("tokenserver","HOWDY","TURING","EMPTY","GRANT_TYPE");
			
			/////// test dev
			p =	WAPI.getPropertiesInGroup(props,"dev",keys);
			assertEquals("proper correct number of dev keys found",2,p.keySet().size());
			// incorrect key should get null
			assertNull("dev turning value",p.get("TURING"));
			
			// verify dev values
			assertEquals("dev HOWDY value","DUTY",p.get("HOWDY"));
			assertEquals("dev tokenserver value","GIVEITTOME",p.get("tokenserver"));
					
			///////// test qa
			p =	WAPI.getPropertiesInGroup(props,"qa",keys);
			assertEquals("proper correct number of qa keys found",4,p.keySet().size());
			assertEquals("qa HOWDY value","HELLO GOV'NUR",p.get("HOWDY"));
			assertEquals("qa TURING value","cool",p.get("TURING"));
			assertEquals("qs GRANT_TYPE","CARY_GRANT",p.get("GRANT_TYPE"));
			// incorrect key should get null
			assertNull("qa EMPTY value",p.get("EMPTY"));
			
			// verify qa values
			assertEquals("qa HOWDY value","HELLO GOV'NUR",p.get("HOWDY"));
			assertEquals("qa tokenserver value","takeitfromthem",p.get("tokenserver"));
			
			////// verify no values found for empty group
			p =	WAPI.getPropertiesInGroup(props,"shhh",keys);
			assertEquals("proper correct number of shhh keys found",0,p.keySet().size());
			
		}
		
}
