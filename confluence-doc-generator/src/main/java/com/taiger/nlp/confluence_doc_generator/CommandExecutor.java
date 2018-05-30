package com.taiger.nlp.confluence_doc_generator;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import lombok.extern.java.Log;

@Log
public class CommandExecutor {
	
	private CommandExecutor () {}
	
	public static String executeCommand (String command) {
		StringBuilder output = new StringBuilder();
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} 	catch (Exception e) {
			log.severe(e.getMessage());
		}

		return output.toString();
	}
}
