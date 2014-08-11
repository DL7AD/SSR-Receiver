package map;

import java.awt.Graphics;

public class Dot extends Earth implements Paintable {
    private Point point;
    private long timeCreated;
    
    public Dot(Point point) {
        this.point = point;
        this.timeCreated = System.currentTimeMillis();
    }
    
    public boolean mustBeRemoved() {
        return this.timeCreated + DotCollection.dotEverySeconds * DotCollection.numberOfDots * 1000 < System.currentTimeMillis();
    }

    @Override
    public void paint(Graphics g) {
        g.fillRect(point.getX() - 1, point.getY() - 1, 2, 2);
    }
}
