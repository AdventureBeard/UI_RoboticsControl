/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author braden
 */
public class MapPanel extends JPanel {

	File imgSrc;
	BufferedImage img;
	int robotX;
	int robotY;

	public MapPanel() {
		robotX = 150;
		robotY = 150;
		this.imgSrc = new File("robot.png");
		try {
			img = ImageIO.read(imgSrc);
		} catch (IOException ex) {
			Logger.getLogger(MapPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}	
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(img, robotX, robotY, this);
		repaint();	
	}

}
