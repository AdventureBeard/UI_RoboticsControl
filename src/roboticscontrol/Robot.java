/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.util.Random;
import java.util.Timer;


/**
 *
 * @author braden
 */
public class Robot implements Runnable {
	
	// Robot "Operating System" Values
	private boolean running;
	MessageService messageService;
	
	// "GPS" Sensor Values
	private int x, y;
	private int heading;
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
		this.heading = 180;
		this.headingDelta = 0;
		this.speed = 0;
		
		this.clawEngaged = false;
		this.armAngle = 0;
		this.armAngleDelta = 0;
		
	}
	
	public void run() {
		
		while (running) {
			String message = messageService.receiveFromUI();
			if (message != null) {
				System.out.println("Robot: Received a message: " + message);
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
		
	}

	private void handshake() {
		messageService.sendToUI("handshake:0");
		messageService.sendToUI("updateSpeed:0");
	}
			
	private void end() {
		this.running = false;
	}

	private void setX(int x) {
		this.x = x;
	}

	private void setY(int y) {
		this.y = y;
	}

	private void turn(int delta) {
		headingDelta = delta;
	}

	private void setSpeed(int speed) {
		this.speed = speed;
		messageService.sendToUI("updateSpeed:" + speed);
	}

	private void changeSpeed(int increment) {
		if (increment > 0 && speed == 3) {
			messageService.sendToUI("updateSpeed:-1");
			return;
		} else if (increment < 0 && speed == 0) {
			messageService.sendToUI("updateSpeed:-2");
			return;
		} else if (increment > 0 && speed < 3) {
			speed += increment;	
		} else if (increment < 0 && speed > 0) {
			speed += increment;
		}
		messageService.sendToUI("updateSpeed:" + speed);
	}

	private void toggleClawEngaged() {
		clawEngaged = !clawEngaged;
		messageService.sendToUI("updateClawStatus:" + ((clawEngaged) ? 1 : 0));
	}

	private void startCamera() {
		
	}

	private void readTemperature() {
		Random random = new Random();
		int temp = random.nextInt((75 - 65) + 1) + 65;
		messageService.sendToUI("updateTemp:" + temp);
	}
	
	private void sendGPSData() {

		messageService.sendToUI("gps:" + this.x + "-" + this.y + "-" + this.heading);
	}

}
