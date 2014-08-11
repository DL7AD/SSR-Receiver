package map;

import gui.Map;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPanel;

public class AircraftPanel extends JPanel { 
    private static AircraftPanel instance;
    public static AircraftPanel getInstance() {
        if(AircraftPanel.instance == null)
            AircraftPanel.instance = new AircraftPanel();
        return AircraftPanel.instance;
    }
    private AircraftPanel() {}
    
    @Override
    public void paint(Graphics g) {
        g.setFont(new Font("Tahoma", Font.PLAIN, 11));
        Map.getInstance().paintAircrafts(g);
    }
    public void repaintAircrafts() {
        repaint();
    }
}