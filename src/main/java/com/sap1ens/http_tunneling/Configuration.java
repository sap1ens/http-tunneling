package com.sap1ens.http_tunneling;

import java.io.IOException;
import java.util.Properties;

/**
 * @author sap1ens
 */
public class Configuration {

    private final String configFilename = "/app.properties";

    private static Configuration instance;

    private Integer serverPort;
    private String clientUri;

    public Configuration() throws IOException {
        load();
    }

    public void load() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream(configFilename));

        this.serverPort = Integer.valueOf(properties.getProperty("server.port"));
        this.clientUri = properties.getProperty("client.uri");
    }

    public static synchronized Configuration getInstance() throws IOException {
        if(instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public String getClientUri() {
        return clientUri;
    }
}
