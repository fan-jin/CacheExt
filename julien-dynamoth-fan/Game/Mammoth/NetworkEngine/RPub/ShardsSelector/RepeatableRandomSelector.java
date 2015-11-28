package Mammoth.NetworkEngine.RPub.ShardsSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMapping;

public class RepeatableRandomSelector implements ShardSelector {
	
	public static final RepeatableRandomSelector INSTANCE = new RepeatableRandomSelector();
	private Map<String, RPubClientId> selectorHistory = new HashMap<String, RPubClientId>();
	
	private RepeatableRandomSelector() {}

	@Override
	public RPubClientId[] selector(PlanMapping mapping, int clientKey) {
		String key = mapping.getChannel() + "_" + mapping.getPlanId() + "_" + clientKey;
		RPubClientId selectedShard = selectorHistory.get(key);
		if(selectedShard == null) {
			RPubClientId[] shards = mapping.getShards();
			selectedShard = shards[ThreadLocalRandom.current().nextInt(shards.length)];
			selectorHistory.put(key, selectedShard);
		}
		return new RPubClientId[]{selectedShard};
	}
}
