package gui;

import stream.FileInputStream;
import decoder.Aircraft;
import decoder.Airspace;
import stream.FileOutputStream;
import stream.NetworkOutputStream;
import stream.SerialInputStream;
import file.Import;
import file.OSMImport;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import javax.swing.table.DefaultTableModel;
import map.*;
import stream.NetworkInputStream;
import jssc.*;
import stream.InputStream;
import stream.OutputStream;

public class Receiver extends javax.swing.JFrame {
    
    private static Receiver instance; //Instance
    
    //Serial buttons
    private ArrayList<JMenuItem> serialButtons;
    
    public static void main(String args[]) {
        //Look and Feel
        try {
            for(javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
        
        //Display splash screen
        SplashScreen initScreen = new SplashScreen();
        initScreen.setVisible(true);
        
        //Delay
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {}
        
        Receiver receiver = Receiver.getInstance(); //Initialize new window
        Airspace.getInstance(); //Start Airspace
        
        initScreen.dispose(); //Close splash screen
        receiver.setVisible(true); //Set main window visible
    }
    public static Receiver getInstance() {
        if(Receiver.instance == null)
            Receiver.instance = new Receiver();
        return Receiver.instance;
    }
    
    /**
     * Initializes Components in the main window.
     */
    private Receiver() {
        this.initComponents();
        
        //Set position
        Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        Dimension windowSize = new Dimension(getPreferredSize());
        int wdwLeft = Math.min(100, (screenSize.width - windowSize.width));
        int wdwTop = Math.min(30, screenSize.height - windowSize.height);
        this.setLocation(wdwLeft, wdwTop);
        
        //Task Bar Icon
        Image taskBarIcon = new ImageIcon(this.getClass().getResource("/gui/taskbaricon.png")).getImage();
        this.setIconImage(taskBarIcon);
    }
    /**
     * Return the instance of the aircraft table in the main window.
     * @return Instance of aircraft table
     */
    public JTable getAircraftTable() {
        return this.aircraftTable;
    }
    
    public void setDataRate(float datarate) {
        this.dataRate.setText((Math.round(datarate * 100) / 100f) + " kbit/s");
    }
    public void setLineStep(int currLine, int maxLines) {
        this.dataRate.setText(currLine + "/ " + maxLines);
    }
    public void setInput(String input) {
        this.input.setText(input);
    }
    public void setPosition(boolean west, String latDegree, String latMinute, boolean south, String lonDegree, String lonMinute, String altitude, byte satellites) {
        this.latitudeField.setText((south ? "S" : "N") + this.numberFormat(latDegree, 2) + "° " + this.numberFormat(latMinute, 2) + "'");
        this.longitudeField.setText((west ? "W" : "E") + this.numberFormat(lonDegree, 3) + "° " + this.numberFormat(lonMinute, 2) + "'");
        this.altitudeField.setText(altitude);
        this.satellitesField.setText(new Byte(satellites).toString());
    }
    public void setPosition(boolean west, String latDegree, String latMinute, boolean south, String lonDegree, String lonMinute) {
        this.latitudeField.setText((south ? "S" : "N") + this.numberFormat(latDegree, 2) + "° " + this.numberFormat(latMinute, 2) + "'");
        this.longitudeField.setText((west ? "W" : "E") + this.numberFormat(lonDegree, 3) + "° " + this.numberFormat(lonMinute, 2) + "'");
        this.altitudeField.setText("-");
        this.satellitesField.setText("-");
    }
    /**
     * Adds Zeros as prefix at the beginning of a string
     * @param number Number that should be converted
     * @param prefixZero Numbers before decimal
     * @return Converted number
     */
    private String numberFormat(String number, int prefixZero) {
        while(number.split("\\.")[0].length() < prefixZero)
            number = "0" + number;
        return number;
    }
    public void resetPosition() {
        this.latitudeField.setText("-");
        this.longitudeField.setText("-");
        this.altitudeField.setText("-");
        this.satellitesField.setText("-");
    }
    public void setAircraftsCount(short uniqueAircrafts, short[] activeAircrafts) {
        int allActiveAircrafts = activeAircrafts[0] + activeAircrafts[1];
        this.aircraftsInRange.setText(new Integer(allActiveAircrafts).toString());
        this.aircraftsWithPosition.setText(new Short(activeAircrafts[1]).toString());
        this.uniqueAircrafts.setText(new Short(uniqueAircrafts).toString());
    }
    public void updateTransponderStatistics(short modeS, short adsb) {
        this.modeS.setText(String.valueOf(modeS));
        this.adsb.setText(String.valueOf(adsb));
    }
    public void updateNearestAircraft(Aircraft aircraft) {
        if(aircraft == null) {
            this.resetNearestAircraft();
            return;
        }
        
        //Distance and Bearing
        this.nearestDistance.setText(aircraft.calculateParabolicDistance() + " NM");
        this.nearestBearing.setText(aircraft.calculateBearing() + "°");
        
        //ICAO-Ident
        this.nearestICAOIdent.setText(Integer.toHexString(aircraft.getIcaoIdent()).toUpperCase());
        
        //Flight Ident
        if(aircraft.getFlightIdent() != null) {
            this.nearestCallsign.setText(aircraft.getFlightIdent());
        } else {
            this.nearestCallsign.setText("unknown");
        }
        
        //Altitude
        if(aircraft.getAltitude() != 0) {
            this.nearestAltitude.setText(Integer.toString(aircraft.getAltitude()) + " ft");
        } else {
            this.nearestAltitude.setText("unknown");
        }
        
        //Track
        if(aircraft.getTrack() != null) {
            this.nearestTrack.setText(Float.toString( Math.round(aircraft.getTrack()*10)/10f ) + "°");
        } else {
            this.nearestTrack.setText("unknown");
        }
        
        //Speed
        if(aircraft.getGroundspeed() != null) {
            this.nearestSpeed.setText(Float.toString( Math.round(aircraft.getGroundspeed()*10)/10f ) + " kt");
        } else {
            this.nearestSpeed.setText("unknown");
        }
        
        //Squawk
        if(aircraft.getSquawk() != null) {
            String squawk = Integer.toString(aircraft.getSquawk());
            while(squawk.length() < 4)
                squawk = "0" + squawk;
            this.nearestSquawk.setText(squawk);
        } else {
            this.nearestSquawk.setText("unknown");
        }
    }
    private void resetNearestAircraft() {
        this.nearestDistance.setText("-");
        this.nearestBearing.setText("-");
        this.nearestICAOIdent.setText("-");
        this.nearestAltitude.setText("-");
        this.nearestTrack.setText("-");
        this.nearestSpeed.setText("-");
        this.nearestSquawk.setText("-");
        this.nearestCallsign.setText("-");
    }
    
    public void setMaxDistance(double distance) {
        this.maxDistanceNM.setText((Math.round(distance * 10) / 10f) + " NM");
        this.maxDistanceKM.setText((Math.round(distance * 18.52) / 10f) + " km");
    }
    public void setActiveAircrafts(short uniqueAircrafts, short aircraftsInRange, short aircraftsWithPosition) {
        this.uniqueAircrafts.setText(String.valueOf(uniqueAircrafts));
        this.aircraftsInRange.setText(String.valueOf(aircraftsInRange));
        this.aircraftsWithPosition.setText(String.valueOf(aircraftsWithPosition));
    }
    public void resetGUI() {
        this.input.setText("None");
        this.dataRate.setText("0.0 kbit/s");
        this.maxDistanceNM.setText("N/A NM");
        this.maxDistanceKM.setText("N/A km");
        this.uniqueAircrafts.setText("0");
        this.aircraftsInRange.setText("0");
        this.aircraftsWithPosition.setText("0");
        this.modeS.setText("0");
        this.adsb.setText("0");
        this.resetNearestAircraft();
    }
    public void updateAircraft(Aircraft aircraft) {
        if(aircraft.getTablePosition() == -1) //Aircraft has no Row Position
            aircraft.setTablePosition(this.getNextTablePosition());
        
        //Get Row Position
        int row = aircraft.getTablePosition();
        
        //ICAO Ident
        this.aircraftTable.setValueAt(Integer.toHexString(aircraft.getIcaoIdent()).toUpperCase(), row, 0);
        
        //Flight Ident
        this.aircraftTable.setValueAt(aircraft.getFlightIdent(), row, 1);
        
        //Position
        Point position = aircraft.getPosition();
        if(position != null) {
            String latitude = new Float(Math.round(position.getLatitude() * 1000) / 1000f).toString();
            this.aircraftTable.setValueAt(latitude, row, 2);
            String longitude = new Float(Math.round(position.getLongitude() * 1000) / 1000f).toString();
            this.aircraftTable.setValueAt(longitude, row, 3);
        }

        //Altitude
        if(aircraft.getAltitude() != null)
            this.aircraftTable.setValueAt(aircraft.getAltitude() + " ft", row, 4);
        
        //Vertical Speed
        if(aircraft.getVerticalRate() != null)
            this.aircraftTable.setValueAt(aircraft.getVerticalRate() + " ft/m", row, 5);

        //Speed
        if(aircraft.getGroundspeed() != null)
            this.aircraftTable.setValueAt((Math.round(aircraft.getGroundspeed()*10)/10f) + " kt", row, 6);
        
        //Track
        if(aircraft.getTrack() != null)
            this.aircraftTable.setValueAt((Math.round(aircraft.getTrack()*10)/10f) + "°", row, 7);
        
        //Squawk
        if(aircraft.getSquawk() != null) {
            String squawk = Short.toString(aircraft.getSquawk());
            while(squawk.length() < 4)
                squawk = "0" + squawk;
            this.aircraftTable.setValueAt(squawk, row, 8);
        }
        
        //Distance & Bearing
        Float dist = aircraft.calculateParabolicDistance();
        if(dist != null) {
            this.aircraftTable.setValueAt(aircraft.calculateBearing() + "°", row, 9);
            this.aircraftTable.setValueAt(dist + " NM", row, 10);
        }

        //Receivers
        ArrayList<String> receiversArr = aircraft.getActiveReceivers();
        String receivers = "";
        for(String receiver: receiversArr) {
            if(!receivers.equals(""))
                receivers += "/ ";
            receivers += receiver;
        }
        this.aircraftTable.setValueAt(receivers, row, 11);

        //Last Packet
        this.aircraftTable.setValueAt(aircraft.getLastPacket(), row, 13);
    }
    private int getNextTablePosition() {
        int row = -1;
        try {
            while(this.aircraftTable.getValueAt(++row, 0) != null);
        } catch(ArrayIndexOutOfBoundsException e) {
            DefaultTableModel model = (DefaultTableModel) this.aircraftTable.getModel();
            model.addRow(new Object[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null});
            row = this.aircraftTable.getRowCount() - 1;
        }
        return row;
    }
    public void removeAircraftFromTable(Aircraft aircraft) {
        //Find Aircraft in table
        int row = -1;
        while(++row < this.aircraftTable.getRowCount() && (this.aircraftTable.getValueAt(row, 0) == null || Integer.parseInt(this.aircraftTable.getValueAt(row, 0).toString(), 16) != aircraft.getIcaoIdent()));
        
        if(row < this.aircraftTable.getRowCount()) { //Aircraft found
            //Remove Aircraft from table
            aircraft.resetTablePosition();
            for(int col = 0; col < this.aircraftTable.getColumnCount(); col++)
                this.aircraftTable.setValueAt(null, row, col);
        }
    }
    public void resetTable() {
        for(int row = 0; row < this.aircraftTable.getRowCount(); row++)
            for(int col = 0; col < this.aircraftTable.getColumnCount(); col++)
                this.aircraftTable.setValueAt(null, row, col);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nearestCallsign = new javax.swing.JLabel();
        nearestDistance = new javax.swing.JLabel();
        nearestAltitude = new javax.swing.JLabel();
        nearestTrack = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        nearestICAOIdent = new javax.swing.JLabel();
        nearestBearing = new javax.swing.JLabel();
        nearestSpeed = new javax.swing.JLabel();
        nearestSquawk = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        input = new javax.swing.JLabel();
        dataRate = new javax.swing.JLabel();
        maxDistanceNM = new javax.swing.JLabel();
        maxDistanceKM = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        modeA = new javax.swing.JLabel();
        modeS = new javax.swing.JLabel();
        adsb = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        aircraftTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        latitudeField = new javax.swing.JLabel();
        longitudeField = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        altitudeField = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        satellitesField = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        aircraftsWithPosition = new javax.swing.JLabel();
        aircraftsInRange = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        uniqueAircrafts = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        exitButton = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        menuInputStream = new javax.swing.JMenu();
        menuFile = new javax.swing.JMenu();
        loadFile = new javax.swing.JMenuItem();
        menuNetwork = new javax.swing.JMenu();
        startNetwork = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        menuSerial = new javax.swing.JMenu();
        menuRTL = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        stopProgress = new javax.swing.JMenuItem();
        menuOutputStream = new javax.swing.JMenu();
        logToFile = new javax.swing.JCheckBoxMenuItem();
        serverOnline = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenu9 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu10 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu11 = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SSR Receiver");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Nearest Aircraft (Linear calculation)"));

        jLabel1.setText("Callsign:");

        jLabel2.setText("Distance:");

        jLabel3.setText("Altitude:");

        jLabel4.setText("Track:");

        nearestCallsign.setText("-");

        nearestDistance.setText("-");

        nearestAltitude.setText("-");

        nearestTrack.setText("-");

        jLabel9.setText("Hex:");

        jLabel10.setText("Bearing:");

        jLabel11.setText("GS:");

        jLabel12.setText("Squawk:");

        nearestICAOIdent.setText("-");

        nearestBearing.setText("-");

        nearestSpeed.setText("-");

        nearestSquawk.setText("-");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(nearestAltitude, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(nearestDistance, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nearestCallsign, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nearestTrack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nearestICAOIdent, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nearestSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nearestBearing, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nearestSquawk, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nearestCallsign)
                    .addComponent(jLabel9)
                    .addComponent(nearestICAOIdent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(nearestDistance)
                    .addComponent(jLabel10)
                    .addComponent(nearestBearing))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nearestAltitude)
                    .addComponent(jLabel11)
                    .addComponent(nearestSpeed))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(nearestTrack)
                    .addComponent(jLabel12)
                    .addComponent(nearestSquawk)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Receiver/ File Status"));

