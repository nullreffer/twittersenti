package com.thirdlabs.EconMachinaFeeder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Queue;

import org.bson.Document;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class QueueListener implements Runnable {

	MongoClient mongoClient;
	MongoDatabase db;
	Queue<DataFeed> queue;
	
	public QueueListener(Queue<DataFeed> queue)
	{
		this.mongoClient = new MongoClient();
		this.db = mongoClient.getDatabase("econmachina");
		this.queue = queue;
	}
	
	public void run() {
		
		while (true)
		{
			DataFeed df = this.queue.poll();
			while (df != null)
			{
				insertFeed(df);
				df = this.queue.poll();
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// eat it
			}
		}
	}
	
	private void insertFeed(DataFeed feed)
	{
		MongoCollection<Document> dbCollection = db.getCollection("socialfeeds");
		
		Document doc = new Document();
		doc.append("createdTime", feed.createdTime);
		doc.append("source", feed.source);
		doc.append("id", feed.id);
		doc.append("text", feed.text);
		doc.append("locale", feed.locale);
		doc.append("feedLocation", feed.feedLocation);
		doc.append("userId", feed.userId);
		doc.append("insertedTime", feed.insertedTime);
		
		doc.append("properties", feed.properties);
		
		try {
			dbCollection.insertOne(doc);
		} catch (Exception e) {
			System.out.println(feed.createdTime + ": " + e.getMessage());
		}
		
		// db.getCollection("restaurants").insertOne(
	}

}
