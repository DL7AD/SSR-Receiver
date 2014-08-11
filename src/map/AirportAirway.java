package map;

import java.awt.Graphics;
import java.util.ArrayList;

public class AirportAirway extends Airway implements Paintable {
    
    private String airport;
    private String runway;
    
    public AirportAirway(String name, int type) {
        super(name, type);
    }
    protected void setAirport(String airport) {
        this.airport = airport;
    }
    public String getAirport() {
        return this.airport;
    }
    protected void setRunway(String runway) {
        this.runway = runway;
    }
    public String getRunway() {
        return this.runway;
    }
    
    /**
     * Returns the point from the route which is the nearest to the airport.
     * @return Nearest route point to airport
     */
    private Point getNearestAirportToRoutePoint() {
        Point[] points = new Point[4];
        Point nearest;
        
        points[0] = this.getRoute().get(0).getPointA();
        points[1] = this.getRoute().get(0).getPointB();
        points[2] = this.getRoute().get(this.getRoute().size() - 1).getPointA();
        points[3] = this.getRoute().get(this.getRoute().size() - 1).getPointB();
        
        Point airport = Airport.get(this.getAirport()).getPoint();
        
        nearest = points[0];
        for(int i = 1; i < 4; i++)
            if(airport.getDistance(points[i]) < airport.getDistance(nearest))
                nearest = points[i];
        
        return nearest;
    }
    private ArrayList<Line> getRouteWithRouteToAirportLine() {
        ArrayList<Line> route = this.getRoute();
        
        Point routePoint = this.getNearestAirportToRoutePoint();
        Point runwayPoint = this.getRunwayPoint();
        
        if(routePoint != null && runwayPoint != null) {
            if(route.get(0).getPointA() == routePoint) {
                for(int i = route.size() - 1; i > 0; i--)
                    route.add(i + 1, route.get(i));
                route.add(0, new Line(routePoint, runwayPoint));
            } else if(route.get(route.size() - 1).getPointB() == routePoint) {
                route.add(new Line(routePoint, runwayPoint));
            }
        }
        
        return route;
    }
    /**
     * Return the point from the runway which belongs to this SID. Returns null
     * if the found runway is too far away.
     * @return Point from runway
     */
    private Point getRunwayPoint() {
        Point airportPoint = Airport.get(this.getAirport()).getPoint();
        byte runwayName = this.removeLCR(this.getRunway());
        
        Point nearestRunwayPoint = null;
        for(Runway runway: Runway.getAll()) {
            Point runwayPoint = null;
            if(this.removeLCR(runway.getNameA()) == runwayName || this.removeLCR(runway.getNameB()) == runwayName) { //Airport has a runway named like SIDs runway
                //Find out the right point by its direction
                Point a = runway.getLine().getPointB();
                Point b = runway.getLine().getPointA();
                if(Math.abs(new Line(a, b).calculateCourse() - runwayName * 10) < 30) {
                    runwayPoint = a;
                } else if(Math.abs(new Line(b, a).calculateCourse() - runwayName * 10) < 30) {
                    runwayPoint = b;
                }
            }
            
            if(runwayPoint != null) {
                double currDistance = runwayPoint.getDistance(airportPoint);
                if((nearestRunwayPoint == null || (currDistance < 10 && currDistance < nearestRunwayPoint.getDistance(airportPoint))))
                    nearestRunwayPoint = runwayPoint;
            }
            
        }
        
        return nearestRunwayPoint;
    }
    /**
     * Return the runway name without direction designation and parses value
     * into an byte. E.g. 25L will be converted to 25.
     * @param runwayName Runway name
     * @return Runway name without LCR
     */
    private byte removeLCR(String runwayName) {
        runwayName = runwayName.replaceAll("[^\\d.]", "");
        return Byte.parseByte(runway);
    }
    
    @Override
    public void paint(Graphics g) {
        g.setColor(new java.awt.Color(204, 112, 112));
        
        ArrayList<Line> route = this.getRouteWithRouteToAirportLine();
        
        //Set First A Point        
        Point pointA = route.get(0).getPointA();
        Point pointB;
        
        //Paint circles and calculate manipulated points
        for(int i = 1; i < route.size(); i++) { //All lines which have a predecessor
            
            Line current = route.get(i); //Get current line
            Line predesessor = route.get(i - 1); //Get line before
            
            if(current.getPointA() != predesessor.getPointB()) //Lines are not connected
                continue;
            
            //Calculate angles
            double a = predesessor.calculateCourse(); //Course line A
            double b = (current.calculateCourse() + 180) % 360; //Course line B
            double c = (Math.abs(a - b) + 360) % 360; //Difference between course A and B
            if(c > 180) c = 360 - c;
            if(a > b) {
                double z = a;
                a = b;
                b = z;
            }
            if(b - a > 180)
                a += 360;
            double d = ((a + b) / 2d) % 360;
            Point m = current.getPointA().calculatePoint((float)d, (float)(200 * 2 / 60d / Math.PI / Math.sin(Math.toRadians(c / 2))));
            
            //Get the point in the upper left corner and in the lower right corner
            Point ul = m.calculatePoint(315f, (float)(200 / 30d / Math.PI * Math.sqrt(2))); //Upper left corner
            Point lr = m.calculatePoint(135f, (float)(200 / 30d / Math.PI * Math.sqrt(2))); //Lower right corner
            
            //Calculate in which direction the route is turning
            int ca = new Float(current.calculateCourse()).intValue();
            int cb = new Float(predesessor.calculateCourse()).intValue();
            int direction = 0;
            if(Math.abs(-360 + cb - ca) <= 90) {
                direction = -360 + cb - ca > 0 ? 1 : -1;
            } else if(Math.abs(-180 + cb - ca) <= 90) {
                direction = -180 + cb - ca > 0 ? -1 : 1;
            } else if(Math.abs(cb - ca) <= 90) {
                direction = cb - ca > 0 ? 1 : -1;
            } else if(Math.abs(180 + cb - ca) <= 90) {
                direction = 180 + cb - ca > 0 ? -1 : 1;
            }
            int turn = cb - ca;
            if(turn < -180)
                turn += 360;
            if(turn > 180)
                turn -= 360;
            
            //Paint an arc
            g.drawArc(
                ul.getX(),
                ul.getY(),
                lr.getX() - ul.getX(),
                lr.getY() - ul.getY(),
                270 - cb - direction * 90,
                turn
            );
            
            //Calculate how much the line must be shortened
            float length = (float)(200 / 30d / Math.PI / Math.tan(Math.toRadians(c / 2)));
            
            //Draw line
            pointB = predesessor.getPointB().calculatePoint(predesessor.calculateCourse(), length);
            g.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
            
            //Draw Current point A
            ((IntersectionPaintable)Intersection.getIntersection(predesessor.getPointA())).paintIntersection(g);
            
            //Set next A point from line
            pointA = current.getPointA().calculatePoint((current.calculateCourse() + 180) % 360, length);
            
        }
        
        //Draw last line
        pointB = route.get(route.size()-1).getPointB();
        g.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
        
        //Draw name
        if(this.nameActivated)
            g.drawString(this.getName(), (pointA.getX() + pointB.getX()) / 2 + 5, (pointA.getY() + pointB.getY()) / 2 + 5);
    }
}