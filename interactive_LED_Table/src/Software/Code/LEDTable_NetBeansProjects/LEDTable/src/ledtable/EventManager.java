//Class EventManager: created by Daniel Koerner
//
//      used for CaseStudy @ HS-Fulda, May, 2018
//      ##  last edit: 23.05.2018
//
//
//
//      The EventManager is a class to recieve all incoming Events and interactions 
//          with the Raspberry like incoming serial Data or pressed Buttons
//          -- not only recieve 
//
//          it also does "re-build" the incomming serial data (2 char to 1 integer)
//          it also keeps the programm running 
//      
//      SUMM:
//      Events:
//       - calibration-Button - start calibration
//       - passive/interactiv Button - switch between 2 modes
//       - Off-Button - shutdown the Raspberry correctly
   //    - serial data: check what comes in: data or command
//      -> get Informatin from Command (startByte), re-build dataByte
//
//      Others:
//       - get the current PID (ProcessID of programm to "kill" it safely at the "Glediator's" start)
//       - hand over the values to the valueManager
//       - "start" the program and keep the programm running



package ledtable;

//  library-dir:    /opt/pi4j/lib/pi4j-core.jar
import com.pi4j.io.serial.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EventManager {
    
    //***expecting command-Bytes (serial data, Arduino)
    final static int start=64;
    final static int ACK=128;
    final static int ETX=192;
    
    
    static FileManager fm;
    static ValueManager vm; 

    
    //***mode variables
    //interactive at the beginning
    static boolean interactiveMode=true;
    static boolean passiveMode=false;
    static boolean calibrationMode = false;
    
    //***serial data variables
    //recieved Bytes in ascending sequence for current col
    static byte [] data=new byte[24];
    //calculated int-values for current col
    static int [] val=new int[12];
    //counter for saving recieved Byte to the right place
    static int lastByte=0;
    //variable for the very start (are we synchron with measuring arduinos? did we get a col yet?)
    static boolean synchron=false;
    //counter for the right col
    static int col=12;   
    //array for incomming Bytes 
    static byte [] incBytes;
    //NOTE: incomming Bytes can start somewhere in the middle of the col
    //they dont have to start with startByte
    //AND they have diffrent length's! Most of time 28 Bytes per "push" (24 data, 1 start, 1 ACK, 2 ETX) but sometimes more like 32 oder even more
    //so it happens that the byte where you start could also "be shiftet" and suddenly you start with another Byte
        
    //no function anymore
    static int [] outInt=new int[12];
    static byte [] outBytes=new byte[12];
    static boolean ACKbyte=false;
    

    //start programm and keep it running
    public static void main(String args[]) throws InterruptedException, IOException {
        
    
    fm = new FileManager();
    //create Files to write the boolean touch-data in
    fm.createFiles();
    
    vm = new ValueManager();
        

         
//*****initialisation for buttons and serial data and how to hanlde those events 
        initEvents();

//*****COUNTINOUS LOOP TO KEEP THE PROGRAMM RUNNING            
            while(true) {
                Thread.sleep(500);
                //well, sometimes if you switch from interactive to passive it happens that you finish your interrupted calcValue()
                //then you have maybe one col which is 0 (off)
                //so just set all LED's 1 (on) every 1 second
                if(passiveMode){
                    fm.passiveMode();
                }
                
                if(calibrationMode){
                    fm.showX();
                }
                //cal.out();//debugging
            }

    }
    
    //initialize current col 
    public static void startByte(byte b){
       
       //its just for the very first start of the programm 
       //for not using the first dtaByte without having an col
       synchron = true;
       //get the current col
       col = 0b00111111 & b ;
       //System.out.println("new Col = " + col);//debugging
       if(col==11){
         //  System.out.println("check");//debugging
       }
       //variable for sort the bytes to the right arrayposition
       //next byte is gonna be databyte 0
       lastByte=0;
       }
    
    //no important function anymore
    public static void ACKByte(){
        ACKbyte=true;
    }
    
    //not important function anymore
    public static void ETXByte() throws IllegalStateException, IOException{
       
/*        
//pass the first ETXByte
        if(ACKbyte==true){
            ACKbyte=false;
            
        //send values for this row with second ETXByte    
        }else{
            if(calibrationMode){
                return;
            }

            //System.out.println("Serial write");
            int STX=start+col;       
            
            //start + col
            serial.write((byte)STX);
            //values: 12 boxes -> 12 bytes
           // System.out.println(STX);
            for(int i=0;i<12;i++){
              //  System.out.println(outBytes[i]);
            serial.write(outBytes[i]);
            }
            //System.out.println(ETX);
            //ETX
            serial.write((byte)ETX);
        }*/
    }
    
    //sort the byte into the array
    public static void dataByte(byte b){
        if (synchron){
            
            //save the 24 Bytes
            data[lastByte]=b;
            lastByte ++;
            
            //if all 24 bytes are recieved
            if(lastByte==24){
                lastByte=0;
                //"re-build" the Bytes to integer-values
                calcValue();
            }
        }
    }
    
    //"re-build" the bytes to integer-values
    public static void calcValue() {

//
        // data: (1) [MSB_1][LSB_1][MSB_2][LSB_2][MSB_3][LSB_3][MSB_4][LSB_4][MSB_5]
        //           [LSB_5][MSB_6][LSB_6][MSB_7][LSB_7][MSB_8][LSB_8][MSB_9][LSB_9]
        //           [MSB_10][LSB_10][MSB_11][LSB_11][MSB_12][LSB_12] (24)
        // 10 significant Bits: first byte: (MSB)[000 abcde], 
        //                     second Byte: (LSB)[000 fghij]
        //
        
        //go through all the 24 bytes
        for (int i = 0; i < val.length; i++) {
           
            //alwas 2 bytes belog together: i*2 and i*2+1 (like [0,1][2,3][4,5]...
            //rebuild the int values (10bit) which are ripped apart values
            
            //(Byte 1)      [000 abcde] << 5 = [000000 ab] [cde 00000]
            //(Byte 2)                          (OR)       [000 fghij]
            //(int, 2 Byte, 10 Bit)     =      [000000 ab] [cde fghij] 
            
            
            val[i] = ((data[i * 2] << 5) | (data[i * 2 + 1]));
        }

        //calibration mode activated by the Button
        if (calibrationMode) {
            //System.out.println("TRUE");//debugging
            
            //check the recieved values for lowest and highest
            vm.calibration(col, val);
            
        } else {
            
            //check if there is a touch
            vm.analyze(col, val);
            
            /*for(int i=0;i<outInt.length;i++){//debugging
                outBytes[i]=(byte) outInt[i];
            }*/
        }
    }
    
    //initialisation for buttons and serial data
    public static void initEvents(){
        
        getPID();
//******ACTIVATE GPIO INTERRUPTS FOR MANUAL CALIBRATION AND SWITCHING BETWEEN INTERACTIVE AND PASSIVE MODE               
        
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        
        //***Button for calibrationMode
        //gpio pin #02 (GPIO27) as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput calibrationButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
        // set shutdown state for this input pin
        calibrationButton.setShutdownOptions(true);
        //set Debounce to 3 seconds
        calibrationButton.setDebounce(3000);
        
        //***Button for schwitching the mode
        //gpio pin #03 (GPIO22) as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput nextStateButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_DOWN);
        nextStateButton.setShutdownOptions(true);
        nextStateButton.setDebounce(3000);
        
        //***Button for shut down the raspi
        //gpio pin #03 (GPIO22) as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput OffButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06, PinPullResistance.PULL_DOWN);
        nextStateButton.setShutdownOptions(true);
        nextStateButton.setDebounce(3000);

        
//****HOW TO HANDLE AN INTERRUPT FROM CALIBRATION-BUTTON
        // create and register gpio pin listener
        calibrationButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                            
                if(event.getState().isHigh()){
                    
                    //ignore calibrationButton if we are in passiveMode
                    if(passiveMode){
                        calibrationMode=false;
                        return;
                    }
                    
                    //if we are already in the calibrationMode mode 
                    if(calibrationMode == true){
                        //leave the calibrationMode mode and go back to the interactive mode
                        calibrationMode=false;
                        //if calibrationMode == false, we call analyze() for calculating if there is a touch
                        
                        //print the values (for debugging)
                        //cal.out() also calculates the Threshhold-values
                        vm.out();
                    
                    //if we are not in the calibrationMode mode
                    }else{
                        
                        //reset the current values (min, max, threshhold)
                        vm.reset();
                        //calibration mode just for looking after highest and lowest values 
                        //****HOW TO CALIBRATE
                        calibrationMode=true;
                        //if calibrationMode==true, we call calibration() for initialize the lowest and highest values
                        
                    }
                }
            }
        });
        
