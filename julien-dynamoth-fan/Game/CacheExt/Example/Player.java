/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt.Example;

import CacheExt.BaseObject;
import CacheExt.Operation;
/**
 *
 * @author fjin1
 */
public class Player extends BaseObject {

    private String name;
    private int var;
    
    public Player(String k, String n, int v)
    {
        super(k);
        this.name = n;
        this.var = v;
    }
    
    public int getVar()
    {
        return var;
    }
    
    public void setVar(int i)
    {
        var = i;
    }
    
    public void multiplyVar(int n)
    {
        var*=n;
    }
    
    @Override
    public String toString()
    {
        return "Player\n-Key: " + super.getKey() +
                "\n-Version: " + super.getVersion() +
                "\n-Name: " + name +
                "\n-Variable: " + var;
    }
    
    public static void main(String args[])
    {
        Player p = new Player("test", "person1", 3);
        System.out.println(p);
        p.applyUpdate(new Operation(1, "setVar", 5));
        System.out.println(p);
    }
}
