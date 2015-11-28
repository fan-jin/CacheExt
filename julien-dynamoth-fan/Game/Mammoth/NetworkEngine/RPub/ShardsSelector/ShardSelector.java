package Mammoth.NetworkEngine.RPub.ShardsSelector;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMapping;

public interface ShardSelector {

	public RPubClientId[] selector(PlanMapping mapping, int clientKey);

}
