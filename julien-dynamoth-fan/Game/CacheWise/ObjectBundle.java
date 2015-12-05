/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author fjin1
 */
public class ObjectBundle {
    
    BaseObject obj;
    LinkedList<Operation> methods = new LinkedList<Operation>();
    
    public ObjectBundle(BaseObject o)
    {
        obj = o;
    }
    
    public synchronized boolean queue(Operation ops)
    {
        log("ObjectBundle::queue: operation=" + ops.getName());
        if (obj == null && methods.isEmpty())
        {
            // object has not yet been received from the server, and the first update arrives
            // enqueue the first update
            log("ObjectBundle::queue: object=null, queue is empty, add to queue " + ops.getName());
            methods.add(ops);
            return true;
        }
        else if (obj == null && !methods.isEmpty())
        {
            // object has not yet been received from the server, subsequent update arrives
            Operation last = methods.getLast(); // get the latest update in the queue
            if (ops.getVersion() == last.getVersion() + 1) // correct version arrives
            {
                log("ObjectBundle::queue: object=null, appending to previous update with version=" + last.getVersion() + " in queue");
                methods.add(ops);
                return true;
            }
            else if (ops.getVersion() > last.getVersion() + 1) // missed at least one update message
            {
                log("ObjectBundle::queue: object=null, missed update detected, clearing update queue");
                methods.clear(); // clear the queue because an update was already missed, rendering all queued updates useless
                return false;
            }
        }
        else if (obj != null && methods.isEmpty())
        {
            // object has been received from the server, first update arrives
            log("ObjectBundle::queue: object is present, queue is empty");
            if (ops.getVersion() == obj.getVersion() + 1) // version is correct
            {
                log("ObjectBundle::queue: appending update to queue");
                methods.add(ops);
                return true;
            }
            else if (ops.getVersion() > obj.getVersion() + 1) // missed at least one update message
            {
                log("ObjectBundle::queue: missed update detected, clearing update queue");
                methods.clear(); // clear the queue because an update was already missed, rendering all queued updates useless
                return false;
            }
            else if (ops.getVersion() <= obj.getVersion()) // older update arrives, ignore
            {
                log("ObjectBundle::queue: older update " + ops.getName() + " arrived, ignoring");
                return true;
            }
        }
        else if (obj != null && !methods.isEmpty())
        {
            // object has been received from the server, subsequent update arrives
            Operation last = methods.getLast(); // get the latest update in the queue
            if (ops.getVersion() == last.getVersion() + 1) // correct version arrives
            {
                log("ObjectBundle::queue: object is present, appending to previous update " + last.getVersion() + " in queue");
                methods.add(ops);
                return true;
            }
            else if (ops.getVersion() > last.getVersion() + 1) // missed at least one update message
            {
                log("ObjectBundle::queue: missed update detected, clearing update queue");
                methods.clear(); // clear the queue because an update was already missed, rendering all queued updates useless
                return false;
            }
        }
        return false;
    }
    
    public synchronized void apply()
    {
        // object has not yet been received from the server, do nothing
        if (obj != null)
        {
            validateQueue();
            while (!methods.isEmpty()) obj.applyUpdate(methods.remove());
        }
    }
    
    private void validateQueue()
    {
        Iterator<Operation> iterator = methods.iterator();
        while (iterator.hasNext())
        {
            Operation ops = iterator.next();
            if (ops.getVersion() <= obj.getVersion())
            {
                log("ObjectBundle::validateQueue: older update " + ops.getName() + " detected, removing");
                // clean up older updates
                iterator.remove();
            }
            else
            {
                // stop at new updates
                break;
            }
        }
    }
    
    public BaseObject getObj()
    {
        return obj;
    }
    
    public void setObj(BaseObject o)
    {
        obj = o;
    }
    
    protected void log(String s)
    {
        System.out.println(s);
    }
}
