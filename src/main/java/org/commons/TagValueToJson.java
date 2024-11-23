package org.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TagValueToJson {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public void convertTagValueToJson(){

        Map<String, Map<String, String>> typeMap = new LinkedHashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode rootNode = objectMapper.createArrayNode();

        /* Assigning presence of group to each Object type */
        Map<String, Boolean> hasGroupMap = new HashMap<String, Boolean>()
        {
            {
                put("A", true);
                put("B", false);
            }
        };

        try {
            logger.info("reading the tag number mapping file..");
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\main\\resources\\tagNumberMapping.txt"));
            logger.info("reading the input file..");
            BufferedReader br2 = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\main\\resources\\input.txt"));

            /* Creating a map for each object type which holds the label for each tag*/
            String l;
            while ((l = br.readLine()) != null) {
                String[] types = l.split(":");
                if (types.length > 0) {
                    String type = types[0].trim();
                    String typeObj = types[1].trim();

                    Map<String, String> tagMap = new LinkedHashMap<>();

                    String[] tags = typeObj.split(";");
                    for (String t : tags) {
                        String[] keyVal = t.split("->");
                        if (keyVal.length == 2) {
                            tagMap.put(keyVal[0].trim(), keyVal[1].trim());
                        }
                    }
                    typeMap.put(type, tagMap);
                }
            }
            logger.info("Objects type and its mapping : " + typeMap);

            logger.info("iterating the input file and converting to json");

            String s;
            while ((s = br2.readLine()) != null) {
                String type = getObjectType(s);
                ObjectNode node = getJsonForInput(typeMap.get(type), hasGroupMap.get(type), s);
                logger.info("Json for the input string: " + node);
                rootNode.add(node);
            }

            logger.info("writing the json to output.json");
            writeToJson(rootNode);

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }

    /**
     * Split the input string and returns the value of tag 1.
     * This method splits the input string which holds tag and value pair and returns the value of tag 1.
     *
     * @param s input string.
     * @return value of tag 1 in string
     */
    public static String getObjectType(String s){
        return s.split("1=")[1].split(";")[0];
    }

    /**
     * Converts input string into Json.
     * This method takes the input string which holds tag and value pair and returns a Json for the input.
     *
     * @param tagMap contains the label for each tag in map.
     * @param hasGroup tells whether Object type has group or not (true or false)
     * @param input line from input string.
     * @return Json for the input string.
     */
    public static ObjectNode getJsonForInput(Map<String, String> tagMap, boolean hasGroup, String input){
        ObjectNode parentNode = new ObjectMapper().createObjectNode();
        ObjectMapper objectMapper = new ObjectMapper();
        List<ObjectNode> groups = new ArrayList<>();
        ObjectNode newGroup = null;
        boolean groupFlag = false;

        String[] pairs = input.split(";");
        for (String p : pairs) {
            if(p.isEmpty())
                continue;
            String[] keyVal = p.split("=");
            if (keyVal.length == 2) {
                String tag = keyVal[0].trim();
                String val = keyVal[1].trim();
                if (!hasGroup) {
                    addFieldToNode(parentNode, tagMap, tag, val);
                } else {
                    if (tag.equals("40")) {
                        if (!parentNode.has("GRPS")) {
                            parentNode.set("GRPS", objectMapper.createArrayNode());
                        }
                        if (groupFlag) {
                            groups.add(newGroup);
                        }
                        newGroup = objectMapper.createObjectNode();
                        addFieldToNode(newGroup, tagMap, tag, val);
                        groupFlag = true;
                    } else if (tag.equals("35") || tag.equals("87")) {
                        if (groupFlag) {
                            addFieldToNode(newGroup, tagMap, tag, val);
                        }
                    } else {
                        addFieldToNode(parentNode, tagMap, tag, val);
                    }
                }
            }
        }
        if(hasGroup) {
            if (groupFlag) {
                groups.add(newGroup);
            }
            ArrayNode groupNodes = objectMapper.createArrayNode();
            groupNodes.addAll(groups);
            parentNode.replace("GRPS", groupNodes);
        }

        return parentNode;
    }

    /**
     * Adds a field to Object Node.
     * This method takes the Object node and adds a field using label of the tag and value.
     *
     * @param node Object node that needs to be updated
     * @param tagMap contains the label for each tag in map.
     * @param tag holds tag
     * @param val holds value for the tag
     */
    public static void addFieldToNode(ObjectNode node, Map<String, String> tagMap, String tag, String val){
        String label = tagMap.get(tag);
        if (label != null) {
            node.put(label, val);
        } else {
            node.put(tag, val);
        }
    }

    /**
     * Writes the data to a Json file.
     * This method takes the array node and writes it a json file.
     *
     * @param rootNode data in ArrayNode format.
     */
    public static void writeToJson(ArrayNode rootNode){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), rootNode);
        } catch (IOException e) {
            logger.error("Exception occurred: ",e);
        }
    }
}
