package com.thirdlabs.EconMachinaFeeder;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public abstract class BaseAsyncDataFeeder {

	private final int FEEDER_THREAD_COUNT = 12;
	
	private ExecutorService executor;
	
	protected DataHandler dataHandler;
	
	public BaseAsyncDataFeeder(DataHandler dataHandler)
	{
		executor = Executors.newFixedThreadPool(FEEDER_THREAD_COUNT);
		this.dataHandler = dataHandler;
	}
	
	public abstract String getSourceName();
	
	public Future<Void> BeginFeed() {
		Future<Void> asyncFeeder = executor.submit(new Callable<Void>() {

			public Void call() throws Exception {
				Feed();
				return null;
			}
		});
		
		return asyncFeeder;
	}

	public abstract void Feed();
	
	public void OnData(String feedJson)
	{
		Object json = new JSONTokener(feedJson).nextValue();
		ArrayList<JSONObject> feedObjects = new ArrayList<JSONObject>();
		if (json instanceof JSONObject)
		{
			feedObjects.add((JSONObject)json);
		} else if (json instanceof JSONArray)
		{
			JSONArray jsonArray = (JSONArray)json;
			for (int i = 0; i < jsonArray.length(); i++) {
				feedObjects.add(jsonArray.getJSONObject(i));
			}
		}
		
		for (JSONObject j : feedObjects) {
			this.dataHandler.OnFeed(j);
		}
	}
}
