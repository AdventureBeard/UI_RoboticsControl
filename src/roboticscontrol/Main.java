/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

/**
 *
 * @author braden
 */
public class Main {

	
	public static void main(String[] args) {
		
		final MessageService ms = new MessageService();
		(new Thread(new UserInterface(ms))).start();
		(new Thread(new Robot(ms))).start();
		
	}

	
}
