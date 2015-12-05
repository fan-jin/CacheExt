/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 *
 * @author fjin1
 */
public interface CacheExt extends Remote {
    
    public void loadObj(String key, byte[] obj) throws RemoteException;
    public byte[] getObj(String key) throws RemoteException;
    public boolean performOperation(String key, Operation m) throws RemoteException;
}
