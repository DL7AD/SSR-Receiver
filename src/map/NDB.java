package map;
import exception.DecodingCoordinateException;
import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;

public class NDB extends Beacon implements Paintable,IntersectionPaintable {
    private static HashMap<String,NDB> list = new HashMap<String,NDB>(100);
    
    private boolean nameActivated;
    private boolean frequencyActivated;
    
    public NDB(String name, float frequency, String latitude, String longitude) throws DecodingCoordinateException {
        this.setName(name);
        this.setFrequency(frequency);
        this.setPoint(new Point(latitude, longitude));
    }
    public static void add(NDB ndb) {
        NDB.list.put(ndb.getName(), ndb);
    }
    public static NDB getNDB(String name) {
        return NDB.list.get(name);
    }
    public static void resetAll() {
        NDB.list.clear();
    }
    public static Collection<NDB> getAll() {
        return NDB.list.values();
    }
    public static void setNameActivated(boolean nameActivated) {
        for(NDB ndb: NDB.list.values())
            ndb.nameActivated = nameActivated;
    }
    public static void setFrequencyActivated(boolean frequencyActivated) {
        for(NDB ndb: NDB.list.values())
            ndb.frequencyActivated = frequencyActivated;
    }

    @Override
    public void paint(Graphics g) {
        Point point = this.getPoint();
        if(Math.abs(point.getX()-500) > 5000 || Math.abs(point.getY()-500) > 5000)
            return;
        g.setColor(new java.awt.Color(116, 116, 116));
        g.fillRect(point.getX() - 1, point.getY() - 1, 3, 3);
        
        if(this.nameActivated) {
            g.setColor(new java.awt.Color(75, 75, 75));
            g.drawString(this.frequencyActivated ? this.getName() + "(" + this.getFrequency() + ")" : this.getName(), point.getX() + 3, point.getY() - 3);
        }
    }
    
    @Override
    public void paintIntersection(Graphics g) {
        Point point = this.getPoint();
        if(Math.abs(point.getX()-500) > 5000 || Math.abs(point.getY()-500) > 5000)
            return;
        g.fillRect(point.getX() - 1, point.getY() - 1, 3, 3);
        
        if(this.nameActivated)
            g.drawString(this.frequencyActivated ? this.getName() + "(" + this.getFrequency() + ")" : this.getName(), point.getX() + 3, point.getY() - 3);
    }
}