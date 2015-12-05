/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author fjin1
 */
public class Invocation {
    
    public static <T> Method validate(BaseObject o, String name, T ... arguments)
    {
        System.out.println("Invocation::validate: name=" + name);
        boolean match = false;
        Method m = null;
        Class<?> c = o.getClass();
        Method[] methods = c.getMethods();
        for (Method method : methods)
        {
            if (method.getName().equals(name))
            {
                System.out.println("Invocation::validate: method name match on " + name);
                boolean matchName = true;
                boolean matchParams = true;
                Class<?>[] ptypes = method.getParameterTypes();
                System.out.println("Invocation::validate: number of params is " + ptypes.length);
                System.out.println("Invocation::validate: passed number of args is " + arguments.length);
                if (ptypes.length != arguments.length) continue; // mismatch on number of params
                System.out.println("Invocation::validate: matching number of params");
                for (int i = 0; i < ptypes.length; i++)
                {
                    Class<?> p = arguments[i].getClass();
                    System.out.println("Invocation::validate: passed arg class: " + p.getName());
                    System.out.println("Invocation::validate: method param class: " + ptypes[i].getName());
                    if (!ptypes[i].getName().equals(p.getName()))
                    {
                        // Match types on Java Primitives
                        if (ptypes[i].getName().equals("int") && p.getName().equals("java.lang.Integer")) continue;
                        if (ptypes[i].getName().equals("boolean") && p.getName().equals("java.lang.Boolean")) continue;
                        if (ptypes[i].getName().equals("byte") && p.getName().equals("java.lang.Byte")) continue;
                        if (ptypes[i].getName().equals("char") && p.getName().equals("java.lang.Char")) continue;
                        if (ptypes[i].getName().equals("float") && p.getName().equals("java.lang.Float")) continue;
                        if (ptypes[i].getName().equals("long") && p.getName().equals("java.lang.Long")) continue;
                        if (ptypes[i].getName().equals("short") && p.getName().equals("java.lang.Short")) continue;
                        if (ptypes[i].getName().equals("double") && p.getName().equals("java.lang.Double")) continue;
                        matchParams = false;
                        break;
                    }
                }
                if (matchName && matchParams)
                {
                    match = true;
                    m = method;
                    break;
                }
            }
        }
        System.out.println("Invocation::validate: " + match);
        return m;
    }
}
