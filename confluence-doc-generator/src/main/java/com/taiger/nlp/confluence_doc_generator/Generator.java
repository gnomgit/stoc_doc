package com.taiger.nlp.confluence_doc_generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.util.Assert;

import lombok.extern.java.Log;

/**
 * 
 *
 */
@Log
public class Generator {
	
	public static void main( String[] args ) {
		
    	CommandLineOptions clo = new CommandLineOptions(args).checkConsistency();
    	Configuration.loadProperties();
    	Configuration.loadOptions(clo);
    	generate ();
    }
    
    private static void generate () {
    	log.info("GETTING ANCESTOR PAGE\n");
    	Configuration.id = getId(Requester.requestGet(createCheckPageUrl()));
    	if (Configuration.id == null) {
    		log.info("CREATING ANCESTOR PAGE\n");
    		Configuration.id = analyseResponseForId (Requester.requestPost());
    	}
    	log.info("ANCESTOR PAGE: " + Configuration.id + "\n");
    	Assert.notNull(Configuration.id, "page not created");
    	Configuration.getEndPointsList().forEach(ep -> {
    		String fileName = ep + ".json";
    		log.info("WORKING ON: " + fileName + "\n");
    		String json = Requester.requestGet(Configuration.getSpecificUrl(ep));
    		if (json != null && !json.isEmpty()) {
	    		if (Configuration.isClientDoc()) {
	    			json = reduceJsonCLI (json);
	    		}
	    		if (!json.isEmpty()) {
	    			log.info("GENERATING FILE: " + fileName + "\n");
	    			generateFile(fileName, json);
	    			log.info("UPLOADING PAGE: " + endPointTitle(ep) + "\n");
	    			log.info(CommandExecutor.executeCommand(createCommand(ep)));
	    		}
    		}	else {
    			log.severe("WRONG REQUEST ANSWER: " + json + "\nCHECK " + Configuration.getSpecificUrl(ep));
    		}
    		
    	});
    	log.info("FINISHED\n");
	}
    
    public static String reduceJsonCLI (String json) {
    	StringBuilder result = new StringBuilder("{");
        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = null;
		try {
			rootNode = mapper.readTree(json);
		} 	catch (IOException e) {
			log.severe(e.getMessage());
		}  
		
		Assert.notNull(rootNode);

        Iterator<Map.Entry<String,JsonNode>> fieldsIterator = rootNode.getFields();
        while (fieldsIterator.hasNext()) {

            Map.Entry<String,JsonNode> field = fieldsIterator.next();
            
            if (field.getKey().equals("definitions")) {
            	result.append("\"definitions\":{}");
            	//field.setValue(mapper.readTree("{\"definitions\":{}}").getFields().next().getValue());
            }	else if (field.getKey().equals("paths")) {
            	String pathsJson = field.getValue().toString();
            	JsonNode rootPathsNode = null;
            	Iterator<Map.Entry<String,JsonNode>> pathsFieldsIterator = null;
				try {
					rootPathsNode = mapper.readTree(pathsJson);
					pathsFieldsIterator = rootPathsNode.getFields();
				} 	catch (IOException e) {
					log.severe(e.getMessage());
				}
				
				Assert.notNull(rootPathsNode);
				Assert.notNull(pathsFieldsIterator);
            	
            	int count = 0;
            	result.append("\"paths\":" + "{");
            	while (pathsFieldsIterator.hasNext()) {
            		 Map.Entry<String,JsonNode> path = pathsFieldsIterator.next();
            		 boolean copied = false;
            		 
            		 if (path.getValue().toString().contains(Configuration.getCliDocKey())) {
            			 result.append("\"" + path.getKey() + "\":");
            			 result.append(path.getValue().toString());
            			 copied = true;
            			 count++;
            		 }
            		 
            		 if (copied && pathsFieldsIterator.hasNext()) result.append(",");
            	}
            	if (count == 0) return "";
            	if (result.charAt(result.length()-1) == ',') result.deleteCharAt(result.length()-1);
            	result.append("}");
            	
            }	else {
            	result.append("\"" + field.getKey() + "\":" + field.getValue());
            }
            
            if (fieldsIterator.hasNext()) {
            	result.append(",");
            }
            
        }
        result.append("}");
        return result.toString();
    }

	private static String createCommand(String endPoint) {
		String command = Configuration.getSwaggerConfluenceCommand();
		command = command.replaceFirst("@CONFLUENCEAPI", Configuration.getConfluenceUrl())
				.replaceFirst("@TYPE", Configuration.type)
				.replaceFirst("@KEY", Configuration.key)
				.replaceFirst("@ENDPOINTFILE", endPoint + ".json")
				.replaceFirst("@ENDPOINTNAME", endPointTitle(endPoint))
				.replaceFirst("@USERNPASS", Configuration.userNPassword)
				.replaceFirst("@ANCESTOR", Configuration.id);
				
		log.info(command);
		return command;
	}
	
	private static String endPointTitle (String endPoint) {
		return  endPoint + "-" + Configuration.version;
	}
	
	private static String createCheckPageUrl () {
		return Configuration.getCheckPageUrl()
				.replaceFirst("@TYPENTITLE", encode(Configuration.getTypeNTilte()))
				.replaceFirst("@KEY", Configuration.key);
	}

	private static String analyseResponseForId (String jsonTestResponse) {
		JSONObject object = new JSONObject(jsonTestResponse);
		String[] keys = JSONObject.getNames(object);
		for (String key : keys) {
			if (key.equals("id")) {
				try {
					return (String) object.get(key);
				}	catch (Exception e) {
					log.severe(e.getMessage());
				}    
		    }
		}
		
		return null;
	}
	
	private static String getId (String jsonResponse) {
		String result = "";
		Pattern p = Pattern.compile("\"id\":\"[0-9]*\",");
		Matcher m = p.matcher(jsonResponse);
		if (m.find()) {
			Assert.hasText(m.group());
			Assert.isTrue(m.group().contains(":"));
			result = m.group().split(":")[1].replace("\"", "").replaceAll(",", "");
			return result;
		}
		return null;
	}
	
	private static void generateFile (String path, String text) {
		try {
			File file = new File(path);
  
			if (file.exists() && !file.delete()) {
				log.severe(path + " exists and couldn't be deleted");
				return;
			}
			
			if (file.createNewFile()){
				log.info(path + " created\n");
			}	else {
				log.severe(path + " couldn't be created");
				return;
			}
			
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
				writer.write(text);
			}
      
		} 	catch (IOException e) {
			log.severe(e.getMessage());
		}
	}
	
	public static String encode (String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		}	catch (UnsupportedEncodingException e) {
			log.severe(e.getMessage());
		}
		return null;
    }
    
}
