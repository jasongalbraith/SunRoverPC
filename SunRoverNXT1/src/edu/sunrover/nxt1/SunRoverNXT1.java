package edu.sunrover.nxt1;

import java.io.*;
import lejos.nxt.*;
import lejos.nxt.addon.tetrix.*;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;

/*
 * This NXT controls the Tetrix motors. (S1)
 * It also has three ultrasonic sensors. (S2, S3, and S4)
 * 
 */
public class SunRoverNXT1 implements Runnable {

	// These constants define what state the SunRover is in.
	final int DRIVE = 0;
	final int WEBCAM = 1;
	final int ARM = 2;
	final int GRAB = 3;
	final int FINE = 4;
	// This is how much the NXT should change servo values.
	final float CHANGE = 0.5f;
	final float FINE_POWER = 0.1f;
	// Constant values for servos
	final float SERVO_MAX = 178.0f;
	final float SERVO_MIN = 48.0f;
	// These are the starting positions of the servos.
	float servo1pos = 128f;
	float servo2pos = 128f;
	float servo3pos = 128f;
	float servo4pos = 128f;
	boolean proceed = true;
	// Set up global variables used in this program.
	NXTConnection connection;
	DataInputStream dis;
	DataOutputStream dos;
	TetrixMotorController mc;
	TetrixServoController ms;
	TetrixMotor m1;
	TetrixMotor m2;
	TetrixServo s1;
	TetrixServo s2;
	TetrixServo s3;
	TetrixServo s4;

	// Set up the NXT
	public SunRoverNXT1() {
		// Set up the Tetrix controllers
		mc = new TetrixMotorController(SensorPort.S1,
				TetrixControllerFactory.DAISY_CHAIN_POSITION_1);
		ms = new TetrixServoController(SensorPort.S1,
				TetrixControllerFactory.DAISY_CHAIN_POSITION_2);
		// Set up the motors and servos.
		m1 = mc.getBasicMotor(TetrixMotorController.MOTOR_1);
		m2 = mc.getBasicMotor(TetrixMotorController.MOTOR_2);
		s1 = ms.getServo(TetrixServoController.SERVO_1);
		s2 = ms.getServo(TetrixServoController.SERVO_2);
		s3 = ms.getServo(TetrixServoController.SERVO_3);
		s4 = ms.getServo(TetrixServoController.SERVO_4);
		s1.setAngle(servo1pos);
		s2.setAngle(servo2pos);
		s3.setAngle(servo3pos);
		s4.setAngle(servo4pos);
		System.out.println("Connecting...");
		// Wait for a USB Connection.
		connection = USB.waitForConnection();
		System.out.println("Connected!");
		// Create a data input stream to read from.
		dis = connection.openDataInputStream();
		dos = connection.openDataOutputStream();
		// Start the first thread
		Thread t = new Thread(this);
		t.start();
	}

	// Start the SunRover
	public static void main(String[] args) {
		new SunRoverNXT1();
	}

