package com.publictransportation.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

/**
 * This class is accountable for communicate with server, using HttpPost to send and receive JSONObject
 * 
 * @author Henio
 * @since 2014/02
 *
 */
public class ServerCommunication {
	
	private static final String SERVER_URL = "https://dashboard.appglu.com/v1/queries/";
	private static final String AUTHORIZATION = "Basic V0tENE43WU1BMXVpTThWOkR0ZFR0ek1MUWxBMGhrMkMxWWk1cEx5VklsQVE2OA==";
	private static final String APP_GLU = "staging";
	
	/**
	 * Create and send an HTTP request to server, and return a response
	 * 
	 * @param url
	 * @param paramsJson
	 * @return JSONObject
	 */
	public static JSONObject postData(final String url, final JSONObject paramsJson){
		JSONObject responseJson = new JSONObject();
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(ServerCommunication.SERVER_URL + url);
		
		Log.i("HttpPostUrl", httpPost.getURI().toString());
		
		try {
			StringEntity params = new StringEntity(paramsJson.toString());
			httpPost.setEntity(params);
			
			Log.i("sendJson Server", paramsJson.toString());

			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("Authorization", ServerCommunication.AUTHORIZATION);
			httpPost.setHeader("X-AppGlu-Environment", ServerCommunication.APP_GLU);
			
			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder data = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				data.append(line);
			}
			Log.i("responseJson Server", data.toString());
			responseJson = new JSONObject(data.toString());

		}catch (UnknownHostException e){
			Log.e("ServerCommunication - Internet Connection Error", e.toString());
		}catch (Exception e) {
			Log.e("ServerCommunication", e.toString());
		} 
		return responseJson;
	}	
}