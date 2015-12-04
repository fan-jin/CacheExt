package Mammoth.NetworkEngine.RPub.Manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import Mammoth.Client.Client;
import Mammoth.NetworkEngine.NetworkEngineID;
import Mammoth.NetworkEngine.RPub.RPubDataMessage;
import Mammoth.NetworkEngine.RPub.RPubMessage;
import Mammoth.NetworkEngine.RPub.RPubMessageListener;
import Mammoth.NetworkEngine.RPub.RPubNetworkID;
import Mammoth.NetworkEngine.RPub.RPubPublishMessage;
import Mammoth.NetworkEngine.RPub.RPubSubscribeMessage;
import Mammoth.NetworkEngine.RPub.RPubSubscriptionMessage;
import Mammoth.NetworkEngine.RPub.RPubUnsubscribeMessage;
import Mammoth.NetworkEngine.RPub.Client.JedisRPubClient;
import Mammoth.NetworkEngine.RPub.Client.RPubClient;
import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.ControlMessages.AddRPubClientControlMessage;
import Mammoth.NetworkEngine.RPub.ControlMessages.ChangeChannelMappingControlMessage;
import Mammoth.NetworkEngine.RPub.ControlMessages.ChangePlanControlMessage;
import Mammoth.NetworkEngine.RPub.ControlMessages.ControlMessage;
import Mammoth.NetworkEngine.RPub.ControlMessages.CreateChannelControlMessage;
import Mammoth.NetworkEngine.RPub.ControlMessages.RemoveRPubClientControlMessage;
import Mammoth.NetworkEngine.RPub.Game.RConfig;
import Mammoth.NetworkEngine.RPub.Game.Messages.RGameMoveMessage;
import Mammoth.NetworkEngine.RPub.LoadAnalyzing.AbstractResponseTimeTracker;
import Mammoth.NetworkEngine.RPub.Manager.Plan.Plan;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanDiff;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanDiffImpl;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanId;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanImpl;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMapping;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMappingImpl;
import Mammoth.NetworkEngine.RPub.Util.RPubHostInfo;
import Mammoth.Util.Properties.PropertyManager;

public class DynamothRPubManager extends AbstractRPubManager {
	
	public static boolean WILDCARD_CHANNEL_SUBSCRIPTION = true;
	public static boolean LAZY_PLAN_PROPAGATION = true;
	public boolean DebugForceRerouteTileMessages = false;
		
	/**
	 * Map of all rpub clients:
	 * - client ID
	 * - jedis RPub client instance
	 * Not all clients might be active at the same time!
	 * When we receive appropriate control messages, we might add/remove clients from this list.
	 */
	public Map<RPubClientId, JedisRPubClient> rpubClients = new HashMap<RPubClientId, JedisRPubClient>();
	
	/**
	 * Current plan
	 * Perhaps eventually we should keep a list of all plans?
	 */
	private Plan currentPlan = null;
	
	/**
	 * History of past (unexpired) plans
	 */
	private List<Plan> planHistory = new ArrayList<Plan>();
	
	/**
	 * Lock object that should be locked when manipulating / using the current plan
	 * Prevents subscriptions while the plan is being changed
	 */
	private Object planChangeLock = new Object();
	
	/**
	 * List of channels that we are currently subscribed to
	 */
	private Set<String> currentSubscriptions = new HashSet<String>();
	
	/**
	 * Our RPub message listener which invokes the 'real' listener. This listener
	 * is also able to intercept some messages.
	 */
	RPubMessageListener customListener = null;

