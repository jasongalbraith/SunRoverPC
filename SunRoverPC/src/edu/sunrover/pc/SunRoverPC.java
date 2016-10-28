package edu.sunrover.pc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class SunRoverPC {

	// This socket, input stream, and output stream are for
	// The PC with the controller on it.
	ServerSocket server;
	Socket client;
	DataInputStream dis;
	DataOutputStream dos;
	// This connection, input, and output stream are for the first NXT.
	NXTComm nxtComm1;
	String nxtName1 = "MOTORS";
	DataInputStream nxis1;
	DataOutputStream nxos1;
	// This connection, input, and output stream are for the first NXT.
	NXTComm nxtComm2;
	String nxtName2 = "SENSORS1";
	DataInputStream nxis2;
	DataOutputStream nxos2;
	// This connection, input, and output stream are for the first NXT.
	NXTComm nxtComm3;
	String nxtName3 = "SENSORS2";
	DataInputStream nxis3;
	DataOutputStream nxos3;
	static boolean running = true;
	boolean auto = false;
	boolean kill = false;
	//These values are used to start two seperate threads
	final int READ1 = 0;
	final int READ2 = 1;
	final int WRITE = 2;
	int threadToStart = READ1;
	//Sensor array
	static int[] sensors = new int[7];
	final static int COMPASS = 0;
	final static int FORWARD = 1;
	final static int BACKWARD = 2;
	final static int LEFT = 3;
	final static int RIGHT = 4;
	final static int FORWARD_DOWN = 5;
	final static int BACKWARD_DOWN = 6;
	Autonomous autoPilot;
	boolean autonomousOn = false;
	long startTime = 0;
	long sensor1Input;
	long sensor2Input;
	int controlInput = 255255;

	public SunRoverPC() {
		try {
			String command = "cmd /c cd c:\\workspace\\SunRoverNXT1\\bin & c:\\lejos\\bin\\nxj -r -u -name "+ nxtName1 +" edu.sunrover.nxt1.SunRoverNXT1";
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			command = "cmd /c cd c:\\workspace\\SunRoverNXT2\\bin & c:\\lejos\\bin\\nxj -r -u -name "+ nxtName2 +" edu.sunrover.nxt2.SunRoverNXT2";
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			command = "cmd /c cd c:\\workspace\\SunRoverNXT3\\bin & c:\\lejos\\bin\\nxj -r -u -name "+ nxtName3 +" edu.sunrover.nxt3.SunRoverNXT3";
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			startTime = System.currentTimeMillis();
			autoPilot = new Autonomous(this);
			connectNXT1();
			connectNXT2();
			connectNXT3();
			socketConnection();
		} catch (NXTCommException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	public void connectNXT1() throws NXTCommException {
		nxtComm1 = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
		NXTInfo[] nx = nxtComm1.search(nxtName1);
		nxtComm1.open(nx[0]);
		nxos1 = new DataOutputStream(nxtComm1.getOutputStream());
		nxis1 = new DataInputStream(nxtComm1.getInputStream());		
		new StartThreads(this, StartThreads.WRITE1);
	}
	
	public void connectNXT2() throws NXTCommException {
		nxtComm2 = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
		NXTInfo[] nx = nxtComm2.search(nxtName2);
		nxtComm2.open(nx[0]);
		nxos2 = new DataOutputStream(nxtComm2.getOutputStream());
		nxis2 = new DataInputStream(nxtComm2.getInputStream());
		new StartThreads(this, StartThreads.READ2);
	}
	
	public void connectNXT3() throws NXTCommException {
		nxtComm3 = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
		NXTInfo[] nx = nxtComm3.search(nxtName3);
		nxtComm3.open(nx[0]);
		nxos3 = new DataOutputStream(nxtComm3.getOutputStream());
		nxis3 = new DataInputStream(nxtComm3.getInputStream());
		new StartThreads(this, StartThreads.READ3);
	}
	
	public void socketConnection() throws IOException {
		// Make the connection to the PC.
		server = new ServerSocket(1234);
		System.out.println("Waiting...");
		client = server.accept();
		System.out.println("Got Socket");
		dis = new DataInputStream(client.getInputStream());
		dos = new DataOutputStream(client.getOutputStream());
		new StartThreads(this, StartThreads.READPC);
	}
	
/*	public synchronized static int readControlInput(){
		
		return controlInput;
	
	}
	
	public synchronized static void writeControlInput(int theControlInput){
		
		controlInput = theControlInput;
		
	}
	*/

	public static void main(String[] args) {
		new SunRoverPC();
	}

}