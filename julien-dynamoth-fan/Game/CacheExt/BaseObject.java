/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author fjin1
 */
public abstract class BaseObject implements Serializable {

    protected int version;
    private String key;
    
    public BaseObject(String k)
    {
        this.version = 0;
        this.key = k;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public int getVersion()
    {
        return version;
    }
    
    public void incVersion()
    {
        version++;
    }
    
    public boolean applyUpdate(Operation m)
    {
       return call(m.getName(), m.getArgs());
    }
    
    private synchronized <T> boolean call(String name, T ... arguments)
    {
        Method m;
        if ((m = Invocation.validate(this, name, arguments)) == null) return false;
        try
        {
            System.out.println("BaseObject::call: invoking method " + name + " at key " + key);
            m.invoke(this, arguments);
            this.incVersion();
            return true;
        } catch (IllegalAccessException e)
        {
            System.err.println("BaseObject::call: IllegalAccessException exception: " + e.toString());
            e.printStackTrace();
        } catch (IllegalArgumentException e)
        {
            System.err.println("BaseObject::call: IllegalArgumentException exception: " + e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            System.err.println("BaseObject::call: InvocationTargetException exception: " + e.toString());
            e.printStackTrace();
        }
        return false;
    }
}
