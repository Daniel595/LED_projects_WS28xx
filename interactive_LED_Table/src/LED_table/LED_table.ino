#include <WS2812B.h>
#define NUM_LEDS 144
#define ROW_NUM 12
#define COL_NUM 12
#define SERIAL_BAUD 230400
#define BUFFER_SIZE 28     // 1*ACK A2 + 12*data A1 + 1*ETX A1 + 12*data A2 + 1*ETX A2
#define CAL_PIN PB12
#define MODE_PIN PB13
#define DETECTION_PIN PB14

// Declare our NeoPixel strip object:
WS2812B strip = WS2812B(NUM_LEDS);

// ############### LED cells ###################
class LED_cell{
  
  const uint8_t LED_ON = 1;
  const uint8_t LED_OFF = 0;
  
  public:
    LED_cell();
    void update_cell(int new_value);      // check the new measurement value and set a valid state
    void calibrate(int new_value);        // check the new measurement value and calibrate cell
    void reset_calibration();             // delete information from last calibration
    void initialize(int col, int row);    // initialize the cell and set the absolut number of it
    uint32_t Wheel(byte WheelPos);        // return a color value (from WS2812B example)
    uint16_t get_num();                   // return the absolut strip-number of the cell
    void set_color(uint32_t color);       // set the current pixel color
    void light();                         // check the current state and turn the LED on/off
    void set_fade(int ticks);
    void fading_tick();
    
  private:
    uint8_t state;                // state for pixel on/off
    uint16_t num;                 // absolute strip number of the pixel
    int max_val;                  // calibration value max
    int min_val;                  // calibration value min
    int threshold;                // calibration value threshhold (min + (max - min) * 0.3 )
    uint32_t color;               // current pixel color
    int fade_ticks;               // 
    float fading;                 //
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    
};

LED_cell::LED_cell(){
  this->threshold = 300;
  this->max_val = 0;
  this->min_val = 1020;
  this->color = strip.Color(0,0,0);
  this->state = LED_OFF;
  this->fade_ticks = 0;
  this->fading = 0.95;
  this->red=0;
  this->green=0;
  this->blue=0;
}

void LED_cell::set_fade(int ticks){
  this->fade_ticks = ticks;
  this->red = (this->color >> 16) & B11111111;
  this->green = (this->color >> 8) & B11111111;
  this->blue = this->color & B11111111;
}

void LED_cell::fading_tick(){
  if(this->fade_ticks > 0){
    strip.setPixelColor(this->num, strip.Color(this->red,this->green,this->blue));
    this->fade_ticks --;
    this->red *= fading;
    this->green*= fading;
    this->blue*= fading;
  }else{
    strip.setPixelColor(this->num, strip.Color(0,0,0));
  }
}

void LED_cell::set_color(uint32_t color){
  this->color = color;
}

uint16_t LED_cell::get_num(){
  return this->num;
}

// layout is "snake like", so every second row has to be numerated backwards
void LED_cell::initialize(int col, int row){
  if(row == 0 || row%2 == 0){
    this->num = col + row * COL_NUM;        // numerate forward - left to right
  }else{
    this->num = (row+1)*COL_NUM - (col+1);  // numerate backward - right to left
  }
}


void LED_cell::light(){
  if(this->state == LED_ON){
    strip.setPixelColor(this->num, this->color);
  }else{
    strip.setPixelColor(this->num, strip.Color(0,0,0));
  }
}

void LED_cell::update_cell(int new_value){
  if(new_value >= this->threshold){
    this->state = LED_ON;
    this->light();
  }
  else{
    this->state = LED_OFF;
    this->light();
  }
}


void LED_cell::calibrate(int new_value){
  if(new_value > this->max_val){
    this->max_val = new_value;
  }
  if(new_value < this->min_val){
    this->min_val = new_value;
  }
  this->threshold = this->min_val + (this->max_val - this->min_val) * 0.3;
}

