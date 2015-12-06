/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CacheWise.Test;

import CacheWise.BaseObject;
import CacheWise.Operation;
import CacheWise.Serialization;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
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
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
    
    public static void main (String[] args)
    {
        if (args.length > 0)
        {
            String action = args[0];
            int iteration = Integer.valueOf(args[1]);
            String prefix = args[2];
            String src = args[3];
            TestImage image = new TestImage("test", src);
            
            DateFormat dF = new SimpleDateFormat("yyyy_MM_dd HH-mm-ss");
            Calendar cal = Calendar.getInstance();
            String fileName = dF.format(cal.getTime())+ ".csv";
            fileName = prefix + fileName;
            fileName = "../results/" + fileName;
            File fp = new File(fileName);
            FileWriter fw = null;
            try{
                if (fp.createNewFile()){
                    fw = new FileWriter(fileName, true);
                    System.out.println("Writing to file: " + fileName);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            
            for (int i = 0; i < iteration; i++)
            {
                long before = System.nanoTime();
                if (action.equals("flip"))
                {
                    image.applyUpdate(new Operation(image.getVersion() + 1, "flipHorizontal"));
                }
                else if (action.equals("rotate"))
                {
                    image.applyUpdate(new Operation(image.getVersion() + 1, "rotateClockwise", 180));
                }
                long after = System.nanoTime();
                try
                {
                    if (fw !=null){
                        fw.write((after - before) + "\n");
                        fw.flush();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println((after - before));
            }
        }
    }
}
