package Mammoth.NetworkEngine.RPub.ControlMessages;

import Mammoth.NetworkEngine.RPub.Client.RPubClientId;
import Mammoth.NetworkEngine.RPub.Manager.Plan.PlanMapping;

public class ChangeChannelMappingControlMessage extends ControlMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1932025888410084780L;
	private String channel;
	private PlanMapping mapping;
	private RPubClientId sourceClientId;

	public ChangeChannelMappingControlMessage(String channel, PlanMapping mapping, RPubClientId sourceClientId) {
		this.channel = channel;
		this.mapping = mapping;
		this.sourceClientId = sourceClientId;
	}

	public String getChannel() {
		return channel;
	}

	public PlanMapping getMapping() {
		return mapping;
	}

	public RPubClientId getSourceClientId() {
		return sourceClientId;
	}

}
