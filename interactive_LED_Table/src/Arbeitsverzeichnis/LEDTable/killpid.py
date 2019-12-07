import os
import signal
import RPi.GPIO as GPIO

javaPID          = '/home/pi/LEDTable/pid_j.txt'
pythonPID        = '/home/pi/LEDTable/pid_py.txt'

#kill java programm
try:
    file=open(javaPID,"r")
    pid = file.readline()
    os.kill(int(pid),signal.SIGKILL)
    print ("java killed")
except (OSError):
    print ("no running java programm")

#kill python programm
try:
    file=open(pythonPID,"r")
    pid2 = file.readline()
    os.kill(int(pid2),signal.SIGKILL)
    print ("python killed")
except OSError:
    print("no running python programm")
