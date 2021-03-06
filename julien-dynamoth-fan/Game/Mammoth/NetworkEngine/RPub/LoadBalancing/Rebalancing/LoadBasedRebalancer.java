package Mammoth.NetworkEngine.RPub.LoadBalancing.Rebalancing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.LoadBalancing.LoadEvaluation.LoadEvaluator;
import Mammoth.NetworkEngine.RPub.Manager.Plan.Plan;
import Mammoth.NetworkEngine.RPub.Util.RPubHostInfo;

public abstract class LoadBasedRebalancer extends AbstractRebalancer {

	protected LoadEvaluator currentLoadEvaluator = null;
	protected Map<RPubClientId, RPubHostInfo> hostInfoMap = null;
	/**
	 * List of hosts that are active - currently being used
	 */
	protected Set<RPubClientId> activeHosts = new HashSet<RPubClientId>();
	
	public LoadBasedRebalancer(Plan currentPlan, int currentTime,
			LoadEvaluator currentLoadEvaluator,
			Map<RPubClientId, RPubHostInfo> hostInfoMap) {
		super(currentPlan, currentTime);
		
		this.currentLoadEvaluator = currentLoadEvaluator;
		this.hostInfoMap = hostInfoMap;
		
		// Add rpub client 0 to active hosts
		this.activeHosts.add(new RPubClientId(0));
	}

	public synchronized LoadEvaluator getCurrentLoadEvaluator() {
		return currentLoadEvaluator;
	}

	public synchronized void setCurrentLoadEvaluator(LoadEvaluator currentLoadEvaluator) {
		this.currentLoadEvaluator = currentLoadEvaluator;
	}

	public synchronized Map<RPubClientId, RPubHostInfo> getHostInfoMap() {
		return hostInfoMap;
	}

	public synchronized void setHostInfoMap(Map<RPubClientId, RPubHostInfo> hostInfoMap) {
		this.hostInfoMap = hostInfoMap;
	}

	public Set<RPubClientId> getActiveHosts() {
		return activeHosts;
	}
}
