package map;

import exception.DecodingCoordinateException;
import gui.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Point {
    
    private double latitude;
    private double longitude;
    
    public Point(String latitude, String longitude) throws DecodingCoordinateException {
        switch(latitude.charAt(0)) {
            case 'N':
                this.latitude = this.parseCoordinate(latitude);
                break;
            case 'S':
                this.latitude = -this.parseCoordinate(latitude);
                break;
            default:
                throw new DecodingCoordinateException();
        }
        switch(longitude.charAt(0)) {
            case 'E':
                this.longitude = this.parseCoordinate(longitude);
                break;
            case 'W':
                this.longitude = -this.parseCoordinate(longitude);
                break;
            default:
                throw new DecodingCoordinateException();
        }
    }
    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public Point(byte latDegree, byte latMinute, byte latSecond, short lonDegree, byte lonMinute, byte lonSecond) {
        this.latitude = latDegree + latMinute / 60d + latSecond / 3600d;
        this.longitude = lonDegree + lonMinute / 60d + lonSecond / 3600d;
    }
    
    private double parseCoordinate(String degrees) throws DecodingCoordinateException {
        StringTokenizer split = new StringTokenizer(degrees.substring(1), ".");
        
        double coords;
        try {
            coords = Integer.parseInt(split.nextToken()) +
                     Integer.parseInt(split.nextToken()) / 60d + 
                     Float.parseFloat(split.nextToken() + "." + split.nextToken()) / 3600d;
        } catch(NoSuchElementException e) {
            throw new DecodingCoordinateException();
        } catch(NumberFormatException e) {
            throw new DecodingCoordinateException();
        }
        
        return coords;
    }
    private void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public int getX() {
        Map map = Map.getInstance();
        int layerSizeX = Map.getInstance().getMapLayerSize().width;
        return new Double( (this.longitude - map.getLeftTopMapPoint().getLongitude()) * map.getMapZoom() + layerSizeX / 2).intValue();
    }
    public int getY() {
        return new Double(this.getYDouble()).intValue();
    }
    public double getYDouble() {
        Map map = Map.getInstance();
        double correction = 1 / Math.cos(Math.toRadians(this.latitude));
        int layerSizeY = Map.getInstance().getMapLayerSize().height;
        return (-(this.latitude - map.getLeftTopMapPoint().getLatitude()) * map.getMapZoom() * correction) + layerSizeY / 2;
    }
    /**
     * Convertes WGS84-coordinates to cartesian coordinates. The returned
     * values are given in meters.
     * @param altitude Altitude in feet
     * @return Cartesian coordinates in NM
     * @see http://forum.diegeodaeten.de/index.php?mode=thread&id=1624#p1625
     */
    public double[] getCartesianCoordinates(int altitude) {
        //Convertion
        altitude = new Float(altitude / 3.28f).intValue();
        double lat = Math.toRadians(this.latitude);
        double lon = Math.toRadians(this.longitude);
        
        int a = 6378137;
        double eQua = 0.00669437999013;
        double n = a / Math.sqrt(1 - eQua * Math.pow(Math.sin(lat), 2));
        double x = (n + altitude) * Math.cos(lat) * Math.cos(lon);
        double y = (n + altitude) * Math.cos(lat) * Math.sin(lon);
        double z = (n * (1 - eQua) + altitude) * Math.sin(lat);
        double[] ret = {x, y, z};
        return ret;
    }
    public double getLatitude() {
        return this.latitude;
    }
    public double getLongitude() {
        return this.longitude;
    }
    /**
     * Calculates a Point from one other point in a specific distance and course.
     * @param start Startpoint
     * @param course Course from initial point to calculated point
     * @param distance Distance between both points
     * @return Calculated point
     */
    public Point calculatePoint(float course, float distance) {
        double correction = 1 / Math.cos(Math.toRadians(this.getLatitude())); //Latitude correction
        double courseR = Math.toRadians(course);
        double nsVelocityDegree = Math.cos(courseR) * distance / 60d; //Latitude per x minute
        double weVelocityDegree = Math.sin(courseR) * distance * correction / 60d; //Longitude per x minute
        return new Point(this.getLatitude() + nsVelocityDegree, this.getLongitude() + weVelocityDegree); //Position in x minutes
    }
    /**
     * Calculates a new point by a point and x,y-shift
     * @param x Shift in x direction (Pixel)
     * @param y Shift in y direction (Pixel)
     * @return Shifted Point
     */
    public Point movePoint(int x, int y) {
        //Latitude
        Point p = this.calculateLatitudePoint(y);
        
        //Longitude
        double degreePerPixelX = 1 / Map.getInstance().getMapZoom();
        p.setLongitude(this.longitude - x * degreePerPixelX);
        
        return p;
    }
    private Point calculateLatitudePoint(int yReach) {
        //Nested intervals
        Point pA = new Point(90, 0);
        Point pB;
        Point pC = new Point(-90, 0);
        do {
            pB = new Point((pA.getLatitude() + pC.getLatitude()) / 2, this.longitude);
            if(this.getY() - pB.getY() < yReach) {
                pC = pB;
            } else {
                pA = pB;
            }       
        } while (this.getY() - pB.getY() != yReach);
        return pB;
    }
    public double getDistance(Point point) {
        return Math.acos(Math.sin(Math.toRadians(this.getLatitude())) * Math.sin(Math.toRadians(point.getLatitude())) + Math.cos(Math.toRadians(this.getLatitude())) * Math.cos(Math.toRadians(point.getLatitude())) * Math.cos(Math.toRadians(point.getLongitude()) - Math.toRadians(this.getLongitude()))) * 6378.137;
    }
    
    @Override
    public Point clone() {
        return new Point(this.latitude, this.longitude);
    }
}