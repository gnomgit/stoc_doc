package com.taiger.nlp.confluence_doc_generator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
public class CommandLineOptions {
	
	CommandLine cmd;
	
    public CommandLineOptions(String... args) {
    	Options options = new Options();

        Option user = new Option("u", "user", true, "user credentials in user:pass format");
        user.setRequired(true);
        options.addOption(user);

        Option title = new Option("t", "title", true, "page title");
        title.setRequired(true);
        options.addOption(title);
        
        Option ancestor = new Option("a", "ancestor", true, "id of the ancestor page, ie: 172982324");
        ancestor.setRequired(true);
        options.addOption(ancestor);
        
        Option key = new Option("k", "key", true, "key name of the space, ie: IM");
        key.setRequired(true);
        options.addOption(key);
        
        Option type = new Option("y", "type", true, "type of the documentation, ie:[DEV] for development or [CLI] for clients");
        type.setRequired(false);
        options.addOption(type);
        
        Option text = new Option("x", "text", true, "text to be inserted in the page");
        text.setRequired(false);
        options.addOption(text);
        
        Option version = new Option("v", "version", true, "version to be inserted in the page");
        version.setRequired(true);
        options.addOption(version);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        
        try {
            
        	cmd = parser.parse(options, args);
        
        } 	catch (ParseException e) {
            log.severe(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
            return;
        }
    }
    
    public CommandLineOptions checkConsistency () {
    	
    	Assert.hasText(cmd.getOptionValue('u'), "user can't be blank");
    	Assert.hasText(cmd.getOptionValue('t'), "title can't be blank");
    	Assert.hasText(cmd.getOptionValue('a'), "ancestor can't be blank");
    	Assert.hasText(cmd.getOptionValue('k'), "key can't be blank");
    	Assert.hasText(cmd.getOptionValue('v'), "version can't be blank");
    	
    	return this;
    }

    
}
