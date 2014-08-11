package map;
import exception.DecodingCoordinateException;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

public class Runway extends Earth implements Paintable {
    private static final int approachLineLength = 10;
    
    private static ArrayList<Runway> list = new ArrayList<Runway>(100);
    
    private Line line;
    private String nameA;
    private String nameB;
    private int approachA;
    private int approachB;
    
    private boolean approachActivated;
    private boolean approachTActivated;
    
    public Runway(String nameA, String nameB, String latitudeA, String longitudeA, String latitudeB, String longitudeB, int courseA, int courseB) throws DecodingCoordinateException {
        this.setNameA(nameA);
        this.setNameB(nameB);
        this.setApproachA(courseA);
        this.setApproachB(courseB);
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
    private void setApproachA(int course) {
        this.approachA = course;
    }
    public int getApproachA() {
        return this.approachA;
    }
    private void setApproachB(int course) {
        this.approachB = course;
    }
    public int getApproachB() {
        return this.approachB;
    }
    private void setNameA(String name) {
        this.nameA = name;
    }
    public String getNameA() {
        return this.nameA;
    }
    private void setNameB(String name) {
        this.nameB = name;
    }
    public String getNameB() {
        return this.nameB;
    }
    
    public static void add(Runway runway) {
        Runway.list.add(runway);
    }
    public static void resetAll() {
        Runway.list.clear();
    }
    public static Collection<Runway> getAll() {
        return (Collection<Runway>)Runway.list.clone();
    }
    public static void setApproachActivated(boolean approachActivated) {
        for(Runway runway: Runway.list)
            runway.approachActivated = approachActivated;
    }
    public static void setApproachTActivated(boolean approachTActivated) {
        for(Runway runway: Runway.list)
            runway.approachTActivated = approachTActivated;
    }

    @Override
    public void paint(Graphics g) {
        //Draw runway
        Line line = this.getLine();
        Point pointA = line.getPointA();
        Point pointB = line.getPointB();

        if(Math.abs(pointA.getX()-500) > 5000 || Math.abs(pointA.getY()-500) > 5000)
            return;
        if(Math.abs(pointB.getX()-500) > 5000 || Math.abs(pointB.getY()-500) > 5000)
            return;
        
        g.setColor(new java.awt.Color(80, 80, 200));
        g.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
        
        //Draw approach
        if(this.approachActivated) {
            if(this.getApproachB() != 0)
                this.paintApproach(g, pointA, pointB);
            if(this.getApproachA() != 0)
                this.paintApproach(g, pointB, pointA);
        }
    }
    private void paintApproach(Graphics g, Point pointA, Point pointB) {
        //Calculate course
        float course = new Line(pointA, pointB).calculateCourse();
        
        //Draw centerline
        g.setColor(new java.awt.Color(102, 102, 102));
        Point finalApproachPoint = pointA.calculatePoint(course, 10);
        g.drawLine(pointA.getX(), pointA.getY(), finalApproachPoint.getX(), finalApproachPoint.getY());

        float left = course < 90 ? 270 + course : course - 90;
        float right = course > 270 ? course - 270 : course + 90;
        float leftY = course < 30 ? 330 + course : course - 30;
        float rightY = course > 330 ? course - 330 : course + 30;

        //Draw markers
        Point markerPoint = null;
        for(int mileDot = 1; mileDot <= Runway.approachLineLength; mileDot++) {
            markerPoint = pointA.calculatePoint(course, mileDot);

            float length = mileDot % 5 == 0 ? 0.4f : 0.1f;

            Point leftMarkerPoint = markerPoint.calculatePoint(left, length);
            Point rightMarkerPoint = markerPoint.calculatePoint(right, length);

            g.drawLine(markerPoint.getX(), markerPoint.getY(), leftMarkerPoint.getX(), leftMarkerPoint.getY());
            g.drawLine(markerPoint.getX(), markerPoint.getY(), rightMarkerPoint.getX(), rightMarkerPoint.getY());
        }

        //Draw T
        if(this.approachTActivated) {
            Point leftInnerT = markerPoint.calculatePoint(leftY, 5);
            Point rightInnerT = markerPoint.calculatePoint(rightY, 5);
            g.drawLine(markerPoint.getX(), markerPoint.getY(), leftInnerT.getX(), leftInnerT.getY());
            g.drawLine(markerPoint.getX(), markerPoint.getY(), rightInnerT.getX(), rightInnerT.getY());

            Point leftOuterT = leftInnerT.calculatePoint(left, 3);
            Point rightOuterT = rightInnerT.calculatePoint(right, 3);
            g.drawLine(leftInnerT.getX(), leftInnerT.getY(), leftOuterT.getX(), leftOuterT.getY());
            g.drawLine(rightInnerT.getX(), rightInnerT.getY(), rightOuterT.getX(), rightOuterT.getY());
        }
    }
}