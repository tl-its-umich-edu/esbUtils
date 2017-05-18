package edu.umich.ctools.esb.utils;

/*
 * Wrapper for calls to the UMich ESB using the IBM API Manager.
 * Will auto renew tokens on failure.
 * Properties are provided in a HashMap to the contructor. 
 * For ease this provides a utility function to read groups of properties
 * from a file.
 * 
 * Properties required for connection and authorization are:
 * - apiPrefix
 * - tokenServer
 * - key
 * - secret
 * - grant_type
 * - scope
 * 
 * There may be additional properties in a group that are used by the caller to fill in 
 * fields and headers for specific queries.
 * 
 * This is based on a prior version of WAPI suited for the WSO2 API Manager.  This 
 * is likely not compatible without further work.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONException;
import org.json.JSONObject;

// Documentation at http://unirest.io/java.html
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;

public class WAPI 
{
	private static Log M_log = LogFactory.getLog(WAPI.class);

	private final static int HTTP_SUCCESS = 200;
	private final static int HTTP_BAD_REQUEST = 400;
	private final static int HTTP_UNAUTHORIZED = 401;
	private final static int HTTP_FORBIDDEN = 403;
	private final static int HTTP_NOT_FOUND = 404;
	private final static int HTTP_GATEWAY_TMEOUT = 504;
	private final static int HTTP_UNKNOWN_ERROR = 666;
	
	private final static String SUCCESS = "SUCCESS";
	private final static String BAD_REQUEST = "BAD REQUEST";
	private final static String UNAUTHORIZED = "UNAUTHORIZED";
	private final static String FORBIDDEN = "FORBIDDEN";
	private final static String NOT_FOUND = "NOT FOUND";
	private final static String GATEWAY_TMEOUT = "GATEWAY TIMEOUT";
	private final static String UNKNOWN_ERROR = "UNKNOWN ERROR";
	private final static String GRANT_TYPE = "grant_type";
	private final static String SCOPE = "scope";
	private final static String CONTENT_TYPE = "Content-Type";
	private final static String CONTENT_TYPE_PARAMETER = "application/x-www-form-urlencoded";
	private final static String AUTHORIZATION = "Authorization";
	private final static String CLIENT_CREDENTIALS = "client_credentials";
	private final static String PRODUCTION = "PRODUCTION";
	
	private final static String ERROR_MSG = "No results due to error. See meta for more details.";
	private final String BEARER = "Bearer";

	// query values that may come from properties.
	private String apiPrefix;
	private String tokenServer;
	private String key;
	private String secret;
	private String token;
	private String grant_type_value = CLIENT_CREDENTIALS;
	private String scope_value = PRODUCTION;
	
	private String renewal;
	
	public WAPI() {
		super();
	}

	//WAPI constructor will have single variable which will be a map holding all necessary variables
	//The values for the map should come from a properties file used by the application that depends
	//on this library. The values used here are to allow API connection and authorization.
	public WAPI(HashMap<String, String> value) {
		
		this.setApiPrefix(value.get("apiPrefix"));
		this.tokenServer = value.get("tokenServer");
		this.key = value.get("key");
		this.secret = value.get("secret");
		this.renewal = buildRenewal(this.key, this.secret);

		// override default value if have explicit value.

		if (value.get("grant_type") != null) {
			this.grant_type_value = value.get("grant_type");
		}
		if (value.get("scope") != null) {
			this.scope_value = value.get("scope");
		}

		M_log.info("tokenServer: " + tokenServer);
		M_log.info("key: " + elideString(this.key));
		M_log.info("secret: " + elideString(this.secret));
		M_log.info("renewal: " + elideString(this.renewal));
	}

	// Print short version of a confidential string to identify it.
	private String elideString(String stringToElide) {
		return stringToElide.substring(0,3)+"..."+stringToElide.substring(stringToElide.length() -3);
	}
	
	public String getApiPrefix() {
		return apiPrefix;
	}

	public void setApiPrefix(String apiPrefix) {
		this.apiPrefix = apiPrefix;
	}
	
	//A token must be created at the time of construction
	//this token will allow use of the ESB APIs
	public String buildRenewal(String key, String secret) {
		String b64 = base64KeySecret(key, secret);
		b64 = "Basic " + b64; 
		return b64;
	}

	//esb calls require base 64 strings for authorization
	public String base64KeySecret(String key, String secret) {
		String keySecret = key + ":" + secret;
		byte[] binaryData = keySecret.getBytes();
		keySecret = Base64.encodeBase64String(binaryData);
		return keySecret;
	}

//perform ESB request. If there is an exception, return exception result response.
//	public WAPIResultWrapper doRequestWSO2(String request){
//		M_log.info("doRequest: " + request);
//		WAPIResultWrapper wrappedResult = null;
//		JSONObject jsonObject = null;
//		HttpResponse<String> response = null;
//		
//		try{
//			response = Unirest.get(request)
//					.header(AUTHORIZATION, this.token)
//					.header("Accept", "json")
//					.asString();
//			M_log.debug("Raw body: " + response.getBody());
//			M_log.info("Status: " + response.getStatus());
//			M_log.info("Status Text: " + response.getStatusText());
//			jsonObject = new JSONObject(response.getBody());
//			wrappedResult = new WAPIResultWrapper(response.getStatus(), "COMPLETED", jsonObject);
//		}
//		catch(NullPointerException | UnirestException | JSONException e){
//			int checkStatus = HTTP_UNKNOWN_ERROR;
//			M_log.error("Error: " + e);
//			M_log.error("Error attempting to make request: " + request);
//			M_log.error("Error in doRequest: " + e.getMessage());
//			if( response != null){
//				checkStatus = response.getStatus();
//			}
//			wrappedResult = reportError(checkStatus);
//		}
//
//		return wrappedResult;
//	}	
	

	// Wrapper for calls that don't require special headers..
	public WAPIResultWrapper doRequest(String request){
		return doRequest(request,null);
	}
	
	// Make sure there is a headers map, add default value for the accept header
	// and renew authorization token if it doesn't exist.
	public HashMap<String,String> addDefaultRequestHeaders(HashMap<String,String> headers ){
		
		if (headers == null) {
			headers = new HashMap<String,String>();
		}
		
		// ensure that authorization has been done.
		if (headers.get(AUTHORIZATION) == null) {
			if (this.token == null) {
				renewToken();
			}
			headers.put(AUTHORIZATION, this.token);
		}
		
		if (headers.get("Accept") == null) {
			headers.put("Accept", "json");
		}

		return headers;
	}
	
	// Make a request and allow specifying headers.
	public WAPIResultWrapper doRequest(String request,HashMap<String,String> headers){	
		M_log.info("doRequest: " + request +" headers: "+headers);
		WAPIResultWrapper wrappedResult = null;
		JSONObject jsonObject = null;
		HttpResponse<String> response = null;
		
		headers = addDefaultRequestHeaders(headers);
		
		M_log.info("doRequest: request: "+request.toString());
		M_log.info("doRequest: headers: "+headers.toString());
		if (headers.get("Authorization") == null) {
			M_log.error("request has null Authorization. Maybe token renewal timeout.");
			return reportError(HTTP_UNAUTHORIZED);
		}

		StopWatch sw = StopWatch.createStarted();
		try{
			response = Unirest.get(request)
					.headers(headers)
					.asString();
			M_log.debug("Raw body: " + response.getBody());
			M_log.info("Status: " + response.getStatus());
			M_log.info("Status Text: " + response.getStatusText());
			jsonObject = new JSONObject(response.getBody());
			wrappedResult = new WAPIResultWrapper(response.getStatus(), "COMPLETED", jsonObject);
		}
		catch(NullPointerException | UnirestException | JSONException e){
			int checkStatus = HTTP_UNKNOWN_ERROR;
			M_log.error("Error: " + e);
			M_log.error("Error attempting to make request: " + request);
			M_log.error("Error in doRequest: " + e.getMessage());
			if( response != null){
				checkStatus = response.getStatus();
			}
			wrappedResult = reportError(checkStatus);
		}
		finally {
			sw.stop();
			M_log.info("WAPI: doRequest elapsed: "+sw.toString()+" request: "+request);
		}
		return wrappedResult;
	}	
	
	
	//Error reporting for bad status.
	public WAPIResultWrapper reportError(int status) {
		M_log.info("reportError() called");
		M_log.info("status: " + status);
		String errMsg= null;
		switch(status){
			
		case HTTP_BAD_REQUEST:
			status = HTTP_BAD_REQUEST;
			errMsg = BAD_REQUEST;
			break;
			
		case HTTP_UNAUTHORIZED:
			status = HTTP_UNAUTHORIZED;
			errMsg = UNAUTHORIZED;
			break;
		
		case HTTP_FORBIDDEN:
			status = HTTP_FORBIDDEN;
			errMsg = FORBIDDEN;
			break;	
			
		case HTTP_NOT_FOUND:
			status = HTTP_NOT_FOUND;
			errMsg = NOT_FOUND;
			break;
			
		case HTTP_GATEWAY_TMEOUT:
			status = HTTP_GATEWAY_TMEOUT;
			errMsg = GATEWAY_TMEOUT;
			break;
			
		case HTTP_UNKNOWN_ERROR:
			status = HTTP_UNKNOWN_ERROR;
			errMsg = UNKNOWN_ERROR;
			break;
			
		default:
			status = HTTP_UNKNOWN_ERROR;
			errMsg = UNKNOWN_ERROR;
		}
		return new WAPIResultWrapper(status, errMsg, new JSONObject("{error : " + ERROR_MSG + "}"));
	}

	//Make a request, if error returned then try to renew token and try again.
	public WAPIResultWrapper getRequest(String request) throws UnirestException{
		WAPIResultWrapper wrappedResult = doRequest(request);
		M_log.info("getRequest() called with request: " + request);
		if(wrappedResult.getStatus()==HTTP_UNAUTHORIZED){
			wrappedResult = renewToken();
			if(wrappedResult.getStatus()==HTTP_SUCCESS){
				wrappedResult = doRequest(request);
			}
		}
		return wrappedResult;
	}

	//Renew token. Tokens are only good for one hour, so in the event a user is still
	//logged in the token will need to be renewed.
	
	public WAPIResultWrapper renewToken(){
		M_log.info("renewToken() called");
		HttpResponse<JsonNode> tokenResponse = null;
		// if need to renew token then get rid of the old one.
		this.token = null;
		try{
			tokenResponse = runTokenRenewalPost();
			if(tokenResponse.getStatus()==HTTP_SUCCESS){
				JSONObject json = new JSONObject(tokenResponse.getBody().toString());
				this.token = this.BEARER + " " + json.getString("access_token");
			}
			if(tokenResponse.getStatus()!=HTTP_SUCCESS){
				M_log.error("Error renewing token: " + tokenResponse.getStatusText());
				return new WAPIResultWrapper(tokenResponse.getStatus(),"ERROR DURING TOKEN RENEWAL", new JSONObject("{" + tokenResponse.getBody() + "}"));
			}
		}
		catch(Exception e){
			M_log.error("renewalToken exception: " + e.getMessage());
			if(tokenResponse==null){
				return new WAPIResultWrapper(HTTP_UNKNOWN_ERROR,"ERROR RENEWING TOKEN: NULL", new JSONObject("{error: " + ERROR_MSG + "}"));
			}
			return reportError(tokenResponse.getStatus());
		}
		M_log.info("token successfully renewed - token: " + elideString(this.token));
		M_log.info("token successfully renewed - token: " + this.token);
		return new WAPIResultWrapper(tokenResponse.getStatus(),"TOKEN RENEWED", new JSONObject(tokenResponse.getBody()));
	}
	    
	//Specific request for renewing token.
	public HttpResponse<JsonNode> runTokenRenewalPost() throws UnirestException{
		M_log.info("runTokenRenewalPost() called");
		M_log.info("TokenServer: " + this.tokenServer);
		HttpResponse<JsonNode> tokenResponse = null;
		MultipartBody tokenRequest = null;
		
		HashMap<String,String> headers = new HashMap<String,String>();
		headers.put(CONTENT_TYPE, CONTENT_TYPE_PARAMETER);
		
		HashMap<String,Object> fields = new HashMap<String,Object>();
		fields.put(GRANT_TYPE, grant_type_value);
		fields.put(SCOPE, scope_value);
		fields.put("client_id", key);
		fields.put("client_secret",secret);
		
		M_log.info("runTokenRenewalPost: headers: "+headers.toString());
		M_log.info("runTokenRenewalPost: fields: "+fields.toString());
		
		StopWatch sw = StopWatch.createStarted();
		try{
			tokenRequest = Unirest.post(this.tokenServer)
					.headers(headers)
					.fields(fields)
					;
			M_log.error("tokenRequest: "+tokenRequest);
			tokenResponse = tokenRequest.asJson();
			M_log.error("tokenResponseBody: "+tokenResponse.getBody());
		}
		catch(UnirestException e){
			M_log.error("Unirest exception renewing token: " + e);
			throw e;
		}
		catch(RuntimeException e){
			M_log.error("General Error renewing token: " + e);
			throw e;
		}
		finally {
			sw.stop();
			M_log.info("WAPI: runTokenRenewalPost elapsed: "+sw.toString());
		}
		return tokenResponse;
	}

	// this is the WSO2 style renewal
//	public HttpResponse<JsonNode> runTokenRenewalPostWSO2(){
//		M_log.info("runTokenRenewalPost() called");
//		M_log.info("TokenServer: " + this.tokenServer);
//		HttpResponse<JsonNode> tokenResponse = null;
//		
//		try{
//			tokenResponse = Unirest.post(this.tokenServer)
//					.header(CONTENT_TYPE, CONTENT_TYPE_PARAMETER)
//					.header(AUTHORIZATION, this.renewal)
//					//	.field(GRANT_TYPE, CLIENT_CREDENTIALS)
//					.field(GRANT_TYPE, grant_type_value)
//					//					.field(SCOPE, PRODUCTION)
//					.field(SCOPE, scope_value)
//					.asJson();
//			M_log.debug(tokenResponse.getBody());
//		}
//		catch(Exception e){
//			M_log.error("Error renewing token: " + tokenResponse.getStatusText());
//			return null;
//		}
//		return tokenResponse;
//	}
	
	// get the select group of properties from the properties file.
	// selected properties start with "<group>."
	public static HashMap<String,String> getPropertiesInGroup(Properties props, String group, List<String> propertyNames) {
	
		HashMap<String, String> value = new HashMap<String, String>();
		for(String key: propertyNames) {
			String propertyValue = props.getProperty(group + "." +key);
			if (propertyValue != null) {
				value.put(key, propertyValue);
			}
		}
		return value;
	}
	
}