	// This is where all the work happens. We set up all the Tetrix Motors,
	// Servos, and Controllers.
	@Override
	public void run() {
		// All sorts of things could break. Gotta catch 'em all!
		try {
			// These are the local variables we will use in our input.
			int input = 0;
			int btn1 = 0;
			int btn2 = 0;
			int btn3 = 0;
			int btn4 = 0;
			int btn5 = 0;
			int btn8 = 0;
			int power1 = 0;
			int power2 = 0;
			int state = 0;
			System.out.println("Starting...");
			while (proceed == true) {
				// If the escape button is down, kill it.
				if (Button.ESCAPE.isDown()) {
					proceed = false;
				}
				try {
					// Read in one input.
					input = dis.readInt();
					// Button one is the first bit.
					btn1 = input / 1000000 % 2;
					// Button two is the next bit.
					btn2 = input / 1000000 % 4 / 2;
					// Button three is the next bit.
					btn3 = input / 1000000 % 8 / 4;
					// Button four is the next bit.
					btn4 = input / 1000000 % 16 / 8;
					// Button five is the next bit.
					btn5 = input / 1000000 % 32 / 16;
					// Button eight is the next bit.
					btn8 = input / 1000000 % 64 / 32;
					// If button 8 is down, kill it.
					if (btn8 == 1) {
						proceed = false;
					} 
					else if (btn1 == 1 && state != DRIVE) {
						state = DRIVE;
					}
					else if (btn2 == 1 && state != WEBCAM) {
						state = WEBCAM;
					}
					else if (btn3 == 1 && state != ARM) {
						state = ARM;
					}
					else if (btn4 == 1 && state != GRAB) {
						state = GRAB;
					}
					else if (btn5 == 1 && state != FINE) {
						state = FINE;
					}
					else if (state == DRIVE) {
						// If the button was not pressed, gather more info.
						// Power1 is the thousands places, -255
						// Power2 is the millions places, -255
						// We need to encode all the numbers in one input,
						// so we add 255 when it is sent from the PC.
						// (Negative numbers might effect our buttons...)
						// When we receive it on the NXT, we have to
						// subtract again.
						power1 = input / 1000 % 1000 - 255;
						power2 = input % 1000 - 255;
						// If the value is less than ten, ignore it.
						// Often the joystick will not re-center exactly,
						// and we
						// don't want to give a very small amount of power
						// to the motors.
						if ((Math.abs(power1) < 10)
								&& (Math.abs(power2) < 10)) {
							m1.stop();
							m2.stop();
						} else if (power1 >= 10) {
							// If the left joystick is forwards, move
							// forwards.
							m1.forward();
							m2.backward();
							m1.setPower(Math.abs(power1));
							m2.setPower(Math.abs(power1));
						} else if (power1 <= -10) {
							// If the left joystick is backwards, move
							// backwards.
							m1.backward();
							m2.forward();
							m1.setPower(Math.abs(power1));
							m2.setPower(Math.abs(power1));
						} else if (power2 > 0) {
							// If the right joystick is left, turn in place
							// left.
							m1.backward();
							m2.backward();
							m1.setPower(Math.abs(power2));
							m2.setPower(Math.abs(power2));
						} else {
							// If the right joystick is right, turn in place
							// right.
							m1.forward();
							m2.forward();
							m1.setPower(Math.abs(power2));
							m2.setPower(Math.abs(power2));
						}
					} else if (state == WEBCAM) {
						power1 = input / 1000 % 1000 - 255;
						power2 = input % 1000 - 255;
						if (Math.abs(power2) < 10) {
							// Do nothing with Servo 1
						} else if (power2 > 0) {
							servo3pos = servo3pos - CHANGE;
							if (servo3pos < SERVO_MIN)
								servo3pos = SERVO_MIN;
							s3.setAngle(servo3pos);
						} else {
							servo3pos = servo3pos + CHANGE;
							if (servo3pos > SERVO_MAX)
								servo3pos = SERVO_MAX;
							s3.setAngle(servo3pos);
						}
						if (Math.abs(power1) < 10) {
							// Do nothing with Servo 2
						} else if (power1 < 0) {
							servo4pos = servo4pos - CHANGE;
							if (servo4pos < SERVO_MIN)
								servo4pos = SERVO_MIN;
							s4.setAngle(servo4pos);
						} else {
							servo4pos = servo4pos + CHANGE;
							if (servo4pos > SERVO_MAX)
								servo4pos = SERVO_MAX;
							s4.setAngle(servo4pos);
						}
					}
					else if (state == ARM) {
						power1 = input / 1000 % 1000 - 255;
						power2 = input % 1000 - 255;
						if (Math.abs(power2) < 10) {
							// Do nothing with Servo 1
						} else if (power2 > 0) {
							servo1pos = servo1pos + CHANGE;
							if (servo1pos > SERVO_MAX)
								servo1pos = SERVO_MAX;
							s1.setAngle(servo1pos);
						} else {
							servo1pos = servo1pos - CHANGE;
							if (servo1pos < SERVO_MIN)
								servo1pos = SERVO_MIN;
							s1.setAngle(servo1pos);
						}
						if (Math.abs(power1) < 10) {
							// Do nothing with Servo 2
						} else if (power1 < 0) {
							servo2pos = servo2pos + CHANGE;
							if (servo2pos > SERVO_MAX)
								servo2pos = SERVO_MAX;
							s2.setAngle(servo2pos);
						} else {
							servo2pos = servo2pos - CHANGE;
							if (servo2pos < SERVO_MIN)
								servo2pos = SERVO_MIN;
							s2.setAngle(servo2pos);
						}
					}
				    else if (state == GRAB) {
						power2 = input % 1000 - 255;
						if (Math.abs(power2) > 10) {
							if (power2 > 0) {
								Motor.A.forward();
								Motor.A.setSpeed(Math.abs(power2));
							} else {
								Motor.A.backward();
								Motor.A.setSpeed(Math.abs(power2));
							}
						} else {
							Motor.A.stop();
						}
					}
					else if (state == FINE) {
						power1 = input / 1000 % 1000 - 255;
						power2 = input % 1000 - 255;
						// If the value is less than ten, ignore it.
						// Often the joystick will not re-center exactly,
						// and we
						// don't want to give a very small amount of power
						// to the motors.
						if ((Math.abs(power1) < 10)
								&& (Math.abs(power2) < 10)) {
							m1.stop();
							m2.stop();
						} else if (power1 >= 10) {
							// If the left joystick is forwards, move
							// forwards.
							m1.forward();
							m2.backward();
							m1.setPower((int) Math.abs((float) power1
									* FINE_POWER));
							m2.setPower((int) Math.abs((float) power1
									* FINE_POWER));
						} else if (power1 <= -10) {
							// If the left joystick is backwards, move
							// backwards.
							m1.backward();
							m2.forward();
							m1.setPower((int) Math.abs((float) power1
									* FINE_POWER));
							m2.setPower((int) Math.abs((float) power1
									* FINE_POWER));
						} else if (power2 > 0) {
							// If the right joystick is left, turn in place
							// left.
							m1.backward();
							m2.backward();
							m1.setPower((int) Math.abs((float) power2
									* FINE_POWER));
							m2.setPower((int) Math.abs((float) power2
									* FINE_POWER));
						} else {
							// If the right joystick is right, turn in place
							// right.
							m1.forward();
							m2.forward();
							m1.setPower((int) Math.abs((float) power2
									* FINE_POWER));
							m2.setPower((int) Math.abs((float) power2
									* FINE_POWER));
						}
					}
				} catch (Exception ex) {
					// If something breaks, close the connection
					ex.printStackTrace();
					dis.close();
					dos.close();
					connection.close();
					System.exit(0);
				}
				// If the program ends, close the connection.
				dis.close();
				dos.close();
				connection.close();
			}// close threadToStart if statement
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
