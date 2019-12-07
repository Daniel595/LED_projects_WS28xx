#!/usr/bin/env python3
# NeoPixel library strandtest example
# Author: Tony DiCola (tony@tonydicola.com)
#
# Direct port of the Arduino NeoPixel library strandtest example.  Showcases
# various animations on a strip of NeoPixels.

import os
import time
from neopixel import *
import argparse
#import cronus.beat as beat
#import threading
#import timerExample

# LED strip configuration:
LED_COUNT      = 144      # Number of LED pixels.
LED_PIN        = 18      # GPIO pin connected to the pixels (18 uses PWM!).
#LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_FREQ_HZ    = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA        = 10      # DMA channel to use for generating signal (try 10)
LED_BRIGHTNESS = 255     # Set to 0 for darkest and 255 for brightest
LED_INVERT     = False   # True to invert the signal (when using NPN transistor level shift)
LED_CHANNEL    = 0       # set to '1' for GPIOs 13, 19, 41, 45 or



#**********following Code is added by Daniel Koerner**********

#working directories
filepath       = '/home/pi/LEDTable/boolean/sequential/led'
modepath       = '/home/pi/LEDTable/mode.txt'
pidpath        = '/home/pi/LEDTable/pid_py.txt'

#List for saving the last state
arrayList=[i for i in range(144)]


#function to check the state of the current matrix box
def readFile(i):
    file=open(filepath+str(i+1)+'.txt',"r")
    read = file.read(1)
    
    if(read=='1'):
        #print('1')
        arrayList[i]='1'
        return 1        
    elif (read=='0'):
        #print('0')
        arrayList[i]='0'
        return 0
    else:
        #print('-')
        return
     

#EDIT: by Daniel Koerner, original function is 'rainbow()'
#only those boxes which have a '1' at their files will light up
def interactiveRainbow(strip, wait_ms=10, iterations=1):
    """Draw rainbow that fades across all pixels at once."""
    for j in range(256*iterations):
        for i in range(strip.numPixels()):
            val=readFile(i)
            if(val=='1'):
                strip.setPixelColor(i, wheel((i+j) & 255))   
            elif(val=='0'):
                strip.setPixelColor(i, Color(0,0,0))
#if we read the file while its written 
#then we just take the last state of this pixel which is saved in "arrayList"                    
            else:
                if(arrayList[i]=='1'):
                    strip.setPixelColor(i, wheel((i+j) & 255))
                else:
                    strip.setPixelColor(i, Color(0,0,0))
        strip.show()
        time.sleep(wait_ms/1000.0)
        
        
#**********end of added Code from Daniel Koerner**********
        
def wheel(pos):
    """Generate rainbow colors across 0-255 positions."""
    if pos < 85:
        return Color(pos * 3, 255 - pos * 3, 0)
    elif pos < 170:
        pos -= 85
        return Color(255 - pos * 3, 0, pos * 3)
    else:
        pos -= 170
        return Color(0, pos * 3, 255 - pos * 3)        
        

# Main program logic follows:
if __name__ == '__main__':
    # Process arguments
    parser = argparse.ArgumentParser()
    parser.add_argument('-c', '--clear', action='store_true', help='clear the display on exit')
    args = parser.parse_args()

    # Create NeoPixel object with appropriate configuration.
    strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL)
    # Intialize the library (must be called once before other functions).
    strip.begin()

    #print ('Press Ctrl-C to quit.')
    if not args.clear:
        print('Use "-c" argument to clear LEDs on exit')
            

    try:
        
        #get pid and write it to a file 
        pid=os.getpid()
        print(pid)
        f=open(pidpath,'w+')
        f.write("%d" % pid)
        f.close()
        
        while True:

            interactiveRainbow(strip)
                
                
    except KeyboardInterrupt:
        if args.clear:
            colorWipe(strip, Color(0,0,0), 10)
