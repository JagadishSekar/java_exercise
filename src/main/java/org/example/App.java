package org.example;

import org.commons.TagValueToJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * App.java
 *
 */
public class App
{
    private final static Logger logger = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws IOException {

        logger.info("reading the input file..");
        BufferedReader input_br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\main\\resources\\input.txt"));

        logger.info("reading the tag number mapping file..");
        BufferedReader tag_label_map_br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\main\\resources\\tagNumberMapping.txt"));

        /* Assigning presence of group to each Object type */
        Map<String, Boolean> hasGroupMap = new HashMap<String, Boolean>()
        {
            {
                put("A", true);
                put("B", false);
            }
        };

        String outputFileName = "output.json";

        TagValueToJson obj = new TagValueToJson();
        obj.convertTagValueToJson(input_br, tag_label_map_br, hasGroupMap, outputFileName);
    }
}
