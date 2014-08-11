package map;

import java.util.Collection;

public class SID extends AirportAirway {
    public SID(String name, String airport, String runway) {
        super(name, Airway.SID);
        this.setAirport(airport);
        this.setRunway(runway);
    }
    
    public static void addSID(String airwayName, String airport, String runway, Line line) {
        map.SID.tryAddNewSID(airwayName, airport, runway, line);
    }
    public static void addSID(Line line) {
        map.SID.addLineToLastSID(line);
    }
    private static void tryAddNewSID(String airwayName, String airport, String runway, Line line) {
        Airway.tryAddNewAirway(new SID(airwayName, airport, runway), line);
        Airway.setLastName(airwayName);
    }
    private static void addLineToLastSID(Line line) {
        Airway.addLineToLastAirway(line);
    }
    public static Collection<Airway> getAll() {
        return Airway.getAll(Airway.SID);
    }
    public static SID getSelected() {
        return (SID)Airway.getSelected(Airway.SID);
    }
}