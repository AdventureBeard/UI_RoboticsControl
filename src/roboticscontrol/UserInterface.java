/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roboticscontrol;

import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.Timer;

/**
 *
 * @author braden
 */
public class UserInterface extends javax.swing.JFrame implements Runnable {
    /**
     * Creates new form UserInterface
     */
	
	MessageService messageService;
	private boolean running;
	
    public UserInterface(MessageService ms) {
		this.running = true;
		this.messageService = ms;
        
		initComponents();

		this.setVisible(true);
		
		
		/** Create a key event dispatcher for a custom global hotkey system.
		/*	This keeps us from having to add key listeners to every focusable 
			component in the interface.
		**/
		KeyEventDispatcher dispatcher = (KeyEvent event) -> {
			String[] s = event.paramString().split(",");
			String eventType = s[0];
			int keyCode = Integer.parseInt(s[1].substring(s[1].indexOf("=") + 1));
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

		while (running) {
			String message = messageService.receiveFromRobot();
			if (message != null) {
				System.out.println("UI: Received a message: " + message);
				parseMessage(message);
			}
			
			Timer t = new Timer(1000, (ActionEvent e) -> {
				messageService.sendToRobot("gps:0");
			});
		}
	}
	
	private void handleGlobalKeyboardEvent(String eventType, int keyCode) {
		System.out.println(keyCode);
		
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
			case "gps":					updateGPS(parameter);
										break;
			default:					break;
		}
	}
	
	
	
	private void updateLog(String s) {
		Date date = new Date();
		messageLog.append(date + ": " + s + "\n");
		messageLog.setCaretPosition(messageLog.getText().length());
	}
	
	private void updateGPS(String p) {
		String[] args = p.split("-");
		xCoordinateValue.setText(args[0]);
		yCoordinateValue.setText(args[1]);
		headingValue.setText(args[2]);
		
	}
	
	private void updateTemp(String t) {
		temperatureLabel.setText(t + " °F");
		updateLog("Received new temperature reading from robot: " + t + " °F" );
	}
	
	private void updateSpeed(String speed) {
		int s = Integer.parseInt(speed);
		if (s == -1) {
			updateLog("Robot maximum speed reached");
		} else if (s == -2) {
			updateLog("Robot is stationary");
		} else {
			speed1Label.setBackground(new Color(238,238,238));
			speed2Label.setBackground(new Color(238,238,238));
			speed3Label.setBackground(new Color(238,238,238));
			speed0Label.setBackground(new Color(238,238,238));
			if (s == 1) {
				speed1Label.setBackground(Color.red);
			} else if (s == 2) {
				speed2Label.setBackground(Color.red);
			} else if (s == 3) {
				speed3Label.setBackground(Color.red);
			} else {
				speed0Label.setBackground(Color.red);
			}
			updateLog("Robot set speed to " + s );
		}
	}
	
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
        decSpeedButton = new javax.swing.JButton();
        incSpeedButton = new javax.swing.JButton();
        displayPanel = new javax.swing.JPanel();
        gpsDisplayPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        yCoordinateLabel = new javax.swing.JLabel();
        xCoordinateLabel = new javax.swing.JLabel();
        headingLabel = new javax.swing.JLabel();
        xCoordinateValue = new javax.swing.JLabel();
        yCoordinateValue = new javax.swing.JLabel();
        headingValue = new javax.swing.JLabel();
        sensorControlsPanel = new javax.swing.JPanel();
        cameraButton = new javax.swing.JButton();
        thermoButton = new javax.swing.JButton();
        temperatureLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        messageLog = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        actuatorControlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Actuator Controls"));

        clawButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/roboticscontrol/Claw-Open.png"))); // NOI18N
        clawButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                clawButtonMousePressed(evt);
            }
        });

        armAnglePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout armAnglePanelLayout = new javax.swing.GroupLayout(armAnglePanel);
        armAnglePanel.setLayout(armAnglePanelLayout);
        armAnglePanelLayout.setHorizontalGroup(
            armAnglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 233, Short.MAX_VALUE)
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
                .addGap(68, 68, 68)
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

        turnLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/roboticscontrol/ArrowLeft.png"))); // NOI18N
        turnLeftButton.setBorderPainted(false);
        turnLeftButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                turnLeftButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                turnLeftButtonMouseReleased(evt);
            }
        });

        turnRightButton.setIcon(new javax.swing.ImageIcon("/Users/braden/Documents/ArrowRight.png")); // NOI18N
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
        stopButton.setFont(new java.awt.Font("Lucida Grande", 0, 48)); // NOI18N
        stopButton.setText("STOP");
        stopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                stopButtonMousePressed(evt);
            }
        });

        speedPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        speed3Label.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
        speed3Label.setForeground(new java.awt.Color(0, 102, 204));
        speed3Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        speed3Label.setText("▶▶▶");
        speed3Label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        speed3Label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speed3LabelMousePressed(evt);
            }
        });

        speed2Label.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
        speed2Label.setForeground(new java.awt.Color(0, 204, 204));
        speed2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        speed2Label.setText("▶▶");
        speed2Label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        speed2Label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speed2LabelMousePressed(evt);
            }
        });

        speed1Label.setFont(new java.awt.Font("Lucida Grande", 0, 36)); // NOI18N
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
                    .addComponent(speed0Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        speedPanelLayout.setVerticalGroup(
            speedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(speedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(speed3Label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speed2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speed1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speed0Label, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        decSpeedButton.setText("Decrease Speed");
        decSpeedButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                decSpeedButtonMousePressed(evt);
            }
        });

        incSpeedButton.setText("Increase Speed");
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
                .addGroup(movementControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, movementControlsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(turnLeftButton)
                        .addGap(18, 18, 18)
                        .addComponent(turnRightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(movementControlsPanelLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(movementControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(decSpeedButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(incSpeedButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(movementControlsPanelLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(speedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(29, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stopButton)
                .addContainerGap())
        );

        gpsDisplayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("GPS Map"));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("GPS Details"));

        yCoordinateLabel.setText("Y Coordinate:");

        xCoordinateLabel.setText("X Coordinate:");

        headingLabel.setText("Heading: ");

        xCoordinateValue.setText("0");

        yCoordinateValue.setText("0");

        headingValue.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(xCoordinateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xCoordinateValue)
                .addGap(69, 69, 69)
                .addComponent(yCoordinateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yCoordinateValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addComponent(headingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headingValue)
                .addGap(21, 21, 21))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xCoordinateLabel)
                    .addComponent(yCoordinateLabel)
                    .addComponent(headingLabel)
                    .addComponent(xCoordinateValue)
                    .addComponent(yCoordinateValue)
                    .addComponent(headingValue))
                .addGap(11, 11, 11))
        );

        javax.swing.GroupLayout gpsDisplayPanelLayout = new javax.swing.GroupLayout(gpsDisplayPanel);
        gpsDisplayPanel.setLayout(gpsDisplayPanelLayout);
        gpsDisplayPanelLayout.setHorizontalGroup(
            gpsDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gpsDisplayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        gpsDisplayPanelLayout.setVerticalGroup(
            gpsDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gpsDisplayPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        cameraButton.setText("Camera");
        cameraButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                cameraButtonMousePressed(evt);
            }
        });

        thermoButton.setText("Thermometer");
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
                        .addGap(17, 17, 17)
                        .addComponent(temperatureLabel)
                        .addGap(0, 29, Short.MAX_VALUE)))
                .addContainerGap())
        );
        sensorControlsPanelLayout.setVerticalGroup(
            sensorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sensorControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cameraButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sensorControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(thermoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(temperatureLabel))
                .addContainerGap(12, Short.MAX_VALUE))
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
                            .addComponent(sensorControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(actuatorControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sensorControlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(movementControlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actuatorControlsPanel;
    private javax.swing.JPanel armAnglePanel;
    private javax.swing.JButton cameraButton;
    private javax.swing.JButton clawButton;
    private javax.swing.JButton decSpeedButton;
    private javax.swing.JPanel displayPanel;
    private javax.swing.JPanel gpsDisplayPanel;
    private javax.swing.JLabel headingLabel;
    private javax.swing.JLabel headingValue;
    private javax.swing.JButton incSpeedButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private static javax.swing.JTextArea messageLog;
    private javax.swing.JPanel movementControlsPanel;
    private javax.swing.JPanel sensorControlsPanel;
    private javax.swing.JLabel speed0Label;
    private javax.swing.JLabel speed1Label;
    private javax.swing.JLabel speed2Label;
    private javax.swing.JLabel speed3Label;
    private javax.swing.JPanel speedPanel;
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
