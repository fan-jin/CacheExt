package CacheWise.Impl;

import CacheWise.CacheClient;

/**
 *
 * @author Julien Gascon-Samson
 */
public class ReplicaInstance extends CacheClient {
	
    private int clientId = 0;

    public ReplicaInstance(int clientId, String host, int port) {
        super();
        connectToServer("cache", host, port); // connect to server at specified host and port
        this.clientId = clientId;
    }
    
    public ReplicaInstance(int clientId) {
        super();
        connectToServer("cache", "localhost", 1099); // connect to server at localhost:1099 (default)
        this.clientId = clientId;
    }

    @Override
    public void log(String msg)
    {
        System.out.println("Client[" + clientId + "]-" + msg);
    }
}
