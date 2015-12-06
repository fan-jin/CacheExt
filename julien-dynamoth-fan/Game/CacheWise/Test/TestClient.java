/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise.Test;

import CacheWise.CacheClient;
import CacheWise.Operation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author fjin1
 */
public class TestClient extends CacheClient {
	
    protected int clientId = 0;
    
    protected long[] responseTime = new long[2]; // [0] for send, [1] for receive
    protected String op; // filter on operation name
    
    protected FileWriter fw;
    protected String fileName;
    
    public TestClient(int clientId, String host, int port) {
        super();
        this.clientId = clientId;
        connectToServer("cache", host, port); // connect to server at specified host and port
    }
    
    public TestClient(int clientId, String host, int port, String operation, String filePrefix) {
        this(clientId, host, port);
        op = operation; // set operation name
        DateFormat dF = new SimpleDateFormat("yyyy_MM_dd HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        fileName = dF.format(cal.getTime())+ ".csv";
        fileName = filePrefix + fileName; // prepend prefix
        File fp = new File(fileName);
        try{
            if (fp.createNewFile()){
                fw = new FileWriter(fileName, true);
                log("Create log file: " + fileName);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public void setSendTime(long t)
    {
        responseTime[0] = t;
    }
    
    public void setReceiveTime(long t)
    {
        responseTime[1] = t;
    }
    
    public void logResponseTime()
    {
        try {
            if (fw != null){
                fw.write((responseTime[1] - responseTime[0]) + "\n");
                fw.flush();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String msg)
    {
        System.out.println("TestClient[" + clientId + "]-" + msg);
    }
    
    @Override
    public void operationReceived(String key, Operation operation)
    {
        super.operationReceived(key, operation);
        // log on matching operation name
        if (operation.getName().equals(op))
        {
            setReceiveTime(System.nanoTime());
            logResponseTime();
        }
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
            if (action.equals("putimage"))
            {
                TestClient c = new TestClient(id, host, port);
                // store an image in server
                String key = args[4];
                String src = args[5];
                c.remotePut(key, new TestImage(key, src));
                c.unsubscribe(key);
                System.exit(0);
            }
            else if (action.equals("getimage"))
            {
                TestClient c = new TestClient(id, host, port);
                // retrieve an image from server, then display
                String key = args[4];
                c.remoteGet(key);
                c.unsubscribe(key);
                TestImage img = (TestImage) c.localGet(key);
                img.display(720);
            }
            else if (action.equals("flip"))
            {
                String key = args[4];
                TestClient c = new TestClient(id, 
                        host, 
                        port, 
                        "flipHorizontal",
                        "../results/"+"client-"+id+"-flip-"+key+"-response-time-");
                // Operation: flipHorizontal
                // Count: 100 times at 2 seconds interval
                // retrieve the image
                c.remoteGet(key);
                // start sigar
                TestMonitor.main(new String[] {"client-"+id+"-flip-"+key+"-"});
                // wait 10 seconds before begin
                wait(10);
                for (int i = 0; i < 100; i++)
                {
                    c.setSendTime(System.nanoTime());
                    c.remotePerform(key, new Operation(c.getNextVersion(key), "flipHorizontal"));
                    wait(2);
                }
                // wait for 10 seconds for sigar log to flatten
                wait(10);
                System.exit(0);
            }
            else if (action.equals("rotate"))
            {
                String key = args[4];
                TestClient c = new TestClient(id, 
                        host, 
                        port, 
                        "rotateClockwise",
                        "../results/"+"client-"+id+"-rotate-"+key+"-response-time-");
                // Operation: rotateClockwise(180)
                // Count: 100 times at 2 seconds interval
                // retrieve the image
                c.remoteGet(key);
                // start sigar
                TestMonitor.main(new String[] {"client-"+id+"-rotate-"+key+"-"});
                // wait 10 seconds before begin
                wait(10);
                for (int i = 0; i < 100; i++)
                {
                    c.setSendTime(System.nanoTime());
                    c.remotePerform(key, new Operation(c.getNextVersion(key), "rotateClockwise", 180));
                    wait(2);
                }
                // wait for 10 seconds for sigar log to flatten
                wait(10);
                System.exit(0);
            }
        }
    }
    
    public static void wait(int second)
    {
        try
        {
            Thread.sleep(second * 1000);
        }
        catch (InterruptedException e)
        {
            System.err.println("Thread.sleep exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