void LED_cell::reset_calibration(){
  this->max_val = 0;
  this->min_val = 1023;
}



// ############### commands ###################
// Commands
const byte ACK =    B10000000;  //128
const byte START =  B01000000;  //64    
const byte ETX=     B11000000;  //192
// Masks
const byte command_mask = B11100000;
const byte data_mask = B00011111;



// ############### Setup ###################
void setup() {
  Serial.begin(SERIAL_BAUD);      // USB-Port
  Serial1.begin(SERIAL_BAUD);     // UART 1
  pinMode(DETECTION_PIN, OUTPUT);
  

  strip.begin();
  strip.show(); // Initialize all pixels to 'off'
}


// ############### Methods ########################
uint8_t validate(unsigned char read_buffer[BUFFER_SIZE], int data[ROW_NUM]);
uint8_t calibration_switch(uint8_t mode, LED_cell mat[COL_NUM][ROW_NUM]);
bool mode_switch();
void rainbowCycle();
void rainbowCycle_Cells(LED_cell mat[COL_NUM][ROW_NUM]);

// ############### defines #######################
uint8_t CAL = 0;
uint8_t RUN = 1;
uint8_t PASSIVE = 0;
uint8_t INTERACTIVE = 1;


// ############### main ###################
void loop() {
  unsigned char read_buffer[BUFFER_SIZE];    // buffer to read measurement results 
  LED_cell mat[COL_NUM][ROW_NUM];                      // LED_matrix with 144 LED_cells
  uint8_t detection_mode = INTERACTIVE;      // 2 general modes - Interactive lights - or just lights (passive)
  uint8_t mode = RUN;                        // in INTERACTIVE are 2 modes possible - calibration and run
  int data[ROW_NUM];                         // buffer to store measurement results 
  int current_col;                           // collumn where measurement is in process

   digitalWrite(DETECTION_PIN, HIGH);         // turn on the "measurement unit" (2 Arduinos + IR/photo - diods)
   for(int col=0; col<COL_NUM; col++){             // initialize every LED_cell
    for(int row=0; row<ROW_NUM; row++){
      mat[col][row].initialize(col, row);
    }
   }
     
   while(true){   
    detection_mode = mode_switch(detection_mode);   // check passive/active switch  

      // INTERACTIVE MODE
      if(detection_mode == INTERACTIVE){
        mode = calibration_switch(mode, mat);                   // check calibration switch 
        Serial1.readBytes(read_buffer, 1);                      // read one byte until its the "start" byte
        if((read_buffer[0]&command_mask) == START){             // start command?
            current_col = read_buffer[0] & data_mask;               // set current collumn
            rainbowCycle_Cells(mat);                                // set LED-values 
            Serial1.readBytes(read_buffer, BUFFER_SIZE - 1);        // read all bytes from arduino 1 and 2 
            validate(read_buffer, data);                            // extract and validate data from buffer     
            for(int i=0; i<ROW_NUM; i++){                           // update the current collumn (all rows in this col)
             if(mode == RUN){
               mat[current_col][i].update_cell(data[i]);            // case RUN
             }else{
               mat[current_col][i].calibrate(data[i]);              // case CALIBRATION
             }
            }
            if(mode == RUN) strip.show();                           // show LED-strip
            // TODO - write "cal" to the table in case of calibration mode
        }
      }
     // PASSIVE MODE
     else{ 
      mode = RUN;

      // ########### fading random pixel sketch 
      int num_on = random(3);
      for(int i = 0; i<num_on; i++){
        int col = random(COL_NUM);
        int row = random(ROW_NUM);
        mat[col][row].set_color(strip.Color(random(128), random(128), random(128)));
        mat[col][row].set_fade(random(5000));
        //mat[col][row].set_fade(5000);
      }

      for(int col=0; col<COL_NUM; col++){
        for(int row=0; row<ROW_NUM; row++){
          mat[col][row].fading_tick();
        }
      }
      delay(50); 

      // ############## rainbow sketch
      /*
      rainbowCycle();   // run a usual rainbow-sketch
      delay(10);
      */


      
      strip.show();          
                    
     }
  }
}



