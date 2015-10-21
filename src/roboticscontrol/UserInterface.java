/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.awt.Color;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author braden
 */
public class UserInterface extends javax.swing.JFrame implements Runnable {
	MessageService messageService;
	private boolean running;
	BufferedImage image; 
	
    public UserInterface(MessageService ms) {
		this.running = true;

		/** Connect to the messageService that bridges communication between the
		 * robot and the UI.
		 */
		this.messageService = ms;

		/**
		 * Load the robot image.
		 */
		try {
			this.image = ImageIO.read(new File("robot.png"));
		} catch (IOException ex) {
			Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		initComponents();

		/** NetBeans GUI builder doesn't like us manually placing objects in a layout. 
		 * Since we are using animations, it will be critical to manually place, so we will
		 * set the layout for the GPS panel to null.
		 */
		mapPanel.setLayout(null); 
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		
		/** Create a key event dispatcher for a custom global hotkey system.
		/*	This keeps us from having to add key listeners to every focusable 
			component in the interface.
		**/
		KeyEventDispatcher dispatcher = (KeyEvent event) -> {
			String[] s = event.paramString().split(",");
			String eventType = s[0];
			int keyCode = Integer.parseInt(s[1].substring(s[1].indexOf("=") + 1));
			// Parsing the parameter string to get the EventType and KeyCode.
			handleGlobalKeyboardEvent(eventType, keyCode);
			return true;
		};
		
		DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().
				addKeyEventDispatcher(dispatcher);
    }
	
	@Override
	public void run() {
		System.out.println("Interface is running.");
		messageService.sendToRobot("handshake:0");
		
		/** GPS TIMER
		 * 
		 * Pinging the robot for its GPS information every tick is a waste of resources;
		 * we set a new timer so that we only ping for a new GPS location every 50 ms.
		 */
		Timer t = new Timer(50, (ActionEvent e) -> {
			messageService.sendToRobot("gps:0");
		});
		t.start();

		/** Message Check
		 * 
		 * Improvements: Having the thread check for messages constantly is a waste of resources;
		 * What should happen instead is that the message service notifies the UI when it has 
		 * a new message. I am leaving going out of town soon so I am short on time, otherwise I would implement
		 * a better synchronization system.
		 */
		while (running) {
			String message = messageService.receiveFromRobot();
			if (message != null) {
				parseMessage(message);
			}
		}
	}
	


	/** parseMessage
	 * 	Method for converting a message from the robot into some action to modify
	 *  the UI state.
	 * @param m  a String containing an instruction in the format 'command:parameter'
	 */
	private void parseMessage(String m) {
		String[] message = m.split(":");
		String command = message[0];
		String parameter = message[1];
		switch(command) {
			case "handshake":			updateLog("HANDSHAKE SUCCESS: CONNECTION TO ROBOT ESTABLISHED");
										break;
			case "updateTemp":			updateTemp(parameter);
										break;
			case "updateSpeed":			updateSpeed(parameter);
										break;
			case "updateClawStatus":	updateClawStatus(parameter);
										break;
			case "updateCameraStatus":	updateCameraStatus(parameter);
										break;
			case "gps":					updateGPS(parameter);
										break;
			case "bounds":				updateLog("Robot reached bounds of operation area.");
										break;
			default:					break;
		}
	}
	
	
	/** updateLog
	 * Report messages from robot to the interface.
	 * @param s 	a String to be appended to the messageLog text area.
	 */
	private void updateLog(String s) {
		Date date = new Date();
		messageLog.append(date + ": " + s + "\n");
		messageLog.setCaretPosition(messageLog.getText().length());
	}

	/** updateGPS
	 * Accepts a String expecting GPS information in the format:
	 *  XCoordinate#YCoordinate#HeadingAngleInDegrees.
	 * 	Moves robot label element to the new X and Y positions, calculates a heading
	 * 	in degrees between 0 and 360, and then sends angle information to the rotateImage
	 * 	function to get new rotation transformation for the robot image.
	 * @param p 
	 */
	private void updateGPS(String reportedPosition) {
		String[] args = reportedPosition.split("#");
		double x = Double.parseDouble(args[0]);
		double y = Double.parseDouble(args[1]);
		double h = Double.parseDouble(args[2]);

		int posX = (int) Math.round(x);		// Round and convert to integers for UI display purposes.
		int posY = (int) Math.round(y);
		int heading = (int) Math.round(h);

		heading = -1 * (heading - 360) % 360; // Map the heading between 0 and 360.

		xCoordinateValue.setText(String.valueOf(posX)); // Update the UI.
		yCoordinateValue.setText(String.valueOf(posY));
		headingValue.setText(String.valueOf(heading) + "°");

		BufferedImage rotated = rotateImage(image, Math.toRadians(heading)); // Rotate the image using heading.
		ImageIcon ic = new ImageIcon(rotated);
		robot.setIcon(ic);
		robot.setLocation(posX, posY);
	}

	/** updateTemp
	 * Updates the UI with the robot's last reported temperature reading.
	 * @param t 	The robot's last reported temperature.
	 */
	
	private void updateTemp(String t) {
		temperatureLabel.setText(t + " °F");
		updateLog("Received new temperature reading from robot: " + t + " °F" );
	}
	
	/** updateSpeed
	 * Updates the UI according to the speed reported by the robot. 
	 * Sets all speed buttons to the default background, and then highlights in
	 * red the speed button that corresponds to the robots new current speed.
	 * @param speed 	The robot's last reported speed.
	 */
	private void updateSpeed(String speed) {
		int s = Integer.parseInt(speed);
		speed1Label.setBackground(new Color(238,238,238));
		speed2Label.setBackground(new Color(238,238,238));
		speed3Label.setBackground(new Color(238,238,238));
		speed0Label.setBackground(new Color(238,238,238));
		speedRevLabel.setBackground(new Color(238,238,238));

		if (s == 1) {
			speed1Label.setBackground(Color.red);
		} else if (s == 2) {
			speed2Label.setBackground(Color.red);
		} else if (s == 3) {
			speed3Label.setBackground(Color.red);
		} else if (s == -1) {
			speedRevLabel.setBackground(Color.red);
		} else if (s == 0) {
			speed0Label.setBackground(Color.red);
		}
		updateLog("Robot set speed to " + s );
	}


	/** updateClawStatus
	 * Updates the claw icon according to the status of the claw.
	 * @param status A string containing the clawOn or clawOff parameter represented
	 * as a 1 or a 0.
	 */
	private void updateClawStatus(String status) {
		int s = Integer.parseInt(status);
		if (s == 1) {
			clawButton.setIcon(new ImageIcon("Claw-Closed.png"));
			updateLog("Robot set claw to engaged");
		} else {
			clawButton.setIcon(new ImageIcon("Claw-Open.png"));
			updateLog("Robot set claw to disengaged");
		}
	}

	private void updateCameraStatus(String status) {
		int s = Integer.parseInt(status);
		if (s == 1) {
			activateCameraDisplay();
			updateLog("Camera activated");
		} else {
			updateLog("Camera deactivated");
		}	
	}

	private void activateCameraDisplay() {
		JFrame f = new JFrame(); //creates jframe f

		/** Add listener for window close.
		 * Add a listener for closing the window so we can deactivate the camera.
		 */
		f.addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
				messageService.sendToRobot("camera:0");
                e.getWindow().dispose();
            }
        });
	
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //this is your screen size

		BufferedImage bi;
		ImageIcon img = new ImageIcon();
		try {
			bi = ImageIO.read(new File("terrain.jpg"));
			img = new ImageIcon(bi);
		} catch (IOException ex) {
			Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
		}

    	JLabel lbl = new JLabel((Icon) img); 
    	f.getContentPane().add(lbl); 
    	f.setSize(img.getIconWidth(), img.getIconHeight()); 
    	int x = (screenSize.width - f.getSize().width)/2; 
    	int y = (screenSize.height - f.getSize().height)/2;
    	f.setLocation(x, y); 
    	f.setVisible(true); 
	}

	/** setAngle
	 * Accepts a buffered image and an angle in radians and returns a new image
	 * set to that rotation.
	 * @param image		A BufferdImage to be rotated.
	 * @param angle		An angle in radians to rotate to.
	 * @return 			A rotated BufferedImage
	 */
	private BufferedImage rotateImage(BufferedImage image, double angle) {
		int locX = image.getWidth() / 2;
		int locY = image.getHeight() / 2;
		
		AffineTransform transform = new AffineTransform();

		transform.setToRotation(-angle + Math.PI/2, locX, locY); // A little bit of hacky math to get the rotation right.
   		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        	
		image = op.filter(image, null);
		return image;
	}
	
		/** handleGlobalKeyboardEvent
	 * 	Event handler for global keyboard shortcuts for robot functions.
	 * 
	 * @param eventType 	The type of event, such as "KEY_PRESSED" or "KEY_RELEASED"
	 * @param keyCode 		The code of the pressed key
	 */
	private void handleGlobalKeyboardEvent(String eventType, int keyCode) {
		
		if ("KEY_PRESSED".equals(eventType) && keyCode == 65) {
			messageService.sendToRobot("turn:-1");
			turnLeftButton.doClick();
			
		} else if ("KEY_RELEASED".equals(eventType) && keyCode == 65) {
			messageService.sendToRobot("turn:0");
			
		} else if ("KEY_PRESSED".equals(eventType) && keyCode == 68) {
			messageService.sendToRobot("turn:1");
			turnRightButton.doClick();
			
		} else if ("KEY_RELEASED".equals(eventType) && keyCode == 68) {
			messageService.sendToRobot("turn:0");
			
		} else if ("KEY_PRESSED".equals(eventType) && keyCode == 87) {
			messageService.sendToRobot("speed:1");
			
		} else if ("KEY_PRESSED".equals(eventType) && keyCode == 83) {
			messageService.sendToRobot("speed:-1");
			
		} else if ("KEY_PRESSED".equals(eventType) && keyCode == 32) {
			messageService.sendToRobot("claw:0");
			
		} else if ("KEY_PRESSED".equals(eventType) && keyCode == 84) {
			messageService.sendToRobot("temp:0");
			thermoButton.doClick();
			
		} else if ("KEY_PRESSED".equals(eventType) && keyCode == 67) {
			messageService.sendToRobot("camera:0");
			cameraButton.doClick();
			
		} else if ("KEY_PRESSED".equals(eventType) && keyCode == 88) {
			messageService.sendToRobot("speed2:0");
			stopButton.doClick();
		}
	} 
	

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actuatorControlsPanel = new javax.swing.JPanel();
        clawButton = new javax.swing.JButton();
        armAnglePanel = new javax.swing.JPanel();
        movementControlsPanel = new javax.swing.JPanel();
        turnLeftButton = new javax.swing.JButton();
        turnRightButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        speedPanel = new javax.swing.JPanel();
        speed3Label = new javax.swing.JLabel();
        speed2Label = new javax.swing.JLabel();
        speed1Label = new javax.swing.JLabel();
        speed0Label = new javax.swing.JLabel();
        speedRevLabel = new javax.swing.JLabel();
        decSpeedButton = new javax.swing.JButton();
        incSpeedButton = new javax.swing.JButton();
        displayPanel = new javax.swing.JPanel();
        gpsDisplayPanel = new javax.swing.JPanel();
        gpsDetailsPanel = new javax.swing.JPanel();
        yCoordinateLabel = new javax.swing.JLabel();
        xCoordinateLabel = new javax.swing.JLabel();
        headingLabel = new javax.swing.JLabel();
        xCoordinateValue = new javax.swing.JLabel();
        yCoordinateValue = new javax.swing.JLabel();
        headingValue = new javax.swing.JLabel();
        mapPanel = new javax.swing.JPanel();
        robot = new javax.swing.JLabel();
        sensorControlsPanel = new javax.swing.JPanel();
        cameraButton = new javax.swing.JButton();
        thermoButton = new javax.swing.JButton();
        temperatureLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        messageLog = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        actuatorControlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Actuator Controls"));

        clawButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/roboticscontrol/res/Claw-Open.png"))); // NOI18N
        clawButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                clawButtonMousePressed(evt);
            }
        });

        armAnglePanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout armAnglePanelLayout = new javax.swing.GroupLayout(armAnglePanel);
        armAnglePanel.setLayout(armAnglePanelLayout);
        armAnglePanelLayout.setHorizontalGroup(
            armAnglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        armAnglePanelLayout.setVerticalGroup(
            armAnglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 147, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout actuatorControlsPanelLayout = new javax.swing.GroupLayout(actuatorControlsPanel);
        actuatorControlsPanel.setLayout(actuatorControlsPanelLayout);
        actuatorControlsPanelLayout.setHorizontalGroup(
            actuatorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actuatorControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(armAnglePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(actuatorControlsPanelLayout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(clawButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actuatorControlsPanelLayout.setVerticalGroup(
            actuatorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, actuatorControlsPanelLayout.createSequentialGroup()
                .addComponent(armAnglePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clawButton)
                .addContainerGap())
        );

        movementControlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Movement Controls"));

        turnLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/roboticscontrol/res/ArrowLeft.png"))); // NOI18N
        turnLeftButton.setBorderPainted(false);
        turnLeftButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                turnLeftButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                turnLeftButtonMouseReleased(evt);
            }
        });

        turnRightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/roboticscontrol/res/ArrowRight.png"))); // NOI18N
        turnRightButton.setBorderPainted(false);
        turnRightButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                turnRightButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                turnRightButtonMouseReleased(evt);
            }
        });
        turnRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                turnRightButtonActionPerformed(evt);
            }
        });

        stopButton.setBackground(new java.awt.Color(255, 102, 102));
        stopButton.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        stopButton.setText("Stop (X)");
        stopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                stopButtonMousePressed(evt);
            }
        });
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        speed3Label.setFont(new java.awt.Font("Lucida Grande", 0, 34)); // NOI18N
        speed3Label.setForeground(new java.awt.Color(0, 102, 204));
        speed3Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        speed3Label.setText("▶▶▶");
        speed3Label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        speed3Label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speed3LabelMousePressed(evt);
            }
        });

        speed2Label.setFont(new java.awt.Font("Lucida Grande", 0, 34)); // NOI18N
        speed2Label.setForeground(new java.awt.Color(0, 204, 204));
        speed2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        speed2Label.setText("▶▶");
        speed2Label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        speed2Label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speed2LabelMousePressed(evt);
            }
        });

        speed1Label.setFont(new java.awt.Font("Lucida Grande", 0, 34)); // NOI18N
        speed1Label.setForeground(new java.awt.Color(153, 204, 255));
        speed1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        speed1Label.setText("▶");
        speed1Label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        speed1Label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speed1LabelMousePressed(evt);
            }
        });

        speed0Label.setFont(new java.awt.Font("Lucida Grande", 0, 48)); // NOI18N
        speed0Label.setForeground(new java.awt.Color(204, 204, 204));
        speed0Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        speed0Label.setText("-");
        speed0Label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        speed0Label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speed0LabelMousePressed(evt);
            }
        });

        speedRevLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        speedRevLabel.setForeground(new java.awt.Color(153, 204, 255));
        speedRevLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        speedRevLabel.setText("REV");
        speedRevLabel.setToolTipText("");
        speedRevLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        speedRevLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speedRevLabelMousePressed(evt);
            }
        });

        javax.swing.GroupLayout speedPanelLayout = new javax.swing.GroupLayout(speedPanel);
        speedPanel.setLayout(speedPanelLayout);
        speedPanelLayout.setHorizontalGroup(
            speedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(speedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(speedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(speed3Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speed2Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speed1Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speed0Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speedRevLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        speedPanelLayout.setVerticalGroup(
            speedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(speedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(speed3Label, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speed2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speed1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speed0Label, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(speedRevLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        decSpeedButton.setText("Decrease Speed (S)");
        decSpeedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                decSpeedButtonMousePressed(evt);
            }
        });

        incSpeedButton.setText("Increase Speed (W)");
        incSpeedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                incSpeedButtonMousePressed(evt);
            }
        });

        javax.swing.GroupLayout movementControlsPanelLayout = new javax.swing.GroupLayout(movementControlsPanel);
        movementControlsPanel.setLayout(movementControlsPanelLayout);
        movementControlsPanelLayout.setHorizontalGroup(
            movementControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(movementControlsPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(movementControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(movementControlsPanelLayout.createSequentialGroup()
                        .addComponent(turnLeftButton)
                        .addGap(18, 18, 18)
                        .addComponent(turnRightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(movementControlsPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(movementControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(incSpeedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(decSpeedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(speedPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        movementControlsPanelLayout.setVerticalGroup(
            movementControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(movementControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(movementControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(turnLeftButton)
                    .addComponent(turnRightButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(incSpeedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(decSpeedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gpsDisplayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("GPS Map"));

        gpsDetailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("GPS Details"));

        yCoordinateLabel.setText("Y Coordinate:");

        xCoordinateLabel.setText("X Coordinate:");

        headingLabel.setText("Heading: ");

        xCoordinateValue.setText("0");

        yCoordinateValue.setText("0");

        headingValue.setText("0");

        javax.swing.GroupLayout gpsDetailsPanelLayout = new javax.swing.GroupLayout(gpsDetailsPanel);
        gpsDetailsPanel.setLayout(gpsDetailsPanelLayout);
        gpsDetailsPanelLayout.setHorizontalGroup(
            gpsDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gpsDetailsPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(xCoordinateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xCoordinateValue)
                .addGap(42, 42, 42)
                .addComponent(yCoordinateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yCoordinateValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addComponent(headingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headingValue)
                .addGap(27, 27, 27))
        );
        gpsDetailsPanelLayout.setVerticalGroup(
            gpsDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gpsDetailsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(gpsDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xCoordinateLabel)
                    .addComponent(yCoordinateLabel)
                    .addComponent(headingLabel)
                    .addComponent(xCoordinateValue)
                    .addComponent(yCoordinateValue)
                    .addComponent(headingValue))
                .addGap(11, 11, 11))
        );

        mapPanel.setBackground(new java.awt.Color(0, 153, 0));

        robot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/roboticscontrol/res/Robot.png"))); // NOI18N

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mapPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(robot)
                .addGap(195, 195, 195))
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mapPanelLayout.createSequentialGroup()
                .addGap(174, 174, 174)
                .addComponent(robot)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gpsDisplayPanelLayout = new javax.swing.GroupLayout(gpsDisplayPanel);
        gpsDisplayPanel.setLayout(gpsDisplayPanelLayout);
        gpsDisplayPanelLayout.setHorizontalGroup(
            gpsDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gpsDisplayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gpsDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gpsDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        gpsDisplayPanelLayout.setVerticalGroup(
            gpsDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gpsDisplayPanelLayout.createSequentialGroup()
                .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gpsDetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout displayPanelLayout = new javax.swing.GroupLayout(displayPanel);
        displayPanel.setLayout(displayPanelLayout);
        displayPanelLayout.setHorizontalGroup(
            displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gpsDisplayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        displayPanelLayout.setVerticalGroup(
            displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gpsDisplayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        sensorControlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sensor Controls"));

        cameraButton.setText("(C)amera");
        cameraButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                cameraButtonMousePressed(evt);
            }
        });
        cameraButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cameraButtonActionPerformed(evt);
            }
        });

        thermoButton.setText("(T)hermometer");
        thermoButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                thermoButtonMousePressed(evt);
            }
        });

        temperatureLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        temperatureLabel.setText("72 °F");

        javax.swing.GroupLayout sensorControlsPanelLayout = new javax.swing.GroupLayout(sensorControlsPanel);
        sensorControlsPanel.setLayout(sensorControlsPanelLayout);
        sensorControlsPanelLayout.setHorizontalGroup(
            sensorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sensorControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sensorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cameraButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sensorControlsPanelLayout.createSequentialGroup()
                        .addComponent(thermoButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(temperatureLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        sensorControlsPanelLayout.setVerticalGroup(
            sensorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sensorControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cameraButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sensorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(thermoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(temperatureLabel))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        messageLog.setColumns(20);
        messageLog.setRows(5);
        messageLog.setBorder(null);
        jScrollPane2.setViewportView(messageLog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(movementControlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(displayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(actuatorControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sensorControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(3, 3, 3)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(displayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(actuatorControlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sensorControlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(movementControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void turnRightButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_turnRightButtonMousePressed
	    	messageService.sendToRobot("turn:1");
    }//GEN-LAST:event_turnRightButtonMousePressed

        private void turnLeftButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_turnLeftButtonMousePressed
	    	messageService.sendToRobot("turn:-1");
        }//GEN-LAST:event_turnLeftButtonMousePressed

        private void incSpeedButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_incSpeedButtonMousePressed
			messageService.sendToRobot("speed:1");
        }//GEN-LAST:event_incSpeedButtonMousePressed

        private void decSpeedButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_decSpeedButtonMousePressed
			messageService.sendToRobot("speed:-1");
        }//GEN-LAST:event_decSpeedButtonMousePressed

        private void stopButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stopButtonMousePressed
			messageService.sendToRobot("speed2:0");
        }//GEN-LAST:event_stopButtonMousePressed

        private void clawButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clawButtonMousePressed
            messageService.sendToRobot("claw:0");
        }//GEN-LAST:event_clawButtonMousePressed

        private void cameraButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cameraButtonMousePressed
            messageService.sendToRobot("camera:0");
        }//GEN-LAST:event_cameraButtonMousePressed

        private void thermoButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_thermoButtonMousePressed
            messageService.sendToRobot("temp:0");
        }//GEN-LAST:event_thermoButtonMousePressed

    private void turnRightButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_turnRightButtonMouseReleased
        messageService.sendToRobot("turn:0");
    }//GEN-LAST:event_turnRightButtonMouseReleased

    private void turnLeftButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_turnLeftButtonMouseReleased
        messageService.sendToRobot("turn:0");
    }//GEN-LAST:event_turnLeftButtonMouseReleased

    private void speed3LabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speed3LabelMousePressed
        messageService.sendToRobot("speed2:3");
    }//GEN-LAST:event_speed3LabelMousePressed

    private void speed2LabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speed2LabelMousePressed
        messageService.sendToRobot("speed2:2");
    }//GEN-LAST:event_speed2LabelMousePressed

    private void speed1LabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speed1LabelMousePressed
        messageService.sendToRobot("speed2:1");
    }//GEN-LAST:event_speed1LabelMousePressed

    private void speed0LabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speed0LabelMousePressed
        messageService.sendToRobot("speed2:0");
    }//GEN-LAST:event_speed0LabelMousePressed

    private void turnRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_turnRightButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_turnRightButtonActionPerformed

    private void cameraButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cameraButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cameraButtonActionPerformed

    private void speedRevLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speedRevLabelMousePressed
        messageService.sendToRobot("speed2:-1");
    }//GEN-LAST:event_speedRevLabelMousePressed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stopButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actuatorControlsPanel;
    private javax.swing.JPanel armAnglePanel;
    private javax.swing.JButton cameraButton;
    private javax.swing.JButton clawButton;
    private javax.swing.JButton decSpeedButton;
    private javax.swing.JPanel displayPanel;
    private javax.swing.JPanel gpsDetailsPanel;
    private javax.swing.JPanel gpsDisplayPanel;
    private javax.swing.JLabel headingLabel;
    private javax.swing.JLabel headingValue;
    private javax.swing.JButton incSpeedButton;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel mapPanel;
    private static javax.swing.JTextArea messageLog;
    private javax.swing.JPanel movementControlsPanel;
    private javax.swing.JLabel robot;
    private javax.swing.JPanel sensorControlsPanel;
    private javax.swing.JLabel speed0Label;
    private javax.swing.JLabel speed1Label;
    private javax.swing.JLabel speed2Label;
    private javax.swing.JLabel speed3Label;
    private javax.swing.JPanel speedPanel;
    private javax.swing.JLabel speedRevLabel;
    private javax.swing.JButton stopButton;
    private javax.swing.JLabel temperatureLabel;
    private javax.swing.JButton thermoButton;
    private javax.swing.JButton turnLeftButton;
    private javax.swing.JButton turnRightButton;
    private javax.swing.JLabel xCoordinateLabel;
    private javax.swing.JLabel xCoordinateValue;
    private javax.swing.JLabel yCoordinateLabel;
    private javax.swing.JLabel yCoordinateValue;
    // End of variables declaration//GEN-END:variables
}
