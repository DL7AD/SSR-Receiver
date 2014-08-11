package map;
import exception.DecodingCoordinateException;
import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;

public class Fix extends Intersection implements Paintable,IntersectionPaintable {
    private static HashMap<String,Fix> list = new HashMap<String,Fix>(1000, 0.75f);
    
    private Point point;
    private boolean nameActivated;
    
    public Fix(String name, String latitude, String longitude) throws DecodingCoordinateException {
        this.setName(name);
        this.setPoint(new Point(latitude, longitude));
    }
    private void setPoint(Point point) {
        this.point = point;
    }
    public Point getPoint() {
        return this.point;
    }
    public static void add(Fix fix) {
        Fix.list.put(fix.getName(), fix);
    }
    public static Fix getFix(String name) {
        return Fix.list.get(name);
    }
    public static void resetAll() {
        Fix.list.clear();
    }
    public static Collection<Fix> getAll() {
        return Fix.list.values();
    }
    public static void setNameActivated(boolean nameActivated) {
        for(Fix fix: Fix.list.values())
            fix.nameActivated = nameActivated;
    }

    @Override
    public void paint(Graphics g) {
        Point point = this.getPoint();
        if(Math.abs(point.getX()-500) > 5000 || Math.abs(point.getY()-500) > 5000)
            return;
        g.setColor(new java.awt.Color(116, 116, 116));
        g.fillPolygon(
            new int[]{point.getX()    , point.getX() - 3, point.getX() + 3},
            new int[]{point.getY() - 3, point.getY() + 2, point.getY() + 2},
            3
        );
        
        if(this.nameActivated) {
            g.setColor(new java.awt.Color(75, 75, 75));
            g.drawString(this.getName(), point.getX() + 3, point.getY() - 3);
        }
    }
    
    @Override
    public void paintIntersection(Graphics g) {
        Point point = this.getPoint();
        if(Math.abs(point.getX()-500) > 5000 || Math.abs(point.getY()-500) > 5000)
            return;
        g.fillPolygon(
            new int[]{point.getX()    , point.getX() - 3, point.getX() + 3},
            new int[]{point.getY() - 3, point.getY() + 2, point.getY() + 2},
            3
        );
        
        if(this.nameActivated)
            g.drawString(this.getName(), point.getX() + 3, point.getY() - 3);
    }
}