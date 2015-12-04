/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt.Example;

import CacheExt.CacheClient;

/**
 *
 * @author fjin1
 */
public class TestClient extends CacheClient {
	
    private int clientId = 0;

    public TestClient(int clientId, String host, int port) {
        super();
        this.clientId = clientId;
        connectToServer("cache", host, port); // connect to server at specified host and port
    }

    @Override
    public void log(String msg)
    {
        System.out.println("TestClient[" + clientId + "]-" + msg);
    }
    
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            //params: client_id host port action action_params
            int id = Integer.parseInt(args[0]); // client id
            String host = args[1]; // rmiregistry host
            int port = Integer.parseInt(args[2]); // rmiregistry port
            String action = args[3]; // client action
            TestClient c = new TestClient(id, host, port);
            if (action.equals("putimage"))
            {
                // store an image in server
                String key = args[4];
                String src = args[5];
                c.load(key, new TestImage(key, src));
                c.unsubscribe(key);
            }
            else if (action.equals("getimage"))
            {
                // retrieve an image from server, then display
                String key = args[4];
                c.fetch(key);
                c.unsubscribe(key);
                TestImage img = (TestImage) c.retrieve(key);
                img.display(720);
            }
        }
    }
}