	public DynamothRPubManager(RPubNetworkID networkID,
			RPubMessageListener messageListener) {
		super(networkID, messageListener);
		// Read properties file
		Properties props = PropertyManager.getProperties(Client.DEFAULT_CONFIG_FILE);
		
		// Load initial servers (rpub clients will connect to those servers when the app is starting)
		String rawServers = StringUtils.strip(
				props.getProperty("network.rpub.dynamoth.initial_servers"));
		
		// Create a custom message listener to be able to intercept plan change requests (hack!)
		// We also need to be able to intercept incoming RPub subscription/unsubscription messages
		// We do so because we have to maintain an internal list of subscriptions to be able to switch
		// subscriptions from one node to the other when needed.
		// 2014-09-02: our custom listener will also be used to intercept incoming messages to measure
		// the response time, using the ResponseTimeTracker. Such measurements will work only if
		// multiple clients are run on the same machine.
		customListener = new RPubMessageListener() {
			
			@Override
			public void messageReceived(String channelName, RPubMessage message,
					int rawMessageSize) {
				
				// Checker whether we have a data message
				if (message instanceof RPubDataMessage) {
					// Get the payload
					Serializable payload = ((RPubDataMessage)message).getPayload();
					
					// If the payload is an instanceof ControlMessage
					if (payload instanceof ControlMessage) {
						// Call our handler
						processControlMessage((ControlMessage)payload);
					}
				} else if (message instanceof RPubSubscriptionMessage) {
					// We have a subscription message - process it
					processSubscriptionMessage((RPubSubscriptionMessage)message);
				}
				
				// Notify the tracker of the incoming message. The tracker will update it's stats.
				AbstractResponseTimeTracker.getInstance().addIncomingMessage(message, channelName);
				
				// Redirect message to the original listener
				getMessageListener().messageReceived(channelName, message, rawMessageSize);
				
			}
		};
		
		// Prepare initial jedis clients
		for (String server: rawServers.split(";")) {
			// Build host info
			RPubHostInfo hostInfo = new RPubHostInfo(server);
			
			// Create our jedis instance
			JedisRPubClient client = new JedisRPubClient(this.getNetworkID(), hostInfo.getClientId(), 1, hostInfo.getHostName(), hostInfo.getPort(), hostInfo.getDomain(), customListener);
			// Add it
			rpubClients.put(hostInfo.getClientId(), client);
		}
		
		// Create the default plan
		PlanImpl plan = new PlanImpl(new PlanId(0));
		plan.setMapping("track-info", new PlanMappingImpl(new PlanId(0), "track-info", new RPubClientId(0)));
		//plan.setMapping("replication-test-default", new PlanMappingImpl(new PlanId(0), "replication-test-default", new RPubClientId[]{new RPubClientId(0)}, PlanMappingStrategy.DEFAULT_STRATEGY));
		//plan.setMapping("replication-test-pfc", new PlanMappingImpl(new PlanId(0), "replication-test-pfc", new RPubClientId[]{new RPubClientId(0), new RPubClientId(1), new RPubClientId(2)}, PlanMappingStrategy.PUBLISHERS_FULLY_CONNECTED));
		//plan.setMapping("replication-test-sfc", new PlanMappingImpl(new PlanId(0), "replication-test-sfc", new RPubClientId[]{new RPubClientId(0), new RPubClientId(1), new RPubClientId(2)}, PlanMappingStrategy.SUBSCRIBERS_FULLY_CONNECTED));
		//plan.setMapping("replication-test-dynamic", new PlanMappingImpl(new PlanId(0), "replication-test-dynamic", new RPubClientId[]{new RPubClientId(0), new RPubClientId(1), new RPubClientId(2)}, PlanMappingStrategy.PUBLISHERS_FULLY_CONNECTED));
		//plan.setMapping("replication-test-dynamic", new PlanMappingImpl(new PlanId(0), "replication-test-dynamic", new RPubClientId[]{new RPubClientId(0)}, PlanMappingStrategy.DEFAULT_STRATEGY));
		//plan.setMapping("tile_0_0", new PlanMappingImpl(new PlanId(0), "tile_0_0", RPubClientId.buildClientIds(0), PlanMappingStrategy.DEFAULT_STRATEGY));
		
		// Set all tile_i_j_B to RPubClientId1
		for (int i=0; i<RConfig.getTileCountX(); i++) {
			for (int j=0; j<RConfig.getTileCountY(); j++) {
				//plan.setMapping("tile_" + i + "_" + j + "|A", new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j + "|A", new RPubClientId(0)));
				//plan.setMapping("tile_" + i + "_" + j + "|B", new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j + "|B", new RPubClientId(1)));
				//plan.setMapping("tile_" + i + "_" + j + "", new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j + "", new RPubClientId(1)));
				/*plan.setMapping("tile_" + i + "_" + j, new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j, new RPubClientId[] {
						new RPubClientId(0),  new RPubClientId(1)
						}, PlanMappingStrategy.DYNAWAN_ROUTING));*/
			}
		}
		
		// Enable WAN Replication for tile_0_0
		/*plan.setMapping("tile_0_0", new PlanMappingImpl(new PlanId(0), "tile_0_0", new RPubClientId[] {
			new RPubClientId(0),  new RPubClientId(1)
			}, PlanMappingStrategy.DYNAWAN_ROUTING));*/

		currentPlan = plan;

		// Put the default plan in the history
		this.planHistory.add(plan);
	}
        
