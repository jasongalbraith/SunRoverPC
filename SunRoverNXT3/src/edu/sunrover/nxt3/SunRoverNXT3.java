package edu.sunrover.nxt3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.addon.CompassHTSensor;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;

public class SunRoverNXT3 implements Runnable {

	//Set up global variables used in this program.
	NXTConnection connection;
	DataInputStream dis;
	DataOutputStream dos;
	//These values are used to start two seperate threads
	final int SENSORS = 0;
	final int KILL = 1;
	int threadToStart = SENSORS;
	//These sensors are on the robot.
	//The Compass sensor is on S1
	//The Ultrasonic Sensors are on ports S2, S3, and S4, respectively
	CompassHTSensor compass;
	UltrasonicSensor forwardDown;
	UltrasonicSensor backwardDown;
	boolean proceed = true;
	int input = 0;
	int btn8 = 0;

	public SunRoverNXT3() {
		//Set up the sensors
		compass = new CompassHTSensor(SensorPort.S1);
		forwardDown = new UltrasonicSensor(SensorPort.S2);
		backwardDown = new UltrasonicSensor(SensorPort.S3);
		setupNXTConnection();
	}

	public static void main(String[] args) {
		new SunRoverNXT3();
	}

	public void setupNXTConnection() {
		System.out.println("Connecting...");
		//Wait for a USB Connection.
		connection = USB.waitForConnection();
		System.out.println("Connected!");
		//Create a data input stream to read from.
		dis = connection.openDataInputStream();
		dos = connection.openDataOutputStream();
		Thread t = new Thread(this);
		t.setPriority(1);
		t.start();
	}
	
	@Override
	public void run() {
		try {
			System.out.println("Starting...");
			if (threadToStart == SENSORS) {
				//Start the motors thread.
				threadToStart = KILL;
				Thread t = new Thread(this);
				t.setPriority(9);
				t.start();
				int output = 0;
				while (proceed == true) {
					//System.out.println(compass.getDegrees());
					output = (int)(compass.getDegrees())*1000000+forwardDown.getDistance()*1000+backwardDown.getDistance();
					dos.writeInt(output);
					dos.flush();
				}
				System.out.println("Pro false");
			}
			else if (threadToStart == KILL){
				while (proceed == true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					input = dis.readInt();
					btn8 = input / 1000000 % 64 / 32;
					if (btn8 == 1) {
						proceed = false;
						return;
					}
				}
			}
			dis.close();
			dos.close();
			connection.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			try {
				dis.close();
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connection.close();
			if (proceed == true) {
				setupNXTConnection();
			}
		}
	}

}
