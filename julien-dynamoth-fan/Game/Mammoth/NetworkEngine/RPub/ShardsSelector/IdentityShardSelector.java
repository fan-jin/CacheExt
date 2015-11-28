package Mammoth.NetworkEngine.RPub.ShardsSelector;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMapping;

public class IdentityShardSelector implements ShardSelector {

	public static final IdentityShardSelector INSTANCE = new IdentityShardSelector();

	private IdentityShardSelector() {
		
	}
	
	@Override
	public RPubClientId[] selector(PlanMapping mapping, int clientKey) {
		return mapping.getShards();
	}

}
