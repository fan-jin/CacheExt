package Mammoth.NetworkEngine.RPub.Game.Messages;

public class RGameMoveMessage extends RGameMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6271967869529363027L;
	
	private double baseX, baseY;
	private double targetX, targetY;
	
	private int counter;
	
	// Padding
	public static int PADDING_TOTAL_BYTES = 2000;
	private byte[] padding = null; //500*4
	
	public RGameMoveMessage(int playerId, double baseX, double baseY, double targetX, double targetY, int counter) {
		super(playerId);
		
		this.baseX = baseX;
		this.baseY = baseY;
		this.targetX = targetX;
		this.targetY = targetY;
		this.counter = counter;
	}

	public double getBaseX() {
		return baseX;
	}

	public double getBaseY() {
		return baseY;
	}

	public double getTargetX() {
		return targetX;
	}

	public double getTargetY() {
		return targetY;
	}

	public int getCounter() {
		return counter;
	}
	
	public void setPadding(int byteCount) {
		this.padding = new byte[byteCount];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(baseX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(baseY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + counter;
		temp = Double.doubleToLongBits(targetX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(targetY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		RGameMoveMessage other = (RGameMoveMessage) obj;
		if (Double.doubleToLongBits(baseX) != Double
				.doubleToLongBits(other.baseX))
			return false;
		if (Double.doubleToLongBits(baseY) != Double
				.doubleToLongBits(other.baseY))
			return false;
		if (counter != other.counter)
			return false;
		if (Double.doubleToLongBits(targetX) != Double
				.doubleToLongBits(other.targetX))
			return false;
		if (Double.doubleToLongBits(targetY) != Double
				.doubleToLongBits(other.targetY))
			return false;
		return true;
	}

}
