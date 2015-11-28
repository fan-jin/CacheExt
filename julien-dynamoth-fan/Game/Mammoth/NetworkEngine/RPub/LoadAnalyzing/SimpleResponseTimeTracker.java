package Mammoth.NetworkEngine.RPub.LoadAnalyzing;

import java.util.HashMap;
import java.util.Map;

import Mammoth.NetworkEngine.RPub.RPubMessage;

public class SimpleResponseTimeTracker extends AbstractResponseTimeTracker {

	// Map: sent message hashes -> time in ns
	private Map<Integer, Long> outgoingMessages = new HashMap<Integer, Long>();
	
	private Object lockObject = new Object();
	
	protected SimpleResponseTimeTracker() {
	}
	
	public void addOutgoingMessageInternal(RPubMessage message, String channel) {
		long currentTime = System.nanoTime();
		synchronized (lockObject) {
			outgoingMessages.put(message.hashCode(), currentTime);	
		}
	}
	
	public void addIncomingMessageInternal(RPubMessage message, String channel) {	
		synchronized (lockObject) {
			// Check if message is in outgoingMessages; otherwise, discard
			if (outgoingMessages.containsKey(message.hashCode()) == false)
				return;			
			
			long currentTime = System.nanoTime();
					
			long outgoingTime = outgoingMessages.get(message.hashCode());
			long responseTime = currentTime - outgoingTime;
			
			int intResponseTime = nanosecondsToMilliseconds(responseTime);
			
			// Add only if <=4000 (above 4000 considered lost)
			if (intResponseTime <= 4000)
				addResponseTime(intResponseTime);
		}
	}

}
