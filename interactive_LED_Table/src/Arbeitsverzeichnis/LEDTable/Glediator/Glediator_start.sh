#!/bin/sh
sudo python LEDTable/killpid.py
CLASSPATH=/usr/share/java/RxTxcomm.jar
LD_LIBRARY_PATH=/usr/lib/jni
cd LEDTable/Glediator/dist
java -Djava.library.path=/usr/lib/jni -Dgnu.io.rxtx.SerialPorts=/dev/ttyAMA0 -jar Glediator_V2.jar