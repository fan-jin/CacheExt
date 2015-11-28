package Mammoth.NetworkEngine.RPub;

import java.io.Serializable;

import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanId;

public abstract class RPubMessage implements Serializable {

	private static final long serialVersionUID = -6602175620733876669L;
	
	private RPubNetworkID sourceID = null;
	private boolean forward = false;
	private boolean fromInfrastructure = false; // True if messages originates from a component of the infrastructure (LLA, LB, etc.)
	private String sourceDomain = ""; // Domain ID (aka-Cloud) of the emitter of that message
	private String rpubServerDomain = ""; // Domain ID (aka-Cloud) of the pub/sub server that processed this message
	private PlanId planID = null;
	
	public RPubMessage(RPubNetworkID sourceID)
	{
		this.sourceID = sourceID;
	}
	
	public RPubMessage(RPubNetworkID sourceID, PlanId planID)
	{
		this.sourceID = sourceID;
		this.planID = planID;
	}
	
	public RPubMessage(RPubNetworkID sourceID, boolean forward, boolean fromInfrastructure)
	{
		this.sourceID = sourceID;
		this.forward = forward;
		this.fromInfrastructure = fromInfrastructure;
	}
	
	public PlanId getPlanID() {
		return this.planID;
	}

	public void setPlanID(PlanId planID) {
		this.planID = planID;
	}
	
	public RPubNetworkID getSourceID() {
		return this.sourceID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sourceID == null) ? 0 : sourceID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RPubMessage other = (RPubMessage) obj;
		if (sourceID == null) {
			if (other.sourceID != null)
				return false;
		} else if (!sourceID.equals(other.sourceID))
			return false;
		return true;
	}
	
	public boolean isForward() {
		return forward;
	}
	public void setForward(boolean forward) {
		this.forward = forward;
	}
	public boolean isFromInfrastructure() {
		return fromInfrastructure;
	}
	public void setFromInfrastructure(boolean fromInfrastructure) {
		this.fromInfrastructure = fromInfrastructure;
	}

	public String getSourceDomain() {
		return sourceDomain;
	}

	public void setSourceDomain(String sourceDomain) {
		this.sourceDomain = sourceDomain;
	}

	public String getRpubServerDomain() {
		return rpubServerDomain;
	}

	public void setRpubServerDomain(String rpubServerDomain) {
		this.rpubServerDomain = rpubServerDomain;
	}
}
