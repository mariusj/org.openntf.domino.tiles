package org.openntf.tiles.runner;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.openntf.domino.Session;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.notes.NotesThread;

/**
 * A wrapper for accessing Domino databases running outside of Domino.
 * 
 * @author Mariusz Jakubowski
 *
 */
public class DominoRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DominoRunner.class);

    /**
     * Initialize the required libraries.
     * It have to be called once on startup.
     */
    public static void startup() {
        synchronized (Factory.class) {
            if (!Factory.isStarted()) {
                LOG.info("initializing Domino Factory");
                Factory.startup();
                LOG.info("initializing Domino C API");
                com.ibm.domino.napi.c.C.initLibrary(null);
                
                String userIdPassword = System.getProperty("xworlds.userid.password");
                if (userIdPassword != null) {
                    NotesThread.sinitThread();
                    try {
                        lotus.domino.Session sess = NotesFactory.createSession(
                                        (String) null, 
                                        (String) null, 
                                        userIdPassword);
                        LOG.info("created native session {}", sess.getEffectiveUserName());
                    } catch (NotesException e) {
                        e.printStackTrace();
                    }
                    NotesThread.stermThread();
                }
            }
        }
    }
    
    /**
     * Shutdown the ODA.
     */
    public static void shutdown() {
        synchronized (Factory.class) {
            if (Factory.isStarted()) {
                Factory.shutdown();
            }            
        }
    }
    
    /**
     * Initializes the Domino thread/
     */
    public static void initThread() {
        LOG.info("initializing Domino thread");
        if (!Factory.isStarted()) {
            Factory.startup();
        }
        Factory.initThread(Factory.STRICT_THREAD_CONFIG);
        Factory.setSessionFactory(
                Factory.getSessionFactory(SessionType.NATIVE), 
                SessionType.CURRENT);
        NotesThread.sinitThread();
    }

    /**
     * Cleans up a thread running a Domino connection.
     */
    public static void termThread() {
        LOG.info("terminating Domino thread");
        NotesThread.stermThread();
        Factory.termThread();
    }

    /**
     * Runs a code in a Domino thread.
     * @param supplier
     * @return
     */
    public static <T> T runDomino(final Supplier<T> supplier) {
        boolean threadInitialized = Factory.isInitialized();
        if (!threadInitialized) {
            initThread();
        }
        try {
            return supplier.get();
        } finally {
            if (!threadInitialized) {
                termThread();
            }
        }
    }
    
    /**
     * Runs a code in a Domino thread.
     * @param supplier
     * @return
     * @throws Exception 
     */
    public static <T> T runDominoEx(final Callable<T> supplier) throws Exception {
        boolean threadInitialized = Factory.isInitialized();
        if (!threadInitialized) {
            initThread();
        }
        try {
            return supplier.call();
        } finally {
            if (!threadInitialized) {
                termThread();
            }
        }
    }
    

    /**
     * Returns a session.
     * @return a session
     */
    public static Session getSession() {
        Session session = Factory.getSession(SessionType.NATIVE);
        session.setConvertMIME(false);
        return session;
    }

}
