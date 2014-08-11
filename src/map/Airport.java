package map;
import exception.DecodingCoordinateException;
import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;

public class Airport extends Beacon implements Paintable {
    private static HashMap<String,Airport> list = new HashMap<String,Airport>(100, 0.75f);
    
    private boolean nameActivated;
    
    public Airport(String name, float frequency, String latitude, String longitude) throws DecodingCoordinateException {
        this.setName(name);
        this.setFrequency(frequency);
        this.setPoint(new Point(latitude, longitude));
    }
    public static void add(Airport airport) {
        Airport.list.put(airport.getName(), airport);
    }
    public static void resetAll() {
        Airport.list.clear();
    }
    public static Collection<Airport> getAll() {
        return Airport.list.values();
    }
    public static Airport get(String airport) {
        return Airport.list.get(airport);
    }
    public static void setNameActivated(boolean nameActivated) {
        for(Airport airport: Airport.list.values())
            airport.nameActivated = nameActivated;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new java.awt.Color(116, 116, 116));
        Point point = this.getPoint();
        if(Math.abs(point.getX()-500) > 5000 || Math.abs(point.getY()-500) > 5000)
            return;
        g.fillRect(point.getX() - 1, point.getY() - 1, 3, 3);
        if(this.nameActivated)
            g.drawString(this.getName(), point.getX() + 3, point.getY() - 3);
    }
}