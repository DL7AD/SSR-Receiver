/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import decoder.Airspace;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jssc.*;
import map.Point;

public class Preferences extends javax.swing.JFrame {
    
    private static Preferences instance;
    
    //Position
    private DefaultComboBoxModel comboBox;
    private boolean gpsThreadAlvie; //GPS Thread
    private boolean gpsReceiving; //Status of GPS Receiving
    private boolean gpsTransmitting; //Status of GPS Transmitting to Template
    
    //Current saved settings
    
    private boolean savedFixPositionSet;
    
    private String savedSquitterFolder;
    private int savedOutputStreamPort = 33001;
    
    private short savedSquittersPerSecond = 100;
    
    public static Preferences getInstance() {
        return Preferences.getInstance(false);
    }
    public static Preferences getInstance(boolean openWindow) {
        if(Preferences.instance == null)
            Preferences.instance = new Preferences();
        
        if(openWindow) //Data request will not reset set data
            Preferences.instance.setPreferences();
        
        return Preferences.instance;
    }
    
    private void setPreferences() {
        //Get settings for Map
        
        //Get settings for Position
        Point position = Airspace.getMyPosition();
        if(position != null) {
            double latitude = position.getLatitude();
            double longitude = position.getLongitude();
            
            boolean ns = latitude > 0;
            int latDegree = (int)Math.round(Math.abs(latitude));
            double latMinute = (double)(Math.round((Math.abs(latitude) - latDegree) * 6000) / 100d);
            
            boolean we = latitude > 0;
            int lonDegree = (int)Math.round(Math.abs(longitude));
            double lonMinute = (double)(Math.round((Math.abs(longitude) - lonDegree) * 6000) / 100d);
            
            this.latDegree.setText(Integer.toString(latDegree));
            this.latMinute.setText(Double.toString(latMinute));
            this.lonDegree.setText(Integer.toString(lonDegree));
            this.lonMinute.setText(Double.toString(lonMinute));
            this.north.setSelected(ns);
            this.south.setSelected(!ns);
            this.east.setSelected(we);
            this.west.setSelected(!we);
        }
        this.fixPositionSet.setSelected(this.savedFixPositionSet);
        if(this.savedFixPositionSet) {
            this.activateFixPositionForm();
        } else {
            this.activateGPSPositionForm();
        }
        
        //Get settings for serial
        this.folderName.setText(this.savedSquitterFolder);
        this.outputStreamPort.setText(Integer.toString(this.savedOutputStreamPort));
        
        //Get settings for File
        this.squittersPerSecond.setText(Short.toString(this.savedSquittersPerSecond));
    }
    
