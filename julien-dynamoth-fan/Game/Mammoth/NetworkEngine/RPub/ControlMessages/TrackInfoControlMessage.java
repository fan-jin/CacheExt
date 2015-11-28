package Mammoth.NetworkEngine.RPub.ControlMessages;

public class TrackInfoControlMessage extends ControlMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8706559773525644149L;
	private int time;
	private int[] responseTimes;
	private int averageTime;
	private int moveMessageCount;

	public TrackInfoControlMessage(int time, int averageTime, int moveMessageCount) {
		this(time, null, averageTime, moveMessageCount);
	}
	
	public TrackInfoControlMessage(int time, int[] responseTimes, int averageTime, int moveMessageCount) {
		this.time = time;
		this.responseTimes = responseTimes;
		this.averageTime = averageTime;
		this.moveMessageCount = moveMessageCount;
	}

	public int getTime() {
		return time;
	}

	public int[] getResponseTimes() {
		return responseTimes;
	}

	public int getAverageTime() {
		return averageTime;
	}

	public int getMoveMessageCount() {
		return moveMessageCount;
	}

	
}
