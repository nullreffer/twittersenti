package com.thirdlabs.EconMachinaFeeder;

import org.json.JSONObject;

public interface DataHandler {

	public void OnFeed(JSONObject feed);
	
	public void HandleFeed(DataFeed feed);
}
