/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheExt.Example;

import CacheExt.BaseObject;
import CacheExt.Serialization;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.imgscalr.Scalr;

/**
 *
 * @author fjin1
 */
public class TestImage extends BaseObject {

    transient BufferedImage img; // for in-memory image manipulation
    byte[] img_serialized; // for serialization
    
    public TestImage(String key, String src)
    {
        super(key);
        try {
           img = ImageIO.read(new File(src));
           img_serialized = Serialization.serialize(img);
        } catch (IOException e) {
        }
    }
    
    public void flipHorizontal()
    {
        if (img == null) img = retrieveImage();
        img = Scalr.rotate(img, Scalr.Rotation.FLIP_HORZ);
        persistImage(img);
    }
    
    public void flipVertical()
    {
        if (img == null) img = retrieveImage();
        img = Scalr.rotate(img, Scalr.Rotation.FLIP_VERT);
        persistImage(img);
    }
    
    public void rotateClockwise(int degree)
    {
        if (img == null) img = retrieveImage();
        switch (degree)
        {
            case 90:
                img = Scalr.rotate(img, Scalr.Rotation.CW_90);
                break;
            case 180:
                img = Scalr.rotate(img, Scalr.Rotation.CW_180);
                break;
            case 270:
                img = Scalr.rotate(img, Scalr.Rotation.CW_270);
                break;
            default:
                break;
        }
        persistImage(img);
    }
    
    private BufferedImage retrieveImage()
    {
        if (img_serialized == null) 
        {
            return null;
        }
        else
        {
            return (BufferedImage) Serialization.deserializeBufferedImage(img_serialized);
        }
    }
    
    private void persistImage(BufferedImage image)
    {
        img_serialized = Serialization.serialize(image);
    }
    
    public void display(int size)
    {
        if (img == null) img = retrieveImage();
        final BufferedImage copy = Scalr.resize(img, size);
        JFrame f = new JFrame("Image Display");
        f.add(new JComponent() {
          
            BufferedImage img = copy;

            public void paint(Graphics g) {
                g.drawImage(img, 0, 0, null);
            }

            @Override
            public Dimension getPreferredSize() {
                if (img == null) {
                     return new Dimension(100,100);
                } else {
                   return new Dimension(img.getWidth(null), img.getHeight(null));
               }
            }
        });
        f.pack();
        f.setVisible(true);
    }
    
    public static void main (String[] args)
    {
        TestImage image = new TestImage("test", "/home/fjin1/Desktop/comp396/images/mcgill-campus.jpg");
        //image.flipHorizontal();
//        image.rotateClockwise(90);
//        image.rotateClockwise(90);
//        image.flipHorizontal();
        // image.display(720);
        TestImage copy = (TestImage) Serialization.deserialize(Serialization.serialize(image));
        copy.flipHorizontal();
        copy.display(720);
    }
}
