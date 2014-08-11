package gui;

import decoder.Aircraft;
import decoder.Airspace;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import map.*;

public class Map extends javax.swing.JFrame {
    
    //Initial map view
    private Point mapCoordinaesLeftTop = new Point(29.7, -95.5);
    private double mapZoom = 200;
    private double mapZoomMouseWheel = 1.1;
    
    private Integer lastMouseMotionX;
    private Integer lastMouseMotionY;
    
    private static Map instance;
    
    private Map() {
        initComponents();
        
        //Set position
        Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        Dimension windowSize = new Dimension(getPreferredSize());
        int wdwLeft = Math.min(100, (screenSize.width - windowSize.width));
        int wdwTop = Math.min(30, screenSize.height - windowSize.height);
        this.setLocation(wdwLeft, wdwTop);
        
        //Task Bar Icon
        Image taskBarIcon = new ImageIcon(this.getClass().getResource("/gui/taskbaricon.png")).getImage();
        this.setIconImage(taskBarIcon);
        
        final JPanel aircraftPanel = this.aircraftPanel;
        
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        
        //Map mouse listener
        this.aircraftPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {}
            @Override
            public void mouseMoved(MouseEvent e) {
                aircraftPanel.setToolTipText(null);
                
                for(Aircraft aircraft: Airspace.getInstance().getAllAircrafts()) {
                    Point position = aircraft.getLastMapPosition();
                    if(position == null)
                        continue;
                    
                    int left = position.getX() - 5;
                    int right = position.getX() + 5;
                    int top = position.getY() - 5;
                    int bottom = position.getY() + 5;
                    
                    if(e.getX() < left || e.getX() > right)
                        continue;
                    if(e.getY() < top || e.getY() > bottom)
                        continue;
                    
                    String label = Integer.toHexString(aircraft.getIcaoIdent()).toUpperCase();
                    if(aircraft.getFlightIdent() != null)
                        label += " - " + aircraft.getFlightIdent();
                    
                    aircraftPanel.setToolTipText(label);
                }
            }
        });
        //Map mouse listener
        this.aircraftPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(Aircraft aircraft: Airspace.getInstance().getAllAircrafts()) {
                    Point position = aircraft.getLastMapPosition();
                    if(position == null)
                        continue;
                    
                    int left = position.getX() - 5;
                    int right = position.getX() + 5;
                    int top = position.getY() - 5;
                    int bottom = position.getY() + 5;
                    
                    int leftText = position.getX() + 19;
                    int rightText = position.getX() + 80;
                    int topText = position.getY() - 61;
                    int bottomText = position.getY() - 25;
                    
                    if(e.getX() > left && e.getX() < right && e.getY() > top && e.getY() < bottom) {
                        aircraft.setClicked();
                        aircraftPanel.repaint();
                    } else if(e.getX() > leftText && e.getX() < rightText && e.getY() > topText && e.getY() < bottomText) {
                        aircraft.setClicked();
                        aircraftPanel.repaint();
                    }
                    
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }
    public static Map getInstance() {
        if(Map.instance == null)
            Map.instance = new Map();
        
        return Map.instance;
    }
    
    private boolean CCActivated = true;
    private boolean HSActivated = true;
    private boolean LSActivated = true;
    private boolean GEOActivated = true;
    private boolean airportActivated = true;
    private boolean runwayActivated = true;
    private boolean approachActivated = true;
    private boolean approachTActivated = true;
    private boolean SIDActivated = false;
    private boolean STARActivated = false;
    private boolean VORActivated = true;
    private boolean NDBActivated = true;
    private boolean fixActivated = true;
    private boolean lowAirwayActivated = false;
    private boolean highAirwayActivated = false;
    private boolean nameActivated = true;
    private boolean frequencyActivated = false;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        mapLayer = new javax.swing.JLayeredPane();
        aircraftPanel = AircraftPanel.getInstance();
        mapPanel = MapPanel.getInstance();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu9 = new javax.swing.JMenu();
        displayPosition = new javax.swing.JCheckBoxMenuItem();
        jMenu1 = new javax.swing.JMenu();
        vectorDisable = new javax.swing.JRadioButtonMenuItem();
        vectorOne = new javax.swing.JRadioButtonMenuItem();
        vectorTwo = new javax.swing.JRadioButtonMenuItem();
        vectorFour = new javax.swing.JRadioButtonMenuItem();
        vectorEight = new javax.swing.JRadioButtonMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        displayWholeTrack = new javax.swing.JCheckBoxMenuItem();
        displayWholeTrackTimeout = new javax.swing.JCheckBoxMenuItem();
        jMenu2 = new javax.swing.JMenu();
        CCMenu = new javax.swing.JCheckBoxMenuItem();
        HSMenu = new javax.swing.JCheckBoxMenuItem();
        LSMenu = new javax.swing.JCheckBoxMenuItem();
        jMenu3 = new javax.swing.JMenu();
        GEOMenu = new javax.swing.JCheckBoxMenuItem();
        jMenu4 = new javax.swing.JMenu();
        airportMenu = new javax.swing.JCheckBoxMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        runwayMenu = new javax.swing.JCheckBoxMenuItem();
        approachMenu = new javax.swing.JCheckBoxMenuItem();
        approachTMenu = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        SIDMenu = new javax.swing.JCheckBoxMenuItem();
        SIDSelect = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        STARMenu = new javax.swing.JCheckBoxMenuItem();
        STARSelect = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        VORMenu = new javax.swing.JCheckBoxMenuItem();
        NDBMenu = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        fixMenu = new javax.swing.JCheckBoxMenuItem();
        jMenu6 = new javax.swing.JMenu();
        lowAirwayMenu = new javax.swing.JCheckBoxMenuItem();
        highAirwayMenu = new javax.swing.JCheckBoxMenuItem();
        jMenu7 = new javax.swing.JMenu();
        nameMenu = new javax.swing.JCheckBoxMenuItem();
        frequencyMenu = new javax.swing.JCheckBoxMenuItem();
        jMenu8 = new javax.swing.JMenu();
        updateIntervalQuarterSecond = new javax.swing.JRadioButtonMenuItem();
        updateIntervalHalfSecond = new javax.swing.JRadioButtonMenuItem();
        updateIntervalOneSecond = new javax.swing.JRadioButtonMenuItem();
        updateIntervalThreeSeconds = new javax.swing.JRadioButtonMenuItem();
        updateIntervalFiveSeconds = new javax.swing.JRadioButtonMenuItem();
        updateIntervalTenSeconds = new javax.swing.JRadioButtonMenuItem();
        updateIntervalTwentySeconds = new javax.swing.JRadioButtonMenuItem();
        zoomIn = new javax.swing.JMenu();
        zoomOut = new javax.swing.JMenu();

        setTitle("Map");

        mapLayer.setPreferredSize(new java.awt.Dimension(500, 500));
        mapLayer.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                Map.this.mouseWheelMoved(evt);
            }
        });

        aircraftPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        aircraftPanel.setOpaque(false);
        aircraftPanel.setPreferredSize(new java.awt.Dimension(2000, 1300));
        aircraftPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                Map.this.mouseReleased(evt);
            }
        });
        aircraftPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                Map.this.mouseDragged(evt);
            }
        });

        javax.swing.GroupLayout aircraftPanelLayout = new javax.swing.GroupLayout(aircraftPanel);
        aircraftPanel.setLayout(aircraftPanelLayout);
        aircraftPanelLayout.setHorizontalGroup(
            aircraftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 2010, Short.MAX_VALUE)
        );
        aircraftPanelLayout.setVerticalGroup(
            aircraftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1300, Short.MAX_VALUE)
        );

        aircraftPanel.setBounds(0, 0, 2010, 1300);
        mapLayer.add(aircraftPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        mapPanel.setForeground(new java.awt.Color(45, 45, 45));
        mapPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        mapPanel.setOpaque(false);
        mapPanel.setPreferredSize(new java.awt.Dimension(2000, 1300));

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 2010, Short.MAX_VALUE)
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1300, Short.MAX_VALUE)
        );

        mapPanel.setBounds(0, 0, 2010, 1300);
        mapLayer.add(mapPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jMenu9.setText("Me");

        displayPosition.setText("Display Position");
        displayPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayPositionActionPerformed(evt);
            }
        });
        jMenu9.add(displayPosition);

        jMenuBar1.add(jMenu9);

        jMenu1.setText("Airplane");

        buttonGroup1.add(vectorDisable);
        vectorDisable.setText("Disable Vector");
        vectorDisable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                vectorDisableMinutes(evt);
            }
        });
        jMenu1.add(vectorDisable);

        buttonGroup1.add(vectorOne);
        vectorOne.setSelected(true);
        vectorOne.setText("Vector 1 minute");
        vectorOne.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                vectorOneMinute(evt);
            }
        });
        jMenu1.add(vectorOne);

        buttonGroup1.add(vectorTwo);
        vectorTwo.setText("Vector 2 minutes");
        vectorTwo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                vectorTwoMinutes(evt);
            }
        });
        jMenu1.add(vectorTwo);

        buttonGroup1.add(vectorFour);
        vectorFour.setText("Vector 4 minutes");
        vectorFour.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                vectorFourMinutes(evt);
            }
        });
        jMenu1.add(vectorFour);

        buttonGroup1.add(vectorEight);
        vectorEight.setText("Vector 8 minutes");
        vectorEight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                vectorEightMinutes(evt);
            }
        });
        jMenu1.add(vectorEight);
        jMenu1.add(jSeparator5);

        displayWholeTrack.setText("Show track");
        displayWholeTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayWholeTrackActionPerformed(evt);
            }
        });
        jMenu1.add(displayWholeTrack);

        displayWholeTrackTimeout.setText("also from Timeout");
        jMenu1.add(displayWholeTrackTimeout);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("ARTCC");

        CCMenu.setSelected(true);
        CCMenu.setText("CC");
        CCMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                CCMenuMousePressed(evt);
            }
        });
        jMenu2.add(CCMenu);

        HSMenu.setSelected(true);
        HSMenu.setText("HS");
        HSMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                HSMenuMousePressed(evt);
            }
        });
        jMenu2.add(HSMenu);

        LSMenu.setSelected(true);
        LSMenu.setText("LS");
        LSMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                LSMenuMousePressed(evt);
            }
        });
        jMenu2.add(LSMenu);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("GEO");

        GEOMenu.setSelected(true);
        GEOMenu.setText("GEO");
        GEOMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                GEOMenuMousePressed(evt);
            }
        });
        jMenu3.add(GEOMenu);

        jMenuBar1.add(jMenu3);

        jMenu4.setText("Airport");

        airportMenu.setSelected(true);
        airportMenu.setText("Airport");
        airportMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                airportMenuMousePressed(evt);
            }
        });
        jMenu4.add(airportMenu);
        jMenu4.add(jSeparator3);

        runwayMenu.setSelected(true);
        runwayMenu.setText("Runway");
        runwayMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                runwayMenuMousePressed(evt);
            }
        });
        jMenu4.add(runwayMenu);

        approachMenu.setSelected(true);
        approachMenu.setText("Approach");
        approachMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                approachMenuMousePressed(evt);
            }
        });
        jMenu4.add(approachMenu);

        approachTMenu.setSelected(true);
        approachTMenu.setText("Approach T");
        approachTMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                approachTMenuMousePressed(evt);
            }
        });
        jMenu4.add(approachTMenu);
        jMenu4.add(jSeparator2);

        SIDMenu.setText("SID");
        SIDMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                SIDMenuMousePressed(evt);
            }
        });
        jMenu4.add(SIDMenu);

        SIDSelect.setText("Select...");
        SIDSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SIDSelectActionPerformed(evt);
            }
        });
        jMenu4.add(SIDSelect);
        jMenu4.add(jSeparator1);

        STARMenu.setText("STAR");
        STARMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                STARMenuMousePressed(evt);
            }
        });
        jMenu4.add(STARMenu);

        STARSelect.setText("Select...");
        STARSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                STARSelectActionPerformed(evt);
            }
        });
        jMenu4.add(STARSelect);

        jMenuBar1.add(jMenu4);

        jMenu5.setText("Intersection");

        VORMenu.setSelected(true);
        VORMenu.setText("VOR");
        VORMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                VORMenuMousePressed(evt);
            }
        });
        jMenu5.add(VORMenu);

        NDBMenu.setSelected(true);
        NDBMenu.setText("NDB");
        NDBMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                NDBMenuMousePressed(evt);
            }
        });
        jMenu5.add(NDBMenu);
        jMenu5.add(jSeparator4);

        fixMenu.setSelected(true);
        fixMenu.setText("Fix");
        fixMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fixMenuMousePressed(evt);
            }
        });
        jMenu5.add(fixMenu);

        jMenuBar1.add(jMenu5);

        jMenu6.setText("Airway");

        lowAirwayMenu.setText("Low Airway");
        lowAirwayMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lowAirwayMenuMousePressed(evt);
            }
        });
        jMenu6.add(lowAirwayMenu);

        highAirwayMenu.setText("High Airway");
        highAirwayMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                highAirwayMenuMousePressed(evt);
            }
        });
        jMenu6.add(highAirwayMenu);

        jMenuBar1.add(jMenu6);

        jMenu7.setText("Misc");

        nameMenu.setSelected(true);
        nameMenu.setText("Name");
        nameMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                nameMenuMousePressed(evt);
            }
        });
        jMenu7.add(nameMenu);

        frequencyMenu.setText("Frequency");
        frequencyMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                frequencyMenuMousePressed(evt);
            }
        });
        jMenu7.add(frequencyMenu);

        jMenuBar1.add(jMenu7);

        jMenu8.setText("Update Interval");

        buttonGroup2.add(updateIntervalQuarterSecond);
        updateIntervalQuarterSecond.setText("0.25 Seconds");
        jMenu8.add(updateIntervalQuarterSecond);

        buttonGroup2.add(updateIntervalHalfSecond);
        updateIntervalHalfSecond.setText("0.5 Seconds");
        jMenu8.add(updateIntervalHalfSecond);

        buttonGroup2.add(updateIntervalOneSecond);
        updateIntervalOneSecond.setText("1 Second");
        jMenu8.add(updateIntervalOneSecond);

        buttonGroup2.add(updateIntervalThreeSeconds);
        updateIntervalThreeSeconds.setSelected(true);
        updateIntervalThreeSeconds.setText("3 Seconds");
        jMenu8.add(updateIntervalThreeSeconds);

        buttonGroup2.add(updateIntervalFiveSeconds);
        updateIntervalFiveSeconds.setText("5 Seconds");
        jMenu8.add(updateIntervalFiveSeconds);

        buttonGroup2.add(updateIntervalTenSeconds);
        updateIntervalTenSeconds.setText("10 Seconds");
        jMenu8.add(updateIntervalTenSeconds);

        buttonGroup2.add(updateIntervalTwentySeconds);
        updateIntervalTwentySeconds.setText("15 Seconds");
        jMenu8.add(updateIntervalTwentySeconds);

        jMenuBar1.add(jMenu8);

        zoomIn.setText("Zoom +");
        zoomIn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                zoomInMouseClicked(evt);
            }
        });
        jMenuBar1.add(zoomIn);

        zoomOut.setText("Zoom -");
        zoomOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                zoomOutMouseClicked(evt);
            }
        });
        jMenuBar1.add(zoomOut);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(mapLayer, javax.swing.GroupLayout.DEFAULT_SIZE, 1015, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mapLayer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_mouseWheelMoved
        if(evt.getWheelRotation() == 1) {
             this.setMapZoom(this.mapZoom / this.mapZoomMouseWheel);
        } else {
             this.setMapZoom(this.mapZoom * this.mapZoomMouseWheel);
        }
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_mouseWheelMoved

    private void mouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseDragged
        if(this.lastMouseMotionX != null)
        this.mapMove(evt.getX() - this.lastMouseMotionX, evt.getY() - this.lastMouseMotionY);

        this.lastMouseMotionX = evt.getX();
        this.lastMouseMotionY = evt.getY();
    }//GEN-LAST:event_mouseDragged

    private void mouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseReleased
        this.lastMouseMotionX = null;
        this.lastMouseMotionY = null;
    }//GEN-LAST:event_mouseReleased

    private void zoomInMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zoomInMouseClicked
        this.setMapZoom(this.mapZoom * this.mapZoomMouseWheel);
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_zoomInMouseClicked

    private void zoomOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zoomOutMouseClicked
        this.setMapZoom(this.mapZoom / this.mapZoomMouseWheel);
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_zoomOutMouseClicked

    private void CCMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_CCMenuMousePressed
        this.CCActivated = !this.CCMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_CCMenuMousePressed

    private void HSMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HSMenuMousePressed
        this.HSActivated = !this.HSMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_HSMenuMousePressed

    private void LSMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LSMenuMousePressed
        this.LSActivated = !this.LSMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_LSMenuMousePressed

    private void GEOMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GEOMenuMousePressed
        this.GEOActivated = !this.GEOMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_GEOMenuMousePressed

    private void airportMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_airportMenuMousePressed
        this.airportActivated = !this.airportMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_airportMenuMousePressed

    private void runwayMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_runwayMenuMousePressed
        this.runwayActivated = !this.runwayMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_runwayMenuMousePressed

    private void approachMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_approachMenuMousePressed
        this.approachActivated = !this.approachMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_approachMenuMousePressed

    private void approachTMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_approachTMenuMousePressed
        this.approachTActivated = !this.approachTMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_approachTMenuMousePressed

    private void SIDMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SIDMenuMousePressed
        this.SIDActivated = !this.SIDMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_SIDMenuMousePressed

    private void STARMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_STARMenuMousePressed
        this.STARActivated = !this.STARMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_STARMenuMousePressed

    private void VORMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_VORMenuMousePressed
        this.VORActivated = !this.VORMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_VORMenuMousePressed

    private void NDBMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NDBMenuMousePressed
        this.NDBActivated = !this.NDBMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_NDBMenuMousePressed

    private void fixMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixMenuMousePressed
        this.fixActivated = !this.fixMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_fixMenuMousePressed

    private void lowAirwayMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lowAirwayMenuMousePressed
        this.lowAirwayActivated = !this.lowAirwayMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_lowAirwayMenuMousePressed

    private void highAirwayMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_highAirwayMenuMousePressed
        this.highAirwayActivated = !this.highAirwayMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_highAirwayMenuMousePressed

    private void nameMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nameMenuMousePressed
        this.nameActivated = !this.nameMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_nameMenuMousePressed

    private void frequencyMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_frequencyMenuMousePressed
        this.frequencyActivated = !this.frequencyMenu.isSelected();
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_frequencyMenuMousePressed

    private void vectorDisableMinutes(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vectorDisableMinutes
        Aircraft.setVectorLength(0);
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_vectorDisableMinutes

    private void vectorOneMinute(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vectorOneMinute
        Aircraft.setVectorLength(1);
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_vectorOneMinute

    private void vectorTwoMinutes(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vectorTwoMinutes
        Aircraft.setVectorLength(2);
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_vectorTwoMinutes

    private void vectorFourMinutes(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vectorFourMinutes
        Aircraft.setVectorLength(4);
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_vectorFourMinutes

    private void vectorEightMinutes(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vectorEightMinutes
        Aircraft.setVectorLength(8);
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_vectorEightMinutes

    private void SIDSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SIDSelectActionPerformed
        RouteSelector selector = RouteSelector.getInstance(Airway.SID);
        selector.setVisible(true);
    }//GEN-LAST:event_SIDSelectActionPerformed

    private void STARSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_STARSelectActionPerformed
        RouteSelector selector = RouteSelector.getInstance(Airway.STAR);
        selector.setVisible(true);
    }//GEN-LAST:event_STARSelectActionPerformed

    private void displayWholeTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayWholeTrackActionPerformed
        if(!this.displayWholeTrack.isSelected())
            this.displayWholeTrackTimeout.setSelected(false);
        this.displayWholeTrackTimeout.setEnabled(this.displayWholeTrack.isSelected());
    }//GEN-LAST:event_displayWholeTrackActionPerformed

    private void displayPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayPositionActionPerformed
        MapPanel.getInstance().repaintMap();
    }//GEN-LAST:event_displayPositionActionPerformed
    private void mapMove(int x, int y) {
        //Calculate the new shift point and set point to map
        this.setMapPoint(mapCoordinaesLeftTop.movePoint(x, y));
        
        //Update map
        MapPanel.getInstance().repaintMap();
    }
    public void repaintMapPanel() {
        MapPanel.getInstance().repaintMap();
    }
    public void paintMap(Graphics g) {
        Collection<Paintable> mapObjects = new ArrayList(10000);
        
        if(this.GEOActivated)
            mapObjects.addAll(GeoLine.getAll());
        
        if(this.CCActivated)
            mapObjects.addAll(ARTCC.getAll(ARTCC.MEDIUM));
        
        if(this.HSActivated)
            mapObjects.addAll(ARTCC.getAll(ARTCC.HIGH));
        
        if(this.LSActivated)
            mapObjects.addAll(ARTCC.getAll(ARTCC.LOW));
        
        Airway.setNameActivated(this.nameActivated); //Applicable to Airways, SIDs and STARs
        
        if(this.highAirwayActivated)
            mapObjects.addAll(Airway.getAll(Airway.HIGH));
        if(this.lowAirwayActivated)
            mapObjects.addAll(Airway.getAll(Airway.LOW));
        
        Fix.setNameActivated(this.nameActivated);
        if(this.fixActivated)
            mapObjects.addAll(Fix.getAll());
        
        VOR.setNameActivated(this.nameActivated);
        VOR.setFrequencyActivated(this.frequencyActivated);
        if(this.VORActivated)
            mapObjects.addAll(VOR.getAll());
        
        NDB.setNameActivated(this.nameActivated);
        NDB.setFrequencyActivated(this.frequencyActivated);
        if(this.NDBActivated)
            mapObjects.addAll(NDB.getAll());
        
        Runway.setApproachActivated(this.approachActivated);
        Runway.setApproachTActivated(this.approachTActivated);
        if(this.runwayActivated)
            mapObjects.addAll(Runway.getAll());
        
        if(this.SIDActivated && SID.getSelected() != null)
            mapObjects.add(SID.getSelected());
        if(this.STARActivated && STAR.getSelected() != null)
            mapObjects.add(STAR.getSelected());
        
        Airport.setNameActivated(this.nameActivated);
        if(this.airportActivated)
            mapObjects.addAll(Airport.getAll());
        
        for(Paintable mapObject: mapObjects)
            mapObject.paint(g);
    }
    public void paintAircrafts(Graphics g) {
        for(Aircraft mapObject: Airspace.getInstance().getAllAircrafts())
            mapObject.paint(g);
    }
    private void setMapPoint(Point point) {
        this.mapCoordinaesLeftTop = point;
    }
    public Point getLeftTopMapPoint() {
        return this.mapCoordinaesLeftTop;
    }
    public Point getRightBottomMapPoint() {
        return this.mapCoordinaesLeftTop.movePoint(this.mapPanel.getWidth(), this.mapPanel.getHeight());
    }
    private void setMapZoom(double zoom) {
        this.mapZoom = zoom;
    }
    public double getMapZoom() {
        return this.mapZoom;
    }
    public Dimension getMapLayerSize() {
        return this.mapLayer.getSize();
    }
    public float getUpdateInterval() {
        Enumeration<AbstractButton> buttons = this.buttonGroup2.getElements();
        while(buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            if(button.isSelected())
                return Float.parseFloat(button.getText().split(" ")[0]);
        }
        return 3;
    }
    public boolean displayWholeTrack() {
        return this.displayWholeTrack.isSelected();
    }
    public boolean displayWholeTrackTimeout() {
        return this.displayWholeTrackTimeout.isSelected();
    }
    public boolean displayPosition() {
        return this.displayPosition.isSelected();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem CCMenu;
    private javax.swing.JCheckBoxMenuItem GEOMenu;
    private javax.swing.JCheckBoxMenuItem HSMenu;
    private javax.swing.JCheckBoxMenuItem LSMenu;
    private javax.swing.JCheckBoxMenuItem NDBMenu;
    private javax.swing.JCheckBoxMenuItem SIDMenu;
    private javax.swing.JMenuItem SIDSelect;
    private javax.swing.JCheckBoxMenuItem STARMenu;
    private javax.swing.JMenuItem STARSelect;
    private javax.swing.JCheckBoxMenuItem VORMenu;
    private javax.swing.JPanel aircraftPanel;
    private javax.swing.JCheckBoxMenuItem airportMenu;
    private javax.swing.JCheckBoxMenuItem approachMenu;
    private javax.swing.JCheckBoxMenuItem approachTMenu;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JCheckBoxMenuItem displayPosition;
    private javax.swing.JCheckBoxMenuItem displayWholeTrack;
    private javax.swing.JCheckBoxMenuItem displayWholeTrackTimeout;
    private javax.swing.JCheckBoxMenuItem fixMenu;
    private javax.swing.JCheckBoxMenuItem frequencyMenu;
    private javax.swing.JCheckBoxMenuItem highAirwayMenu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JCheckBoxMenuItem lowAirwayMenu;
    private javax.swing.JLayeredPane mapLayer;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JCheckBoxMenuItem nameMenu;
    private javax.swing.JCheckBoxMenuItem runwayMenu;
    private javax.swing.JRadioButtonMenuItem updateIntervalFiveSeconds;
    private javax.swing.JRadioButtonMenuItem updateIntervalHalfSecond;
    private javax.swing.JRadioButtonMenuItem updateIntervalOneSecond;
    private javax.swing.JRadioButtonMenuItem updateIntervalQuarterSecond;
    private javax.swing.JRadioButtonMenuItem updateIntervalTenSeconds;
    private javax.swing.JRadioButtonMenuItem updateIntervalThreeSeconds;
    private javax.swing.JRadioButtonMenuItem updateIntervalTwentySeconds;
    private javax.swing.JRadioButtonMenuItem vectorDisable;
    private javax.swing.JRadioButtonMenuItem vectorEight;
    private javax.swing.JRadioButtonMenuItem vectorFour;
    private javax.swing.JRadioButtonMenuItem vectorOne;
    private javax.swing.JRadioButtonMenuItem vectorTwo;
    private javax.swing.JMenu zoomIn;
    private javax.swing.JMenu zoomOut;
    // End of variables declaration//GEN-END:variables
}