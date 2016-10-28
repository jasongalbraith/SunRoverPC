package edu.sunrover.pc;

import java.io.IOException;

import lejos.pc.comm.NXTCommException;

public class StartThreads extends Thread {
	
	SunRoverPC sunRoverPC;
	final static int READPC = 0;
	final static int READ2 = 1;
	final static int READ3 = 2;
	final static int WRITE1 = 3;
	int threadToStart = WRITE1;
	static boolean value1Changed = false;
	static boolean value2Changed = false;
	static boolean value3Changed = false;
	
	public StartThreads (SunRoverPC newSunRoverPC, int whichThreadToStart) {
		sunRoverPC = newSunRoverPC;
		threadToStart = whichThreadToStart;
		start();
	}
	
	public void run() {
		if (threadToStart == WRITE1) {
			try {
				while (SunRoverPC.running) {
					if (value3Changed) {
						value3Changed = false;
						System.out.println("Writing to NXOS1 = " + sunRoverPC.controlInput);
						sunRoverPC.nxos1.writeInt(sunRoverPC.controlInput);
						sunRoverPC.nxos1.flush();
						Thread.sleep(10);
					}
				}
			} catch (IOException ex) {
				boolean connected = false;
				while (connected == false) {
					try {
						System.out.println("Lost NXT1");
						sunRoverPC.connectNXT1();
						connected = true;
					} catch (NXTCommException e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (threadToStart == READ2) {
			try {
				while (SunRoverPC.running) {
					sunRoverPC.sensor1Input = sunRoverPC.nxis2.readLong();
					SunRoverPC.sensors[SunRoverPC.FORWARD] = (int)(sunRoverPC.sensor1Input % 1000000000000l / 1000000000);
					SunRoverPC.sensors[SunRoverPC.BACKWARD] = (int)(sunRoverPC.sensor1Input % 1000000000 / 1000000);
					SunRoverPC.sensors[SunRoverPC.LEFT] = (int)(sunRoverPC.sensor1Input % 1000000 / 1000);
					SunRoverPC.sensors[SunRoverPC.RIGHT] = (int)(sunRoverPC.sensor1Input % 1000);
					sunRoverPC.sensor1Input += 1000000000000l;
					value1Changed = true;
					Thread.sleep(100);
				}
			} catch (IOException ex) {
				boolean connected = false;
				while (connected == false) {
					try {
						System.out.println("Lost NXT2");
						sunRoverPC.connectNXT2();
						connected = true;
					} catch (NXTCommException e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if (threadToStart == READ3){
			try {
				while (SunRoverPC.running) {
					sunRoverPC.sensor2Input = sunRoverPC.nxis3.readInt();
					SunRoverPC.sensors[SunRoverPC.COMPASS] = (int)(sunRoverPC.sensor2Input / 1000000);
					SunRoverPC.sensors[SunRoverPC.FORWARD_DOWN] = (int)(sunRoverPC.sensor2Input % 1000000 / 1000);
					SunRoverPC.sensors[SunRoverPC.BACKWARD_DOWN] = (int)(sunRoverPC.sensor2Input % 1000);
					sunRoverPC.sensor2Input *= 1000;
					sunRoverPC.sensor2Input += sunRoverPC.autoPilot.getState();
					value2Changed = true;
					Thread.sleep(100);
				}
			} catch (IOException ex) {
				boolean connected = false;
				while (connected == false) {
					try {
						System.out.println("Lost NXT3");
						sunRoverPC.connectNXT3();
						connected = true;
					} catch (NXTCommException e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if (threadToStart == READPC) {
			try {
				while (SunRoverPC.running) {
					if (value1Changed) {
						value1Changed = false;
						sunRoverPC.dos.writeLong(sunRoverPC.sensor1Input);
						sunRoverPC.dos.flush();
					}
					if (value2Changed) {
						value2Changed = false;
						sunRoverPC.dos.writeLong(sunRoverPC.sensor2Input);
						sunRoverPC.dos.flush();
					}
					int value = sunRoverPC.dis.readInt();
					if (sunRoverPC.controlInput != value) {
						sunRoverPC.controlInput = value;
						System.out.println("value from controller =  " + value);
						value3Changed = true;
					}
					//sunRoverPC.nxos1.writeInt(value);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if ((sunRoverPC.controlInput / 1000000 % 64 / 32) == 1) {
						sunRoverPC.nxos2.writeInt(sunRoverPC.controlInput);
						sunRoverPC.nxos2.flush();
						sunRoverPC.nxos3.writeInt(sunRoverPC.controlInput);
						sunRoverPC.nxos3.flush();
						SunRoverPC.running = false;
					}
					if ((sunRoverPC.controlInput / 1000000 % 128 / 64) == 1) {
						if (sunRoverPC.autoPilot.getState() == Autonomous.NOT_RUNNING) {
							sunRoverPC.autoPilot.setState(Autonomous.STOP);
						}
						else {
							sunRoverPC.autoPilot.setState(Autonomous.RESET);
						}
					}
				}
			} catch (IOException ex) {
				boolean connected = false;
				while (connected == false) {
					try {
						System.out.println("Lost Socket");
						sunRoverPC.socketConnection();
						connected = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
 	}

}
