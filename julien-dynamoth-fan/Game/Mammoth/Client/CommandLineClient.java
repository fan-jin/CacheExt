package Mammoth.Client;

import Mammoth.NetworkEngine.RPub.RPubHubWrapper;
import Mammoth.NetworkEngine.RPub.Game.RMain;
import Mammoth.NetworkEngine.RPub.Game.RServer;
import Mammoth.NetworkEngine.RPub.Game.Replication.RReplicationMain;
import Mammoth.NetworkEngine.RPub.Game.Replication.dynamic.DReplicationMain;
import Mammoth.NetworkEngine.RPub.LoadBalancing.LoadBalancer;
import Mammoth.NetworkEngine.RPub.Util.KingDataset;
import Mammoth.NetworkEngine.RPub.Util.MaxThreadsMain;
import Mammoth.NetworkEngine.RPub.Util.RawKingDataset;
import CacheWise.Example.Example;
import CacheWise.Impl.CacheServer;
import CacheWise.Test.TestClient;
import CacheWise.Test.TestImage;

public class CommandLineClient {

	public static void main(String[] args) throws Exception {

		// Echo ec2.region JVM property
		String ec2Region = System.getProperty("ec2.region", "");
		System.out.println("EC2-Region: " + ec2Region);
		
                System.out.println("Launch Args:");
                for (int i = 0; i < args.length; i++)
                {
                    System.out.println("args["+i+"]=" + args[i]);
                }
                
		if (args.length < 0) {
			System.err.println("Invalid arguments");
		}

		String mode = args[0];

		if (mode.equals("rpubhub")) {
			String hubId = "0";
			if (args.length>0) {
				// We might have the HUB-id... pass it
				hubId = args[1];
			}
			RPubHubWrapper.main(new String[]{hubId});			
		} else if (mode.equals("rgame")) {
			// Recopy all args
			String[] rgameArgs = new String[args.length - 1];
			for (int i=1; i<args.length; i++) {
				rgameArgs[i-1] = args[i];
			}
			RMain.main(rgameArgs);
		} else if (mode.equals("rserver")) {
			RServer.main(new String[]{""});
		} else if (mode.equals("loadbalancer")) {
			LoadBalancer.main(new String[]{""});
		} else if (mode.equals("replication-test")) {
			String[] rRepArgs = new String[args.length - 1];
			for (int i=1; i<args.length; i++) {
				rRepArgs[i-1] = args[i];
			}
			RReplicationMain.main(rRepArgs);
		} else if (mode.equals("dynamic-replication-test")) {
			String[] rRepArgs = new String[args.length - 1];
			for (int i=1; i<args.length; i++) {
				rRepArgs[i-1] = args[i];
			}
			DReplicationMain.main(rRepArgs);
		} else if (mode.equals("kingtest")) {
			KingDataset.main(new String[] {});
		} else if (mode.equals("rawkingtest")) {
			RawKingDataset.main(new String[] {});
		} else if (mode.equals("maxthreadsmain")) {
			MaxThreadsMain.main(new String[] {});
		} else if (mode.equals("cacheext-server")) {
                        String[] serverArgs = new String[args.length - 1];
                        if (args.length > 1)
                        {
                            for (int j = 1; j < args.length; j++)
                            {
                                serverArgs[j-1] = args[j];
                            }
                        }
			CacheServer.main(serverArgs);
		} else if (mode.equals("cacheext-client")) {
			String[] clientArgs = new String[args.length - 1];
                        if (args.length > 1)
                        {
                            for (int j = 1; j < args.length; j++)
                            {
                                clientArgs[j-1] = args[j];
                            }
                            TestClient.main(clientArgs);
                        }                        
		}
	}
}
