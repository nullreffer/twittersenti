package com.thirdlabs.EconMachinaFeeder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.AbstractQueue;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

import org.apache.http.client.ClientProtocolException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ClientProtocolException, IOException, GeneralSecurityException
    {
    	while (true) {
	    	try {
		    	AbstractQueue<DataFeed> queue = new ConcurrentLinkedQueue<DataFeed>();
		    	
		    	new Thread(new QueueListener(queue)).start();
		    	
		    	String track = "netflix,hulu,amazon video,amazon prime video,prime video,hbo now,sling tv,slingtv,sling,comcast,cable";
		    	String trackData = URLEncoder.encode(track, "UTF-8");
		    	String trackQuery = URLEncoder.encode("\"" + track.replaceAll(",", "\" OR \"") + "\"", "UTF-8");
		    	
		    	String queryString = "&language=en";
		    	
		    	DataHandler tweetParser = new TwitterDataHandler(queue);
		        
		    	TwitterStreamDataFeeder tweetFeeder = new TwitterStreamDataFeeder(trackData, queryString, tweetParser);
		        tweetFeeder.BeginFeed();
		        
		        if (args.length > 0)
		        {
		        	String sinceDate = args[0];
		        	String untilDate = args[1];
		        	queryString += "&until=" + untilDate + "&since=" + sinceDate;
			        TwitterApiDataFeeder tweetFeeder2 = new TwitterApiDataFeeder(trackQuery, queryString, tweetParser);
			        tweetFeeder2.BeginFeed();
		        }
		        
		        System.out.println("Press return to exit");
		        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		        bufferedReader.readLine();
	    	} catch (Exception e)
	    	{
	    		System.out.println("Restarting: " + e.toString());
	    	}
    	}
    }
}
