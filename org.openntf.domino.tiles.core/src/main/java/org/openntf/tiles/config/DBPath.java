package org.openntf.tiles.config;


import java.io.Serializable;


/**
 * Information about database server and path.
 *
 * @author Mariusz Jakubowski
 *
 */
public final class DBPath implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String server;
    private final String path;
    private final String web;

    /**
     * Constructs a new instance of {@link DBPath}.
     *
     * @param server
     *            name of a server
     * @param path
     *            path to a database
     * @param web
     *            optional web path
     */
    public DBPath(final String server, final String path, final String web) {
        this.server = server;
        this.path = path;
        this.web = web;
    }

    public String getServer() {
        return server;
    }

    public String getPath() {
        return path;
    }

    public String getWeb() {
        return web;
    }

    public String getFullPath() {
        return server + "!!" + path;
    }

    @Override
    public String toString() {
        return server + "!!" + path;
    }
}
