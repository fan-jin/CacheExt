/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt.Example;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.imgscalr.Scalr.*;
import CacheExt.Operation;
import CacheExt.ObjectBundle;

/**
 *
 * @author Julien Gascon-Samson
 */
public class Example {
	public static void main(String[] args) {
		
		// Launch 3 cache clients... Assign them a different id
		ExampleCacheClient client1 = new ExampleCacheClient(1);
                ExampleCacheClient client2 = new ExampleCacheClient(2);
//		ExampleCacheClient client3 = new ExampleCacheClient(3);
		
		// Connect them all.
		// This operation will fail if the RPubHub has not been started!
                Player p = new Player("testplayer", "person1", 5);
                System.out.println(p);
                ObjectBundle bundle = new ObjectBundle(p) {
                    @Override
                    public void log(String s)
                    {
                        System.out.println("Client[" + 1 + "]-" + s);
                    }
                };
                client1.storeAsBundle("testplayer", bundle);
                client1.load("testplayer", p);
                client2.subscribe("testplayer");
                client1.perform("testplayer", new Operation(client1.getNextVersion("testplayer"), "setVar", 15));
                sleep(50);
                System.out.println("breakpoint");
                
                client1.perform("testplayer", new Operation(client1.getNextVersion("testplayer"), "multiplyVar", 2));
//                client2.fetch("testplayer");
                client2.fetch("testplayer");
                sleep(1000);
                client1.perform("testplayer", new Operation(client1.getNextVersion("testplayer"), "multiplyVar", 2));
                sleep(1000);
                
//                client1.perform("testplayer", new Operation(client1.getNextVersion("testplayer"), "setVar", 15));
//                sleep(50);
//                client2.perform("testplayer", new Operation(client2.getNextVersion("testplayer"), "multiplyVar", 2));
//                sleep(50);
                System.out.println("at client 1");
                System.out.println(client1.retrieve("testplayer"));
                System.out.println("at client 2");
                System.out.println(client2.retrieve("testplayer"));
                //System.out.println((Player)CacheClient.deserialize(client1.server.getObj("testplayer")));
//		client2.connect();
//		client3.connect();
                
		
		// Subscribe clients 2 and 3 to key "banana"
//		client2.subscribe("banana");
//		client3.subscribe("banana");
		
		// Wait for the subscriptions to be successfully done
		// (Unfortunately with Dynamoth there is no way yet to tell when subscriptions
		// have been fully established :-( )
		sleep(1000);
		
		// Create 2 dummy operations and have client1 send them to all subscribers (key=banana)
//		ExampleOperation operation1 = new ExampleOperation(1);
//		ExampleOperation operation2 = new ExampleOperation(2);
//		client1.publishOperation("banana", operation1);
//		client1.publishOperation("banana", operation2);
		
		// Upon receiving the operations, clients 2 and 3 should print to their console

		// Let's wait before destroying all threads...
		sleep(3000);
		
		System.exit(0);
	}
	
	private static void sleep(long timeInMs) {
		try {			
			
			Thread.sleep(timeInMs);
		} catch (InterruptedException ex) {
			Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
