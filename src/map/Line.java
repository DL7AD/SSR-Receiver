package map;

public class Line {
    private Point pointA;
    private Point pointB;
    
    public Line(Point pointA, Point pointB) {
        this.setPointA(pointA);
        this.setPointB(pointB);
    }
    private void setPointA(Point point) {
        this.pointA = point;
    }
    public Point getPointA() {
        return this.pointA;
    }
    private void setPointB(Point point) {
        this.pointB = point;
    }
    public Point getPointB() {
        return this.pointB;
    }
    /**
     * Calculates the course between two points
     * @param a Point A
     * @param b Point B
     * @return Course
     */
    public Float calculateCourse() {
        double f = 1/298.257223563d; // WGS-84 ellipsoid params
        double L = Math.toRadians(this.pointA.getLongitude() - this.pointB.getLongitude());
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(this.pointB.getLatitude())));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(this.pointA.getLatitude())));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);
        
        double lambda = L, lambdaP, iterLimit = 100d;
        double cosSqAlpha, cos2SigmaM, sinSigma, cosSigma, sigma, sinLambda, cosLambda;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) + 
            (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
            if(sinSigma==0) // co-incident points
                return 0f;
            cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha*sinAlpha;
            if(cosSqAlpha != 0) {
                cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
            } else {
                cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0 (§6)
            }
            double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1-C) * f * sinAlpha *
            (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
        } while(Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);
        
        if(iterLimit == 0)
            return 0f; // formula failed to converge
        
        // note: to return initial/final bearings in addition to distance, use something like:
        double fwdAz = Math.round(Math.toDegrees(Math.atan2(cosU2*sinLambda,  cosU1*sinU2-sinU1*cosU2*cosLambda)) * 10) / 10d;
        
        if(fwdAz < 0)
            fwdAz += 360;
        
        return (float)fwdAz;
    }
}