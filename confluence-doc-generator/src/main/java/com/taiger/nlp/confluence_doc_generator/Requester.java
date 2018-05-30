package com.taiger.nlp.confluence_doc_generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import lombok.extern.java.Log;

@Log
public class Requester {
	
	private Requester () {}

	public static String requestPost () {
		String json = "";
		String userNPass = Configuration.userNPassword;
		String ancestor = Configuration.ancestor;
		String key = Configuration.key;
		String text = Configuration.text;
		String typeNTitle = Configuration.getTypeNTilte();
		String postJson = Configuration.getJsonPost();
		
		postJson = postJson.replaceFirst("@KEY", key);
		postJson = postJson.replaceFirst("@TYPENTITLE", typeNTitle);
		postJson = postJson.replaceFirst("@ANCESTOR", ancestor);
		postJson = postJson.replaceFirst("@TEXT", text);
		
		HttpPost httppost = new HttpPost(Configuration.getConfluenceContentUrl());
		httppost.setHeader("Authorization", "Basic " + userNPass);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader("Accept", "application/json");
		HttpEntity httpEntity = new ByteArrayEntity(postJson.getBytes());
		httppost.setEntity(httpEntity);
		HttpClient client = HttpClientBuilder.create().build();
		try {
			
			HttpResponse response = client.execute(httppost);
			
			HttpEntity entity = response.getEntity();
			Header encodingHeader = entity.getContentEncoding();
			
			// you need to know the encoding to parse correctly
			Charset encoding = encodingHeader == null ? StandardCharsets.UTF_8 : 
			Charsets.toCharset(encodingHeader.getValue());
			
			// use org.apache.http.util.EntityUtils to read json as string
			json = EntityUtils.toString(entity, encoding);
			log.info(json);
			
		}	catch (IOException e) {
			
			log.severe(e.getMessage());
		}
		
		return json;
	}

	public static String requestGet (String url) {
		StringBuilder result = new StringBuilder();

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.setHeader("Authorization", "Basic " + Configuration.userNPassword);

		// add request header
		request.addHeader("User-Agent", "DocGenerator v0.1");
		HttpResponse response;
		try {
			response = client.execute(request);
			/*log.info("Response Code : " + response.getStatusLine().getStatusCode());*/

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		}	catch (IOException e) {
			log.severe(e.getMessage());
		}

		return result.toString().replace(",\"contact\":{},\"license\":{}", "");
	}
	
	
	
}
