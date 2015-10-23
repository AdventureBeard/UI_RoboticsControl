/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.io.IOException;

/**
 *
 * @author braden
 */
public class Main {

	
	public static void main(String[] args) throws IOException {
		
		/** This program runs two threads, one for the UI and one for the robot. The 
		 * UI is completely event-driven from the robot, meaning the UI doesn't change
		 * unless the robot reports its status information. In that way, the UI is completely honest
		 * and will never show incorrect information.
		 * 
		 * The two threads communicate via a synchronized message service. I didn't need to do
		 * this but, but I thought it would be cool. It makes sense because in real life, the 
		 * robot would probably be far away and certainly wouldn't be sharing the same memory space
		 * as the remote control.  In this way, the two components are fully decoupled. It makes me want
		 * to make a real robot that could talk with this interface.
		 * 
		 * My multithreaded architecture is not very good; it eats up a lot of CPU cycles
		 * and needs much more synchronization, but I didn't make that a priority since this
		 * is a UI project.
		 * 
		 * The robot can be fully controlled by either the mouse or keyboard.
		 */
		 String current = new java.io.File( "." ).getCanonicalPath();
        System.out.println("Current dir:"+current);
		final MessageService ms = new MessageService();
		(new Thread(new UserInterface(ms))).start();
		(new Thread(new Robot(ms))).start();
		
	}

	
}