        jLabel17.setText("Input:");

        jLabel18.setText("Rate/ Step:");

        jLabel19.setText("Max. Dist.:");

        input.setText("None");

        dataRate.setText("0.0 kbit/s");

        maxDistanceNM.setText("N/A NM");

        maxDistanceKM.setText("N/A km");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataRate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxDistanceNM, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                    .addComponent(maxDistanceKM, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(input, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(input))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(dataRate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(maxDistanceNM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(maxDistanceKM))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Transponder"));

        jLabel24.setText("Mode A:");

        jLabel25.setText("Mode S:");

        jLabel26.setText("ADS-B:");

        modeA.setText("N/A");

        modeS.setText("0");

        adsb.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(modeA)
                        .addGap(0, 11, Short.MAX_VALUE))
                    .addComponent(adsb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modeS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(modeA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(modeS))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(adsb))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        aircraftTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Hex", "Call", "Lat", "Lon", "Alt", "V/R", "GS", "TR", "Squawk", "Bearing", "Parabolic Dist", "Receivers", "Timeout", "Last Squitter"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        aircraftTable.setMaximumSize(new java.awt.Dimension(1000, 2000));
        aircraftTable.setMinimumSize(new java.awt.Dimension(1000, 1280));
        aircraftTable.setPreferredSize(new java.awt.Dimension(1000, 2000));
        aircraftTable.getTableHeader().setReorderingAllowed(false) ;
        aircraftTable.setRequestFocusEnabled(false);
        aircraftTable.getTableHeader().setReorderingAllowed(false);
        aircraftTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                aircraftTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(aircraftTable);
        aircraftTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        aircraftTable.getColumnModel().getColumn(1).setPreferredWidth(55);
        aircraftTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        aircraftTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        aircraftTable.getColumnModel().getColumn(4).setPreferredWidth(55);
        aircraftTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        aircraftTable.getColumnModel().getColumn(6).setPreferredWidth(55);
        aircraftTable.getColumnModel().getColumn(7).setPreferredWidth(45);
        aircraftTable.getColumnModel().getColumn(8).setPreferredWidth(50);
        aircraftTable.getColumnModel().getColumn(9).setPreferredWidth(50);
        aircraftTable.getColumnModel().getColumn(10).setPreferredWidth(80);
        aircraftTable.getColumnModel().getColumn(11).setPreferredWidth(146);
        aircraftTable.getColumnModel().getColumn(12).setPreferredWidth(55);
        aircraftTable.getColumnModel().getColumn(13).setPreferredWidth(190);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("My Position"));