// ############### Methods ###################
// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) 
{
  if(WheelPos < 85) 
  {
    return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  } 
  else 
  {
    if(WheelPos < 170) 
    {
     WheelPos -= 85;
     return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
    } 
    else 
    {
     WheelPos -= 170;
     return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
    }
  }
}


void rainbowCycle() 
{
  static uint16_t j=0;
  uint16_t i;
  for(i=0; i< strip.numPixels(); i++) 
  {
    strip.setPixelColor(i, Wheel(((i * 256 / strip.numPixels()) + j) & 255));
  }
  j++;
  if(j==256){
    j=0;
  }
}


void rainbowCycle_Cells(LED_cell mat[COL_NUM][ROW_NUM]) 
{
  static uint16_t j=0;
  uint16_t i;
  for(int col=0; col<COL_NUM; col++){
    for(int row=0; row<ROW_NUM; row++){
      i = mat[col][row].get_num();
      mat[col][row].set_color( Wheel(((i * 256 / strip.numPixels()) + j) & 255));
      mat[col][row].light();
    }
  }
  j++;
  if(j==256){
    j=0;
  }
}


uint8_t calibration_switch(uint8_t mode, LED_cell mat[COL_NUM][ROW_NUM]){
  static int last_state = LOW;
  int current_state = digitalRead(CAL_PIN);
  if(current_state==HIGH && last_state==LOW ){
    last_state = current_state;
    if(mode == RUN){
      for(int col = 0; col < COL_NUM; col++){
        for(int row=0; row < ROW_NUM; row++){
          mat[col][row].reset_calibration();
        }
      }
      return CAL;
    }else{
      return RUN;
    }
  }else{
    last_state = current_state;
    return mode;
  }
}


uint8_t mode_switch(uint8_t detection_mode){
  static int last_state = LOW;
  int current_state = digitalRead(MODE_PIN);
  if(current_state==HIGH && last_state==LOW ){
    last_state = current_state;
    if(detection_mode == INTERACTIVE){
      digitalWrite(DETECTION_PIN, LOW);
      return PASSIVE;
    }else{
      digitalWrite(DETECTION_PIN, HIGH);  // turn on touch detection
      // mode
      return INTERACTIVE;
    }
  }else{
    last_state = current_state;
    return detection_mode;
  }
}

// protocoll
  //    A1:    [start & col] 
  //    A2:    [ACK]
  //    A1:    [MSB_1][LSB_1],[MSB_2][LSB_2], [MSB_3][LSB_3], [MSB_4][LSB_4], [MSB_5][LSB_5], [MSB_6][LSB_6]
  //           [ETX]
  //    A2:    [MSB_7][LSB_7],[MSB_8][LSB_8], [MSB_9][LSB_9], [MSB_10][LSB_10], [MSB_11][LSB_11], [MSB_12][LSB_12]
  //           [ETX]
uint8_t validate(unsigned char read_buffer[BUFFER_SIZE], int data[ROW_NUM]){
  // check for correct ETX and ACK
  if( ((read_buffer[0] & command_mask) == ACK)      // ACK correct?
      && ((read_buffer[13] & command_mask) == ETX)  // ETX from A1 correct?
      && ((read_buffer[26] & command_mask) == ETX)) // ETX from A2 correct?
  {
    uint8_t cnt;
    for(uint8_t i=0; i<=ROW_NUM; i++){
       if(i < 6){cnt = 2*i + 1; }
       else {cnt = 2*i + 2; }
       data[i] = (int)(read_buffer[cnt] << 5) | read_buffer[cnt+1];
    }
    return 1;
  }
  return 0;
}
