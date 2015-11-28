package Mammoth.NetworkEngine.RPub.ShardsSelector;

import java.util.Random;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMapping;

public class RandomShardSelector implements ShardSelector {
	
	public static final RandomShardSelector INSTANCE = new RandomShardSelector();
	private Random rand = new Random();
	
	private RandomShardSelector() {}

	@Override
	public RPubClientId[] selector(PlanMapping mapping, int clientKey) {
		RPubClientId[] shards = mapping.getShards();
		return new RPubClientId[]{ shards[rand.nextInt(shards.length)] };
	}

}
