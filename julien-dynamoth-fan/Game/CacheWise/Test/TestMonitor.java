/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise.Test;

import Mammoth.NetworkEngine.RPub.Util.SigarUtil;
import Mammoth.Util.Log.NetworkData;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;

/**
 *
 * @author fjin1
 */
public class TestMonitor {

    public static void main(final String[] args)
    {
        // Sigar thread
        new Thread(new Runnable()
            {
                @Override
                public void run() {
                    System.out.println("Sigar Thread Starting");
                    // Sigar - for network logging (compare NetData VS computed data)
                    SigarProxy sigar;
                    
                    // NetData
                    NetworkData netData = null;
                    SigarUtil.setupSigarNatives();
                    Sigar sigarImpl = new Sigar();
                    
                    try {
			netData = new NetworkData(sigarImpl);
                    } catch (SigarException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        netData = null;
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        netData = null;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        netData = null;
                    }

                    sigar = SigarProxyCache.newInstance(sigarImpl, 100);
                    
                    DateFormat dF = new SimpleDateFormat("yyyy_MM_dd HH-mm-ss");
                    Calendar cal = Calendar.getInstance();
                    String fileName = dF.format(cal.getTime())+ ".txt";
                    
                    if (args.length > 0)
                    {
                        // prepend provided string
                        fileName = args[0] + fileName;
                    }
                    fileName = "../results/" + fileName;
                    File fp = new File(fileName);
                    FileWriter fw = null;
                    try{
                        if (fp.createNewFile()){
                                fw = new FileWriter(fileName,true);
                        }
                        System.out.println("Writing to file: " + fileName);
                    } catch (Exception e){
                            e.printStackTrace();
                    }
                                        
                    Long[] m;
                    while (true)
                    {
                        try {
                            m = netData.getMetric();

                            long totalrx = m[0];
                            long totaltx = m[1];
                            System.out.println("totalrx: " + totalrx + ", totaltx: " + totaltx);
                            if (fw !=null){
                                fw.write(totalrx + "," + totaltx + "\n");
                                fw.flush();
                            }
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    
                }
            }).start();
    }
}
