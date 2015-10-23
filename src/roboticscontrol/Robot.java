/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * @author braden
 */
public class Robot implements Runnable {

	// Boundaries of the "map"
	final int X_LOW_BOUND = 0;
	final int X_HI_BOUND = 410;
	final int Y_LOW_BOUND = 0;
	final int Y_HI_BOUND = 368;
	
	// Robot "Operating System" Values
	private boolean running;
	MessageService messageService;
	
	// "GPS" Sensor Values
	private double x, y;
	private double dx, dy;
	private double heading;
	private int headingDelta;
	private int speed;
	
	// Camera Sensor Values
	private boolean cameraActive;

	// Actuator Arm Values
	private int armAngle;
	private int armAngleDelta;
	private boolean clawEngaged;


	public Robot(MessageService ms) {
		this.messageService = ms;
		this.running = true;
		
		// DEFAULT VALUES //
		this.x = 205;
		this.y = 184;
		this.dx = 0;
		this.dy = 0;
		this.heading = 3 * Math.PI/2;
		this.headingDelta = 0;
		this.speed = 0;
		this.clawEngaged = false;
		this.armAngle = 0;
		this.armAngleDelta = 0;
		this.cameraActive = false;
		/////////////////////
	}

	
	@Override
	public void run() {
		
		/** GPS Calculation Timer.
		 * I didn't want to calculate the GPS position every tick, so this timer
		 * calls the GPS recalculation every 50ms. This uses a different timer
		 * than the UI. Ultimately I could've used either, but since the UI uses 
		 * Swing I figured using the Swing timer for that one and the Thread timer
		 * for this one made more sense.
		 */	
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				calculatePosition();
				calculateArmAngle();
			}
		};
		timer.scheduleAtFixedRate(task, 0, 50);
		
		/** Message Check
		 * 
		 * Improvements: Having the thread check for messages constantly is a waste of resources;
		 * What should happen instead is that the message service notifies the UI when it has 
		 * a new message. I am leaving going out of town soon so I am short on time, otherwise I would implement
		 * a better synchronization system.
		 */
		while (running) {
			String message = messageService.receiveFromUI();
			if (message != null) {
				parseMessage(message);
			}
		}
	}
	
	 /** parseMessage
	 * 	Method for converting a message from the UI into some action to modify
	 *  the robot state.
	 * @param m  a String containing an instruction in the format 'command:parameter'
	 */
	private void parseMessage(String m) {
		String[] message = m.split(":");
		String command = message[0];
		int parameter = Integer.parseInt(message[1]);
		switch (command) {
			case "handshake" :	handshake();
								break;
			case "status":		sendStatusData();
								break;
			case "end" :		end();
								break;
			case "turn" :		turn(parameter);
								break;		
			case "temp" :		readTemperature();
								break;
			case "claw" :		toggleClawEngaged();
								break;
			case "arm"	:		changeArmAngle(parameter);
								break;
			case "speed":		changeSpeed(parameter);
								break;
			case "speed2":		setSpeed(parameter);
								break;
			case "camera":		toggleCamera();
								break;
		}
	}

	/** handShake
	 * Function to confirm connection between UI and robot.
	 */
	private void handshake() {
		sendMessageToUI("handshake:0");
		sendMessageToUI("updateSpeed:0");
		sendMessageToUI("updateArmAngle:" + armAngle);
	}

	/** calculatePosition
	 * 	Using the current X, Y, heading, and speed values , calculate the robot's
	 *  new GPS coordinates. If the new X and Y positions are outside the operation area,
	 * 	set speed to 0 and report to the UI. Remember that Math.sin and Math.cos use radians.
	 */
	private void calculatePosition() {
		heading += headingDelta * 0.1;
		heading = heading % 6.28;
		dx = Math.cos(heading);
		dy = Math.sin(heading);

		double newX = x + dx * speed;
		double newY = y + dy * speed;

		if (newX > X_LOW_BOUND && newX < X_HI_BOUND) {
			x = newX;
		} else {
			speed = 0;
			sendMessageToUI("bounds:0");
		}

		if (newY > Y_LOW_BOUND && newY < Y_HI_BOUND ) {
			y = newY;
		} else {
			speed = 0;
			sendMessageToUI("bounds:0");
		}
	}
	
	/** calculateArmAngle
	 * Use the current arm delta value to calculate a new arm angle. Ensure it is
	 * between 0 and 90 inclusive.
	 */
	private void calculateArmAngle() {
		int newAngle = armAngle + armAngleDelta;
		if (newAngle <= 90 && newAngle >= 0) {
			armAngle = newAngle;
		}
	}
			
	/** end
	 * Used to turn off robot. Not used.
	 */
	private void end() {
		this.running = false;
	}
	
	/** turn
	 * Respond to a turn event message from the UI.
	 * @param delta 	A new delta value for the heading.
	 */
	private void turn(int delta) {
		headingDelta = delta;
	}

	/** setSpeed
	 * Set the new speed of the robot, paying mind to the max and minimum speeds.
	 * @param speed 	the new speed of the robot.
	 */ 
	private void setSpeed(int speed) {
		if (speed > 3) {
			return;
		} else if (speed < -1) {
			return;
		} else if (speed == 0) {
			this.speed = 0;
			this.dx = 0;
			this.dy = 0;
		} else {
			this.speed = speed;
		}
		sendMessageToUI("updateSpeed:" + speed);
	}
	/** changeSpeed
	 *	Increment the speed by a value parameter sent by the UI. 
	 * @param increment 	the increment to be added to the speed.
	 */
	private void changeSpeed(int increment) {
		setSpeed(speed + increment);
	}

	/** toggleClawEngaged()
	 * Respond to a UI event message to toggle claw status.
	 */
	private void toggleClawEngaged() {
		clawEngaged = !clawEngaged;
		sendMessageToUI("updateClawStatus:" + ((clawEngaged) ? 1 : 0));
	}

	private void changeArmAngle(int parameter) {
		armAngleDelta = parameter;
	}
	
	/** startCamera
	 * Respond to toggle camera event from the UI.
	 */
	private void toggleCamera() {
		cameraActive = !cameraActive;
		sendMessageToUI("updateCameraStatus:" + ((cameraActive) ? 1 : 0));
	}

	/** readTemperature
	 * Respond to read temperature event from the UI. Just generates a random
	 * temperature near room temperature.
	 */
	private void readTemperature() {
		Random random = new Random();
		int temp = random.nextInt((75 - 65) + 1) + 65;
		sendMessageToUI("updateTemp:" + temp);
	}
	
	/** sendGPSData
	 * Send all GPS data to the UI. Converts heading to degrees before sending.
	 */
	private void sendStatusData() {
		sendMessageToUI("updateGPS:" + this.x + "#" + this.y + "#" + Math.floor(Math.toDegrees(this.heading)));
		sendMessageToUI("updateArmAngle:" + armAngle);
	}

	/** sendMessageToUI
	 * Unified send message function. Originally used for thread synchronization, but
	 * I ran out of time before going out of town so I didn't end up making good multithreading
	 * a priority since this was mainly a UI project.
	 * @param s 	the message to send
	 */
	private void sendMessageToUI(String s) {
		messageService.sendToUI(s);	
	}

}
