

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class GridsCanvas extends JPanel {
  int width, height;
  int rows;
  int cols;
  String path="/home/pi/Table_touchscreen/boolean/col";
  BufferedReader br;
  FileReader fr;

  GridsCanvas(int w, int h, int r, int c) {
    setSize(width = w, height = h);
    rows = r;
    cols = c;
  }

  public void paintComponent(Graphics g) {
      int i;
    width = getSize().width;
    height = getSize().height;
    g.setColor(Color.white);

    // draw the rows
    int rowHt = height / (rows);
    for (i = 0; i < rows+1; i++)
      g.drawLine(0, i * rowHt, width, i * rowHt);

    // draw the columns
    int rowWid = width / (cols);
    for (i = 0; i < cols; i++)
        g.setColor(Color.red);
      g.drawLine(i * rowWid, 0, i * rowWid, height);
    
        for (i=0;i<rows;i++){
        for (int k=0; k<cols; k++){
            
        String filename = path + Integer.toString(k) + "/row" + Integer.toString(i)+".txt";
      try {
          fr = new FileReader(filename);
      } catch (FileNotFoundException ex) {
          Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
      }
      br = new BufferedReader(fr);
      String line;
            try {
                while((line = br.readLine()) != null){
                    g.setColor(Color.white);
                    //Rectangle rec=new Rectangle(k*rowWid, i*rowHt+rowHt/2, 40, 40);
                    g.fillRect(k*rowWid+10, i*rowHt, 40, 40);
                    
                   
                    g.setColor(Color.red);
                    if(line.contains("1")){
                        g.fillRect(k*rowWid+10, i*rowHt, 40, 40);
                    }
                    //g.drawString(line, k*rowWid+rowWid/2, i*rowHt+rowHt/2);
                }     } catch (IOException ex) {
                Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
  }
  
  public void displayValues(Graphics g){
    int i;

    width = getSize().width;
    height = getSize().height;
    int rowHt = height / (rows);
    int rowWid = width / (cols);
     

  }
}

/*class lab extends JLabel{
    
    int width = getSize().width;
    int height = getSize().height;
   
  int rows=12;
  int cols=12;
  String path="/home/pi/Table_touchscreen/values/col";
  BufferedReader br;
  FileReader fr;
  int rowWid = width / (cols);
  int rowHt = height / (rows);
    
    public lab() {
        int i;
        for (i=0;i<rows;i++){
        for (int k=0; k<cols; k++){
            
        String filename = path + Integer.toString(k) + "/row" + Integer.toString(i)+".txt";
      try {
          fr = new FileReader(filename);
      } catch (FileNotFoundException ex) {
          Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
      }
      br = new BufferedReader(fr);
      String line;
            try {
                while((line = br.readLine()) != null){
                    
                    drawString(line, k*rowWid+rowWid/2, i*rowHt+rowHt/2);
                }     } catch (IOException ex) {
                Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(GridsCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    }
    
    

}*/



public class Main extends JFrame {
  
    static GridsCanvas xyz;
    
    public Main() {
    xyz = new GridsCanvas(20, 20, 12, 12);
    add(xyz);
    
    this.setSize(500, 500);
  }
    
    public static void refresh(){
        
        xyz.removeAll();
        
        xyz.revalidate();
        xyz.repaint();
    }

  public static void main(String[] a) {
    
     // while (true){
     
     
     new Main().setVisible(true);
     while (true){
         Main.refresh();
         try {
             Thread.sleep(250);
         } catch (InterruptedException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
         
     }
       // try {
      //      Thread.sleep(3000);
      //  } catch (InterruptedException ex) {
      //      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      //  }
        
        
   // }
  }
}