package Mammoth.NetworkEngine.RPub.Game.Messages;

public class RGameActivateMessage extends RGameMessage {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3482413428380144627L;
	private int playerStart;
	private int playerEnd;
	private boolean active;
	private boolean flocking;

	public RGameActivateMessage(int playerStart, int playerEnd, boolean active)
	{
		this(playerStart, playerEnd, active, true);
	}
	
	public RGameActivateMessage(int playerStart, int playerEnd, boolean active, boolean flocking) {
		super(-1);
		this.playerStart = playerStart;
		this.playerEnd = playerEnd;
		this.active = active;
		this.flocking = flocking;
	}

	public int getPlayerStart() {
		return playerStart;
	}

	public int getPlayerEnd() {
		return playerEnd;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isFlocking() {
		return flocking;
	}

	public void setFlocking(boolean flocking) {
		this.flocking = flocking;
	}

}
