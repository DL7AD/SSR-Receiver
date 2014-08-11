package map;
import gui.RouteSelector;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Airway extends Earth implements Paintable {
    private static HashMap<String,Airway> list = new HashMap<String,Airway>(1000, 0.75f); //List of all airways
    
    //Type of airway
    public static final int LOW = 1;
    public static final int HIGH = 2;
    public static final int SID = 3;
    public static final int STAR = 4;
    
    private static String lastName; //Name of last called airway
    
    protected boolean nameActivated; //Diplay name in map
    private ArrayList<Line> route = new ArrayList<Line>(10); //Collection of lines containing to the route
    private int type; //Type of airway
    
    public Airway(String name, int type) {
        this.setName(name);
        this.setType(type);
    }
    /**
     * Sets the type of the airway
     * @param type Type of the airway
     */
    private void setType(int type) {
        this.type = type;
    }
    /**
     * Returns the type of the airway
     * @return Type of the airway
     */
    public int getType() {
        return this.type;
    }
    /**
     * Adds a new line to the route of the airway
     * @param line Line that should be add
     */
    private void add(Line line) {
        this.route.add(line);
    }
    /**
     * Returns a copy of the route from this airway
     * @return Array of lines
     */
    public ArrayList<Line> getRoute() {
        return (ArrayList<Line>)this.route.clone();
    }
    /**
     * Returns a collection of all collected airway
     * @return List of airways
     */
    protected static Collection<Airway> getList() {
        return Airway.list.values();
    }
    /**
     * Tries to add a new airway if not existing. But nevertheless its adding
     * the line in the new or existing airway.
     * @param airway New airway object
     * @param line Line which should be added
     * @param type Type of airway
     */
    public static void tryAddNewAirway(Airway airway, Line line) {
        if(!Airway.list.containsKey(airway.getName()))
            Airway.list.put(airway.getName(), airway);
        Airway.list.get(airway.getName()).add(line);
        Airway.setLastName(airway.getName());
    }
    /**
     * Adds a line to the last called airway
     * @param line Line which should be added
     */
    public static void addLineToLastAirway(Line line) {
        Airway.list.get(Airway.getLastName()).add(line);
    }
    /**
     * Reset all airway
     */
    public static void resetAll() {
        Airway.list.clear();
    }
    /**
     * Returns a collection of all airway collected. The type of the airway must
     * be chosen.
     * @param type Type of the airway
     * @return Collection of airways
     */
    public static Collection<Airway> getAll(int type) {
        Collection<Airway> airways = new ArrayList<Airway>(1000);
        for(Airway airway: Airway.list.values())
            if(airway.getType() == type)
                airways.add(airway);
        return airways;
    }
    /**
     * Get by RouteSelector's selected airway (SID or STAR). If the route is not
     * found, null is returned.
     * @param type Type of the airway
     * @return Selected airway
     */
    public static Airway getSelected(int type) {
        String selected = RouteSelector.getInstance().getSelected(type);
        for(Airway airway: Airway.list.values())
            if(airway.getName().equals(selected)) //airway found
                return airway;
        return null; //No airway found
    }
    /**
     * Sets the configuration whether the name of the airway should be displayed
     * in the map or not.
     * @param nameActivated Display name
     */
    public static void setNameActivated(boolean nameActivated) {
        for(Airway airway: Airway.list.values())
            airway.nameActivated = nameActivated;
    }
    /**
     * Sets the name of the last called airway
     * @param name Name of the airway
     */
    public static void setLastName(String name) {
        Airway.lastName = name;
    }
    /**
     * Returns the name of the last called airway
     * @return Name of the airway
     */
    public static String getLastName() {
        return Airway.lastName;
    }
    
    /**
     * Paints the airway to the map
     * @param g Graphical element
     */
    @Override
    public void paint(Graphics g) {
        g.setColor(new java.awt.Color(0, 0, 0));
        for(Line line: this.getRoute()) {
            Point pointA = line.getPointA();
            Point pointB = line.getPointB();
            if((Math.abs(pointA.getX()-500) > 5000 || Math.abs(pointA.getY()-500) > 5000) && (Math.abs(pointB.getX()-500) > 5000 || Math.abs(pointB.getY()-500) > 5000))
                continue;
            g.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
            if(this.nameActivated && (this.getType() == Airway.HIGH || this.getType() == Airway.LOW))
                g.drawString(this.getName(), (pointA.getX() + pointB.getX()) / 2, (pointA.getY() + pointB.getY()) / 2);
        }
    }
}