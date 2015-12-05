/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise.Demo;

import CacheWise.Demo.ImageJComponent;
import CacheWise.BaseObject;
import java.io.File;
import java.io.IOException;
import org.imgscalr.Scalr.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import org.imgscalr.Scalr;

/**
 *
 * @author fjin1
 */
public class ExampleImage extends BaseObject {
    
    private ImageJComponent img;
    
    public ExampleImage(String key, String src)
    {
        super(key);
        img = new ImageJComponent(src);
    }
    
    public static void main(String[] args)
    {
        ExampleImage i = new ExampleImage("test", "/home/fjin1/Desktop/comp396/images/mcgill-campus.jpg");
        
        JFrame f = new JFrame("Load Image Sample");
            
//        f.addWindowListener(new WindowAdapter(){
//                public void windowClosing(WindowEvent e) {
//                    System.exit(0);
//                }
//            });

        i.img.img = Scalr.resize(i.img.img, 720);
        i.img.img = Scalr.rotate(i.img.img, Rotation.FLIP_HORZ);
        i.img.img = Scalr.rotate(i.img.img, Rotation.FLIP_HORZ);
 
        
        f.add(i.img);
        f.pack();
        f.setVisible(true);
    }
}
