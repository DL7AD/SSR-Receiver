package map;

import decoder.Airspace;
import gui.Map;
import gui.Preferences;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPanel;

public class MapPanel extends JPanel { 
    private static MapPanel instance;
    public static MapPanel getInstance() {
        if(MapPanel.instance == null)
            MapPanel.instance = new MapPanel();
        return MapPanel.instance;
    }
    private MapPanel() {}
    
    @Override
    public void paint(Graphics g) {
        //Set Font
        g.setFont(new Font("Tahoma", Font.PLAIN, 11));
        
        //Paint background
        g.setColor(new java.awt.Color(45, 45, 45));
        g.fillRect(0, 0, 2000, 1100);
        
        //Paint objects
        Map.getInstance().paintMap(g);
        
        //Paint receiver (me)
        Point position = Airspace.getMyPosition();
        if(Map.getInstance().displayPosition() && position != null) {
            g.setColor(new java.awt.Color(204, 112, 112));
            g.drawRect(position.getX() - 3, position.getY() - 3, 6, 6);
            g.drawLine(position.getX() + 5, position.getY() - 5, position.getX() + 20, position.getY() - 20);
            g.drawString("Receiver", position.getX() + 20, position.getY() - 25);
        }
    }
    public void repaintMap() {
        repaint();
    }
}