        public DynamothRPubManager(RPubNetworkID networkID,
			RPubMessageListener messageListener, String property) {
		super(networkID, messageListener);
		// Read properties file
		Properties props = PropertyManager.getProperties(Client.DEFAULT_CONFIG_FILE);
		
		// Load initial servers (rpub clients will connect to those servers when the app is starting)
		String rawServers = StringUtils.strip(
				props.getProperty(property));
		
		// Create a custom message listener to be able to intercept plan change requests (hack!)
		// We also need to be able to intercept incoming RPub subscription/unsubscription messages
		// We do so because we have to maintain an internal list of subscriptions to be able to switch
		// subscriptions from one node to the other when needed.
		// 2014-09-02: our custom listener will also be used to intercept incoming messages to measure
		// the response time, using the ResponseTimeTracker. Such measurements will work only if
		// multiple clients are run on the same machine.
		customListener = new RPubMessageListener() {
			
			@Override
			public void messageReceived(String channelName, RPubMessage message,
					int rawMessageSize) {
				
				// Checker whether we have a data message
				if (message instanceof RPubDataMessage) {
					// Get the payload
					Serializable payload = ((RPubDataMessage)message).getPayload();
					
					// If the payload is an instanceof ControlMessage
					if (payload instanceof ControlMessage) {
						// Call our handler
						processControlMessage((ControlMessage)payload);
					}
				} else if (message instanceof RPubSubscriptionMessage) {
					// We have a subscription message - process it
					processSubscriptionMessage((RPubSubscriptionMessage)message);
				}
				
				// Notify the tracker of the incoming message. The tracker will update it's stats.
				AbstractResponseTimeTracker.getInstance().addIncomingMessage(message, channelName);
				
				// Redirect message to the original listener
				getMessageListener().messageReceived(channelName, message, rawMessageSize);
				
			}
		};
		
		// Prepare initial jedis clients
		for (String server: rawServers.split(";")) {
			// Build host info
			RPubHostInfo hostInfo = new RPubHostInfo(server);
			
			// Create our jedis instance
			JedisRPubClient client = new JedisRPubClient(this.getNetworkID(), hostInfo.getClientId(), 1, hostInfo.getHostName(), hostInfo.getPort(), hostInfo.getDomain(), customListener);
			// Add it
			rpubClients.put(hostInfo.getClientId(), client);
		}
		
		// Create the default plan
		PlanImpl plan = new PlanImpl(new PlanId(0));
		plan.setMapping("track-info", new PlanMappingImpl(new PlanId(0), "track-info", new RPubClientId(0)));
		//plan.setMapping("replication-test-default", new PlanMappingImpl(new PlanId(0), "replication-test-default", new RPubClientId[]{new RPubClientId(0)}, PlanMappingStrategy.DEFAULT_STRATEGY));
		//plan.setMapping("replication-test-pfc", new PlanMappingImpl(new PlanId(0), "replication-test-pfc", new RPubClientId[]{new RPubClientId(0), new RPubClientId(1), new RPubClientId(2)}, PlanMappingStrategy.PUBLISHERS_FULLY_CONNECTED));
		//plan.setMapping("replication-test-sfc", new PlanMappingImpl(new PlanId(0), "replication-test-sfc", new RPubClientId[]{new RPubClientId(0), new RPubClientId(1), new RPubClientId(2)}, PlanMappingStrategy.SUBSCRIBERS_FULLY_CONNECTED));
		//plan.setMapping("replication-test-dynamic", new PlanMappingImpl(new PlanId(0), "replication-test-dynamic", new RPubClientId[]{new RPubClientId(0), new RPubClientId(1), new RPubClientId(2)}, PlanMappingStrategy.PUBLISHERS_FULLY_CONNECTED));
		//plan.setMapping("replication-test-dynamic", new PlanMappingImpl(new PlanId(0), "replication-test-dynamic", new RPubClientId[]{new RPubClientId(0)}, PlanMappingStrategy.DEFAULT_STRATEGY));
		//plan.setMapping("tile_0_0", new PlanMappingImpl(new PlanId(0), "tile_0_0", RPubClientId.buildClientIds(0), PlanMappingStrategy.DEFAULT_STRATEGY));
		
		// Set all tile_i_j_B to RPubClientId1
		for (int i=0; i<RConfig.getTileCountX(); i++) {
			for (int j=0; j<RConfig.getTileCountY(); j++) {
				//plan.setMapping("tile_" + i + "_" + j + "|A", new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j + "|A", new RPubClientId(0)));
				//plan.setMapping("tile_" + i + "_" + j + "|B", new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j + "|B", new RPubClientId(1)));
				//plan.setMapping("tile_" + i + "_" + j + "", new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j + "", new RPubClientId(1)));
				/*plan.setMapping("tile_" + i + "_" + j, new PlanMappingImpl(new PlanId(0), "tile_" + i + "_" + j, new RPubClientId[] {
						new RPubClientId(0),  new RPubClientId(1)
						}, PlanMappingStrategy.DYNAWAN_ROUTING));*/
			}
		}
		
		// Enable WAN Replication for tile_0_0
		/*plan.setMapping("tile_0_0", new PlanMappingImpl(new PlanId(0), "tile_0_0", new RPubClientId[] {
			new RPubClientId(0),  new RPubClientId(1)
			}, PlanMappingStrategy.DYNAWAN_ROUTING));*/

		currentPlan = plan;

		// Put the default plan in the history
		this.planHistory.add(plan);
	}
	
