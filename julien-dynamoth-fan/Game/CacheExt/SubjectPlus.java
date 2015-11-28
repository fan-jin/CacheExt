/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt;

/**
 *
 * @author fjin1
 */
public class SubjectPlus{
    
    private BaseObject o; // object
    private byte[] o_serialized; // serialized object
    
    public SubjectPlus(byte[] o_seralized)
    {
        this.o = (BaseObject) Serialization.deserialize(o_seralized);
        this.o_serialized = o_seralized;
    }
    
    public boolean applyUpdate(Operation m)
    {
       boolean result = o.applyUpdate(m);
       if (result) o_serialized = null; // throw away serialized version
       return result;
    }
        
    private void serialize()
    {
        o_serialized = Serialization.serialize(o);
    }
    
    public byte[] getSerialized()
    {
        if (o_serialized == null) serialize();
        return o_serialized;
    }
    
    public int getVersion()
    {
        return o.getVersion();
    }
}