    private Preferences() {
        //Init components
        initComponents();
        
        //Set position
        Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        Dimension windowSize = new Dimension(getPreferredSize());
        this.setLocation(screenSize.width / 2 - windowSize.width / 2, screenSize.height / 2 - windowSize.height / 2);
        
        //Task Bar Icon
        Image taskBarIcon = new ImageIcon(this.getClass().getResource("/gui/taskbaricon.png")).getImage();
        this.setIconImage(taskBarIcon);
    }
    private void stopGPS() {
        //Disable and enable Buttons
        this.startGPS.setEnabled(true);
        this.stopGPS.setEnabled(false);
        
        //Stop GPS Receiving Thread
        this.gpsThreadAlvie = false;
        try {
            Thread.sleep(100);
        } catch(InterruptedException e) {}
    }
    private void activateGPSPositionForm() {
        this.north.setEnabled(false);
        this.south.setEnabled(false);
        this.west.setEnabled(false);
        this.east.setEnabled(false);
        this.latDegree.setEnabled(false);
        this.lonDegree.setEnabled(false);
        this.latMinute.setEnabled(false);
        this.lonMinute.setEnabled(false);
        this.startGPS.setEnabled(true);
        this.comDropBox.setEnabled(true);
    }
    private void activateFixPositionForm() {
        this.comDropBox.setEnabled(false);
        this.startGPS.setEnabled(false);
        this.stopGPS.setEnabled(false);
        this.north.setEnabled(true);
        this.south.setEnabled(true);
        this.west.setEnabled(true);
        this.east.setEnabled(true);
        this.latDegree.setEnabled(true);
        this.lonDegree.setEnabled(true);
        this.latMinute.setEnabled(true);
        this.lonMinute.setEnabled(true);
    }
    private void resetGPSDataFields() {
        //Reset this display
        this.time.setText("-");
        this.latitude.setText("-");
        this.longitude.setText("-");
        this.satellites.setText("0");
        this.altitude.setText("-");
        this.deviation.setText("-");
        
        //Reset Template display
        Receiver.getInstance().resetPosition();
        
        //Reset Airspace position
        Airspace.setMyPosition(null);
    }
    private void startGPSReceiving() {
        
        //Create a new serial port connection
        SerialPort serial = new SerialPort((String)this.comboBox.getSelectedItem());
        
        try {
            
            //Open port
            serial.openPort();
            
            //Set Serial Port Configuration
            serial.setParams(
                SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
            );
            
            String buffer = new String();
            
            //Main Loop for SSR receiving (Main SSR Decoding starts here)
            while(this.gpsThreadAlvie) {

                //Read buffer while bytes are available in buffer
                while(serial.getInputBufferBytesCount() > 0) {

                    //Read Serial Buffer until there is a break or no buffer
                    byte c;
                    do {
                        c = serial.readBytes(1)[0];
                        if(c != 13 && c != 10)
                            buffer += (char)c;
                    } while(c != 13 && c != 10 && serial.getInputBufferBytesCount() > 0);

                    //Packet does not contain any data or packet is not received fully
                    //If Squitter is not received fully, it will be filled up in the next loop
                    if(buffer.equals("") || (c != 10 && c != 13))
                        continue;

                    //Decode GPS data
                    this.decodeNMEA(buffer);

                    //Reset CommResponse
                    buffer = "";
                }

                //Block Thread that CPU is not used 100 percent
                if(serial.getInputBufferBytesCount() == 0) {
                    try {
                        Thread.sleep(50); //Block Thread
                    } catch(InterruptedException e) { }
                }
            }

            //Close Serial Port
            serial.closePort();

            //Reset all GPS Data fields
            this.resetGPSDataFields();
        } catch(SerialPortException e) {
            if(e.getExceptionType().equals("Port busy")) { //Port is already in use
                JOptionPane.showMessageDialog(null, "Port " + serial.getPortName() + " is already used by another programm.", "COM Port Error", JOptionPane.INFORMATION_MESSAGE);
            } else if(e.getExceptionType().equals("Port not found")) { //Port not found
                JOptionPane.showMessageDialog(null, "Port " + serial.getPortName() + " is not existing.", "COM Port Error", JOptionPane.INFORMATION_MESSAGE);
            } else { //General error
                JOptionPane.showMessageDialog(null, "A general error occured when opening Port " + serial.getPortName() + ". (ERROR_MSG=" + e.getExceptionType() + ")", "COM Port Error", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    private void decodeNMEA(String nmea) {
        if(!nmea.substring(0, 6).equals("$GPGGA"))
            return;
        
        try {
            String[] nmeaS = nmea.split(",");
            
            //Time
            this.time.setText(nmeaS[1].substring(0, 2) + ":" + nmeaS[1].substring(2, 4) + ":" + nmeaS[1].substring(4, 6) + ":" + nmeaS[1].substring(7));
            
            //Satellites
            Byte satellites = Byte.parseByte(nmeaS[7]);
            this.satellites.setText(satellites.toString());
            
            if(this.gpsReceiving && Integer.parseInt(nmeaS[6]) > 0) {
                //Latitude
                String ns = nmeaS[3];
                String latDegree = nmeaS[2].substring(0, 2);
                String latMinute = nmeaS[2].substring(2, 9);
                this.latitude.setText(ns + latDegree + "° " + latMinute + "'");
                
                //Longitude
                String we = nmeaS[5];
                String lonDegree = nmeaS[4].substring(0, 3);
                String lonMinute = nmeaS[4].substring(3, 10);
                this.longitude.setText(we + lonDegree + "° " + lonMinute + "'");
                
                //Deviation
                this.deviation.setText(new Double(Double.parseDouble(nmeaS[8])).toString() + " " + nmea.split(",")[10].toLowerCase());

                //Altitude
                String altitude = new Double(Double.parseDouble(nmeaS[9])) + " " + nmea.split(",")[10].toLowerCase();
                this.altitude.setText(altitude);
                
                if(this.gpsTransmitting && !lonDegree.equals("")) { //Transmit GPS Position
                    Receiver.getInstance().setPosition(
                        ns.toLowerCase().equals("w"),
                        latDegree,
                        latMinute,
                        we.toLowerCase().equals("s"),
                        lonDegree,
                        lonMinute,
                        altitude,
                        satellites
                    );
                    double latitude = (ns.toLowerCase().equals("s") ? -1 : 1) * (Short.parseShort(latDegree) + Double.parseDouble(latMinute) / 60d);
                    double longitude = (we.toLowerCase().equals("w") ? -1 : 1) * (Short.parseShort(lonDegree) + Double.parseDouble(lonMinute) / 60d);
                    Airspace.setMyPosition(new Point(latitude, longitude));
                }
            } else {
                this.latitude.setText("-");
                this.longitude.setText("-");
                this.deviation.setText("-");
                this.altitude.setText("-");
            }
            
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(StringIndexOutOfBoundsException e) {
        } catch(NumberFormatException e) {}
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        we = new javax.swing.ButtonGroup();
        ns = new javax.swing.ButtonGroup();
        TabbedPane = new javax.swing.JTabbedPane();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        squittersPerSecond = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        outputStreamPort = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        folderName = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        changeFolder = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        longitude = new javax.swing.JLabel();
        latitude = new javax.swing.JLabel();
        deviation = new javax.swing.JLabel();
        altitude = new javax.swing.JLabel();
        startGPS = new javax.swing.JButton();
        satellites = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        time = new javax.swing.JLabel();
        stopGPS = new javax.swing.JButton();
        comDropBox = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        fixPositionSet = new javax.swing.JCheckBox();
        jLabel32 = new javax.swing.JLabel();
        north = new javax.swing.JRadioButton();
        south = new javax.swing.JRadioButton();
        west = new javax.swing.JRadioButton();
        east = new javax.swing.JRadioButton();
        lonDegree = new javax.swing.JTextField();
        latDegree = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        latMinute = new javax.swing.JTextField();
        lonMinute = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cancel = new javax.swing.JButton();
        save = new javax.swing.JButton();

        setTitle("Preferences");
        setResizable(false);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("File"));

        jLabel17.setText("Replay speed:");

        jLabel18.setText("Play");

        jLabel19.setText("Squitters every Second");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 154, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(squittersPerSecond, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(squittersPerSecond, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel18))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(367, Short.MAX_VALUE))
        );

        TabbedPane.addTab("Input", jPanel12);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Network"));

        jLabel14.setText("Server Port:");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(outputStreamPort, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(outputStreamPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("File"));

        folderName.setEditable(false);
        folderName.setEnabled(false);

        jLabel13.setText("Log folder:");

        changeFolder.setText("change");
        changeFolder.setMargin(new java.awt.Insets(2, 10, 2, 10));
        changeFolder.setMaximumSize(new java.awt.Dimension(43, 23));
        changeFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeFolderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(folderName, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(changeFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(folderName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(307, Short.MAX_VALUE))
        );

        TabbedPane.addTab("Output", jPanel1);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("GPS Signal"));

        jLabel1.setText("Latitude:");

        jLabel2.setText("Longitude:");

        jLabel3.setText("Altitude:");

        jLabel4.setText("Deviation:");

        jLabel5.setText("Satellites:");

        longitude.setText("-");

        latitude.setText("-");

        deviation.setText("-");

        altitude.setText("-");

        startGPS.setText("Start Receving");
        startGPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startGPSActionPerformed(evt);
            }
        });

        satellites.setText("0");

        jLabel6.setText("Time:");

        time.setText("-");

        stopGPS.setText("Stop Receiving");
        stopGPS.setEnabled(false);
        stopGPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopGPSActionPerformed(evt);
            }
        });

        comDropBox.setMaximumRowCount(25);
        this.comboBox = new javax.swing.DefaultComboBoxModel(SerialPortList.getPortNames());
        comDropBox.setModel(this.comboBox);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel6))
                                .addGap(14, 14, 14)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(time, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                    .addComponent(latitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(longitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(altitude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(satellites, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deviation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(comDropBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startGPS)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopGPS)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startGPS)
                    .addComponent(stopGPS)
                    .addComponent(comDropBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(time)
                        .addComponent(jLabel5)
                        .addComponent(satellites)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(latitude)
                        .addComponent(jLabel3)
                        .addComponent(altitude)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(longitude)
                    .addComponent(jLabel4)
                    .addComponent(deviation))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Position Settings"));

        fixPositionSet.setText("Use fix Position Settings");
        fixPositionSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixPositionSetActionPerformed(evt);
            }
        });

        jLabel32.setText("Latitude:");

        ns.add(north);
        north.setText("N");
        north.setEnabled(false);

        ns.add(south);
        south.setText("S");
        south.setEnabled(false);

        we.add(west);
        west.setText("W");
        west.setEnabled(false);

        we.add(east);
        east.setText("E");
        east.setEnabled(false);

        lonDegree.setEnabled(false);

        latDegree.setEnabled(false);

        jLabel33.setText("Longitude:");

        jLabel7.setText("°");

        jLabel8.setText("°");

        latMinute.setEnabled(false);

        lonMinute.setEnabled(false);

        jLabel9.setText("'");

        jLabel10.setText("'");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fixPositionSet, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel33)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(west))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel32)
                                .addGap(18, 18, 18)
                                .addComponent(north)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(south)
                                .addGap(18, 18, 18)
                                .addComponent(latDegree, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(east)
                                .addGap(18, 18, 18)
                                .addComponent(lonDegree)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lonMinute))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(latMinute, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(178, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(fixPositionSet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(north)
                    .addComponent(south)
                    .addComponent(latDegree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(latMinute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(west)
                    .addComponent(east)
                    .addComponent(lonDegree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(lonMinute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(193, Short.MAX_VALUE))
        );

        TabbedPane.addTab("Position", jPanel2);

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        save.setText("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(save)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancel)
                .addContainerGap())
            .addComponent(TabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(TabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancel)
                    .addComponent(save))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        //Save Map setings
        
        //Save Position settings
        if(this.fixPositionSet.isSelected()) { //Fix Position
            try {
                Receiver.getInstance().setPosition(
                    this.south.isSelected(),
                    this.latDegree.getText(),
                    new Double (Math.round(Double.parseDouble(this.latMinute.getText()) * 100) / 100d).toString(),
                    this.west.isSelected(),
                    this.lonDegree.getText(),
                    new Double (Math.round(Double.parseDouble(this.lonMinute.getText()) * 100) / 100d).toString()
                );
                double latitude = (this.south.isSelected() ? -1 : 1) * (Short.parseShort(this.latDegree.getText()) + Math.round(Double.parseDouble(this.latMinute.getText()) * 10000) / 600000d);
                double longitude = (this.west.isSelected() ? -1 : 1) * (Short.parseShort(this.lonDegree.getText()) + Math.round(Double.parseDouble(this.lonMinute.getText()) * 10000) / 600000d);
                Airspace.setMyPosition(new Point(latitude, longitude));
            } catch(NumberFormatException e) { //Keine oder falsche Positionsdaten eingegeben
                this.resetGPSDataFields();
            }
        } else { //GPS Position
            if(this.gpsReceiving)
                this.gpsTransmitting = true;
        }
        this.savedFixPositionSet = this.fixPositionSet.isSelected();
        
        //Save serial settings
        this.savedSquitterFolder = this.folderName.getText();
        this.savedOutputStreamPort = Integer.parseInt(this.outputStreamPort.getText());
        
        //Save File settings
        this.savedSquittersPerSecond = Short.parseShort(this.squittersPerSecond.getText());
        
        this.setVisible(false);
    }//GEN-LAST:event_saveActionPerformed

    private void fixPositionSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixPositionSetActionPerformed
        //Reset data fields

        if(this.fixPositionSet.isSelected()) {
            this.stopGPS(); //Kill GPS Thread, GPS Displaying and GPS Transmitting
            this.activateFixPositionForm(); //Activate Fix Position Form
        } else {
            this.activateGPSPositionForm(); //Activate GPS Position Form
        }
    }//GEN-LAST:event_fixPositionSetActionPerformed

    private void stopGPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopGPSActionPerformed
        //Kill GPS Thread, GPS Displaying and GPS Transmitting
        this.stopGPS();

        //Reset data Fields
        Receiver.getInstance().resetPosition();
    }//GEN-LAST:event_stopGPSActionPerformed

    private void startGPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startGPSActionPerformed
        //Disable and enable Buttons
        this.startGPS.setEnabled(false);
        this.stopGPS.setEnabled(true);

        //Display GPS Position and disable transmitting position to Template
        this.gpsReceiving = true;
        this.gpsTransmitting = false;

        //Start new Thread displaying GPS Position
        this.gpsThreadAlvie = true;
        final Preferences t = this;
        new Thread() {public void run() {
            t.startGPSReceiving();
        }}.start();
    }//GEN-LAST:event_startGPSActionPerformed

    private void changeFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeFolderActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String folderName = fileChooser.getSelectedFile().getAbsolutePath();
            this.folderName.setText(folderName);
        }
    }//GEN-LAST:event_changeFolderActionPerformed

    //Getter of saved data
    public String getSquitterFolder() { return this.savedSquitterFolder; }
    public int getOutputStreamPort() { return this.savedOutputStreamPort; }
    public short getSquittersPerSecond() { return this.savedSquittersPerSecond; }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane TabbedPane;
    private javax.swing.JLabel altitude;
    private javax.swing.JButton cancel;
    private javax.swing.JButton changeFolder;
    private javax.swing.JComboBox comDropBox;
    private javax.swing.JLabel deviation;
    private javax.swing.JRadioButton east;
    private javax.swing.JCheckBox fixPositionSet;
    private javax.swing.JTextField folderName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTextField latDegree;
    private javax.swing.JTextField latMinute;
    private javax.swing.JLabel latitude;
    private javax.swing.JTextField lonDegree;
    private javax.swing.JTextField lonMinute;
    private javax.swing.JLabel longitude;
    private javax.swing.JRadioButton north;
    private javax.swing.ButtonGroup ns;
    private javax.swing.JTextField outputStreamPort;
    private javax.swing.JLabel satellites;
    private javax.swing.JButton save;
    private javax.swing.JRadioButton south;
    private javax.swing.JTextField squittersPerSecond;
    private javax.swing.JButton startGPS;
    private javax.swing.JButton stopGPS;
    private javax.swing.JLabel time;
    private javax.swing.ButtonGroup we;
    private javax.swing.JRadioButton west;
    // End of variables declaration//GEN-END:variables
}
