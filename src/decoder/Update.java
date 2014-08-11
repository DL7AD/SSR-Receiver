package decoder;

import map.Point;

public class Update {
    //Idendity
    private Integer icaoIdent; //Hex Ident
    private Short squawk; //Squawk
    private String flightIdent; //Flightnumber
    
    //Properties
    private Integer altitude; //Altitude in feet
    private Point position; //Position of the aircraft
    private Float groundspeed; //Groundspeed
    private Integer airspeed; //Airspeed
    private Float track; //True Track
    private Float heading; //Heading
    private Short verticalRate; //Vertical rate/speed
    
    //Packet Information
    private Byte df; //Downlink format
    private boolean modeS;
    private boolean military;
    
    public Update(int icaoIdent) {
        this.icaoIdent = icaoIdent;
    }
    
    public void setSquawk(short squawk) {
        this.squawk = squawk;
    }
    public void setFlightIdent(String flightIdent) {
        this.flightIdent = flightIdent;
    }
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }
    public void setPosition(Point position) {
        this.position = position;
    }
    public void setGroundspeed(float groundspeed) {
        this.groundspeed = groundspeed;
    }
    public void setAirspeed(int airspeed) {
        this.airspeed = airspeed;
    }
    public void setTrack(float track) {
        this.track = track;
    }
    public void setHeading(float heading) {
        this.heading = heading;
    }
    public void setVerticalRate(short verticalRate) {
        this.verticalRate = verticalRate;
    }
    public void setDF(byte df) {
        this.df = df;
    }
    public void setModeS() {
        this.modeS = true;
    }
    public void setMilitary() {
        this.military = true;
    }
    
    public String getFlightIdentTable() {
        return Airspace.getInstance().getAircraft(this.icaoIdent).getFlightIdent();
    }
    public int getIcaoIdent() {
        return this.icaoIdent;
    }
    public String getFlightIdent() {
        return this.flightIdent;
    }
    public Byte getDF() {
        return this.df;
    }
    
    public String update() {
        StringBuilder str = new StringBuilder(100);
        str.append("[");
        if(this.squawk != null)
            str.append("SQ=" + this.squawk + " ");
        if(this.flightIdent != null)
            str.append("Ident=" + this.flightIdent + " ");
        if(this.altitude != null)
            str.append("ALT=" + this.altitude + " ");
        if(this.position != null)
            str.append("POS=" + (Math.round(this.position.getLatitude() * 1000) / 1000f) + "," + (Math.round(this.position.getLongitude() * 1000) / 1000f) + " ");
        if(this.groundspeed != null)
            str.append("GS=" + (Math.round(this.groundspeed * 10) / 10f) + " ");
        if(this.airspeed != null)
            str.append("AS=" + this.airspeed + " ");
        if(this.track != null)
            str.append("TR=" + (Math.round(this.track * 10) / 10f) + " ");
        if(this.heading != null)
            str.append("HDG=" + (Math.round(this.heading * 10) / 10f) + " ");
        if(this.verticalRate != null)
            str.append("VR=" + verticalRate + " ");
        if(this.modeS)
            str.append("MODE S");
        if(this.military)
            str.append("MILITARY");
        
        if(str.charAt(str.length() - 1) == ' ') {
            str.replace(str.length() - 1, str.length() - 1, "]");
        } else {
            str.append("]");
        }
        
        return str.toString();
    }
}