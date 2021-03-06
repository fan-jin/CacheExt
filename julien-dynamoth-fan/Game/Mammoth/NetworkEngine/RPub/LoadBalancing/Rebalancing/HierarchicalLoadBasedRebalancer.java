package Mammoth.NetworkEngine.RPub.LoadBalancing.Rebalancing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.LoadBalancing.LoadEvaluation.LoadEvaluator;
import Mammoth.NetworkEngine.RPub.Manager.Plan.Plan;
import Mammoth.NetworkEngine.RPub.Util.RPubHostInfo;

public class HierarchicalLoadBasedRebalancer extends LoadBasedRebalancer {

	private List<LoadBasedRebalancer> rebalancers = new ArrayList<LoadBasedRebalancer>();
	
	public HierarchicalLoadBasedRebalancer(Plan currentPlan, int currentTime,
			LoadEvaluator currentLoadEvaluator,
			Map<RPubClientId, RPubHostInfo> hostInfoMap) {
		super(currentPlan, currentTime, currentLoadEvaluator, hostInfoMap);
	}
	
	public void addRebalancer(LoadBasedRebalancer rebalancer) {
		this.rebalancers.add(rebalancer);
	}

	@Override
	protected void processIteration() {
		// Make sure we can set a new plan
		if (this.canSetNewPlan() == false)
			return;
		
		// Run through the list but stop when a new plan has been submitted
		for (LoadBasedRebalancer rebalancer: rebalancers) {
			// Set rebalancer's list of active hosts as our current list of active hosts
			rebalancer.getActiveHosts().clear();
			rebalancer.getActiveHosts().addAll(this.activeHosts);
			
			// Process iteration
			rebalancer.processIteration();
			
			// Obtain the list of active hosts from the current rebalancer and put it in our own list
			this.activeHosts.clear();
			this.activeHosts.addAll(rebalancer.getActiveHosts());
			
			// If new plan available then set new plan
			if (rebalancer.isNewPlanAvailable()) {
				setNewPlan(rebalancer.getNewPlan());
				System.out.println("***");
				System.out.println("***");
				System.out.println("*** NEW PLAN GENERATED BY " + rebalancer.getClass().getName() + " ***");
				System.out.println("***");
				System.out.println("***");
				// Bail out
				break;
			}
		}
	}

	@Override
	public synchronized void setCurrentLoadEvaluator(
			LoadEvaluator currentLoadEvaluator) {
		
		super.setCurrentLoadEvaluator(currentLoadEvaluator);
		
		// Set it for all rebalancers
		for (LoadBasedRebalancer rebalancer: rebalancers) {
			rebalancer.setCurrentLoadEvaluator(currentLoadEvaluator);
		}
	}

	@Override
	public synchronized void setHostInfoMap(
			Map<RPubClientId, RPubHostInfo> hostInfoMap) {
		// 
		super.setHostInfoMap(hostInfoMap);
		
		// Set it for all rebalancers
		for (LoadBasedRebalancer rebalancer: rebalancers) {
			rebalancer.setHostInfoMap(hostInfoMap);
		}
	}

	@Override
	public synchronized void setCurrentPlan(Plan plan) {
		// 
		super.setCurrentPlan(plan);
		
		// Set it for all rebalancers
		for (LoadBasedRebalancer rebalancer: rebalancers) {
			rebalancer.setCurrentPlan(plan);
		}		
	}

	@Override
	public synchronized void setCurrentTime(int currentTime) {
		// 
		super.setCurrentTime(currentTime);
		
		// Set it for all rebalancers
		for (LoadBasedRebalancer rebalancer: rebalancers) {
			rebalancer.setCurrentTime(currentTime);
		}
	}

}
