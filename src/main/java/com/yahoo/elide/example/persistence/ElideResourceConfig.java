package com.yahoo.elide.example.persistence;

import com.yahoo.elide.Elide;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.dbmanagers.hibernate5.PersistenceManager;
import com.yahoo.elide.resources.JsonApiEndpoint;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.naming.ConfigurationException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Example application for resource config
 */
public class ElideResourceConfig extends ResourceConfig {
    public ElideResourceConfig() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                JsonApiEndpoint.DefaultOpaqueUserFunction noUserFn = v -> null;
                bind(noUserFn)
                        .to(JsonApiEndpoint.DefaultOpaqueUserFunction.class)
                        .named("elideUserExtractionFunction");

                Map<String, Object> configOverrides = new HashMap<String, Object>();

                Map<String, String> env = System.getenv();
                if (env.containsKey("DATABASE_URL")) {
                    URI dbUri = null;
                    try {
                        dbUri = new URI(System.getenv("DATABASE_URL"));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    String user = dbUri.getUserInfo().split(":")[0];
                    String password = dbUri.getUserInfo().split(":")[1];
                    String url = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

                    configOverrides.put("javax.persistence.jdbc.url", url);
                    configOverrides.put("javax.persistence.jdbc.user", user);
                    configOverrides.put("javax.persistence.jdbc.password", password);
                }

                EntityManagerFactory entityManagerFactory =
                        Persistence.createEntityManagerFactory("com.yahoo.elide.example", configOverrides);
                bind(new Elide(new Slf4jLogger(), new PersistenceManager(entityManagerFactory)))
                        .to(Elide.class).named("elide");
            }
        });
    }
}
