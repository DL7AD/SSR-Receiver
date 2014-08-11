package map;

import java.util.Collection;

public class STAR extends AirportAirway {
    public STAR(String name, String airport, String runway) {
        super(name, Airway.STAR);
        this.setAirport(airport);
        this.setRunway(runway);
    }
    
    public static void addSTAR(String airwayName, String airport, String runway, Line line) {
        map.STAR.tryAddNewSTAR(airwayName, airport, runway, line);
    }
    public static void addSTAR(Line line) {
        map.STAR.addLineToLastSTAR(line);
    }
    private static void tryAddNewSTAR(String airwayName, String airport, String runway, Line line) {
        Airway.tryAddNewAirway(new STAR(airwayName, airport, runway), line);
        Airway.setLastName(airwayName);
    }
    private static void addLineToLastSTAR(Line line) {
        Airway.addLineToLastAirway(line);
    }
    public static Collection<Airway> getAll() {
        return Airway.getAll(Airway.STAR);
    }
    public static STAR getSelected() {
        return (STAR)Airway.getSelected(Airway.STAR);
    }
}