package edu.umich.ctools.esb.utils;

import org.json.JSONObject;

import edu.umich.ctools.esb.utils.WAPI;
import edu.umich.ctools.esb.utils.WAPIResultWrapper;
import junit.framework.TestCase;

public class WAPITest extends TestCase {

//	public void testWAPI() {
//		fail("Not yet implemented");
//	}

//	public void testWAPIHashMapOfStringString() {
//		fail("Not yet implemented");
//	}

	public void testBuidRenewal() {
		WAPI wapi = new WAPI();
		assertEquals(wapi.buildRenewal("AAA", "BBB"),"Basic QUFBOkJCQg==");
	}

	public void testBase64KeySecret() {
		WAPI wapi = new WAPI();
		assertEquals(wapi.base64KeySecret("AAA", "BBB"),"QUFBOkJCQg==");
	}

//	public void testDoRequest() {
//		fail("Not yet implemented");
//	}

//	public void testReportError() {
//		fail("Not yet implemented");
//	}

//	public void testGetRequest() {
//		fail("Not yet implemented");
//	}

//	public void testRenewToken() {
//		fail("Not yet implemented");
//	}

	public void testRunTokenRenewalPost() {
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

}
