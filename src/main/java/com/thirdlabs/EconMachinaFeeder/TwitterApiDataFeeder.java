package com.thirdlabs.EconMachinaFeeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class TwitterApiDataFeeder extends BaseAsyncDataFeeder {

	private final String consumerKey = "[REDACTED]";
	private final String consumerSecret = "[REDACTED]";
	
	private final String track;
	private final String queryString;
	private String fqs = null;
	private String maxId = "999999999999999999999999"; // "720255445617799168";
	
	private HttpClient client;
	private String authToken;
	
	private int count = 0;
	
	public TwitterApiDataFeeder(String track, String queryString, DataHandler feedParser) throws ClientProtocolException, IOException, GeneralSecurityException {
		super(feedParser);

		this.track = track;
		this.queryString = queryString;
		
		this.client = HttpClientBuilder.create().build();
		this.authToken = getAuthToken();
	}
	
	private String getAuthToken() throws ClientProtocolException, IOException
	{
		String token = null;
		
		HttpPost httpPost = new HttpPost("https://api.twitter.com/oauth2/token");
		httpPost.setEntity(new UrlEncodedFormEntity(Collections.singletonList(new BasicNameValuePair("grant_type", "client_credentials"))));
		httpPost.addHeader("Authorization", "Basic " + Base64.encodeBase64String((this.consumerKey + ":" + this.consumerSecret).getBytes(StandardCharsets.UTF_8)));
		HttpResponse response = client.execute(httpPost);
		
		HttpEntity entity = response.getEntity();
		
		JSONObject json = new JSONObject(EntityUtils.toString(entity));
		token = json.getString("access_token");
		
		token = URLDecoder.decode(token, "UTF-8");
		
		return token;
	}

	private void addMaxId()
	{
		String[] queryparams = fqs.split("&");
		StringBuffer result = new StringBuffer();
		boolean hasMax = false;
		boolean hasCount = false;
		for (int x = 0; x < queryparams.length; x++)
		{
			if (queryparams[x] != null && queryparams[x].startsWith("?"))
			{
				queryparams[x] = queryparams[x].substring(1);
			}
			
			if (queryparams[x] != null && queryparams[x].startsWith("count"))
			{
				hasCount = true;
			}
			
			if (queryparams[x] != null && queryparams[x].startsWith("max_id"))
			{
				hasMax = true;
				queryparams[x] = "max_id=" + maxId;
			}
			
			if (queryparams[x] != null && queryparams[x].startsWith("since_id"))
			{
				queryparams[x] = "";
			}
			
			result.append(queryparams[x] + "&");
		}
		
		if (!hasMax)
		{
			result.append("max_id=" + maxId + "&");
		}
		
		if (!hasCount)
		{
			result.append("count=100");
		}
		
		
		fqs = "?" + result.toString();
	}
	
	@Override
	public void Feed() {
		// TODO Auto-generated method stub
		HttpResponse response;
		HttpEntity entity;
		
		while (true) {
			try {
				fqs = fqs != null ? fqs : "?q=" + track + "&count=100&result_type=recent" + queryString; // &until=2016-04-17 // + "&" + (lastId[0].isEmpty() ? "" : ("since_id=" + lastId[0]));
				
				addMaxId();
				
				HttpGet api = new HttpGet("https://api.twitter.com/1.1/search/tweets.json" + fqs);
				api.addHeader("Authorization", "Bearer " + authToken);
				
				response = client.execute(api);
				
				if (response.containsHeader("x-rate-limit-remaining"))
				{
					if (Integer.parseInt(response.getFirstHeader("x-rate-limit-remaining").getValue()) <= 1)
					{
						System.out.println("Waited " + new Date());
						entity = response.getEntity();
						EntityUtils.consume(entity);
						Thread.sleep(1000 * 60 * 15);
						continue;
					}
				}
				
				entity = response.getEntity(); 
			} catch (Exception ee) { this.handleException(ee); continue; }
			
			try {
				
			    String message = EntityUtils.toString(entity);
			    
			     this.OnData(message);
			    
			    System.out.println("Last ID (" + ++this.count +"): " + this.fqs);
			} catch (Exception e)
			{
				try {
					authToken = getAuthToken();
				} catch (Exception ex) { this.handleException(ex); }
			}
			
			try {
				Thread.sleep(1000);
			} catch (Exception ex) { this.handleException(ex); }
		}
	}
	
	@Override
	public void OnData(String feedJson) {
		
		JSONObject json = (JSONObject) new JSONTokener(feedJson).nextValue();
		
		if (json.has("statuses"))
		{
			JSONArray statuses = json.getJSONArray("statuses");
			
			System.out.println("Statuses count: " + statuses.length());
			String minId = "99999999999999999999999999999";
			for (int s = 0; s < statuses.length(); s++)
			{
				JSONObject o = statuses.getJSONObject(s);
				
				minId = minId.compareTo(o.get("id").toString()) < 0 ? minId : o.get("id").toString();
				this.dataHandler.OnFeed(o);
			}
			maxId = minId;
			
			this.fqs = json.getJSONObject("search_metadata").has("next_results") ? json.getJSONObject("search_metadata").getString("next_results") : json.getJSONObject("search_metadata").getString("refresh_url");
		}
	};
	
	private void handleException(Exception e) {
		System.out.println(e.toString());
		try {
		Thread.sleep(100);
		} catch (Exception e1) {}
	}

	@Override
	public String getSourceName() {
		return "Twitter".intern();
	}

}
