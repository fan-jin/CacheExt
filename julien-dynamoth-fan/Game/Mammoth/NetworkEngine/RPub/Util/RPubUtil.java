package Mammoth.NetworkEngine.RPub.Util;

import java.util.Random;

public class RPubUtil {

	private static Random random = new Random();
	
	public static int parseRPubClientId(String server) {
		return Integer.parseInt(server.split(":")[0]);
	}
	
	public static String parseRPubHostName(String server) {
		return server.split(":")[1];
	}
	
	public static String parseRPubHostDomain(String server) {
		return server.split(":")[2];
	}
	
	public static int parseRPubHostPort(String server) {
		String hostPort = server.split(":")[3];
		if (hostPort.equals("")) {
			return 6379;
		} else {
			return Integer.parseInt(hostPort);
		}
	}
	
	public static int parseRPubHostKByteIn(String server) {
		return Integer.parseInt(server.split(":")[4]);
	}
	
	public static int parseRPubHostKByteOut(String server) {
		return Integer.parseInt(server.split(":")[5]);
	}
	
	public static long kiloBytesToBytes(long bytes) {
		return bytes * 1024;
	}
	
	public static int getCurrentSystemTime() {
		return (int) (System.currentTimeMillis() / 1000);
	}
	
	public static Random getRandom() {
		return random;
	}
}