        jLabel32.setText("Latitude:");

        jLabel33.setText("Longitude:");

        latitudeField.setText("-");

        longitudeField.setText("-");

        jLabel6.setText("Altitude:");

        altitudeField.setText("-");

        jLabel8.setText("Satellites:");

        satellitesField.setText("-");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel33)
                    .addComponent(jLabel32)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(satellitesField, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(longitudeField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(altitudeField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(latitudeField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(latitudeField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(longitudeField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(altitudeField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(satellitesField)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Aircrafts"));

        jLabel34.setText("Aicrafts in Range:");

        jLabel35.setText("Aicrafts with Pos.:");

        aircraftsWithPosition.setText("0");

        aircraftsInRange.setText("0");

        jLabel5.setText("All Aircrafts:");

        uniqueAircrafts.setText("0");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel35)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aircraftsWithPosition, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel34))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(aircraftsInRange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(uniqueAircrafts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(uniqueAircrafts))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(aircraftsInRange))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(aircraftsWithPosition))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        exitButton.setText("File");

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        exitButton.add(jMenuItem1);

        jMenuBar1.add(exitButton);

        menuInputStream.setText("Airspace Input");
        menuInputStream.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuInputStreamMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
        });

        menuFile.setText("File");

        loadFile.setText("Load...");
        loadFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFileActionPerformed(evt);
            }
        });
        menuFile.add(loadFile);

        menuInputStream.add(menuFile);

        menuNetwork.setText("Server");
        menuNetwork.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuNetworkMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
        });

        startNetwork.setText("Connect");
        startNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNetworkActionPerformed(evt);
            }
        });
        menuNetwork.add(startNetwork);
        menuNetwork.add(jSeparator5);

        menuInputStream.add(menuNetwork);
        menuInputStream.add(jSeparator2);

        menuSerial.setText("Serial");
        menuSerial.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuSerialMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
        });
        menuInputStream.add(menuSerial);

        menuRTL.setText("RTL");

        jMenuItem4.setText("Start");
        menuRTL.add(jMenuItem4);
        menuRTL.add(jSeparator6);

        jMenuItem11.setText("No devices connected");
        jMenuItem11.setEnabled(false);
        menuRTL.add(jMenuItem11);
        menuRTL.add(jSeparator7);

        jMenuItem5.setText("Stop");
        menuRTL.add(jMenuItem5);

        menuInputStream.add(menuRTL);
        menuInputStream.add(jSeparator1);

        stopProgress.setText("Stop");
        stopProgress.setEnabled(false);
        stopProgress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopProgressActionPerformed(evt);
            }
        });
        menuInputStream.add(stopProgress);

        jMenuBar1.add(menuInputStream);

        menuOutputStream.setText("Airspace Output");
        menuOutputStream.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuOutputStreamMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
        });

        logToFile.setText("Log to File");
        logToFile.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                logToFileItemStateChanged(evt);
            }
        });
        menuOutputStream.add(logToFile);

        serverOnline.setText("Server online");
        serverOnline.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                serverOnlineItemStateChanged(evt);
            }
        });
        menuOutputStream.add(serverOnline);
        menuOutputStream.add(jSeparator4);

        jMenuBar1.add(menuOutputStream);

        jMenu9.setText("Map");

        jMenuItem6.setText("Load Sector Map...");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem6);

        jMenuItem8.setText("Display OSM Map...");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem8);

        jMenuItem3.setText("Display...");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem3);

        jMenuBar1.add(jMenu9);

        jMenu1.setText("Aircraft");

        jMenuItem2.setText("Search...");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu10.setText("Squitters");

        jMenuItem7.setText("Display...");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem7);

        jMenuBar1.add(jMenu10);

        jMenu11.setText("Options");

        jMenuItem10.setText("Preferences...");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu11.add(jMenuItem10);

        jMenuBar1.add(jMenu11);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    
    private void loadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadFileActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getAbsolutePath();
            Airspace.getInstance().addInputStream(new FileInputStream(filename));
        }
    }//GEN-LAST:event_loadFileActionPerformed

    private void stopProgressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopProgressActionPerformed
        Airspace.getInstance().removeAllInputStreams();
    }//GEN-LAST:event_stopProgressActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Map map = Map.getInstance();
                map.setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int returnVal = fileChooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            
            //Delete old data
            ARTCC.resetAll();
            Airport.resetAll();
            Color.resetAll();
            Fix.resetAll();
            GeoLine.resetAll();
            NDB.resetAll();
            Runway.resetAll();
            VOR.resetAll();
            Airway.resetAll();
            
            //Load map
            for(int i = 0; i < files.length; i++)
                new Import(files[i].getAbsolutePath());
            
            //Open map
            Map map = Map.getInstance();
            map.repaintMapPanel();
            map.setVisible(true);
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SquitterViewer decoder = SquitterViewer.getInstance();
                decoder.setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Preferences.getInstance(true).setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        //Create Aircraft list
        ArrayList<String> aircrafts = new ArrayList<String>(Airspace.getInstance().getAllAircrafts().size());
        for(Aircraft aircraft: Airspace.getInstance().getAllAircrafts())
            aircrafts.add(
                Integer.toString(aircraft.getIcaoIdent(), 16).toUpperCase() +
                (aircraft.getFlightIdent() != null ? " " + aircraft.getFlightIdent() : "")
            );
        Collections.sort(aircrafts);

        //Show Option Dialog
        final String response = (String)JOptionPane.showInputDialog(
            this,
            "Choose Aircraft:",
            "Search Aircraft",
            JOptionPane.PLAIN_MESSAGE,
            null,
            aircrafts.toArray(),
            null
        );

        //Show Aircraft Detail Window
        if(response != null) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Airspace airspace = Airspace.getInstance();
                    Aircraft aircraft = airspace.getAircraft(Integer.parseInt(response.split(" ")[0], 16));
                    new AircraftDetails(aircraft).setVisible(true);
                }
            });
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void aircraftTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aircraftTableMouseClicked
        if(!evt.isAltDown())
            return;
        
        //Get indentification of airplane
        try {
            final int icaoIdent = Integer.parseInt((String)this.aircraftTable.getValueAt(this.aircraftTable.getSelectedRow(), 0), 16);
            
            //Show Aircraft Detail Window
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Airspace airspace = Airspace.getInstance();
                    Aircraft aircraft = airspace.getAircraft(icaoIdent);
                    new AircraftDetails(aircraft).setVisible(true);
                }
            });
        } catch(NumberFormatException e) {}
    }//GEN-LAST:event_aircraftTableMouseClicked

    private void startNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startNetworkActionPerformed
        //Create label
        JTextField host = new JTextField(30);
        JTextField port = new JTextField(5);
        JPanel panel = new JPanel(new GridLayout(0,1));
        JPanel adress = new JPanel();
        panel.add(new JLabel("Insert network adress: [host:port]"));
        panel.add(adress);
        adress.add(host);
        adress.add(new JLabel(":"));
        adress.add(port);
        
        //Set up option pane
        int result = JOptionPane.showConfirmDialog(null, panel, "Network Adress", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                //Start network
                NetworkInputStream stream = new NetworkInputStream(
                    host.getText(),
                    Integer.parseInt(port.getText())
                );
                Airspace.getInstance().addInputStream(stream);
                
            } catch(NumberFormatException e) { //Incorrect port typed
                JOptionPane.showMessageDialog(null, "Please enter a correct port.", "Network Error", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_startNetworkActionPerformed

    private void menuOutputStreamMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuOutputStreamMenuSelected
        //Remove old clients
        while(this.menuOutputStream.getItemCount() > 3)
            this.menuOutputStream.remove(3);
        
        int clientsConnected = 0;
        
        for(OutputStream stream: Airspace.getInstance().getOutputStreams())
            if(stream instanceof NetworkOutputStream) {
                for(String client: ((NetworkOutputStream)stream).getConnectedClients()) {
                    JMenuItem item = new JMenuItem(client);
                    item.setEnabled(false);
                    this.menuOutputStream.add(item);
                    clientsConnected++;
                }
            }
        
        if(clientsConnected == 0) {
            JMenuItem noClientsConnected = new JMenuItem("No clients connected");
            noClientsConnected.setEnabled(false);
            this.menuOutputStream.add(noClientsConnected);
        }
    }//GEN-LAST:event_menuOutputStreamMenuSelected

    private void menuInputStreamMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuInputStreamMenuSelected
        Airspace airspace = Airspace.getInstance();
        
        //Disable buttons to prevent having other input streams startet when
        //file input stream already runs
        boolean fileRuns = airspace.hasInputStreamGroupStreams(FileInputStream.class);
        this.menuRTL.setEnabled(!fileRuns);
        this.menuSerial.setEnabled(!fileRuns);
        this.menuNetwork.setEnabled(!fileRuns);
        this.menuFile.setEnabled(!airspace.hasInputStreams());
        
        //Stop button
        this.stopProgress.setEnabled(airspace.hasInputStreams());
    }//GEN-LAST:event_menuInputStreamMenuSelected

    private void menuNetworkMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuNetworkMenuSelected
        //Remove buttons
        while(this.menuNetwork.getItemCount() != 1)
            this.menuNetwork.remove(1);
        
        this.menuNetwork.add(new Separator());
        
        //Add new Network input streams
        for(InputStream stream: Airspace.getInstance().getInputStreams(NetworkInputStream.class)) {
            final String name = stream.getInputStreamName();
            JMenuItem item = new JMenuItem("Stop " + name + " (connected)");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Airspace.getInstance().removeInputStream(name);
                }
            });
            this.menuNetwork.add(item);
        }
        
        if(Airspace.getInstance().getInputStreams(NetworkInputStream.class).size() == 0) {
            JMenuItem noNetworkInputs = new JMenuItem("No server connected");
            noNetworkInputs.setEnabled(false);
            this.menuNetwork.add(noNetworkInputs);
        }
        
        this.menuNetwork.add(new Separator());
        
        //Add stop button
        JMenuItem stop = new JMenuItem("Stop");
        if(!Airspace.getInstance().hasInputStreamGroupStreams(NetworkInputStream.class))
            stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Airspace.getInstance().removeInputStreamGroup(NetworkInputStream.class);
            }
        });
        this.menuNetwork.add(stop);
    }//GEN-LAST:event_menuNetworkMenuSelected

    private void menuSerialMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuSerialMenuSelected
        //Serial input
        String[] ports = SerialPortList.getPortNames();
        
        //Remove buttons
        while(this.menuSerial.getItemCount() != 0)
            this.menuSerial.remove(0);
        
        //Add new buttons
        for(int i = 0; i < ports.length; i++) {
            final String port = ports[i];
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(Airspace.getInstance().hasInputStream(port) ? "Stop " + port : "Start " + port);
            
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(Airspace.getInstance().hasInputStream(port)) { //Stop serial input stream
                        Airspace.getInstance().removeInputStream(port);
                    } else { //Start serial input stream
                        Airspace.getInstance().addInputStream(new SerialInputStream(port));
                    }
                }
            });
            
            this.menuSerial.add(item);
        }
        if(ports.length == 0) {
            JMenuItem item = new JMenuItem("No devices connected");
            item.setEnabled(false);
            this.menuSerial.add(item);
        }
        
        this.menuSerial.add(new Separator());
        
        //Add stop button
        JMenuItem stop = new JMenuItem("Stop");
        if(!Airspace.getInstance().hasInputStreamGroupStreams(SerialInputStream.class))
            stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Airspace.getInstance().removeInputStreamGroup(SerialInputStream.class);
            }
        });
        this.menuSerial.add(stop);
    }//GEN-LAST:event_menuSerialMenuSelected

    private void logToFileItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_logToFileItemStateChanged
        if(this.logToFile.isSelected()) {
            Airspace.getInstance().addOutputStream(new FileOutputStream(Preferences.getInstance().getSquitterFolder()));
        } else {
            Airspace.getInstance().removeOutputStream(true);
        }
    }//GEN-LAST:event_logToFileItemStateChanged

    private void serverOnlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_serverOnlineItemStateChanged
        if(this.serverOnline.isSelected()) {
            Airspace.getInstance().addOutputStream(new NetworkOutputStream(Preferences.getInstance().getOutputStreamPort()));
        } else {
            Airspace.getInstance().removeOutputStream(false);
        }
    }//GEN-LAST:event_serverOnlineItemStateChanged

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
            
        //Delete old data
