package com.thirdlabs.EconMachinaFeeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

public class TwitterStreamDataFeeder extends BaseAsyncDataFeeder {

	private final String token = "[REDACTED]";
	private final String tokenSecret = "[REDACTED]";
	private final String consumerKey = "[REDACTED]";
	private final String consumerSecret = "[REDACTED]";
	private final String track;
	private final String queryString;
	
	private HttpClient client;
	private HttpPost streamApi;
	
	int count = 0;
	public TwitterStreamDataFeeder(String track, String queryString, DataHandler feedParser) throws ClientProtocolException, IOException, GeneralSecurityException {
		super(feedParser);
		
		this.track = track;
		this.queryString = queryString;
		
		client = HttpClients.custom().setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
			public long getKeepAliveDuration(HttpResponse arg0, HttpContext arg1) {
				return Long.MAX_VALUE;
			}
		}).build();
		
		connect();
	}

	private void connect() throws ClientProtocolException, IOException, GeneralSecurityException
	{
		System.out.println("Connecting at: " + new Date());
		
		String url = "https://stream.twitter.com/1.1/statuses/filter.json";
		String urlQuery = "delimited=length&track=" + track + queryString;
		streamApi = new HttpPost(url + "?" + urlQuery);
		

		streamApi.setHeader("Accept-Encoding", "deflate, gzip"); //To use a GZIP Stream 

	    CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);

	    consumer.setTokenWithSecret(token, tokenSecret);
	    
	    try {
			consumer.sign(streamApi);
		} catch (Exception e) {
			throw new GeneralSecurityException(e);			
		}
	}
	
	public String encode(String value) 
	{
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuilder buf = new StringBuilder(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }
	
	private static String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException 
	{
	    SecretKey secretKey = null;

	    byte[] keyBytes = keyString.getBytes();
	    secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

	    Mac mac = Mac.getInstance("HmacSHA1");
	    mac.init(secretKey);

	    byte[] text = baseString.getBytes();

	    return new String(Base64.encodeBase64(mac.doFinal(text))).trim();
	}
	
	@Override
	public String getSourceName() {
		return "Twitter".intern();
	}
	
	@Override
	public void Feed() {
	
		HttpResponse response;
		HttpEntity entity;
		InputStream is;
		try {
			response = client.execute(streamApi);
			entity = response.getEntity(); 
			is = entity.getContent();
		} catch (Exception ee) { this.handleException(ee); return; }
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(is), 1024 * 128);
		int lengthBytes = 0;
		while (true) {
			try {
				int lengthBytesCount = 0;
			    do {
			        lengthBytes = 0;
			        try {
			        	String s = rd.readLine();
			        	lengthBytes = Integer.parseInt(s);
			        	
			        	Thread.sleep(100);
			        } catch (Exception e) {
			        	this.handleException(e);
			        }
			    } while (lengthBytes < 1 && lengthBytesCount++ < 12);
			  
			    char[] messageBuf = new char[lengthBytes];
			    rd.read(messageBuf, 0, lengthBytes);
			    
			    String message = new String(messageBuf);
			    
			    try {
			    	this.OnData(message);
			    } catch (Exception e)
			    {
			    	this.handleException(e);
			    }
			} catch (Exception e)
			{
				try {
					connect();
					response = client.execute(streamApi);
					entity = response.getEntity(); 
					is = entity.getContent();
					rd = new BufferedReader(new InputStreamReader(is), 1024 * 4);
				} catch (Exception ex) { this.handleException(ex); }
			}
			
			if (count++ % 100 == 0)
			{
				System.out.println("Stream proc-ed" + count);
			}
		}
	}

	private void handleException(Exception e) {
		System.out.println(e.toString());
	}
}