	/**
	 * Add or remove the channel to our list of current subscriptions
	 * @param subscriptionMessage RPub subscription message
	 */
	private void processSubscriptionMessage(RPubSubscriptionMessage subscriptionMessage) {
		// If we are in infrastructure mode, then registrations are not modified!
		if (subscriptionMessage.isInfrastructure())
			return;
	
		synchronized(planChangeLock) {
			
			if (subscriptionMessage instanceof RPubSubscribeMessage) {
				// Register subscription to channel
				System.out.println("AddToCurrentSubscriptions->" + subscriptionMessage.getChannelName());
				this.currentSubscriptions.add(subscriptionMessage.getChannelName());
			} else if (subscriptionMessage instanceof RPubUnsubscribeMessage) {
				// Remove channel subscription
				System.out.println("RemoveFromCurrentSubscriptions->" + subscriptionMessage.getChannelName());
				this.currentSubscriptions.remove(subscriptionMessage.getChannelName());
			}
			
		}
	}
	
	/**
	 * Process an incoming RPub control message
	 * (dispatches to other method)
	 * @param controlMessage RPub control message
	 */
	private void processControlMessage(ControlMessage controlMessage) {
		if (controlMessage instanceof ChangePlanControlMessage) {
			processChangePlanControlMessage((ChangePlanControlMessage)controlMessage);
		} else if (controlMessage instanceof AddRPubClientControlMessage) {
			processAddRPubClientControlMessage((AddRPubClientControlMessage)controlMessage);
		} else if (controlMessage instanceof RemoveRPubClientControlMessage) {
			processRemoveRPubClientControlMessage((RemoveRPubClientControlMessage)controlMessage);
		} else if (controlMessage instanceof ChangeChannelMappingControlMessage) {
			processChangeChannelMappingControlMessage((ChangeChannelMappingControlMessage) controlMessage);
		}
	}
	
