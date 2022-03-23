package com.thirdlabs.EconMachinaFeeder;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class DataFeed {

	public String source;
	public String id;
	public String text;
	public String locale;
	public Date createdTime;
	public String feedLocation;
	public String userId;
	
	public Map<String, String> properties = new TreeMap<String, String>();
	
	public Date insertedTime;
	
	public DataFeed()
	{
		this.insertedTime = new Date();
	}
	
	// CREATE TABLE (source nvarchar(32) not null, id nvarchar(128) not null, text nvarchar(max) not null, 
	// locale nvarchar(8), createdTime (datetime), feedLocation nvarchar(256), userId nvarchar(128), properties nvarchar(max) 
	// CONSTRAINT PK_feed PRIMARY KEY NONCLUSTERED (source, id) )
	
	@Override
	public String toString()
	{
		
		StringBuffer sb = new StringBuffer();
		sb.append("\nSource: " + source);
		sb.append("\nId: " + id);
		sb.append("\nText: " + text);
		sb.append("\nLocale: " + locale);
		sb.append("\nCreated: " + createdTime);
		sb.append("\nLocation: " + feedLocation);
		sb.append("\nUser: " + userId);
		
		for (String k : properties.keySet())
		{
			sb.append("\n" + k + ": " + properties.get(k));	
		}
		
		return sb.toString();
	}
}
