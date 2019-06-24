package org.openntf.tiles.config;

import java.util.HashMap;
import java.util.Vector;

import org.openntf.domino.Document;
import org.openntf.domino.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A map of String -> database path.
 *
 * @author Mariusz Jakubowski
 *
 */
public class Databases extends HashMap<String, DBPath> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(Databases.class);

    /**
     * Caches info about all databases.
     * <p>
     * Configuration document can contain paths to other databases. 
     * These paths can be automatically read if they follow this convention:
     * <ul>
     * <li>server on which database is stored is saved in a field named
     * [name]Server</li>
     * <li>path to a database is stored in a field named [name]DB</li>
     * <li>http path to a database is stored in a field named [name]Web; 
     * this field is optional</li>
     * </ul>
     * where [name] is a symbolic name of a database, eg. mediaDB, mediaServer,
     * mediaWeb. <br>
     * This information is cached into {@link #dbInfo} field.
     *
     * @param cfg a document with configuration
     */
    public void cacheDatabases(final Document cfg) {
        Vector<Item> items = cfg.getItems();
        for (Item item : items) {
            if (item.getName().endsWith("DB")) {
                String name = item.getName().substring(0, item.getName().length() - 2);
                DBPath db = cacheDBPath(cfg, name);
                if (db != null) {
                    put(name, db);
                }
                LOG.info("db config {}={}", name, db.getFullPath());
            }
        }
    }

    /**
     * Cache info about a database.
     *
     * @param cfg a configuration document
     * @param key a name of a database
     * @return info about database
     * @see #cacheDatabases(lotus.domino.Document)
     */
    private DBPath cacheDBPath(final Document cfg, final String key) {
        String path = cfg.getItemValueString(key + "DB");
        String server = cfg.getItemValueString(key + "Server");
        if (server == null || server == "") {
            server = cfg.getParentDatabase().getServer();
        }
        String web = cfg.getItemValueString(key + "Web");
        return new DBPath(server, path, web);
    }

}
