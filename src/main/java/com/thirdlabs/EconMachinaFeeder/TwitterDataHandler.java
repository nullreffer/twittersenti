package com.thirdlabs.EconMachinaFeeder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

public class TwitterDataHandler implements DataHandler {

	private final String TWITTER_SOURCE="Twitter";
	private final String TWITTER_DATEFORMAT="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
	
	private SimpleDateFormat sf = new SimpleDateFormat(TWITTER_DATEFORMAT);
	private Queue<DataFeed> queue;
	
	public TwitterDataHandler(Queue<DataFeed> queue)
	{
		this.queue = queue;
		
		sf.setLenient(true);
	}
	
	public void OnFeed(JSONObject feed) {
		
		DataFeed df = new DataFeed();
		
		df.createdTime = getTwitterDate(getString(feed, "created_at"));
		df.feedLocation = getString(feed, "geo") + "|" + getString(feed, "coordinates");
		df.id = getString(feed, "id");
		
		if (feed.has("metadata"))
		{
			df.locale = getString(feed.getJSONObject("metadata"), "iso_language_code");
		}
		df.source = TWITTER_SOURCE;
		df.text = getString(feed, "text");
		df.userId = getString(feed.getJSONObject("user"), "id");
		
		df.properties.put("user_location", getString(feed.getJSONObject("user"), "location"));
		
		// country from timezone
		// age, gender, income level, marital status
		
		HandleFeed(df);
	}
	
	public void HandleFeed(DataFeed feed)
	{
		if (!feed.text.startsWith("RT ")) {
			this.queue.add(feed);
		}
	}

	private String getString(JSONObject feed, String field)
	{
		if (feed == null || !feed.has(field) || feed.isNull(field))
			return "";
		
		Object fval = feed.get(field);
		
		if (fval == null)
			return "";
		
		return fval.toString();
	}
	
	public Date getTwitterDate(String date) {
		try {
			return sf.parse(date);
		} catch (Exception e)
		{
			return null;
		}
	}
}