	public void applyPlan(Plan newPlan) {
		// Time-stamp plan
		newPlan.setTime((int)(Math.round(System.currentTimeMillis() / 1000.0)));
		
		synchronized (planChangeLock) {
			
			// Compute planDiff
			PlanDiff planDiff = new PlanDiffImpl(this.currentPlan, newPlan, this.hashCode());
			
			Plan oldPlan = this.currentPlan;
			
			// Put the new plan in the plan history
			this.planHistory.add(newPlan);
			
			// Switch to new plan
			setCurrentPlan(newPlan);
			
			
			// Connect new channels
			// For each shard...
			for (RPubClientId clientId: planDiff.getShards()) {
				// For each subscription channel
				for (String channel: planDiff.getOwnSubscriptions(clientId, currentSubscriptions)) {
					// Create channel just-in-case
					createChannel(channel);
					System.out.println("Subscribing to " + channel + " on " + clientId.getId());
					// Subscription is performed on 'current' shard with infrastructure mode
					subscribeClientToChannel(getNetworkID(), channel, true);
				}
				
			}
			
			// Switch to old plan
			setCurrentPlan(oldPlan);
			
			// TODO: FIND OUT WHY UNSUBS ARE NOT WORKING PROPERLY
			// HINT: if the channel was not defined in the previous plan,
			// then it is possible that the unsub cannot be sent!
			// We should assume plan0.
			// 2014-05-27: Should have been fixed, LoadBalancer auto-adds info for all unknown channels
			// and the new plan contains those info. 
			
			// Disconnect old channels
			// For each shard...
			for (RPubClientId clientId: planDiff.getShards()) {
				// For each unsubscription channel
				for (String channel: planDiff.getOwnUnsubscriptions(clientId, currentSubscriptions)) {
					// Unsubscription is performed on 'current' shard with infrastructure mode
					unsubscribeClientFromChannel(getNetworkID(), channel, true);
					System.out.println("Unsubscribing to " + channel + " on " + clientId.getId());

				}
				
			}
			
			// Switch again to new plan
			setCurrentPlan(newPlan);
			
			System.out.println("Applying plan " + this.currentPlan);
			
			// DEBUG: inform some listener that we changed the plan
			//publishToChannel("dynamoth-debug", new RPubPublishMessage(getNetworkID(), new PlanAppliedControlMessage()));
			
			// DEBUG: print the planId of all current mappings
			/*
			System.out.println("---Begin printing planId of all mappings---");
			for (String channel: currentPlan.getAllChannels()) {
				System.out.println("   " + channel + " (" + currentPlan.getMapping(channel).getPlanId().getId() + ")");
			}
			System.out.println("---End printing planId of all mappings---");
			*/

			System.out.println("---Begin printing channels---");
			for (RPubClientId clientId: currentPlan.getAllShards()) {
				for (String channel: currentPlan.getClientChannels(clientId)) {
					System.out.println("   " + channel + " -> RPubClientId" + clientId.getId());
				}
			}
			System.out.println("---End printing channels---");
		}
	}
	
