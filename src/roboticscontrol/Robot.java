/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author braden
 */
public class Robot implements Runnable {

	final int X_LOW_BOUND = 0;
	final int X_HI_BOUND = 410;
	final int Y_LOW_BOUND = 0;
	final int Y_HI_BOUND = 367;
	
	// Robot "Operating System" Values
	private boolean running;
	MessageService messageService;
	
	// "GPS" Sensor Values
	private double x, y;
	private double dx, dy;
	private double heading;
	private int headingDelta;
	private int speed;
	
	// Actuator Arm Values
	private int armAngle;
	private int armAngleDelta;
	private boolean clawEngaged;

	public Robot(MessageService ms) {
		this.messageService = ms;
		this.running = true;
		
		this.x = 50;
		this.y = 50;
		this.dx = 0;
		this.dy = 0;
		this.heading = 360;
		this.headingDelta = 0;
		this.speed = 0;
		
		this.clawEngaged = false;
		this.armAngle = 0;
		this.armAngleDelta = 0;
	}
	
	@Override
	public void run() {
		
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				calculatePosition();
			}
		};
		timer.scheduleAtFixedRate(task, 0, 50);

		while (running) {
			String message = messageService.receiveFromUI();
			if (message != null) {
				parseMessage(message);
			}
		}
	}
	
	private void parseMessage(String m) {
		String[] message = m.split(":");
		String command = message[0];
		int parameter = Integer.parseInt(message[1]);
		switch (command) {
			case "handshake" :	handshake();
								break;
			case "gps":			sendGPSData();
								break;
			case "end" :		end();
								break;
			case "turn" :		turn(parameter);
								break;		
			case "temp" :		readTemperature();
								break;
			case "claw" :		toggleClawEngaged();
								break;
			case "speed":		changeSpeed(parameter);
								break;
			case "speed2":		setSpeed(parameter);
								break;
		}
	}
	
	private void calculatePosition() {
		heading += headingDelta * 0.1;
		dx = Math.sin(heading);
		dy = Math.cos(heading);

		double newX = x + dx * speed * -1;
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

	private void handshake() {
		sendMessageToUI("handshake:0");
		sendMessageToUI("updateSpeed:0");
	}
			
	private void end() {
		this.running = false;
	}

	private void turn(int delta) {
		headingDelta = delta;
	}

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

	private void changeSpeed(int increment) {
		setSpeed(speed + increment);
	}

	private void toggleClawEngaged() {
		clawEngaged = !clawEngaged;
		sendMessageToUI("updateClawStatus:" + ((clawEngaged) ? 1 : 0));
	}

	private void startCamera() {
		
	}

	private void readTemperature() {
		Random random = new Random();
		int temp = random.nextInt((75 - 65) + 1) + 65;
		sendMessageToUI("updateTemp:" + temp);
	}
	
	private void sendGPSData() {
		sendMessageToUI("gps:" + this.x + "-" + this.y);
	}

	private void sendMessageToUI(String s) {
		messageService.sendToUI(s);	
	}

}
