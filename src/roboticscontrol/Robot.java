/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


/**
 *
 * @author braden
 */
public class Robot implements Runnable {

	private int x, y;
	private int speed;
	private boolean clawEngaged;
	private int deltaAngle;
	private int angle;
	private boolean running;
	MessageService messageService;

	public Robot(MessageService ms) {
		this.x = 50;
		this.y = 50;
		this.speed = 0;
		this.clawEngaged = false;
		this.deltaAngle = 0;
		this.running = true;
		System.out.println("Robot activated!");
		this.messageService = ms;
		
	}
	
	public void run() {
		System.out.println("Robot is running.");
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
			case "stop" :	stop();
							break;
			case "turn" :	turn(parameter);
							break;		
			case "temp" :	readTemperature();
							break;
			case "claw" :	toggleClawEngaged();
							break;
			case "speed":	changeSpeed(parameter);
							break;
			case "speed2":	setSpeed(parameter);
							break;
		}
	}
	
	public void stop() {
		this.running = false;
	}

	private void setX(int x) {
		this.x = x;
	}

	private void setY(int y) {
		this.y = y;
	}

	private void turn(int deltaAngle) {
		if (deltaAngle > 0) {
			
		} else if (deltaAngle < 0) {
			
		}
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

}