	/**
	 * Apply a partial plan
	 * @param controlMessage
	 */
	private void processChangeChannelMappingControlMessage(ChangeChannelMappingControlMessage controlMessage) {
		synchronized (planChangeLock) {
			
			if (controlMessage.getChannel().equals("tile_0_0"))
				System.out.println("Change channel mapping request (src=RPubClientId" + controlMessage.getSourceClientId().getId() + "): " + controlMessage.getChannel() + "->" + controlMessage.getMapping().getShards()[0].getId());
			
			// Create a dummy new plan based on the existing plan to reuse our plan change algo
			PlanImpl plan = new PlanImpl((PlanImpl) currentPlan);
			plan.setMapping(controlMessage.getChannel(), controlMessage.getMapping());
			
			//Update the plan id of the overall plan
			PlanId newPlanId = new PlanId(Math.max(controlMessage.getMapping().getPlanId().getId(), plan.getPlanId().getId()));
			plan.setPlanId(newPlanId);

			// Apply it
			applyPlan(plan);
		}
	}
	
	/**
	 * Apply a new plan
	 */
	private void processChangePlanControlMessage(ChangePlanControlMessage changePlanControlMessage) {
		
		applyPlan(changePlanControlMessage.getNewPlan());
	}
	
	private void processAddRPubClientControlMessage(AddRPubClientControlMessage message) {
		// Add the new rpub client
		JedisRPubClient client = new JedisRPubClient(this.getNetworkID(), message.getClientId(), 1, message.getHostName(), message.getHostPort(), "", customListener);
		rpubClients.put(message.getClientId(), client);
		// Connect it ** SHOULD BE REMOVED ** because we will only connect when it's needed
		//client.connect();
		System.out.println("Adding new RPubClient " + message.getClientId());
	}
	
	private void processRemoveRPubClientControlMessage(RemoveRPubClientControlMessage message) {
		// Disconnect the rpub client if it was connected
		if (rpubClients.get(message.getClientId()).isConnected()) {
			rpubClients.get(message.getClientId()).disconnect();
		}
		// Remove the rpub client
		rpubClients.remove(message.getClientId());
		System.out.println("Removing RPubClient " + message.getClientId());
	}
	
	public Plan getCurrentPlan() {
		return this.currentPlan;
	}
	
	public void setCurrentPlan(Plan currentPlan) {
		this.currentPlan = currentPlan;
	}
	
	public List<Plan> getPlanHistory() {
		return this.planHistory;
	}
	
	public RPubClientId getHashedShardId(String channelName) {
		// Get shard info
		
		int hashCode = channelName.hashCode();
		if (hashCode < 0)
			hashCode = -hashCode;
		
		int shard = hashCode % rpubClients.size();
		//hashShardCount[shard].incrementAndGet();
		
		System.out.println("Sharding index: " + shard );

		return new RPubClientId(shard);
		//return hashCode % (redisHosts.size()-1) + 1;
	}
	
	private RPubClient getHashedShard(String channelName) {
		return this.rpubClients.get(getHashedShardId(channelName));
	}

	@Override
	public void initialize() {
		// Create and connect all initially-registered Jedis nodes
		for (Map.Entry<RPubClientId,JedisRPubClient> entry: this.rpubClients.entrySet() ) {
			entry.getValue().connect();
		}
		
		// Subscribe ourself to the plan push channel
		//publishToSubscriptionChannel(getNetworkID(), new RPubSubscribeMessage((RPubNetworkID)(getNetworkID()), "plan-push-channel"));
	}

	@Override
	public void createChannel(String channelName) {
		
		synchronized (planChangeLock) {
		
			// Publish the channel creation to the LLAs
			// Obtain all the RPub clients that are handling 'channelName'
			// Publish the CreateChannelControlMessage to them over the LoadAnalyzer channel.
			//for (RPubClient client: this.getPublicationShards(channelName)) {
			for (RPubClient client: buildShards(currentPlan.getMapping(channelName).getShards())) {
				client.publishToChannel("loadanalyzer-channel", new RPubPublishMessage(this.getNetworkID(), new CreateChannelControlMessage(channelName)));
			}
			
		}
	}

