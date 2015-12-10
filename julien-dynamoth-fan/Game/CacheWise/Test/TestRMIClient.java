/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise.Test;

import CacheWise.Operation;
import static CacheWise.Test.TestClient.wait;

/**
 *
 * @author fjin1
 */
public class TestRMIClient extends TestClient {
    
    public TestRMIClient(int clientId, String host, int port) {
        super(clientId, host, port);
    }
    
    public TestRMIClient(int clientId, String host, int port, String operation, String filePrefix) {
        super(clientId, host, port, operation, filePrefix);
    }
    
    @Override
    public void log(String msg)
    {
        System.out.println("TestRMIClient[" + super.clientId + "]-" + msg);
    }
    
    @Override
    public void operationReceived(String key, Operation operation)
    {
        // fetch the latest copy from server, only if it's not from itself
        if (!op.equals(operation.getName())) remoteGet(key);
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
            if (action.equals("flip"))
            {
                String key = args[4];
                int interval = Integer.valueOf(args[5]);
                TestRMIClient c = new TestRMIClient(id, 
                        host, 
                        port, 
                        "flipHorizontal",
                        "../results/"+"rmi-client-"+id+"-flip-"+key+"-response-time-");
                // Operation: flipHorizontal
                // Count: 100 times at n seconds interval
                // retrieve the image
                c.remoteGet(key);
                // start sigar
                TestMonitor.main(new String[] {"rmi-client-"+id+"-flip-"+key+"-"});
                // wait 30 seconds before begin
                wait(30);
                for (int i = 0; i < 100; i++)
                {
                    Operation o = new Operation(c.getNextVersion(key), "flipHorizontal");
                    c.setStartTime(System.nanoTime());
                    c.localGet(key).applyUpdate(o);
                    c.remotePut(key, c.localGet(key));
                    c.setEndTime(System.nanoTime());
                    c.logResponseTime();
                    c.publishOperation(key, o);
                    wait(interval + (int)(Math.random() * (interval + 1))); // wait randomly to avoid collision
                }
                // wait for 30 seconds for sigar log to flatten
                wait(30);
                System.exit(0);
            }
            else if (action.equals("rotate"))
            {
                String key = args[4];
                int interval = Integer.valueOf(args[5]);
                TestRMIClient c = new TestRMIClient(id, 
                        host, 
                        port, 
                        "rotateClockwise",
                        "../results/"+"rmi-client-"+id+"-rotate-"+key+"-response-time-");
                // Operation: rotateClockwise(180)
                // Count: 100 times at n seconds interval
                // retrieve the image
                c.remoteGet(key);
                // start sigar
                TestMonitor.main(new String[] {"rmi-client-"+id+"-rotate-"+key+"-"});
                // wait 30 seconds before begin
                wait(30);
                for (int i = 0; i < 100; i++)
                {
                    Operation o = new Operation(c.getNextVersion(key), "rotateClockwise", 180);
                    c.setStartTime(System.nanoTime());
                    c.localGet(key).applyUpdate(o);
                    c.remotePut(key, c.localGet(key));
                    c.setEndTime(System.nanoTime());
                    c.logResponseTime();
                    c.publishOperation(key, o);
                    wait(interval + (int)(Math.random() * (interval + 1))); // wait randomly to avoid collision
                }
                // wait for 30 seconds for sigar log to flatten
                wait(30);
                System.exit(0);
            }
        }
    }
}
