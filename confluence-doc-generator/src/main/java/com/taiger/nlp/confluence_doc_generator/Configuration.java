package com.taiger.nlp.confluence_doc_generator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
public class Configuration {
	
	private static Properties properties;
	public static String userNPassword;
	public static String id;
	public static String ancestor;
	public static String title;
	public static String key;
	public static String text;
	public static String type;
	public static String version;
	
	
	private Configuration () {}
	
	public static void loadProperties () {
		String appConfigPath = "src/main/resources/config.properties";
		properties = new Properties();
		try {
			properties.load(new FileInputStream(appConfigPath));
		} 	catch (IOException e) {
			log.severe(e.getMessage());
		}
	}
	
	public static void loadOptions (CommandLineOptions clo) {
		userNPassword = clo.getCmd().getOptionValue('u');
		ancestor = clo.getCmd().getOptionValue('a');
		title = clo.getCmd().getOptionValue('t');
		key = clo.getCmd().getOptionValue('k');
		version = clo.getCmd().getOptionValue('v');
		text = (clo.getCmd().hasOption('x')) ? clo.getCmd().getOptionValue('x') : "";
		type = (clo.getCmd().hasOption('y')) ? clo.getCmd().getOptionValue('y') : "";
	}
	
	public static List<String> getEndPointsList () {
		List<String> eps = new ArrayList<>();
		
		String csList = properties.getProperty("endPoints");
		Assert.hasText(csList, "no end point list defined");
		Arrays.asList( csList.split(",")).forEach(eps::add);
		
		return eps;
	}
	
	private static String getDocUrl () {
		String url = properties.getProperty("docUrl");
		Assert.notNull(url, "no docUrl defined");
		
		return url;
	}
	
	public static String getConfluenceUrl () {
		String url = properties.getProperty("confluenceUrl");
		Assert.notNull(url, "no confluenceUrl defined");
		
		return url;
	}
	
	public static String getConfluenceContentUrl () {
		String url = properties.getProperty("confluenceUrlContent");
		Assert.notNull(url, "no confluenceUrlContent defined");
		
		return url;
	}
	
	private static String getIMatchUrl () {
		String url = properties.getProperty("imatchUrl");
		Assert.notNull(url, "no imatchUrl defined");
		
		return url;
	}
	
	private static String getGetPageUrl () {
		String url = properties.getProperty("getPageUrl");
		Assert.notNull(url, "no getPageUrl defined");
		
		return url;
	}
	
	public static String getSpecificUrl (String endPoint) {
		Assert.hasText(endPoint, "end point not defined or empty");
		String docUrl = getDocUrl();
		String iMatchUrl = getIMatchUrl();
		
		return iMatchUrl + (endPoint.equals("gateway")? "": endPoint) + docUrl;
	}
	
	public static String getSwaggerConfluenceCommand () {
		String json = properties.getProperty("swagger-confluence-command");
		Assert.hasText(json, "no swagger-confluence command defined");
		
		return json;
	}
	
	public static String getJsonPost () {
		String json = properties.getProperty("postJson");
		Assert.hasText(json, "no json defined");
		
		return json;
	}
	
	public static String getJsonTestResponse () {
		String json = properties.getProperty("testJsonResponse");
		Assert.hasText(json, "no test response json defined");
		
		return json;
	}

	public static String getTestCommand () {
		String command = properties.getProperty("testSCCommand");
		Assert.hasText(command, "no test command defined");
		
		return command;
	}
	
	public static String getCheckPageUrl () {
		String confluenceUrl = getConfluenceUrl();
		String getPageUrl = getGetPageUrl();
		
		return confluenceUrl + getPageUrl;
	}
	
	public static String getTypeNTilte () {
		String type = Configuration.type;
		String version = Configuration.version;
		
		return type + " " + Configuration.title + " " + version;
	}
	
	public static String getCliDocKey () {
		String key = properties.getProperty("cliDocKey");
		Assert.hasText(key, "no client key defined");
		
		return key;
	}
	
	public static boolean isClientDoc () {
		Assert.notNull(properties, "must load properties");
		String key = getCliDocKey();
		Assert.notNull(type, "must have type in command line call");
		
		return type.contains(key);
	}
}