//        ARTCC.resetAll();
//        Airport.resetAll();
//        Color.resetAll();
//        Fix.resetAll();
//        GeoLine.resetAll();
//        NDB.resetAll();
//        Runway.resetAll();
//        VOR.resetAll();
//        Airway.resetAll();
        
        //Load map window
        Map map = Map.getInstance();
        
        //Load map
        new OSMImport(map.getLeftTopMapPoint(), map.getRightBottomMapPoint());
        
        //Open map
        map.repaintMapPanel();
        map.setVisible(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adsb;
    private javax.swing.JTable aircraftTable;
    private javax.swing.JLabel aircraftsInRange;
    private javax.swing.JLabel aircraftsWithPosition;
    private javax.swing.JLabel altitudeField;
    private javax.swing.JLabel dataRate;
    private javax.swing.JMenu exitButton;
    private javax.swing.JLabel input;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu10;
    private javax.swing.JMenu jMenu11;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JLabel latitudeField;
    private javax.swing.JMenuItem loadFile;
    private javax.swing.JCheckBoxMenuItem logToFile;
    private javax.swing.JLabel longitudeField;
    private javax.swing.JLabel maxDistanceKM;
    private javax.swing.JLabel maxDistanceNM;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuInputStream;
    private javax.swing.JMenu menuNetwork;
    private javax.swing.JMenu menuOutputStream;
    private javax.swing.JMenu menuRTL;
    private javax.swing.JMenu menuSerial;
    private javax.swing.JLabel modeA;
    private javax.swing.JLabel modeS;
    private javax.swing.JLabel nearestAltitude;
    private javax.swing.JLabel nearestBearing;
    private javax.swing.JLabel nearestCallsign;
    private javax.swing.JLabel nearestDistance;
    private javax.swing.JLabel nearestICAOIdent;
    private javax.swing.JLabel nearestSpeed;
    private javax.swing.JLabel nearestSquawk;
    private javax.swing.JLabel nearestTrack;
    private javax.swing.JLabel satellitesField;
    private javax.swing.JCheckBoxMenuItem serverOnline;
    private javax.swing.JMenuItem startNetwork;
    private javax.swing.JMenuItem stopProgress;
    private javax.swing.JLabel uniqueAircrafts;
    // End of variables declaration//GEN-END:variables
}