/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise.Impl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import CacheWise.CacheWise;
import CacheWise.SubjectPlus;
import CacheWise.Operation;
import CacheWise.CacheClient;
/**
 *
 * @author fjin1
 */
public class CacheServer extends CacheClient implements CacheWise {
    
    private Hashtable<String, SubjectPlus> hashtable;
    
    public CacheServer()
    {
        hashtable = new Hashtable<String, SubjectPlus>();
    }
    
    public void loadObj(String key, byte[] obj) throws RemoteException
    { 
        System.out.println("CacheServer::loadObj: key=" + key);
        hashtable.put(key, new SubjectPlus(obj));
    }
    
    public byte[] getObj(String key) throws RemoteException
    {
        System.out.println("CacheServer::getObj: key=" + key);
        SubjectPlus s = hashtable.get(key);
        if (s != null) {
            return s.getSerialized();
        } else {
            return null;
        }
    }

    public boolean performOperation(String key, Operation m) throws RemoteException
    {
        System.out.println("CacheServer::performOperation: key=" + key + ", version=" + m.getVersion());
        SubjectPlus s = hashtable.get(key);
        synchronized (hashtable) {
            if (s != null) {
                if (m.getVersion() == s.getVersion() + 1)
                {
                    System.out.println("CacheServer::performOperation: version correct");
                    publishOperation(key, m); // publish update first before applying
                    if (s.applyUpdate(m))
                    {
                        return true;
                    }
                    return false;
                }
                else
                {
                     System.err.println("CacheServer::performOperation: incorrect version key=" +
                       key + ", version=" +
                       s.getVersion() + ", update_version: " + m.getVersion());
                     return false;
                }
            }
        }        
        return false;
    }
    
    public static void main(String args[])
    {
        Registry registry;
        String server_binding = "cache";
        String server = "localhost";
        int port = 1099;
        
        try {
            // Create a new cache server object
            CacheServer c = new CacheServer();
            // Dynamically generate the stub (client proxy)
            CacheWise stub = (CacheWise) UnicastRemoteObject.exportObject(c, 0);

            // Bind the remote object's stub in the registry
            // Start rmiregistry on localhost with default port unless specified 
            if (args.length > 0)
            {
                port = Integer.valueOf(args[0]);
            }
            registry = LocateRegistry.createRegistry(port);

            registry.rebind(server_binding, stub);
            
            System.err.println("CacheServer ready at "+ server+":"+port);
        }
        catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
    @Override
    public void operationReceived(String key, Operation operation) {
        System.out.println("CacheServer::operationReceived: do nothing");
    }
    
    @Override
    public void log(String msg)
    {
        System.out.println(msg);
    }
}
