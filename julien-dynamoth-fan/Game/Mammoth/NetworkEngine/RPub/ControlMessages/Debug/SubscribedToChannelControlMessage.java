package Mammoth.NetworkEngine.RPub.ControlMessages.Debug;

import Mammoth.NetworkEngine.RPub.RPubNetworkID;
import Mammoth.NetworkEngine.RPub.ControlMessages.ControlMessage;

public class SubscribedToChannelControlMessage extends ControlMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6933702576727293926L;
	private RPubNetworkID networkId;

	public SubscribedToChannelControlMessage(RPubNetworkID networkId) {
		this.networkId = networkId;
	}

	public RPubNetworkID getNetworkId() {
		return networkId;
	}

	
}
