//Class FileManager: created by Daniel Koerner
//
//      used for CaseStudy @ HS-Fulda, May, 2018
//      ##  last edit: 23.05.2018
//
//
//
//      The FileManager is a class to create directories and folders 
//      and for writing to / reading from files 
//      


//      SUMM:
//      it reads:
//      - the last calibration from files on the system (the threshhold-values)
//      it writes:
//      - the "boolean" data (actually 1/0, the output-states for the matrix-fields)
//      - the last calibration
//      - the pid (process id to kill the programm when starting "Glediator")
//      - mode ('i' for interactive, 'p' for passive)
//      - DISCARDED: the Values of the measurements


package ledtable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {
    
    //working-directory
    static String path="/home/pi/LEDTable/";
   
    //write the current Threshhold-values to files fater calibration
    public void writeThresh(int [][] thresh){
        
         PrintWriter pw;
        
        for (int i=0;i<12;i++){
           for (int k=0;k<12;k++){
               
               String calPath=path+"calibration/box" + Integer.toString(i) + "_" + Integer.toString(k)+".txt";
                  
               try{
                   pw = new PrintWriter(calPath);
//write the value to the file
                    pw.println(thresh[i][k]);
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR");
                }
           }            
        }
    }
    
    //get the Threshhold from the last calibration (we do that after restart)
    public int [][] readThresh() throws FileNotFoundException, IOException{
        
        BufferedReader br;
        FileReader fr;
        int [][]thresh = new int[12][12];
        
        for (int i=0;i<12;i++){
           for (int k=0;k<12;k++){
                
               String calPath=path+"calibration/box" + Integer.toString(i) + "_" + Integer.toString(k)+".txt";
               fr=new FileReader(calPath); 
               br = new BufferedReader(fr);
               
               /*String line;
               while((line=br.readLine())!= null){
                   thresh [i][k]=Integer.parseInt(line);
               }*/
               
               String content=br.readLine();
               int val = Integer.parseInt(content);
               thresh[i][k]=val;
               
               br.close();
               fr.close();
           }
        }
        
        return thresh;
    }

    //Create folders and files, called at the very beginning
    public void createFiles(){

      
        
       // System.out.println("Create Files");//debugging
        
//Create all folders

        File first = new File(path);
        first.mkdir();
      
        String calPath= path+"calibration";
        File cal = new File(calPath);
        cal.mkdir();
      
        String bool = path + "boolean/";
        File f1 = new File(bool);
        f1.mkdir(); 
        String seq = bool + "sequential/";
        File f2 = new File(seq);
        f2.mkdir(); 
      
      
      
      
//File for mode (i -interactive/p-passive)
      String mpath=path+"mode.txt";
      
//create the file mode.txt
      File m=new File(mpath);
      PrintWriter pw; 
      PrintWriter pw2; 
                try{
                    
//start with interactive mode
                    pw = new PrintWriter(mpath);
                    pw.println('i');
                    pw.close();} 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                


        
//Create files for every box (led1 to led 144)
        for (int a = 1; a < 145; a++) {
            
            String dpath = seq + "led" + Integer.toString(a) + ".txt";
            File f = new File(dpath);

//create the file ledi.txt
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            
//write 0 to the file (initial off)
            try {
                pw = new PrintWriter(dpath);
                pw.println(0);
                pw.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

//Create folders for every col max min
      /*  for (int i=0; i<12; i++){
            String fpath =path + "values/max/col" + Integer.toString(i);
            String fpath2=path + "values/min/col" + Integer.toString(i);
            File folder = new File(fpath);
            File folder2 = new File(fpath2);
            folder.mkdir();
            folder2.mkdir();
            
            //Create files for every row
            for(int a=0; a<12; a++){
                String dpath=fpath   + "/row" + Integer.toString(a) + ".txt";
                String dpath2=fpath2 + "/row" + Integer.toString(a) + ".txt";
                
                File f = new File( dpath);
                File f2 = new File( dpath2);
                
                try {
                    f.createNewFile();
                    f2.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //write 0 to the file
                
                try{
                    pw = new PrintWriter(dpath);
                    pw2 = new PrintWriter(dpath2);
                    pw.println(0);
                    pw2.println(0);
                    pw.close();
                    pw2.close();
                } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }*/
                
                
    /*    
//Create folders for every col
        for (int i=0; i<12; i++){
            String fpath=path + "values/col" + Integer.toString(i);
            File folder = new File(fpath);
            folder.mkdir();
            
            //Create files for every row
            for(int a=0; a<12; a++){
                String dpath=fpath + "/row" + Integer.toString(a) + ".txt";
                File f = new File( dpath);
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //write 0 to the file
                
                try{
                    pw = new PrintWriter(dpath);
                    pw.println(0);
                    pw.close();} 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }*/
        
        /*for(int i=0;i<12;i++){
         String fpath=path + "boolean/col" + Integer.toString(i);
            File folder = new File(fpath);
            folder.mkdir();
            
            //Create files for every row
            for(int a=0; a<12; a++){
                String dpath=fpath + "/row" + Integer.toString(a) + ".txt";
                File f = new File( dpath);
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //write 0 to the file
                PrintWriter pw; 
                try{
                    pw = new PrintWriter(dpath);
                    pw.println(0);
                    pw.close();} 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
        
        

                
    }
    
    //discarded
    //write values (folder col, file row)
    public void writeValue(){
        /*PrintWriter pw;
        String folderPath=path+ "values/";
        
        
        for(int j=0;j<12;j++){
          for(int i=0; i<12; i++){  
            
            String min=  folderPath + "min/col" + Integer.toString(j) + "/row"+ Integer.toString(i) + ".txt"; 
            String max=  folderPath + "max/col" + Integer.toString(j) + "/row"+ Integer.toString(i) + ".txt"; 

            
            try{
                    pw = new PrintWriter(min);
                    pw.println(Calibration.minValues[j][i]);
                    pw.close();
                    pw = new PrintWriter(max);
                    pw.println(Calibration.maxValues[j][i]);
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR");
                }
        } 
        }*/
        
        
    }

    //write boolean values (on/off(1/0)) of the recieved col to the correct file 
    public void writeBoolean(int col, int [] val){
                
        PrintWriter pw;
       
        /* String folderPath=path+ "boolean/col" + Integer.toString(col) + "/row"; 
        
        for(int i=0; i<val.length; i++){
            
            String filePath = folderPath + Integer.toString(i) + ".txt";
            
            try{
                    pw = new PrintWriter(filePath);
                    pw.println(val[i]);
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR");
                }
        }   */
        
        
//check every value
        for(int i=0; i<val.length; i++){
            
            
//find the correct led-Number and choose the correct file
            String filePath = path+ "boolean/sequential/led" + Integer.toString((matrixOrder[i][col])) + ".txt";
            
            try{
                    pw = new PrintWriter(filePath);
                    
//write the value (1/0) to the file
                    pw.println(val[i]);
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR");
                }
        } 
        
    }
    
    //write the pid of this programm to a file
    public void writePid(int pid){
        String pidPath=path+"pid_j.txt";
        PrintWriter pw;
        
        try{
                    pw = new PrintWriter(pidPath);
                    pw.println(pid);
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR");
                }
        
    }
    
    //set all booleans 1, write 'p' to modefile
    public void passiveMode(){
        
        PrintWriter pw;
        
        String mpath=path+"mode.txt";
        
         try{
                    pw = new PrintWriter(mpath);
                    pw.println('p');
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("pm1");
                }
        
        
        for (int i=1;i<145;i++){
            String filePath = path+ "boolean/sequential/led";
            try{
                filePath = filePath + Integer.toString(i) + ".txt";
                    pw = new PrintWriter(filePath);
                    pw.println(1);
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("pm2");
                }
        }
    }
    
    //write 'i' to modfile
    public void interactiveMode(){
        
        PrintWriter pw;
        
        String mpath=path+"mode.txt";
        
         try{
                    pw = new PrintWriter(mpath);
                    pw.println('i');
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR");
                }
        }
    
    //write the matrixdata to show a 'X'
    //X should say its the calibration-mode
    public void showX(){
                PrintWriter pw;
       
         for(int i=0; i<12; i++){
            for (int k=0;k<12;k++){

//find the correct led-Number and choose the correct file
            String filePath = path+ "boolean/sequential/led" + Integer.toString((matrixOrder[k][i])) + ".txt";
            
            try{
                    pw = new PrintWriter(filePath);
                    
//write the value (1/0) to the file
                    pw.println(letterX[i][k]);
                    pw.close();
            } 
                catch (FileNotFoundException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("ERROR");
                }
        } 
    }
    }
    
    
    
        
    // order of the LED-Numbers to the matrix (serpentine-Layout)
    //[row][col]
    static int matrixOrder[][]=new int [][]{                                //rows:    
                        {1  ,2  ,3  ,4  ,5  ,6  ,7  ,8  ,9  ,10 ,11 ,12} ,  //0
                        {24 ,23 ,22 ,21 ,20 ,19, 18 ,17 ,16 ,15 ,14, 13} ,  //1
                        {25 ,26 ,27 ,28 ,29 ,30 ,31 ,32 ,33 ,34 ,35 ,36} ,  //2
                        {48 ,47 ,46 ,45 ,44 ,43 ,42 ,41 ,40 ,39 ,38 ,37} ,  //3
                        {49 ,50 ,51 ,52 ,53 ,54 ,55 ,56 ,57 ,58 ,59 ,60} ,  //4
                        {72 ,71 ,70 ,69 ,68 ,67 ,66 ,65 ,64 ,63 ,62 ,61} ,  //5
                        {73 ,74 ,75 ,76 ,77 ,78 ,79 ,80 ,81 ,82 ,83 ,84} ,  //6
                        {96 ,95 ,94 ,93 ,92 ,91 ,90 ,89 ,88 ,87 ,86 ,85} ,  //7
                        {97 ,98 ,99 ,100,101,102,103,104,105,106,107,108},  //8
                        {120,119,118,117,116,115,114,113,112,111,110,109},  //9
                        {121,122,123,124,125,126,127,128,129,130,131,132},  //10
                        {144,143,142,141,140,139,138,137,136,135,134,133}   //11
                  };//col:0    1   2   3   4   5   6   7   8   9  10  11   
    
    //show X to let the user know its the calibration-mode
    int letterX[][]=new int [][]{
        {1,0,0,0,0,0,0,0,0,0,0,1},
        {0,1,0,0,0,0,0,0,0,0,1,0},
        {0,0,1,0,0,0,0,0,0,1,0,0},
        {0,0,0,1,0,0,0,0,1,0,0,0},
        {0,0,0,0,1,0,0,1,0,0,0,0},
        {0,0,0,0,0,1,1,0,0,0,0,0},
        {0,0,0,0,0,1,1,0,0,0,0,0},
        {0,0,0,0,1,0,0,1,0,0,0,0},
        {0,0,0,1,0,0,0,0,1,0,0,0},
        {0,0,1,0,0,0,0,0,0,1,0,0},
        {0,1,0,0,0,0,0,0,0,0,1,0},
        {1,0,0,0,0,0,0,0,0,0,0,1},
    };
}
