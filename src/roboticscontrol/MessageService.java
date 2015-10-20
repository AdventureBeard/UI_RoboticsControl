/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author braden
 */
public class MessageService {
	private final Queue<String> toRobot;
	private final Queue<String> toUI;
	
	public MessageService() {
		this.toRobot = new LinkedList<>();
		this.toUI = new LinkedList<>();
	}
	
	public synchronized void sendToRobot(String s) {
		toRobot.add(s);
		//System.out.println("Message added to robot inbox");
	}
	
	public synchronized String receiveFromRobot() {
		if(!toUI.isEmpty()) {
			return toUI.remove();
		}
		return null;
	}
	
	public synchronized void sendToUI(String s) {
		toUI.add(s);
		//ystem.out.println("Message added to UI inbox");
	}
	
	public synchronized String receiveFromUI() {
		if (!toRobot.isEmpty()) {
			return toRobot.remove();
		}
		return null;
	}
}
