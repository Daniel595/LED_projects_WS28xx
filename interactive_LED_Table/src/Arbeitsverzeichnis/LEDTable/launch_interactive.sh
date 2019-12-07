#!/bin/sh
sudo python LEDTable/killpid.py &
sudo pkill -f Glediator &
java -jar LEDTable/LEDTable.jar &
sudo python LEDTable/strandtest_interactive.py


