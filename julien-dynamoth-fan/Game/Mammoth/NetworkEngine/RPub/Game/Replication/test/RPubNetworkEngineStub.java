package Mammoth.NetworkEngine.RPub.Game.Replication.test;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.ClosedChannelException;

import Mammoth.NetworkEngine.NetworkEngineID;
import Mammoth.NetworkEngine.Exceptions.NoSuchClientException;
import Mammoth.NetworkEngine.RPub.RPubNetworkEngine;
import Mammoth.NetworkEngine.RPub.Manager.DynamothRPubManager;

public class RPubNetworkEngineStub extends RPubNetworkEngine {

	private DynamothRPubManager manager;

	public RPubNetworkEngineStub(DynamothRPubManager manager) {
		this.manager = manager;
	}
	
	public DynamothRPubManager getRPubManager() {
		return this.manager;
	}
	
	public void send(NetworkEngineID ClientId, Serializable object)
			throws IOException, ClosedChannelException, NoSuchClientException {
		
	}


}
