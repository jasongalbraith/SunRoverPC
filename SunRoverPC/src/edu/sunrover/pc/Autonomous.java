package edu.sunrover.pc;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Autonomous implements Runnable, KeyListener {
	
	final int MAX_STATES = 10;
	int[] buttons = new int[7];
	boolean running = true;
	boolean pressedSpace = false;
	int state = 0;
	int count = 0;
	int threshold = 3;
	int compassAdjust = 5;
	final static int NOT_RUNNING = 0;
	final static int STOP = 1;
	final static int FORWARD = 2;
	final static int REVERSE = 3;
	final static int SLIGHTLEFT = 4;
	final static int SLIGHTRIGHT = 5;
	final static int READJUST = 6;
	final static int RESET = 7;
	//These final ints used for checking direction on a compass
	final int closeCompassCheck = 5;
	final int compassMax = 360;
	final int jumpCompassCheck =  compassMax-5;
	
	String[] stateDescription = {"NOT_RUNNING", 
					   "STOP",
					   "FORWARD",
			   		   "REVERSE",
			   		   "SLIGHTLEFT",
			   		   "SLIGHTRIGHT",
			   		   "READJUST"};
	JFrame stateFrame = new JFrame("State");
	JLabel stateLabel = new JLabel("");
	JLabel currentCompassLabel = new JLabel("");
	JLabel directionLabel = new JLabel("");
	JLabel testCaseLabel = new JLabel("");
	JLabel ifTest1Label = new JLabel("");
	JLabel ifTest2Label = new JLabel("");
	Container compassTestCont = new Container();
	Container ifTestsCont = new Container();
	Font stateFont = new Font("Arial", Font.BOLD, 50);
	Font compassFont = new Font("Arial", Font.BOLD, 25);
	SunRoverPC sunRoverPC;
	
	public Autonomous(SunRoverPC newSunRoverPC) {
		stateFrame.setSize(800,400);
		stateFrame.setLayout(new BorderLayout());
		compassTestCont.setLayout(new BorderLayout());
		ifTestsCont.setLayout(new BorderLayout());
		ifTestsCont.add(ifTest1Label, BorderLayout.WEST);
		ifTestsCont.add(ifTest2Label, BorderLayout.EAST);
		compassTestCont.add(currentCompassLabel, BorderLayout.EAST);
		compassTestCont.add(directionLabel, BorderLayout.CENTER);
		compassTestCont.add(testCaseLabel, BorderLayout.WEST);
		stateFrame.add(ifTestsCont, BorderLayout.NORTH);
		stateFrame.add(compassTestCont, BorderLayout.SOUTH);
		stateFrame.add(stateLabel, BorderLayout.CENTER);
		stateFrame.addKeyListener(this);
		ifTest1Label.setFont(compassFont);
		ifTest2Label.setFont(compassFont);
		currentCompassLabel.setFont(compassFont);
		directionLabel.setFont(compassFont);
		testCaseLabel.setFont(compassFont);
		stateLabel.setFont(stateFont);
		stateFrame.setVisible(true);
		sunRoverPC = newSunRoverPC;
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		try {
			int currentCompass = 0;
			state = NOT_RUNNING;
			while (SunRoverPC.running) {
				stateLabel.setText(stateDescription[state]);
				if (state == NOT_RUNNING) {
					Thread.sleep(500);
				}
				else if (state == STOP) {
					setJoystick(0, 0);
					resetButtons();
					buttons[0] = 1;
					setButtonsPower(buttons, 0, 0);
					currentCompass = SunRoverPC.sensors[SunRoverPC.COMPASS];
					currentCompassLabel.setText("Current Compass= " + currentCompass);
					Thread.sleep(1000);
					state = FORWARD;
					setJoystick(-100, 0);
					testCaseLabel.setText("Not-Running");
				}
				else if (state == FORWARD) {
					if (SunRoverPC.sensors[SunRoverPC.FORWARD] < 100) {
						setJoystick(100, 0);
						count++;
						testCaseLabel.setText("previous was FORWARD");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = REVERSE;
					}
					else if (SunRoverPC.sensors[SunRoverPC.FORWARD_DOWN] > 80) {
						setJoystick(100, 0);
						count++;
						testCaseLabel.setText("previous was FORWARD");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = REVERSE;
					}
					else if (SunRoverPC.sensors[SunRoverPC.COMPASS] - compassAdjust > currentCompass) {
						int temp = SunRoverPC.sensors[SunRoverPC.COMPASS] - compassAdjust;
						ifTest1Label.setText(""+ temp );
						ifTest2Label.setText(">  "+ currentCompass);
						setJoystick(0, 0);
						Thread.sleep(1000);
						testCaseLabel.setText("previous was FORWARD");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = SLIGHTLEFT;
					}
					else if (SunRoverPC.sensors[SunRoverPC.COMPASS] + jumpCompassCheck > currentCompass && currentCompass > 354) {
						int temp = SunRoverPC.sensors[SunRoverPC.COMPASS] +jumpCompassCheck;
						ifTest1Label.setText(""+ temp );
						ifTest2Label.setText(">  "+ currentCompass + "&& > 354");
						setJoystick(0, 0);
						Thread.sleep(1000);
						testCaseLabel.setText("previous was FORWARD");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = SLIGHTLEFT;
					}
					else if (SunRoverPC.sensors[SunRoverPC.COMPASS] + compassAdjust < currentCompass) {
						int temp = SunRoverPC.sensors[SunRoverPC.COMPASS] + compassAdjust;
						ifTest1Label.setText(""+ temp );
						ifTest2Label.setText("<  "+ currentCompass);
						setJoystick(0, 0);
						Thread.sleep(1000);
						testCaseLabel.setText("previous was FORWARD");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = SLIGHTRIGHT;
					}
					else if(SunRoverPC.sensors[SunRoverPC.COMPASS] - jumpCompassCheck < currentCompass && currentCompass < 6) {
						int temp = SunRoverPC.sensors[SunRoverPC.COMPASS] - jumpCompassCheck;
						ifTest1Label.setText(""+ temp );
						ifTest2Label.setText("<  "+ currentCompass + "&& < 5");
						setJoystick(0, 0);
						Thread.sleep(1000);
						testCaseLabel.setText("previous was FORWARD");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = SLIGHTRIGHT;
					}
				}
				else if (state == REVERSE) {
					if (count > threshold) {
						count = 0;
						setJoystick(0, 0);
						Thread.sleep(1000);
						testCaseLabel.setText("previous was REVERSE");
						currentCompassLabel.setText( " Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = READJUST;
					}
					else if (SunRoverPC.sensors[SunRoverPC.BACKWARD] < 100) {
						setJoystick(-100, 0);
						count++;
						testCaseLabel.setText("previous was REVERSE");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = FORWARD;
					}
					else if (SunRoverPC.sensors[SunRoverPC.BACKWARD_DOWN] > 80) {
						setJoystick(-100, 0);
						count++;
						testCaseLabel.setText("previous was REVERSE");
						currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
						state = FORWARD;
					}
				}
				else if (state == SLIGHTLEFT) {
					if (SunRoverPC.sensors[SunRoverPC.COMPASS] - compassAdjust > currentCompass) {
						testCaseLabel.setText("Normal");
						while (SunRoverPC.sensors[SunRoverPC.COMPASS]  > currentCompass){
							setJoystick(0,-25);
							currentCompassLabel.setText("currentCompass =" + currentCompass + " Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS] );
						}
						setJoystick(0,0);
					}
					else if (SunRoverPC.sensors[SunRoverPC.COMPASS] + jumpCompassCheck > currentCompass && currentCompass > 355){
						testCaseLabel.setText("JumpZero");
						while (SunRoverPC.sensors[SunRoverPC.COMPASS] + jumpCompassCheck > currentCompass|| SunRoverPC.sensors[SunRoverPC.COMPASS] > currentCompass){
							setJoystick(0,-25);
							currentCompassLabel.setText("currentCompass =" + currentCompass + " Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS] );
						}
						setJoystick(0,0);	
					}
					setJoystick(0, 0);
					Thread.sleep(1000);
					setJoystick(-100, 0);
					testCaseLabel.setText("previous was SlightLeft");
					currentCompassLabel.setText( "Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
					state = FORWARD;
				}
				else if (state == SLIGHTRIGHT) {
					 if (SunRoverPC.sensors[SunRoverPC.COMPASS] + compassAdjust < currentCompass){
						testCaseLabel.setText("Normal");
						while (SunRoverPC.sensors[SunRoverPC.COMPASS]  < currentCompass ){
							setJoystick(0,25);
							currentCompassLabel.setText("currentCompass =" + currentCompass + " Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS] );
						}
						setJoystick(0,0);
					}
					else if (SunRoverPC.sensors[SunRoverPC.COMPASS] - jumpCompassCheck < currentCompass && currentCompass < 5) {
						testCaseLabel.setText("jump zero");
						
						while (SunRoverPC.sensors[SunRoverPC.COMPASS] - jumpCompassCheck < currentCompass || SunRoverPC.sensors[SunRoverPC.COMPASS] < currentCompass){
							setJoystick(0,25);
							currentCompassLabel.setText("currentCompass =" + currentCompass + " Actual Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS] );
						}
						setJoystick(0,0);	
					}
					setJoystick(0, 0);
					Thread.sleep(1000);
					setJoystick(-100, 0);
					testCaseLabel.setText("previous was SlightRight");
					currentCompassLabel.setText( " Compass = " + SunRoverPC.sensors[SunRoverPC.COMPASS]);
					state = FORWARD;
				}
				else if (state == READJUST) {
					setJoystick(0, 100);
					Thread.sleep((int)(Math.random() * 1000 * 10));
					setJoystick(0, 0);
					Thread.sleep(1000);
					count = 0;
					currentCompass = SunRoverPC.sensors[SunRoverPC.COMPASS];
					setJoystick(-100, 0);
					testCaseLabel.setText("previous was READJUST");
					state = FORWARD;
				}
				else if (state == RESET) {
					setJoystick(0, 0);
					resetButtons();
					buttons[0] = 1;
					setButtonsPower(buttons, 0, 0);
					state = NOT_RUNNING;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setJoystick(int power1, int power2) {
		try{
			if (sunRoverPC.nxos1 != null) {
				sunRoverPC.nxos1.writeInt((power1+255)*1000+(power2+255));
				sunRoverPC.nxos1.flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}	
	
	public void setButtonsPower(int[] buttonsIn, int power1, int power2) {
		try{
			int output = (power1+255)*1000+(power2+255);
			for (int i = 0; i < buttonsIn.length; i++) {
				output += (buttonsIn[i] * Math.pow(2, i)) * 1000000;
			}
			if (sunRoverPC.nxos1 != null) {
				sunRoverPC.nxos1.writeInt(output);
				sunRoverPC.nxos1.flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void kill() {
		state = 0;
	}
	
	public void resetButtons() {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = 0;
		}
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int newState) {
		state = newState;
	}
	
	public String getStateDescription() {
		return stateDescription[state];
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_SPACE){
			if (pressedSpace == false){
			state = STOP;
			pressedSpace = true;
			}
			else {
				setJoystick(0,0);
				state = NOT_RUNNING;
				pressedSpace = false;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {		
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}


}