	@Override
	public RPubClient[] getPublicationShards(String channelName) {
		// Get the shards that shall be used for publication messages
		// Under the Dynamoth Model, the current shard corresponding to -channelName- shall be used
		
		synchronized (planChangeLock) {
						
			if (DebugForceRerouteTileMessages && channelName.startsWith("tile_")) {
				return buildShards(new RPubClientId[] {new RPubClientId(1)});
			}
			PlanMapping mapping = currentPlan.getMapping(channelName);
			RPubClientId[] shards = mapping.getShards();

			//return new RPubClient[] { this.getHashedShard(channelName) };
			return buildShards(mapping.getStrategy().selectPublicationShards(mapping, this.hashCode()));
		
		}
	}

	@Override
	public RPubClient[] getSubscriptionShards(String channelName) {
		// Get the shards that shall be used for subscription messages
		// Under the Dynamoth Model, the shard corresponding to -channelName- shall be used
		// (same as publication shard)
		
		synchronized (planChangeLock) {
			
			PlanMapping mapping = currentPlan.getMapping(channelName);
			RPubClientId[] shards = mapping.getShards();

			//return new RPubClient[] { this.getHashedShard(channelName) };
			return buildShards(mapping.getStrategy().selectSubscriptionShards(mapping, this.hashCode()));
		
		}
	}

	@Override
	public RPubClient[] getAllActiveShards() {
		// Returns all active shards
		
		return this.rpubClients.values().toArray(new RPubClient[] {}); 
	}
	
	public RPubClient[] buildShards(RPubClientId[] shardIDs) {
		RPubClient[] clients = new RPubClient[shardIDs.length];
		for (int i=0; i<shardIDs.length; i++) {
			clients[i] = this.rpubClients.get(shardIDs[i]);
		}
		return clients;
	}

	// Override prePublishToChannel and postPublishToChannel so that we can make sure the client
	// is connected before issuing the message
	@Override
	protected void prePublishToChannel(RPubClient client, String channelName, RPubMessage message) {
		// Ensure client is connected
		if (client.isConnected() == false) {
			client.connect();
		}
		String actualChannelName = channelName;
		if(message instanceof RPubSubscriptionMessage) {
			actualChannelName = ((RPubSubscriptionMessage) message).getChannelName();
		}
		PlanId channelPlanId = currentPlan.getMapping(actualChannelName).getPlanId();
		message.setPlanID(channelPlanId);
		// If message is publication and contains a RGameMoveMessage, then record the hash and time in a
		// global hash table
		if (message instanceof RPubPublishMessage ) {
			RPubPublishMessage publishMessage = (RPubPublishMessage)message;
			if (publishMessage.getPayload() instanceof RGameMoveMessage) { 
				
				// Put in global response time tracker
				AbstractResponseTimeTracker.getInstance().addOutgoingMessage(message, channelName);
			}
		}
		
		// Set message's source domain
		message.setSourceDomain(System.getProperty("ec2.region", ""));
		
		// Set message's rpub domain
		message.setRpubServerDomain( ((JedisRPubClient) client).getJedisDomain());
	}

	@Override
	protected void postPublishToChannel(RPubClient client, String channelName, RPubMessage message) {
		
	}

	@Override
	public void publishToSubscriptionChannel(NetworkEngineID networkID,
			RPubSubscriptionMessage message) {
		// TODO Auto-generated method stub
		synchronized (planChangeLock) {
			PlanId channelPlanId = currentPlan.getMapping(message.getChannelName()).getPlanId();
			message.setPlanID(channelPlanId);
			super.publishToSubscriptionChannel(networkID, message);
		}
	}
	
	
}