//****HOW TO HANDLE AN INTERRUPT FROM NEXT-STATE-BUTTON        
        nextStateButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
             
                if(event.getState().isHigh()){
                    
                    if(interactiveMode){
                        //set the passive Mode (state)
                        //it helps to dicard incomming serial data and save ressources
                        interactiveMode=false;
                        passiveMode=true;
                        //set all matrix-boxes 1 (all boxes on)
                        //write 'p' to the mode.txt file (to let the python-script know that its passive now)
                        fm.passiveMode();
                                               
                    }else{
                        //set the interactive mode
                        //enable serial read
                        interactiveMode=true;
                        passiveMode=false;
                       //write 'i' to the mode.txt file 
                        fm.interactiveMode();
                    }
                }
                
            }
        });

//****HOW TO HANDLE AN INTERRUPT FROM NEXT-STATE-BUTTON        
        OffButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
             
                if(event.getState().isHigh()){
                     
                  
                    try {
                        
                        Process p = Runtime.getRuntime().exec("sudo shutdown -h now");
                    
                    } catch (IOException ex) {
                        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                       
      
                }
                
            }
        }); 
        
        

//******HOW TO HANDLE INCOMMING SERIAL DATA        
        //create serial Instance
        final Serial serial = SerialFactory.createInstance();
        //**CREATE A SERIAL LISTENER (push if there are serial datas incomming)        
        serial.addListener(new SerialDataEventListener() {
            //***HANDLE INCOMMING DATA
            @Override
            public void dataReceived(SerialDataEvent event) {
                
                try {
                    //just discard the data if its passiveMode (dave resources)
                    if(passiveMode){
                        event.discardData();
                        return;
                    }
                    
                    //if its interactiveMode get bytes
                    incBytes=event.getBytes();
                    
                    //check the gotten bytes
                    for(int i=0;i<incBytes.length;i++){
                        
                        //first, have a look at our Command-Bits
                        int currentByte = incBytes[i] & 0b11000000;
                        
                        //check if it is a command
                        switch(currentByte){
                            
                            //command start, call startByte(..)
                            case start : startByte(incBytes[i]); break;
                            //command ACK, call ACKByte(..)
                            case ACK : ACKByte();break;
                            //command ETX, call ETXByte(..)
                            case ETX : ETXByte();break;
                            
                            //if its not a command, its a dataByte, call dataByte(..)
                            default: dataByte(incBytes[i]);break;
                        }
                    }
                    
                } catch (IOException e) {
                    System.out.println("IOExeption: "+e.toString());
                }
            }
            
        });

        //****CONFIGURATION OF THE SERIAL PORT
        try {
            SerialConfig config = new SerialConfig();
            config.device("/dev/ttyAMA0")
                  .baud(Baud._230400)
                  .dataBits(DataBits._8)
                  .parity(Parity.NONE)
                  .stopBits(StopBits._1)
                  .flowControl(FlowControl.NONE);
            // parse optional command argument options to override the default serial settings.
           // if(args.length > 0){
            //   config = CommandArgumentParser.getSerialConfig(config, args);
           // }
            // open the default serial device/port with the configuration settings
            serial.open(config);          
            
        }
        catch(IOException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            return;
        }
        
        
        
    }
    
    //write the process-id of this programm to a file at working-dir
    public static void getPID(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] split = name.split("@");
        //System.out.println(split[0]);
        int pid=Integer.parseInt(split[0]);
        fm.writePid(pid);
    }
}