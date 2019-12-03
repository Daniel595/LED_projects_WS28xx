
#include <Adafruit_NeoPixel.h>
#ifdef __AVR__
#include <avr/power.h>
#endif

//Number of Pixels used in this lamp
#define NUM_PIXELS 52

//Used Pins in this application
#define LED_PIN 6           //output for controling the LEDs of the lamp
#define DIP_SWITCH_PIN 10   //digital input for reading the dip-switch-state
#define POTI_PIN A0         //analog input for reading the poti (brightness-control)

//available States, depending on dip-switch
#define STATE_LED_WHITE 0   //switch == 0 -> leds white
#define STATE_LED_RAINBOW 1 //switch == 1 -> leds rainbow

//define the speed of the rainbow-"flow"
#define RAINBOW_SPEED 15    //the bigger the value, the slower the flow so -> 1 is the fastest

Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUM_PIXELS, LED_PIN, NEO_GRB + NEO_KHZ800);   

// IMPORTANT: To reduce NeoPixel burnout risk, add 1000 uF capacitor across
// pixel power leads, add 300 - 500 Ohm resistor on first pixel's data input
// and minimize distance between Arduino and first pixel.  Avoid connecting
// on a live circuit...if you must, connect GND first.

typedef struct {
  int red;
  int green;
  int blue;
}color_of_lamp;


void setup() {
  // This is for Trinket 5V 16MHz, you can remove these three lines if you are not using a Trinket
#if defined (__AVR_ATtiny85__)
  if (F_CPU == 16000000) clock_prescale_set(clock_div_1);
#endif
  // End of trinket special code

  strip.begin();
  strip.setBrightness(255);
  strip.show(); // Initialize all pixels to 'off'

  pinMode(DIP_SWITCH_PIN, INPUT);

  Serial.begin(9600);
}



void loop() {

  static int poti_value = 0;        //analog value from poti
  static int dip_switch_state = 0;

  //set Brightness to maximum
  strip.setBrightness(255);
  // read the analog signal from the poti:
  poti_value = analogRead(POTI_PIN);
  //read the digital signal from the dip-switch:
  dip_switch_state = digitalRead(DIP_SWITCH_PIN);
  
  switch(dip_switch_state){
    case STATE_LED_WHITE: led_white(poti_value); break;
    case STATE_LED_RAINBOW: rainbowCycle(RAINBOW_SPEED); break;
    default: rainbowCycle(RAINBOW_SPEED); break;
  }
 }


//dimmable white LED's
static void led_white(int poti_value){
  static color_of_lamp current_color;
  
  // map the poti value to the single rgb-values
  current_color.red = map(poti_value, 0, 1024, 19, 190);
  current_color.green = map(poti_value, 0, 1024, 18, 185);
  current_color.blue = map(poti_value, 0, 1024, 3, 30);
  
  //a little filter aigainst "jitter"
  current_color = get_color_to_set(current_color);

  //set the color
  strip.fill(strip.Color(current_color.red, current_color.green,current_color.blue), 0, NUM_PIXELS);
  strip.show();
  delay(10);
}


//small filter to eliminate "jitter" of the light
color_of_lamp get_color_to_set(color_of_lamp new_color){
  static color_of_lamp current_color = {0};

  if( (abs(current_color.red - new_color.red)) > 3){
    current_color.red = new_color.red;
    current_color.green = new_color.green;
    current_color.blue = new_color.blue;
  }

  return current_color;
}



// Slightly different, this makes the rainbow equally distributed throughout
void rainbowCycle(uint8_t wait) {
  uint16_t i, j;


  for (j = 0; j < 256 * 5; j++) { // 5 cycles of all colors on wheel
      int poti_value = analogRead(POTI_PIN);
      int brightness = map(poti_value, 0, 1023, 20, 255);
      strip.setBrightness(brightness);
    for (i = 0; i < strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel(((i * 256 / strip.numPixels()) + j) ));
    }
    
    int dip_switch_state = digitalRead(DIP_SWITCH_PIN);
    if(dip_switch_state == STATE_LED_WHITE)
    {
      break;
    }
    
    strip.show();
    delay(wait);
  }
}



// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  WheelPos = 255 - WheelPos;
  if (WheelPos < 85) {
    return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  }
  if (WheelPos < 170) {
    WheelPos -= 85;
    return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
  WheelPos -= 170;
  return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
}
