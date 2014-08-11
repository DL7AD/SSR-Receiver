package map;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class DotCollection {
    public static final int dotEverySeconds = 15;
    public static final int numberOfDots = 5;
    
    private long lastAdd;
    Vector<Dot> dots = new Vector<Dot>(5);
    
    public void tryAddDot(Point point) {
        if(this.lastAdd + DotCollection.dotEverySeconds * 1000 > System.currentTimeMillis()) //There was paint a dot less than dotEverySeconds seconds ago
            return;
        
        this.dots.add(new Dot(point));
        this.lastAdd = System.currentTimeMillis();
    }
    public void tryDeleteObsoleteDots() {
        Iterator<Dot> dots = this.dots.iterator();
        while(dots.hasNext()) {
            Dot dot = dots.next();
            if(dot.mustBeRemoved())
                dots.remove();
        }
    }
    public Collection<Dot> getAll() {
        this.tryDeleteObsoleteDots();
        return (Collection<Dot>)this.dots.clone();
    }
}