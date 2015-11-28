/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt;

import CacheExt.Messages.CacheOperationMessage;
import Mammoth.NetworkEngine.Exceptions.AlreadyConnectedException;
import Mammoth.NetworkEngine.Exceptions.NoSuchChannelException;
import Mammoth.NetworkEngine.RPub.RPubNetworkEngine;
import Mammoth.Util.Message.Handler;
import Mammoth.Util.Message.Message;
import Mammoth.Util.Message.Reactor;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Julien Gascon-Samson
 */
public abstract class CacheClient {
	// Dynamoth stuff
	private RPubNetworkEngine engine; // Mammoth/Dynamoth network engine
	private Reactor reactor; // Mammoth reactor

        // RMI stuff
        private CacheExt server; // server stub

        // Local storage stuff
        private Hashtable<String, BaseObject> hashtable = new Hashtable<String, BaseObject>(); // hashtable for objects
        private ArrayList<String> subscription = new ArrayList<String>(); // arraylist for subscriptions

	public CacheClient()
        {
            connect();
	}
	
	public void connect() {
		engine = new RPubNetworkEngine();
		try {
			engine.connect();
		} catch (IOException ex) {
			Logger.getLogger(CacheClient.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AlreadyConnectedException ex) {
			Logger.getLogger(CacheClient.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		// Create reactor
		reactor = new Reactor("CacheClient-Reactor", engine);
		
		// Subscribe to appropriate messages
		reactor.register(CacheOperationMessage.class, new Handler() {

			@Override
			public void handle(Message msg) {
				handleCacheOperationMessage((CacheOperationMessage)msg);
			}
		});
	}
        
        public void connectToServer(String server_name, String server_host, int server_port)
        {
            Registry registry;

            try
            {
                registry = LocateRegistry.getRegistry(server_host, server_port);
                server = (CacheExt) registry.lookup(server_name);
                if(server!=null)
                {
                    System.out.println("Successfully Connected to " + server_name);
                }
                else
                {
                    System.out.println("Unsuccessful Connection to " + server_name);
                }
            }
            catch (Exception e) 
            {
                log("connectToServer exception: " + e.toString());
                e.printStackTrace();
            }
        }
        
        public void perform(String key, Operation m)
        {
            try
            {
                if (!subscription.contains(key)) subscribe(key);
                if (!server.performOperation(key, m)) fetch(key);
            }
            catch (RemoteException e)
            {
                log("perofrm exception: " + e.toString());
                e.printStackTrace();
            }
        }
        
        public void load(String key, BaseObject o)
        {
            try
            {
                if (!subscription.contains(key)) subscribe(key);
                server.loadObj(key, Serialization.serialize(o));
            }
            catch (RemoteException e)
            {
                log("load exception: " + e.toString());
                e.printStackTrace();
            }
        }
        
        public void put(String key, BaseObject o)
        {
            hashtable.put(key, o);
        }
        
        public BaseObject get(String key)
        {
            return hashtable.get(key);
        }
        
        public void fetch(String key)
        {
            try
            {
                BaseObject o = (BaseObject) Serialization.deserialize(server.getObj(key));
                if (o != null) {
                    put(key, o);
                    if (!subscription.contains(key)) subscribe(key);
                }
                
            }
            catch (RemoteException e)
            {
                log("load exception: " + e.toString());
                e.printStackTrace();
            }
        }
        
        public int getVersion(String key)
        {
            BaseObject o;
            if ((o = hashtable.get(key)) != null)
            {
                return o.getVersion();
            }
            else
            {
                return -1;
            }
        }
        
        public int getNextVersion(String key)
        {
            return getVersion(key) > -1 ? getVersion(key) + 1 : -1;
        }
        
	/**
	 * Handle our cache operation message: invoke operationReceived.
	 * 
	 * @param message Our cache operation message
	 */
	private void handleCacheOperationMessage(CacheOperationMessage message) {
		operationReceived(message.getKey(), message.getOperation());
	}
	
	/**
	 * Publish a given operation to all interested clients (subscribers)
	 * 
	 * @param key Key of object it applies to
	 * @param operation Operation object
	 */
	public void publishOperation(String key, Operation operation) {
		try {
			engine.send(key, new CacheOperationMessage(key, operation));
		} catch (IOException ex) {
			Logger.getLogger(CacheClient.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchChannelException ex) {
			Logger.getLogger(CacheClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Subscribe to a given key. You will then start receiving notifications for this key.
	 * 
	 * @param key Key to subscribe to
	 */
	public void subscribe(String key) {
		try {
			engine.subscribeChannel(key, engine.getId());
                        subscription.add(key);
		} catch (NoSuchChannelException ex) {
			Logger.getLogger(CacheClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Unscribes from a given key. Notifications will not be transmitted anymore.
	 * 
	 * @param key Key to unsubscribe from.
	 */
	public void unsubscribe(String key) {
		try {
			engine.unsubscribeChannel(key, engine.getId());
                        subscription.remove(key);
		} catch (NoSuchChannelException ex) {
			Logger.getLogger(CacheClient.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Triggered when an operation has been received
	 * 
	 * @param key Key of object it applies to
	 * @param operation Operation
	 */
	public void operationReceived(String key, Operation operation)
        {
            System.out.println("receive update from server for client 1=" + System.nanoTime());
            log("CacheClient::operationReceived: key=" + key + ", operation=" + operation.getName());
            BaseObject obj;
            if ((obj = get(key)) != null)
            {
                log("CacheClient::operationReceived: key=" + key + ", version=" + obj.getVersion() + ", update_version=" + operation.getVersion());
                if (operation.getVersion() == obj.getVersion() + 1) 
                {
                    log("CacheClient::operationReceived: version correct, apply update");
                    obj.applyUpdate(operation);
                    System.out.println("after performing for client 1=" + System.nanoTime());
                }
                else
                {
                    log("CacheClient::operationReceived: version incorrect, discard update");
                    log("CacheClient::operationReceived: fetch latest copy of key=" + obj.getKey() + " from server");
                    try
                    {
                        obj = (BaseObject) Serialization.deserialize(server.getObj(key));
                        if (obj != null) put(key, obj);
                    }
                    catch (RemoteException e)
                    {
                        log("CacheClient::operationReceived exception: " + e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }
        
        public abstract void log(String msg);
}
