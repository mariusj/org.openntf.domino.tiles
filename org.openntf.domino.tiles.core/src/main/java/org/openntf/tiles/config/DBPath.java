package org.openntf.tiles.config;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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

    private final List<String> servers;

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
        this.servers = Collections.unmodifiableList(Arrays.asList(server));
    }

    /**
     * Constructs a new instance of {@link DBPath}.
     * This constructor includes a list of servers. The preferred server
     * should be included as first in the list. Is is then returned in 
     * {@link #getServer()}.
     *
     * @param servers
     *            a list of servers
     * @param path
     *            path to a database
     * @param web
     *            optional web path
     */
    public DBPath(List<String> servers, String path, String web) {
        this.path = path;
        this.web = web;
        this.servers = Collections.unmodifiableList(servers);
        this.server = servers.get(0);
    }

    public String getServer() {
        return server;
    }

    public List<String> getServers() {
        return servers;
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
