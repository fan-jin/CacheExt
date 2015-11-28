package Mammoth.NetworkEngine.RPub.ShardsSelector;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMapping;

public class DynaWANLocalShardSelector implements ShardSelector {

	public DynaWANLocalShardSelector() {}

	public static final DynaWANLocalShardSelector INSTANCE = new DynaWANLocalShardSelector();
	
	@Override
	public RPubClientId[] selector(PlanMapping mapping, int clientKey) {
		// We don't care about the parameters...
		// We obtain the ec2 region for this machine... and then based on the region, we select an appropriate shard!
		String ec2Region = System.getProperty("ec2.region", "us-east-1");

		if (ec2Region.contains("us-east")) {
			return new RPubClientId[] {new RPubClientId(0)};
		} else if (ec2Region.contains("ap-southeast")) {
			return new RPubClientId[] {new RPubClientId(1)};
		}
		
		return new RPubClientId[] {new RPubClientId(0)};
	}

}
