package map;
import exception.DecodingCoordinateException;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

public class GeoLine extends Earth implements Paintable {
    private static ArrayList<GeoLine> list = new ArrayList<GeoLine>(100);
    
    private java.awt.Color color;
    private Line line;
    
    public GeoLine(String latitudeA, String longitudeA, String latitudeB, String longitudeB, String colorName) throws DecodingCoordinateException {
        this.setColor(Color.getColor(colorName));
        this.setLine(new Line(
            new Point(latitudeA, longitudeA),
            new Point(latitudeB, longitudeB)
        ));
    }
    public GeoLine(Point a, Point b, String colorName) {
        this.setColor(Color.getColor(colorName));
        this.setLine(new Line(a, b));
    }
    private void setLine(Line line) {
        this.line = line;
    }
    public Line getLine() {
        return this.line;
    }
    private void setColor(java.awt.Color color) {
        this.color = color;
    }
    public java.awt.Color getColor() {
        return this.color;
    }
    public static void add(GeoLine geoLine) {
        GeoLine.list.add(geoLine);
    }
    public static void resetAll() {
        GeoLine.list.clear();
    }
    public static Collection<GeoLine> getAll() {
        return (Collection<GeoLine>)GeoLine.list.clone();
    }

    @Override
    public void paint(Graphics g) {
        Line line = this.getLine();
        Point pointA = line.getPointA();
        Point pointB = line.getPointB();
        if(Math.abs(pointA.getX()-500) > 5000 || Math.abs(pointA.getY()-500) > 5000)
            return;
        if(Math.abs(pointB.getX()-500) > 5000 || Math.abs(pointB.getY()-500) > 5000)
            return;
        g.setColor(this.getColor());
        g.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
    }
}