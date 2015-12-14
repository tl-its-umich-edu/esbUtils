package edu.umich.ctools.esb.utils;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

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

	private final static String CONTENT_TYPE = "Content-Type";
	private final static String CONTENT_TYPE_PARAMETER = "application/x-www-form-urlencoded";
	private final static String AUTHORIZATION = "Authorization";
	private final static String GRANT_TYPE = "grant_type";
	private final static String CLIENT_CREDENTIALS = "client_credentials";
	private final static String SCOPE = "scope";
	private final static String PRODUCTION = "PRODUCTION";
	private final static String ERROR_MSG = "No results due to error. See meta for more details.";

	private final String BEARER = "Bearer";

	private String apiPrefix;
	private String tokenServer;
	private String key;
	private String secret;
	private String renewal;
	private String token;
	
	public WAPI() {
		super();
	}

	//WAPI constructor will have single variable which will be a map holding all necessary variables
	//The values for the map should come from a properties file used by the application that depends
	//on this library. 
	public WAPI(HashMap<String, String> value) {
		this.setApiPrefix(value.get("apiPrefix"));
		this.tokenServer = value.get("tokenServer");
		this.key = value.get("key");
		this.secret = value.get("secret");
		this.renewal = buildRenewal(this.key, this.secret);
		
		M_log.info("tokenServer: " + tokenServer);
		M_log.info("key: ..." + this.key.substring(this.key.length() -3));
		M_log.info("secret: ..." + this.secret.substring(this.secret.length() -3));
		M_log.info("renewal: ..." + this.renewal.substring(this.renewal.length() -3));
	}
	
	public String getApiPrefix() {
		return apiPrefix;
	}

	public void setApiPrefix(String apiPrefix) {
		this.apiPrefix = apiPrefix;
	}
	
	//a token must be created at the time of construction
	//this token will allow use of the ESB APIs
	public String buildRenewal(String key, String secret) {
		String b64 = base64KeySecret(key, secret);
		b64 = "Basic " + b64; 
		return b64;
	}

	//esb calls require base 64 strings for authroization
	public String base64KeySecret(String key, String secret) {
		String keySecret = key + ":" + secret;
		byte[] binaryData = keySecret.getBytes();
		keySecret = Base64.encodeBase64String(binaryData);
		return keySecret;
	}

	//perform ESB request. If there is an exception, return exception result response.
	public WAPIResultWrapper doRequest(String request){
		M_log.info("doRequest: " + request);
		WAPIResultWrapper wrappedResult = null;
		JSONObject jsonObject = null;
		HttpResponse<String> response = null;
		try{
			response = Unirest.get(request)
					.header(AUTHORIZATION, this.token)
					.header("Accept", "json")
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
		return wrappedResult;
	}	
	
	//Error handling for bad calls
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
		M_log.info("token successfully renewed - token: ..." + this.token.substring(this.token.length() -3));
		return new WAPIResultWrapper(tokenResponse.getStatus(),"TOKEN RENEWED", new JSONObject(tokenResponse.getBody()));
	}
	
	//Specific request for renewing token
	public HttpResponse<JsonNode> runTokenRenewalPost(){
		M_log.info("runTokenRenewalPost() called");
		M_log.info("TokenServer: " + this.tokenServer);
		HttpResponse<JsonNode> tokenResponse = null;
		try{
			tokenResponse = Unirest.post(this.tokenServer)
					.header(CONTENT_TYPE, CONTENT_TYPE_PARAMETER)
					.header(AUTHORIZATION, this.renewal)
					.field(GRANT_TYPE, CLIENT_CREDENTIALS)
					.field(SCOPE, PRODUCTION)
					.asJson();
			M_log.debug(tokenResponse.getBody());
		}
		catch(Exception e){
			M_log.error("Error renewing token: " + tokenResponse.getStatusText());
			return null;
		}	
		return tokenResponse;
	}
		
}