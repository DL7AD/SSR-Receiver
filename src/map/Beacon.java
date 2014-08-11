package map;

public class Beacon extends Intersection {
    private float frequency;
    private Point point;
    protected void setFrequency(float frequency) {
        this.frequency = frequency;
    }
    public float getFrequency() {
        return this.frequency;
    }
    protected void setPoint(Point point) {
        this.point = point;
    }
    public Point getPoint() {
        return this.point;
    }
}