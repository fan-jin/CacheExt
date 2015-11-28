/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author fjin1
 */
public class Serialization {
    
    private Serialization()
    {
        
    }
    
    /**
     * Serialize the object
     *
     * @param o the object to serialize
     */
    public static byte[] serialize(Object o)
    {
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(baos);
            objectOut.writeObject(o);
            objectOut.close();
            return baos.toByteArray();
        }
        catch (Exception e)
        {
            System.err.println("Serialization error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deserialize the object
     *
     * @param o the object to deserialize
     */
    public static Object deserialize(byte[] o)
    {
        try{
            ByteArrayInputStream bais = new ByteArrayInputStream(o);
            ObjectInputStream objectIn = new ObjectInputStream(bais);
            Object obj = objectIn.readObject();
            objectIn.close();
            return obj;
        }
        catch (Exception e)
        {
            System.err.println("Deserialization error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }  
    
}
