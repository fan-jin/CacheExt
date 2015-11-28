package Mammoth.NetworkEngine.RPub;

public interface RPubMessageListener {
	void messageReceived(String channelName, RPubMessage message, int rawMessageSize);
}
