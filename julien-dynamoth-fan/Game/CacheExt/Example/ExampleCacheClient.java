package CacheExt.Example;

import CacheExt.CacheClient;

/**
 *
 * @author Julien Gascon-Samson
 */
public class ExampleCacheClient extends CacheClient {
	
    private int clientId = 0;

    public ExampleCacheClient(int clientId) {
        super();
        connectToServer("cache", "localhost", 1099);
        this.clientId = clientId;
    }

    @Override
    public void log(String msg)
    {
        System.out.println("Client[" + clientId + "]-" + msg);
    }
}
