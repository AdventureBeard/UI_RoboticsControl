/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;

/**
 *
 * @author braden
 */
public class ArmAnglePanel extends JPanel {

	double angle;

	/** Here we override the paintComponent function so we can make our own custom graphics. In 
	 * this we draw a rectangle to represent the arm angle, and use the last given angle to
	 * create an AffineTransform that provides us with the proper transform to apply to our 
	 * Graphics2D object.
	 * @param g  A graphics object.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform transform = new AffineTransform();
		transform.setToRotation(Math.toRadians(angle + 90), this.getWidth()/2 + 20, this.getHeight()/2 +20);	
		g2d.setTransform(transform);
		g2d.setPaint(Color.red);
		g2d.fillRect(this.getWidth()/2 + 20, this.getHeight()/2 + 20, 5, this.getHeight()/2);

	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

}
