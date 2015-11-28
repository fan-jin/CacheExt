/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt;

import java.io.Serializable;

/**
 *
 * @author fjin1
 */
public class Operation implements Serializable {
    private String name;
    private Object[] args;
    private int version;

    public <T> Operation(int version, String name, T ... arguments)
    {
        this.name = name;
        this.version = version;
        this.args = arguments;        
    }
    
    public String getName()
    {
        return name;
    }
    
    public Object[] getArgs()
    {
        return args;
    }
    
    public int getVersion()
    {
        return version;
    }
}
