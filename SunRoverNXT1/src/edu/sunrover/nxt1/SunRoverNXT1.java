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

	final int FINE = 0;
	final int WEBCAM = 1;
	final int ARM = 2;
	final int GRAB = 3;
	final int DRIVE = 4;
	// This is how much the NXT should change servo values.
	final float CHANGE = 0.5f;
	final double FINE_POWER = 0.1;
	// These are the starting positions of the servos.
	float webcamHeightAngle = 90f;
	float webcamRotateAngle = 90f;
	float armHeightAngle = 70f;
	float armRotateAngle = 90f;
	float servoMinAngle = 0f;
	float servoMaxAngle = 180f;
	boolean proceed = true;
	// Set up global variables used in this program.
	NXTConnection connection;
	DataInputStream dis;
	TetrixServoController ms;
	TetrixMotorController mc1;
	TetrixMotorController mc2;
	TetrixEncoderMotor m1;
	TetrixEncoderMotor m2;
	TetrixEncoderMotor m3;
	TetrixEncoderMotor m4;
	TetrixServo s1;
	TetrixServo s2;
	TetrixServo s3;
	TetrixServo s4, s5;

	// Set up the NXT
	public SunRoverNXT1() {
		// Set up the Tetrix controllers
		mc1 = new TetrixMotorController(SensorPort.S1,
				TetrixControllerFactory.DAISY_CHAIN_POSITION_1);
		mc2 = new TetrixMotorController(SensorPort.S1,
				TetrixControllerFactory.DAISY_CHAIN_POSITION_2);
		ms = new TetrixServoController(SensorPort.S1,
				TetrixControllerFactory.DAISY_CHAIN_POSITION_3);
		// Set up the motors and servos.
		m1 = mc1.getEncoderMotor(TetrixMotorController.MOTOR_1);
		m2 = mc1.getEncoderMotor(TetrixMotorController.MOTOR_2);
		m3 = mc2.getEncoderMotor(TetrixMotorController.MOTOR_1);
		m4 = mc2.getEncoderMotor(TetrixMotorController.MOTOR_2);
		s1 = ms.getServo(TetrixServoController.SERVO_1);
		s2 = ms.getServo(TetrixServoController.SERVO_2);
		s3 = ms.getServo(TetrixServoController.SERVO_3);
		s4 = ms.getServo(TetrixServoController.SERVO_4);
		s5 = ms.getServo(TetrixServoController.SERVO_5);
		setupNXTConnection();
	}

	// Start the SunRover
	public static void main(String[] args) {
		new SunRoverNXT1();
	}
	
	public void setupNXTConnection() {
		s1.setAngle(webcamHeightAngle);
		s2.setAngle(webcamRotateAngle);
		s3.setAngle(armHeightAngle);
		s4.setAngle(armHeightAngle);
		s5.setAngle(armRotateAngle);
		System.out.println("Connecting...");
		// Wait for a USB Connection.
		connection = USB.waitForConnection();
		System.out.println("Connected!");
		// Create a data input stream to read from.
		dis = connection.openDataInputStream();
		// dos = connection.openDataOutputStream();
		// Start the first thread
		Thread t = new Thread(this);
		t.start();
	}

	// This is where all the work happens. We set up all the Tetrix Motors,
	// Servos, and Controllers.
	@Override
	public void run() {
		// All sorts of things could break. Gotta catch 'em all!
		// try {
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
		int state = FINE;
		System.out.println("Starting...");
		try {
			while (proceed == true) {
				// try {
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
				//System.out.println(input);
				// If button 8 is down, kill the program.
				if (btn8 == 1) {
					proceed = false;
				} else if ((btn1 == 1) && (state != FINE)) {
					state = FINE;
				} else if ((btn2 == 1) && (state != WEBCAM)) {
					state = WEBCAM;
				} else if ((btn3 == 1) && (state != ARM)) {
					state = ARM;
				} else if ((btn4 == 1) && (state != GRAB)) {
					state = GRAB;
				} else if ((btn5 == 1) && (state != DRIVE)) {
					state = DRIVE;
				} else if (state == FINE) {
					// If the value is less than ten, ignore it.
					// Often the joystick will not re-center exactly,
					// and we
					// don't want to give a very small amount of power
					// to the motors.
					if ((Math.abs(power1) < 10) && (Math.abs(power2) < 10)) {
						m1.stop();
						m2.stop();
						m3.stop();
						m4.stop();
					} else if (power1 >= 10) {
						// If the left joystick is forwards, move
						// forwards.
						m1.forward();
						m2.backward();
						m3.forward();
						m4.backward();
						m1.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
						m2.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
						m3.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
						m4.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
					} else if (power1 <= -10) {
						// If the left joystick is backwards, move
						// backwards.
						m1.backward();
						m2.forward();
						m3.backward();
						m4.forward();
						m1.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
						m2.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
						m3.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
						m4.setPower((int) Math
								.abs((double) power1 * FINE_POWER));
					} else if (power2 > 0) {
						// If the right joystick is left, turn in place
						// left.
						m1.backward();
						m2.backward();
						m3.backward();
						m4.backward();
						m1.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
						m2.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
						m3.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
						m4.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
					} else {
						// If the right joystick is right, turn in place
						// right.
						m1.forward();
						m2.forward();
						m3.forward();
						m4.forward();
						m1.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
						m2.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
						m3.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
						m4.setPower((int) Math
								.abs((double) power2 * FINE_POWER));
					}
				} else if (state == WEBCAM) {
					if (Math.abs(power1) < 10) {
						// Do nothing with Servo 1
					} else if (power1 < 0) {
						webcamHeightAngle = webcamHeightAngle - CHANGE;
						if (webcamHeightAngle < servoMinAngle)
							webcamHeightAngle = servoMinAngle;
						s1.setAngle(webcamHeightAngle);
					} else {
						webcamHeightAngle = webcamHeightAngle + CHANGE;
						if (webcamHeightAngle > servoMaxAngle)
							webcamHeightAngle = servoMaxAngle;
						s1.setAngle(webcamHeightAngle);
					}
					if (Math.abs(power2) < 10) {
						// Do nothing with Servo 1
					} else if (power2 > 0) {
						webcamRotateAngle = webcamRotateAngle - CHANGE;
						if (webcamRotateAngle < servoMinAngle)
							webcamRotateAngle = servoMinAngle;
						s2.setAngle(webcamRotateAngle);
					} else {
						webcamRotateAngle = webcamRotateAngle + CHANGE;
						if (webcamRotateAngle > servoMaxAngle)
							webcamRotateAngle = servoMaxAngle;
						s2.setAngle(webcamRotateAngle);
					}
				} else if (state == ARM) {
					if (Math.abs(power1) < 10) {
						// Do nothing with Servo 3
					} else if (power1 > 0) {
						armHeightAngle = armHeightAngle + CHANGE;
						if (armHeightAngle > servoMaxAngle)
							armHeightAngle = servoMaxAngle;
						s3.setAngle(armHeightAngle);
						s4.setAngle(armHeightAngle);
					} else {
						armHeightAngle = armHeightAngle - CHANGE;
						if (armHeightAngle < servoMinAngle)
							armHeightAngle = servoMinAngle;
						s3.setAngle(armHeightAngle);
						s4.setAngle(armHeightAngle);
					}
					if (Math.abs(power2) < 10) {
						// Do nothing with Servo 5
					} else if (power2 > 0) {
						armRotateAngle = armRotateAngle + CHANGE;
						if (armRotateAngle > servoMaxAngle)
							armRotateAngle = servoMaxAngle;
						s5.setAngle(armRotateAngle);
					} else {
						armRotateAngle = armRotateAngle - CHANGE;
						if (armRotateAngle < servoMinAngle)
							armRotateAngle = servoMinAngle;
						s5.setAngle(armRotateAngle);
					}
				} else if (state == GRAB) {
					if (Math.abs(power2) > 10) {
						if (power2 > 0) {
							Motor.A.forward();
							Motor.A.setSpeed(power2);
						} else {
							Motor.A.backward();
							Motor.A.setSpeed(power2);
						}
					} else {
						Motor.A.stop();
					}
				} else if (state == DRIVE) {
					// If the value is less than ten, ignore it.
					// Often the joystick will not re-center exactly,
					// and we
					// don't want to give a very small amount of power
					// to the motors.
					if ((Math.abs(power1) < 10) && (Math.abs(power2) < 10)) {
						m1.stop();
						m2.stop();
						m3.stop();
						m4.stop();
					} else if (power1 >= 10) {
						// If the left joystick is forwards, move
						// forwards.
						m1.forward();
						m2.backward();
						m3.forward();
						m4.backward();
						m1.setPower(Math.abs(power1));
						m2.setPower(Math.abs(power1));
						m3.setPower(Math.abs(power1));
						m4.setPower(Math.abs(power1));
					} else if (power1 <= -10) {
						// If the left joystick is backwards, move
						// backwards.
						m1.backward();
						m2.forward();
						m3.backward();
						m4.forward();
						m1.setPower(Math.abs(power1));
						m2.setPower(Math.abs(power1));
						m3.setPower(Math.abs(power1));
						m4.setPower(Math.abs(power1));
					} else if (power2 > 0) {
						// If the right joystick is left, turn in place
						// left.
						m1.backward();
						m2.backward();
						m3.backward();
						m4.backward();
						m1.setPower(Math.abs(power2));
						m2.setPower(Math.abs(power2));
						m3.setPower(Math.abs(power2));
						m4.setPower(Math.abs(power2));
					} else {
						// If the right joystick is right, turn in place
						// right.
						m1.forward();
						m2.forward();
						m3.forward();
						m4.forward();
						m1.setPower(Math.abs(power2));
						m2.setPower(Math.abs(power2));
						m3.setPower(Math.abs(power2));
						m4.setPower(Math.abs(power2));
					}
				}
			}
			dis.close();
			connection.close();
		} catch (IOException ex) {
			try {
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connection.close();
			System.out.println("Exited while loop");
			if (proceed == true) {
				setupNXTConnection();
			}
		}
	}

}
