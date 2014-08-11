package map;

public class Intersection extends Earth {
    public static Point getIntersection(String name) {
        Fix fix = Fix.getFix(name);
        if(fix != null)
            return fix.getPoint();
        
        VOR vor = VOR.getVOR(name);
        if(vor != null)
            return vor.getPoint();
        
        NDB ndb = NDB.getNDB(name);
        if(ndb != null)
            return ndb.getPoint();
        
        return null;
    }
    public static Intersection getIntersection(Point point) {
        for(Fix fix: Fix.getAll())
            if(fix.getPoint() == point)
                return fix;
        
        for(NDB ndb: NDB.getAll())
            if(ndb.getPoint() == point)
                return ndb;
        
        for(VOR vor: VOR.getAll())
            if(vor.getPoint() == point)
                return vor;
        
        return null;
    }
}