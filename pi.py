from time import sleep
import RPi.GPIO as GPIO

pin = 36
GPIO.setmode(GPIO.BOARD)
GPIO.setup(pin, GPIO.OUT)

pwm = GPIO.PWM(pin, 50)
pwm.start(0) 

MIN = 2#2.5
MAX = 12#10.0
while True:
	try:
		pwm.ChangeDutyCycle(MIN)
		print("First")
		sleep(1)
		pwm.ChangeDutyCycle((MAX + MIN)*0.5)
		print("Second")
		sleep(1)
		pwm.ChangeDutyCycle(MAX)
		print("Third")
		sleep(1)
	except KeyboardInterrupt:
		break
GPIO.cleanup()
