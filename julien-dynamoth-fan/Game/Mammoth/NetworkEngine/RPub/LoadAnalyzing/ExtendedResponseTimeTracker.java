package Mammoth.NetworkEngine.RPub.LoadAnalyzing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Mammoth.NetworkEngine.RPub.RPubMessage;
import Mammoth.NetworkEngine.RPub.RPubNetworkID;
import Mammoth.NetworkEngine.RPub.RPubPublishMessage;

public class ExtendedResponseTimeTracker extends AbstractResponseTimeTracker {

	// New addition: have all of that by-channel...
	private HashMap<String, HashMap<RPubNetworkID, ExtendedResponseTimeTrackerPair>> pendingMessages = new HashMap<String, HashMap<RPubNetworkID, ExtendedResponseTimeTrackerPair>>();
	
	private Object lockObject = new Object();
	
	public ExtendedResponseTimeTracker() {
	}
	
	private String stripChannelName(String channel) {
		int starIndex = channel.indexOf('|');
		//return "ONECHANNEL";
		if (starIndex == -1)
			return channel;
		else
			return channel.substring(0, starIndex);
			
	}
	
	private String[] generateChannelNames(String strippedChannelName) {
		String[] channels = new String[2];
		//channels[0] = strippedChannelName + "";
		channels[0] = strippedChannelName + "|A";
		channels[1] = strippedChannelName + "|B";
		return channels;
	}

	@Override
	public void addOutgoingMessageInternal(RPubMessage message, String channel) {
		if (message instanceof RPubPublishMessage) {
			RPubPublishMessage publishMessage = (RPubPublishMessage) message;
			
			// Set message's sender time
			long senderTime = System.nanoTime();
			publishMessage.setSenderTime(senderTime);
			
			// Add all 'pending' messages with offsetted time
			synchronized (lockObject) {
			
				// Get that specific hashmap for that channel
				HashMap<RPubNetworkID, ExtendedResponseTimeTrackerPair> channelPendingMessages = pendingMessages.get(channel);
				if (channelPendingMessages == null) {
					// Nothing to piggyback...
					//pendingMessages.put(strippedChannel, new HashMap<RPubNetworkID, ExtendedResponseTimeTrackerPair>());
					return;
				}
				
				// Clear the message's current timestamps (should only happen if same message is sent twice...)
				publishMessage.getTimestamps().clear();
				
				for (Map.Entry<RPubNetworkID, ExtendedResponseTimeTrackerPair> pendingMessage: channelPendingMessages.entrySet()) {
					long time = pendingMessage.getValue().getSenderTime() + (System.nanoTime() - pendingMessage.getValue().getReceivedTime());
					//time = pendingMessage.getValue().getSenderTime();
					publishMessage.getTimestamps().put(pendingMessage.getKey(), time);
				}
				// Clear all pending
				channelPendingMessages.clear();
			}
		}
	}

	@Override
	public void addIncomingMessageInternal(RPubMessage message, String channel) {
		if (message instanceof RPubPublishMessage) {
			
			String strippedChannel = stripChannelName(channel);
			
			for (String generatedChannelName: generateChannelNames(strippedChannel)) {
			
				RPubPublishMessage publishMessage = (RPubPublishMessage) message;
				// Add this incoming message to our hashmap...  Next time we send a message,
				// we will resend it piggybacked.
				synchronized (lockObject) {
					if (pendingMessages.containsKey(generatedChannelName) == false) {
						pendingMessages.put(generatedChannelName, new HashMap<RPubNetworkID, ExtendedResponseTimeTrackerPair>());
					}
					HashMap<RPubNetworkID, ExtendedResponseTimeTrackerPair> channelPendingMessages = pendingMessages.get(generatedChannelName);
					channelPendingMessages.put(message.getSourceID(), new ExtendedResponseTimeTrackerPair(publishMessage.getSenderTime(), System.nanoTime()));
					
					// If >100 then clear some of them
					if (channelPendingMessages.size()>100) {
						int toRemove = channelPendingMessages.size() - 100;
						Set<RPubNetworkID> keysToRemove = new HashSet<RPubNetworkID>();
						for (RPubNetworkID key: channelPendingMessages.keySet()) {
							if (toRemove <= 0) break;
							keysToRemove.add(key);
							toRemove--;
						}
						for (RPubNetworkID key: keysToRemove) {
							channelPendingMessages.remove(key);
						}
					}
				}
				
				// For all 'our' messages: compute response time and add it
				// (for other messages that are piggybacked but not from us, ignore them)
				for (Map.Entry<RPubNetworkID, Long> timestamp: publishMessage.getTimestamps().entrySet()) {
					if (isLocalRPubClientId(timestamp.getKey())) {
						
						long responseTime = System.nanoTime() - timestamp.getValue();
						// Divide it by two : important because actual measured response is half of it because our extended method
						// generates twice the latency... A->PubSub->B;B->PubSub->A
						responseTime = (long) (responseTime / 2.0);
						int intResponseTime = nanosecondsToMilliseconds(responseTime);
						
						if (intResponseTime <= 4000)
							addResponseTime(intResponseTime);
						
					}
				}
			}
		}
	}

}
