/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.atos.ari.vital.conf;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

public class ConfigReader {
    
    private static ConfigReader instance;
    private Properties config;
    
    public static final String MONGO_URL = "vital-core-cep.mongo.uri";
       
    private ConfigReader() {
        String fileName = System.getProperty("jboss.server.config.dir") + "/vital-properties.xml";
        File file = new File(fileName);

        config = new Properties();
        
        try {
            config.loadFromXML(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }
    
    public String get(String key) {
        return config.getProperty(key);
    }
}
