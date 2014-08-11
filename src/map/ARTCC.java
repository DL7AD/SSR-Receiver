package map;
import exception.DecodingCoordinateException;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;

public class ARTCC extends Earth implements Paintable {
    private static ArrayList<ARTCC> list = new ArrayList<ARTCC>(1000);
    
    public static final int LOW = 1;
    public static final int MEDIUM = 2;
    public static final int HIGH = 3;
    
    private int type;
    private Line line;
    
    public ARTCC(String latitudeA, String longitudeA, String latitudeB, String longitudeB, int type) throws DecodingCoordinateException {
        this.setType(type);
        this.setLine(new Line(
            new Point(latitudeA, longitudeA),
            new Point(latitudeB, longitudeB)
        ));
    }
    private void setLine(Line line) {
        this.line = line;
    }
    public Line getLine() {
        return this.line;
    }
    private void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return this.type;
    }
    public static void add(ARTCC artcc) {
        ARTCC.list.add(artcc);
    }
    public static void resetAll() {
        ARTCC.list.clear();
    }
    public static Collection<ARTCC> getAll(int type) {
        ArrayList<ARTCC> lines = new ArrayList<ARTCC>(1000);
        for(ARTCC line: ARTCC.list) {
            if(line.getType() == type)
                lines.add(line);
        }
        return lines;
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
        
        if(this.getType() == ARTCC.MEDIUM) {
            g.setColor(new java.awt.Color(128, 128, 128));
            g.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
            
        } else if(this.getType() == ARTCC.HIGH || this.getType() == ARTCC.LOW) {
            
            Graphics2D g2d = (Graphics2D)g;
            
            if(this.getType() == ARTCC.HIGH) {
                g2d.setColor(new java.awt.Color(102, 102, 102));
            } else if(this.getType() == ARTCC.LOW) {
                g2d.setColor(new java.awt.Color(73, 78, 73));
            }
            
            BasicStroke basicStroke = new BasicStroke(
                1,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
                1.0f,
                new float[] {2f, 0f, 2f},
                2f
            );
            g2d.setStroke(basicStroke); //Set dashed stroke
            g2d.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
            g2d.setStroke(new BasicStroke()); //Reset dashed stroke
        }
    }
}