package Mammoth.NetworkEngine.RPub.Client;

import Mammoth.NetworkEngine.RPub.RPubId;

public class RPubClientId extends RPubId {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1696553048230089385L;
	
	/**
	 * Default client ID - 0. Typically all channels not specified in a plan go through this client.
	 */
	public static final RPubClientId Default = new RPubClientId(0);
	
	// Not the best thing in the world, but we assume that only one entity will
	// spawn IDs for new RPubClients (the load balancer)
	
	static private int nextId = 0;
	
	protected RPubClientId() {
		super();
	}
	
	public RPubClientId(int id) {
		super(id);
	}

	public synchronized static RPubClientId generate() {
		int next = nextId;
		nextId++;
		return new RPubClientId(next);
	}
	
	public static RPubClientId[] buildClientIds(int... shards) {
		RPubClientId[] clients = new RPubClientId[shards.length];
		int i=0;
		for (int shard: shards) {
			clients[i] = new RPubClientId(shard);
			i++;
		}
		return clients;
	}
}
