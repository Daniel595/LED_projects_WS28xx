Simplification:

This table has not such cool "light pattern" as Deconink's because Glediator does not work here.
In return it starts up very quickly and you never have to access a GUI. 

I use 3 "Arduinos". Once i did it with 2 Arduinos and 1 Raspberry Pi like http://blog.deconinck.info/post/2016/12/19/A-Dirt-Cheap-F-Awesome-Led-Table. 
I replaced the RPI with a arduino to avoid the long booting-time. A nano was to weak (not enough RAM), instead I use a STM32 STM32F103C8T6 Development Board (20k RAM) for ~3€. You need a FTDI adapter to flash it or to write a bootloader but it works well with Arduino IDE.


Similarities:


The table is very similar to Deconink's. 
I used all his circuits like for "sniffing" the UART-communication from the Nanos and of course for the IR-LED matrix.
(Soldering is a hard piece of work)


Differences:


I can't run Glediator from the STM32 but it starts up in about 1 second (instead of ~1min.).
I use 2 switches - one for interactive/passive mode and one to start/end calibration. 
In passive mode I shut down the touch detection. Calibration works the same as Deckonink's. 
The Power supply of the touch detection Nanos happens from the STM32 (with MOSFET Transistor because STM32 Pin makes 3V3 but i need 5V for the Nanos). 
For passive mode I shut them down to save sth. (maybe lifetime of Nanos and IR-LEDs).
