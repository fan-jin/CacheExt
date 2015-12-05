/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise;

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
        private CacheWise server; // server stub

        // Local storage stuff
        private Hashtable<String, ObjectBundle> hashtable = new Hashtable<String, ObjectBundle>(); // hashtable for objects
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
                server = (CacheWise) registry.lookup(server_name);
                if(server!=null)
                {
                    log("Successfully Connected to " + server_name);
                }
                else
                {
                    log("Unsuccessful Connection to " + server_name);
                }
            }
            catch (Exception e) 
            {
                log("connectToServer exception: " + e.toString());
                e.printStackTrace();
            }
        }
        
        public void remotePerform(String key, Operation m)
        {
            try
            {
                if (!subscription.contains(key)) subscribe(key);
                if (!server.performOperation(key, m)) remoteGet(key);
            }
            catch (RemoteException e)
            {
                log("perofrm exception: " + e.toString());
                e.printStackTrace();
            }
        }
        
        public void remotePut(String key, BaseObject o)
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
        
        public void localPut(String key, BaseObject o)
        {
            // if object exists, replace, otherwise, create new
            if (hashtable.containsKey(key))
            {
                hashtable.get(key).setObj(o);
            }
            else
            {
                hashtable.put(key, new ObjectBundle(o));
            }
        }
        
        public void storeAsBundle(String key, ObjectBundle b)
        {
            // if object exists, replace, otherwise, create new
            hashtable.put(key, b);
        }
        
        public BaseObject localGet(String key)
        {
            return hashtable.get(key).getObj();
        }
        
        private ObjectBundle getBundle(String key)
        {
            return hashtable.get(key);
        }
        
        public void remoteGet(String key)
        {
            log("CacheClient::fetch: key=" + key);
            try
            {
                // subscribe then retrieve
                if (!subscription.contains(key)) subscribe(key);
                BaseObject o = (BaseObject) Serialization.deserialize(server.getObj(key));
                if (o != null) {
                    log("CacheClient::remoteGet: key=" + key + ", received version=" + o.getVersion());
                    log("received:\n" + o);
                    localPut(key, o);
                    // apply updates in the queue if any
                    if (!getBundle(key).apply())
                    {
                        remoteGet(key);
                    }
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
            if ((o = localGet(key)) != null)
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
            log("CacheClient::operationReceived: key=" + key + ", operation=" + operation.getName());
            ObjectBundle bundle;
            if ((bundle = getBundle(key)) == null)
            {
                log("CacheClient::operationReceived: key=" + key + ", operation=" + operation.getName() +", creating bundle");
                localPut(key, null);
                bundle = getBundle(key); // retrieve newly created bundle
            }
            if (!bundle.queue(operation))
            {
                log("CacheClient::operationReceived: error queuing operation");
                log("CacheClient::operationReceived: fetch latest copy of key=" + bundle.getObj().getKey() + " from server");
                try
                {
                    BaseObject obj = (BaseObject) Serialization.deserialize(server.getObj(key));
                    if (obj != null) localPut(key, obj);
                }
                catch (RemoteException e)
                {
                    log("CacheClient::operationReceived exception: " + e.toString());
                    e.printStackTrace();
                }
            }
            else
            {
                if (bundle.getObj() != null) 
                {
                    if (!bundle.apply())
                    {
                        remoteGet(key);
                    }
                }
            }
        }
        
        public abstract void log(String msg);
}
