#define ROW_NUM 12
#define COL_NUM 12
#define COL_NUM_PER_NANO 6
#define SERIAL_BAUD 230400
#define serial_timeout_ms 100
#define measure_delay_us 2000


//Commands
byte ACK =    B10000000;  //128
byte start =  B01000000;  //64    
byte ETX=     B11000000;  //192


//variables
int level;
int value;
byte read_buffer[13];
byte data[12];
byte test_data[]={13,14,15,16,17,18,19,20,21,22,23,24};
//Incomming Byte
byte inc;





char current_row=0;
//digitalpins for row and analogpins for measure
char row_pins[]={2,3,4,5,6,7};

char current_col;
//digitalpins for col
char col_pins[]={8,9,10,11,12,13};


//Wiring !!
//A1: row 0=D2, row 1=D3, 2=D4, 3=D5, 4=D6, 5=D7
//    col 0=D8, col 1=D9, 2=D10, 3=D11, 4=D12, 5=D13
//A2: row 6=D2, 7=D3, 8=D4, 9=D5, 10=D6, 11=D7
//    col 6=D8, col 7=D9, 8=D10, 9=D11, 10=D12, 11=D13

//analog-pins: row 0=A2, 1=A3, 2=A4, 3=A5, 4=A6, 5=A7
//A0, A1 used as digital IO (debug LED,check LED)

void setup() {
  analogReference(DEFAULT);
  
  Serial.begin(SERIAL_BAUD);
  Serial.setTimeout(serial_timeout_ms);
  //use as digital pin
 // pinMode(A0, OUTPUT);
  //pinMode(A1, OUTPUT);

  //initialize Portdirections
  for(char i=0; i<6 ; i++){
    //row_pins as OUTPUT (HIGH: 5V, LOW: GND)
    pinMode(row_pins[i],OUTPUT);
    digitalWrite(row_pins[i],LOW);
    
    //col Pins as INPUT (HIGH: ??pullup, Low: HiZ)
    pinMode(col_pins[i],INPUT);
    digitalWrite(col_pins[i],LOW);
  }
}

void loop() {
  //A2
  //incoming Serial data
  if(Serial.available()>0){
    inc=Serial.read();

    //handle start-byte
    if((inc & 0B11100000) == start){
    //get collumn out of the start byte
    current_col = inc & 0B00011111;

    //col to GND
    //(wiring) A1: col 0-5,   A2: col 6-11
    if(current_col > 5){
      pinMode(col_pins[current_col-6], OUTPUT);
      digitalWrite(col_pins[current_col-6],LOW);
    }

    //send ACK
    Serial.write(ACK);
    //check_LED();

      //Measure
      for(char i=0; i<6; i++){
        current_row=i;
        measure();
      }
      
      //read masters data (12 Byte) + ETX = 13 Byte
      if(Serial.readBytesUntil(ETX,read_buffer, 13 )==0){
       // debug_LED();
      }

      //send 6 (int) values in 12 byte
      for(char i=0; i < 12 ;i++){
      Serial.write(data[i]);
      }
      //send ETX
      Serial.write(ETX);
      Serial.flush();

      //set col back to HiZ
      if(current_col > 5){   
      pinMode(col_pins[current_col-6], INPUT);
      digitalWrite(col_pins[current_col-6],LOW);
      }
      
      delayMicroseconds(35); 
    }
  }
}


  

  //Data protokoll
  //send Data: 
  //    A1:    [start & row] 
  //           [MSB_1][LSB_1],[MSB_2][LSB_2], [MSB_3][LSB_3], [MSB_4][LSB_4], [MSB_5][LSB_5], [MSB_6][LSB_6]
  //           [ETX]
  
  //    A2:    [MSB_7][LSB_7],[MSB_8][LSB_8], [MSB_9][LSB_9], [MSB_10][LSB_10], [MSB_11][LSB_11], [MSB_12][LSB_12]
  //           [ETX]



void measure(){
  //current_col should be GND
  //1. measure of level(ambient light)
  level = reflection();

  //2. measure + IR-reflection
  digitalWrite(row_pins[current_row],HIGH);
  value = reflection();
  digitalWrite(row_pins[current_row],LOW);

  //delta of measurement, 2 Byte, 10 Bit -> [xxxxxxab] [cdefghij]
  int delta = level-value;
  if(delta<0){
    delta=0;
  }
  //prepare for sending
  //MSB of value: 0B [000 abcde]
  data[current_row * 2] = ((byte) (delta >> 5) & 0B00011111);
  //LSB of value: 0B [000 fghij]
  data[current_row * 2 + 1] = ((byte)delta & 0B00011111);
  
}

int reflection (){
  int reflection;
  delayMicroseconds(measure_delay_us);
  reflection=analogRead(row_pins[current_row]);
  return reflection;
}

void check_LED(){
  digitalWrite(A0,HIGH);
  delayMicroseconds(1);
  digitalWrite(A0,LOW);
}

void debug_LED(){
  digitalWrite(A1,HIGH);
  delayMicroseconds(1);
  digitalWrite(A1,LOW);
}
