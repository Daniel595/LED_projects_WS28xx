//Class ValueManager: created by Daniel Koerner
//
//      used for CaseStudy @ HS-Fulda, May, 2018
//      ##  last edit: 23.05.2018
//
//
//
//      The ValueManager is a class to calculate the Threshholdvalues and Compare them with
//          incoming measurements to decide if there is a touch or not
//          it also does the calibration of the matrix (check for smallest and highes values)
// 
//      
//      SUMM:
//      - compare incoming value with calculatet value, decide if there is touch or not
//      - calculate the Threshhold-value from the calibration-values (max/min)
//      - calibration: check incoming values for max and min



package ledtable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValueManager {
    
    public FileManager fm=new FileManager();
    


    //threshholdfactor: (maxVal-minVal) * threshholdFactor 
    //for deciding if there is a touch or not
    double threshholdFactor=0.3;
    
    //maxValues - minValues
    public int[][] delta = new int[12][12];
    //the threshhold-values for every LED
    public int [][] threshhold = new int[12][12];
    //LED-States 
    //[col] [row]
    public boolean [][] bool = new boolean[12][12];
    
    //public int[][] LEDNum = new int[12][12];

    //constructor
    public ValueManager(){
        
        try {
            //get the last saved calibration
            this.threshhold=fm.readThresh();
        
        } catch (FileNotFoundException ex) {
            
            //if there is no saved file use the initial Thresh (some random values)
            this.out();
            Logger.getLogger(ValueManager.class.getName()).log(Level.SEVERE, null, ex);
        
        } catch (IOException ex) {
            Logger.getLogger(ValueManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    //reset max/minValues for coming calibration
    public void reset(){
        for (int i=0; i< 12; i++){
            for (int k=0; k<12; k++){
                //set the min values high
                minValues[k][i]=1023;
                //set the maxValues low
                maxValues[k][i]=0;
            }
        }
    }
    
    //manual calibration, find the min and the maxValues
    public void calibration(int col, int [] val) {
        
        int currentValue; 
        
        for(int i=0;i<val.length;i++){
        
            currentValue=val[i];

//smaller than current minValue ?            
//write the smallest val into the Array
            if(minValues[col][i] > currentValue){
                minValues[col][i]=currentValue;
            }
 
//bigger than current maxValue?            
//write biggest value into Array
            if(maxValues[col][i] < currentValue){
                maxValues[col][i] = currentValue;
            }
        }
    }
    
    //debugging, print the values, calc and save the Threshholdvalues
    public void out() {

        int[][] val = new int[12][12];
        boolean[][] booli = new boolean[12][12];

        for (int x = 0; x < 5; x++) {
            switch (x) {
                case 0:
                    val = minValues;
                    break;
                case 1:
                    val = maxValues;
                    break;
                case 2:
                    val = this.calcThreshholdRetDelta();
                    break;
                case 3:
                    val = this.threshhold;fm.writeThresh(this.threshhold);
                    break;
                case 4:
                    booli = this.bool;
                    break;
            }

//make the print fitting for just copy/paste it to initial minValue
//dont have a closer look to the following lines :D
            String out = "";
            for (int i = 0; i < 12; i++) {
                for (int k = 0; k < 12; k++) {

                    if (k == 0) {
                        out += "{";
                    }

                    if (x < 4) {
                        if (k < 11) {
                            out += (Integer.toString(val[i][k]) + " \t, ");
                        } else {
                            out += (Integer.toString(val[i][k]) + " \t ");
                        }
                    } else {
                        if (k < 11) {
                            out += Boolean.toString(booli[k][i]) + "\t,";
                        } else {

                            out += Boolean.toString(booli[k][i]) + "\t";
                        }
                    }

                    if (k == 11) {
                        out += "},\n";

                    }
                }
            }
            
//summary: print 5 things:
            //1. minValues
            //2. maxValues
            //3. delta (max-min)
            //4. threshholdValue (min + delta*threshholdFactor)
            //5. current boolean Values (on/off, true/false)
            System.out.println(out);
        }

    }
    
    //calculate delta and the Threshholdvalues
    public int [][] calcThreshholdRetDelta (){
        
        int [][] val=new int[12][12];
         
        for (int i=0; i< 12; i++){
            for (int k=0; k<12; k++){
                val [k][i]= maxValues[k][i] - minValues[k][i];
                //calc delta and the threshhold-value
                //save the threshholdvalues
                this.threshhold[k][i] = (int)(this.threshholdFactor * val[k][i]) + minValues[k][i];
            }
        }
        //return delta
        delta = val; 
        return val;
    }
    
    //check if there is a touch in the incomming column
    public void analyze(int col, int [] val) {
        //var for compare with the current threshholdvalue
        int thresh;
        //array for touch (0/1) for the current column
        int [] touch=new int[12];
        
        //check every box of the current column
        for (int i=0;i<12;i++){
            
            //get threshhold for this very box
            thresh = this.threshhold[col][i];
            
            //check if there is a touch or not (higher or lower than the threshholdValue)
            if(val[i]>thresh){
                this.bool[col][i] = true;
                //touch at box with led[col][i] 
                touch[i]=1;
            }else{
                this.bool[col][i] = false;
                //NO touch at box with led[col][i] 
                touch[i]=0;
            }
        }
        
        //call function to write the values to the files
        fm.writeBoolean(col, touch);
        
    }
    
    
        
//initial calibration if there is no saved calibration
//measured values [col] [row]    
//initial measurement from a random light-level on a random day, with some luck it could fit
    public static int[][] minValues = new int[][]{
{264 	, 452 	, 289 	, 269 	, 143 	, 235 	, 36 	, 178 	, 325 	, 248 	, 335 	, 0 	 },
{88 	, 209 	, 129 	, 241 	, 234 	, 258 	, 250 	, 197 	, 232 	, 381 	, 361 	, 0 	 },
{157 	, 46 	, 129 	, 291 	, 220 	, 129 	, 132 	, 63 	, 0 	, 158 	, 514 	, 0 	 },
{134 	, 258 	, 270 	, 268 	, 355 	, 330 	, 272 	, 46 	, 200 	, 250 	, 0 	, 67 	 },
{379 	, 290 	, 41 	, 333 	, 223 	, 349 	, 327 	, 35 	, 342 	, 144 	, 270 	, 158 	 },
{232 	, 243 	, 111 	, 291 	, 111 	, 97 	, 96 	, 326 	, 0 	, 140 	, 156 	, 60 	 },
{183 	, 177 	, 237 	, 249 	, 204 	, 133 	, 696 	, 194 	, 377 	, 0 	, 70 	, 0 	 },
{300 	, 332 	, 256 	, 212 	, 0 	, 199 	, 149 	, 372 	, 130 	, 49 	, 0 	, 158 	 },
{245 	, 251 	, 207 	, 225 	, 210 	, 106 	, 712 	, 405 	, 154 	, 70 	, 379 	, 108 	 },
{43 	, 190 	, 52 	, 140 	, 0 	, 111 	, 76 	, 168 	, 128 	, 0 	, 706 	, 0 	 },
{369 	, 127 	, 239 	, 224 	, 140 	, 54 	, 423 	, 482 	, 95 	, 93 	, 138 	, 71 	 },
{162 	, 205 	, 147 	, 184 	, 127 	, 434 	, 0 	, 20 	, 19 	, 58 	, 126 	, 0 	 }

    };
    
    //
    public static int[][] maxValues = new int[][]{
{723 	, 723 	, 721 	, 722 	, 719 	, 718 	, 521 	, 719 	, 723 	, 719 	, 721 	, 406 	 },
{712 	, 720 	, 715 	, 720 	, 720 	, 719 	, 720 	, 713 	, 720 	, 719 	, 723 	, 682 	 },
{720 	, 501 	, 713 	, 719 	, 797 	, 774 	, 696 	, 713 	, 654 	, 717 	, 725 	, 533 	 },
{713 	, 721 	, 720 	, 724 	, 729 	, 727 	, 723 	, 630 	, 720 	, 717 	, 327 	, 692 	 },
{724 	, 723 	, 690 	, 726 	, 784 	, 784 	, 723 	, 514 	, 725 	, 707 	, 724 	, 716 	 },
{723 	, 720 	, 710 	, 723 	, 726 	, 714 	, 712 	, 724 	, 551 	, 719 	, 721 	, 715 	 },
{718 	, 718 	, 720 	, 722 	, 720 	, 774 	, 728 	, 720 	, 726 	, 412 	, 709 	, 226 	 },
{723 	, 722 	, 721 	, 720 	, 442 	, 742 	, 715 	, 725 	, 718 	, 609 	, 565 	, 715 	 },
{721 	, 721 	, 719 	, 718 	, 721 	, 703 	, 731 	, 727 	, 723 	, 716 	, 722 	, 698 	 },
{580 	, 716 	, 644 	, 716 	, 628 	, 721 	, 708 	, 721 	, 721 	, 564 	, 728 	, 360 	 },
{724 	, 718 	, 720 	, 717 	, 719 	, 714 	, 726 	, 728 	, 712 	, 717 	, 720 	, 718 	 },
{722 	, 721 	, 717 	, 716 	, 719 	, 768 	, 524 	, 583 	, 564 	, 712 	, 710 	, 487 	 }
    };
    
    
}
