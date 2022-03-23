package com.thirdlabs.EconMachinaFeeder;

public class WebCrawlerDataFeeder extends BaseAsyncDataFeeder {

	private String name;
	
	public WebCrawlerDataFeeder(String name, String uri, WebCrawlerDataHandler dataHandler) {
		super(dataHandler);
		
		this.name = name;
	}
	
	@Override
	public void Feed() {
		// TODO

	}

	@Override
	public String getSourceName() {
		return this.name;
	}
}
