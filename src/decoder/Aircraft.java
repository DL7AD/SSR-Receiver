package decoder;

import exception.ACASInoperativeException;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import map.Dot;
import map.DotCollection;
import map.Paintable;
import map.Point;
import gui.Receiver;
import gui.Map;

/**
 * Represents an aircraft in the airspace. This class includes the decoder for
 * received squitters. The decode methods splits tasks up into parts. This tasks
 * are calculated by the calculating methods. These methods applies the decoded
 * data in the aircrafts object.
 * This class provides also information about different timeouts and table
 * updater methods.
 * 
 * Functions which are not included: Full ACAS System
 *                                   TIS-B
 *                                   ELM
 *                                   Military (encrypted, unable to decode)
 * 
 * This class provides Getter-Methods to get decoded values of squitter packets
 * 
 * @author Sven Steudte DL7AD DOK D20
 * @version 0.4
 */
public class Aircraft implements Paintable {
    
    //Constants
    public final static short MAX_RANGE = 300;                      //Max Range of Aircraft in NM to be decoded (Validity check)
    public final static char[] CATEGORY = {'D','C','B','A'};        //Aircraft categories
    
    public final static byte RECEIVER_TIMEOUT = 5;                  //Elapsed time to remove receiver in seconds
    public final static float PACKET_TIMEOUT_TIME = 1.1f;           //Elapsed time of position packet expiry in seconds for time synchronized airplanes
    public final static float PACKET_TIMEOUT_NTIME = 5.0f;          //Elapsed time of position packet expiry in seconds for time non synchronized airplanes
    public final static byte TABLE_TIMEOUT = 30;                    //Elapsed time to remove aircraft from table in seconds
    public final static byte MAP_TIMEOUT = 30;                      //Elapsed time to remove aircraft from map while no position was calculated in seconds
    public final static byte DATA_TIMEOUT = 120;                    //Elapsed time to remove aircrafts data in seconds
    
    public static int AIRPLANE_VECTOR_LENGTH = 1;                   //Vector size of aircraft movement in minutes
    
    public final static int AIRBORNE = 1;                           //Indicates Airborne Position message
    public final static int SURFACE = 2;                            //Indicates Surface Position message
    public final static int TISB = 3;                               //Indicates TIS-B Position message
    
    public final static int NO_DOWNLINK_REQUEST = 4;                //No downlink request
    public final static int REQUEST_TO_SEND_COMMB_MESSAGE = 5;      //Request to send COMM B Message
    public final static int COMMB_BROADCAST_MESSAGE1_AVAILABLE = 6; //COMM B Broadcast 1 message available
    public final static int COMMB_BROADCAST_MESSAGE2_AVAILABLE = 7; //COMM B Broadcast 2 message available
    public final static int ELM_REQUEST = 8;                        //ELM Request
    
    public final static int GEOMETRIC = 9;                          //Indicates Altitude is measured by GNSS
    public final static int BAROMETRIC = 10;                        //Andicates Altitude is measured by Altitude indicator
    
    public final static int NO_CONDITION_INFORMATION = 11;          //Indicates no surveillance condition information
    public final static int PERMANENT_ALERT = 12;                   //Indicates permanent alert
    public final static int TEMPORARY_ALERT = 13;                   //Indicates temporary alert
    public final static int SPI_CONDITION = 14;                     //Indicates Special Position Indication surveillance
    
    public final static int IAS = 15;                               //Indicates sent Airspeed is IAS
    public final static int TAS = 16;                               //Indicated sent Airspeed is TAS
    
    public final static int NO_EMERGENCY = 17;                      //Indicates aircraft has no emergency
    public final static int GENERAL_EMERGENCY = 18;                 //Indicates general emergency
    public final static int MEDICAL_EMERGENCY = 19;                 //Indicates medical emergency
    public final static int MINIMUM_FUEL = 20;                      //Indicates minimum fuel aboard the aircraft
    public final static int NO_COMMUNICATIONS = 21;                 //Indicates communication failure
    public final static int UNLAWFUL_INTERFERENCE = 22;             //Indicates unlawful interference
    public final static int DOWNED_AIRCRAFT = 23;                   //Indicates downes aircraft
    
    public final static int VELOCITY_ERROR_10M_AND_MORE = 24;       //Indicates velocity error is more than 10m
    public final static int VELOCITY_ERROR_MAX_10M = 25;            //Indicates velocity error is 3 - 10m
    public final static int VELOCITY_ERROR_MAX_3M = 26;             //Indicates velocity error is 1 - 3m
    public final static int VELOCITY_ERROR_MAX_1M = 27;             //Indicates velocity error is 0.3 - 1m
    public final static int VELOCITY_ERROR_MAX_0_3M = 28;           //Indicates velocity error is less than 0.3m
    
    //Calculation Data
    private long[] oddPosition = new long[4];                       //Raw Odd packet (0: Latitude, 0: Longitude, 1: LastUpdate)
    private long[] evenPosition = new long[4];                      //Raw Even packet (0: Latitude, 0: Longitude, 1: LastUpdate)
    
    //General Properties
    private int icaoIdent;                                          //Hex Ident (unique transponder ident)
    private Short squawk;                                           //Squawk
    private String flightIdent;                                     //Flight Ident (Flightnumber or Registration)
    
    private Character category;                                     //Category
    private Point position;                                         //Position
    private Point lastMapPosition;                                  //Last position painted on the map
    private Integer altitude;                                       //Altitude in feet
    private Short verticalRate;                                     //Vertical rate in feet/min
    private Float groundspeed;                                      //Groundspeed
    private Float track;                                            //True Track
    private Integer airspeed;                                       //Airspeed
    private Integer airspeedType;                                   //Airspeed Type (TAS or IAS)
    private Float heading;                                          //Heading
    
    //System Properties
    private Integer navigationAccuracyForVelocity;                  //Navigation Accuracy for velocity
    private Integer verticalRateSource;                             //Vertical rate source (Barometric or Geometric)
    private Short differenceFromBarometricAltitude;                 //Difference between Barometric and Geometric Altitude (positive = Above Baro, negative = Below Baro Alt)
    private Boolean antenna;                                        //Has signle antenna
    private Boolean time;                                           //Indicates a correct UTC even and odd transmission. E.g. even packets should be send at second 14.2, 14.4, 14.6 and so on - odd packets e.g. at 14.1, 14.3, 14.5 and so on.
    private boolean isMilitary;                                     //Indicates aircraft is military
    
    //Aircraft status
    private Boolean intentChangeFlag;                               //Intent to send a Airborne Velocity Message
    private Boolean specialPositionIdentification;                  //Has Special Position Identification aboard
    private Boolean airborne;                                       //Indicates aircraft is airborne
    private Boolean alert;                                          //Indicates aircraft has an alert
    private Integer emergencyStatus;                                //Indicates an emergency status
    private Integer downlinkRequest;                                //Indicates type of Downlink request
    private Integer surveillance;                                   //Indicates surveillance status
    private Byte utilityMessage;                                    //Indicates Utility message
    
    //Transponder capabilities
    private Boolean continuationFlag;
    private Byte ACASCapability;
    private Byte modeSSubnetworkVersionNumber;
    private Boolean transponderEnhancedProtocolIndicator;
    private Boolean specificServicesCapability;
    private Byte uplinkELMCapability;
    private Byte downlinkELMCapability;
    private Boolean aircraftIdentificationCapability;
    private Boolean squitterCapabilitySubfield;
    private Boolean surveillanceIdentifierCodeCapability;
    private Boolean commonUsageGICBCapabilityReport;
    private Integer statusOfDTESubAdress;
    private Boolean CDTIOperational;
    private Boolean TCASOperational;
    private Boolean ACASOperational;
    private Boolean crossLinkCapability;
    private Byte sensityLevel;
    private Short maximumAirspeed;
    
    //Packet
    private long lastChange;                                                    //Indices time of last Packet
    private long lastPositionChange;                                            //Indicates time of last Position change
    private int[] packetsCount = new int[32];                                   //Count of good packets which have been received
    private Squitter lastPacket;                                                //Raw data of last received Packet
    private Hashtable<String,Long> receivers = new Hashtable<String,Long>(5);   //Receivers receiving this aircraft
    
    //Table
    private int tablePosition = -1;                                             //Row Position in table
    
    //Map
    private DotCollection dotCollection;                                        //Collection of Dots behind the aircraft
    private ArrayList<Point> positionCollection = new ArrayList<Point>(50);     //Whole track of aircraft
    private boolean isClicked;                                                  //Set true if aircraft is clicked on the map
    
    //Statistics
    private short statsModeS;
    private short statsADSB;
    private short statsMisc;
    
    private short statsAcquisition;
    private short statsSquawk;
    private short statsAltitudeModeS;
    private short statsAltitudeADSB;
    private short statsAltitudeMisc;
    private short statsPosition;
    private short statsVelocity;
    private short statsACAS;
    private short statsIdent;
    private short statsADSR;
    
    /**
     * Creates a new airplane object
     * @param icaoIdent ICAO ident of the airplane
     */
    public Aircraft(int icaoIdent) {
        this.icaoIdent = icaoIdent;
        this.dotCollection = new DotCollection();
    }
    
    
    
    /* Decoding/Calculating - Assign data or interpret data into specific values */
    
    public void setTCASOperational(boolean TCASOperational) {
        this.TCASOperational = TCASOperational;
    }
    public void setACASOperational(boolean ACASOperational) {
        this.ACASOperational = ACASOperational;
    }
    public void setCDTIOperational(boolean CDTIOperational) {
        this.CDTIOperational = CDTIOperational;
    }
    public void setCategory(char category) {
        this.category = category;
    }
    public void setTime(boolean time) {
        this.time = time;
    }
    public void setAntenna(boolean antenna) {
        this.antenna = antenna;
    }
    public void setIntentChangeFlag(boolean intentChangeFlag) {
        this.intentChangeFlag = intentChangeFlag;
    }
    public void setAirborne(boolean airborne) {
        this.airborne = airborne;
    }
    public void setCrossLinkCapability(boolean crossLinkCapability) {
        this.crossLinkCapability = crossLinkCapability;
    }
    public void setSensityLevel(byte sensityLevel) {
        this.sensityLevel = sensityLevel;
    }
    public void setSpecialPositionIdentification(boolean specialPositionIdentification) {
        this.specialPositionIdentification = specialPositionIdentification;
    }
    public void setAlert(boolean alert) {
        this.alert = alert;
    }
    public void setUtilityMessage(byte utilityMessage) {
        this.utilityMessage = utilityMessage;
    }
    public void setContinuationFlag(boolean continuationFlag) {
        this.continuationFlag = continuationFlag;
    }
    public void setACASCapability(byte ACASCapability) {
        this.ACASCapability = ACASCapability;
    }
    public void setModeSSubnetworkVersionNumber(byte modeSSubnetworkVersionNumber) {
        this.modeSSubnetworkVersionNumber = modeSSubnetworkVersionNumber;
    }
    public void setTransponderEnhancedProtocolIndicator(boolean transponderEnhancedProtocolIndicator) {
        this.transponderEnhancedProtocolIndicator = transponderEnhancedProtocolIndicator;
    }
    public void setSpecificServicesCapability(boolean specificServicesCapability) {
        this.specificServicesCapability = specificServicesCapability;
    }
    public void setUplinkELMCapability(byte uplinkELMCapability) {
        this.uplinkELMCapability = uplinkELMCapability;
    }
    public void setDownlinkELMCapability(byte downlinkELMCapability) {
        this.downlinkELMCapability = downlinkELMCapability;
    }
    public void setAircraftIdentificationCapability(boolean aircraftIdentificationCapability) {
        this.aircraftIdentificationCapability = aircraftIdentificationCapability;
    }
    public void setSquitterCapabilitySubfield(boolean squitterCapabilitySubfield) {
        this.squitterCapabilitySubfield = squitterCapabilitySubfield;
    }
    public void setSurveillanceIdentifierCodeCapability(boolean surveillanceIdentifierCodeCapability) {
        this.surveillanceIdentifierCodeCapability = surveillanceIdentifierCodeCapability;
    }
    public void setCommonUsageGICBCapabilityReport(boolean commonUsageGICBCapabilityReport) {
        this.commonUsageGICBCapabilityReport = commonUsageGICBCapabilityReport;
    }
    public void setStatusOfDTESubAdress(int statusOfDTESubAdress) {
        this.statusOfDTESubAdress = statusOfDTESubAdress;
    }
    public void setMilitary() {
        this.isMilitary = true;
    }
    /**
     * Adds Zeros as prefix at the beginning of a string
     * @param number Number that should be converted
     * @param prefixZero Numbers before decimal
     * @return Converted number
     */
    private String numberFormat(String number, int prefixZero) {
        while(number.length() < prefixZero)
            number = "0" + number;
        return number;
    }
    /**
     * Adds Zeros as prefix at the beginning of a string
     * @param number Number that should be converted
     * @param prefixZero Numbers before decimal
     * @return Converted number
     */
    private String numberFormat(int number, int prefixZero) {
        return this.numberFormat(String.valueOf(number), prefixZero);
    }
    /**
     * Decodes Flight Ident
     * @param ident Raw data bits
     */
    public void decodeFlightIdent(long ident) {
        String bits = Long.toBinaryString(ident);
        bits = numberFormat(bits, 48);
        
        String decIdent = "";
        for(int i = 0; i < 8; i++) {
            byte ind = Byte.parseByte(bits.substring(i*6, i*6+6), 2);
            decIdent += this.decodeChar(ind);
        }
        
        this.flightIdent = decIdent;
    }
    /**
     * Interprete an integer to its specific character for decoding the Flight
     * Ident.
     * @param chr Integer character
     * @return Decoded character
     */
    private String decodeChar(byte chr) {
        if(chr == 32)
            return "";
        if(chr > 47 && chr < 68)
            return String.valueOf(chr-48);
        String[] table = {"","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        return table[chr];
    }
    /**
     * Calculates aircrafts new position by even and odd position packets.
     * Runs also methods to calculate course and speed.
     * @see http://www.lll.lu/~edward/edward/adsb/DecodingADSBposition.html
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting9/1090-WP-9-14.pdf
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting29/1090-WP29-07-Draft_CPR101_Appendix.pdf
     * @see http://www.bazl.admin.ch/dokumentation/grundlagen/02643/index.html?lang=de&download=NHzLpZeg7t,lnp6I0NTU042l2Z6ln1acy4Zn4Z2qZpnO2Yuq2Z6gpJCDeoB9fWym162epYbg2c_JjKbNoKSn6A--
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-18-DRAFT_DO-260B-V42.pdf Page 908ff
     */
    public void calculatePosition(boolean cprFormat, int rawLatitude, int rawLongitude, int flightStatus, boolean time) {
        //Set packet
        long[] packet = {rawLatitude, rawLongitude, System.currentTimeMillis(), flightStatus};
        if(cprFormat) { //Odd
            this.oddPosition = packet;
        } else { //Even
            this.evenPosition = packet;
        }
        
        //Packets with a timeout of x seconds or longer are deprecated and will not calculated
        if(Math.abs(this.evenPosition[2] - this.oddPosition[2]) >= (time ? Aircraft.PACKET_TIMEOUT_TIME : Aircraft.PACKET_TIMEOUT_NTIME) * 1000)
            return;
        
        //Even and odd packet does not have the same flight status
        if(this.evenPosition[3] != this.oddPosition[3])
            return;
        
        //Determine Type
        double na = 0;
        if(flightStatus == Aircraft.AIRBORNE) {
            na = 360;
        } else if(flightStatus == Aircraft.SURFACE) {
            na = 90;
        }
        
        //Calculate Dlat
        double dlat0 = na / 60d;
        double dlat1 = na / new Double(60 - 1);
        
        //Calculate j
        int j = new Double(Math.floor( ((59d * this.evenPosition[0] - 60d * this.oddPosition[0]) / 131072d) + 0.5d )).intValue();
        
        //Calculate rlat for both packets
        double rlat0 = dlat0 * (this.calculateModulo(j,60) + this.evenPosition[0] / 131072d);
        double rlat1 = dlat1 * (this.calculateModulo(j,59) + this.oddPosition[0]  / 131072d);
        
        //Calculate NL for both packets and check if NL(rlat0) == NL(rlat1)
        int nl0 = this.determineNL(rlat0);
        int nl1 = this.determineNL(rlat1);
        if(nl0 != nl1) //Does not equal
            return;
        
        //Both packets are ok - find out newest latitude and put it into this.latitude
        double decLatitude;
        byte newer; //Defines, which packet ist newer
        if(this.evenPosition[2] < this.oddPosition[2]) { //Odd packet is newer
            decLatitude = rlat1;
            newer = 1;
        } else { //Even packet is newer
            decLatitude = rlat0;
            newer = 0;
        }
        
        //Southern hemisphere is shown as 270-360 degree
        if(decLatitude > 180)
            decLatitude -= 360;
        
        //Calculate n(i)
        int ni = nl0 - newer;
        
        //Calculate d_lon for one packet because nl0 and nl1 equals
        double dlon;
        if(ni > 0) {
            dlon = na / ni;
        } else {
            dlon = na;
            return;
        }
        
        //Calculate M
        int nlt = newer == 1 ? this.determineNL(rlat1) : this.determineNL(rlat0);
        int m = new Double(Math.floor((((this.evenPosition[1] * (nlt - 1)) - (this.oddPosition[1] * nlt)) / 131072d) + 0.5d)).intValue();
        
        //Calculate final longitude
        double decLongitude = dlon * ( this.calculateModulo(m, ni) + (newer == 1 ? this.oddPosition[1] : this.evenPosition[1]) / 131072d);
        
        //Western hemisphere is shown as 270-360 degree
        if(decLongitude > 180)
            decLongitude -= 360;
        
        //Validate Position
        Double distanceToLastPoint = this.calculateParabolicDistance(new Point(decLatitude, decLongitude));
        if(distanceToLastPoint != null && distanceToLastPoint > Aircraft.MAX_RANGE)
            return;
        
        //Update Position
        this.position = new Point(decLatitude, decLongitude);
        this.positionCollection.add(this.position);
        
        //Timeout update
        this.lastPositionChange = System.currentTimeMillis();
        
        //Paint dot
        this.dotCollection.tryAddDot(this.position);
    }
    /**
     * Calculates modulo by a specific pattern
     * @param val Value
     * @param modval Divisor
     * @return Modulo value
     */
    private int calculateModulo(int val, int modval) {
        int modulo = val % modval;
        if(val < 0)
            modulo = modulo + modval;
        return modulo;
    }
    /**
     * Calculates NL by reading a table.
     * There is a little deviation between the calculation of southern and
     * northern hemishere. This is described in th second source on page 30.
     * The returned value is not corrected to this deviation.
     * @param latitude Latitude of r_lat calculation
     * @return Returns specified NL-value
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting9/1090-WP-9-14.pdf
     * @see http://diseqc.org.ua/projects/hard/adsb/1090-WP30-12-Proposed%20_New_Appendix-CPR101.pdf
     */
    private byte determineNL(double latitude) {
        if(Math.abs(latitude) < 10.47047130) {return 59;
        } else if(Math.abs(latitude) < 14.82817437) {return 58;
        } else if(Math.abs(latitude) < 18.18626357) {return 57;
        } else if(Math.abs(latitude) < 21.02939493) {return 56;
        } else if(Math.abs(latitude) < 23.54504487) {return 55;
        } else if(Math.abs(latitude) < 25.82924707) {return 54;
        } else if(Math.abs(latitude) < 27.93898710) {return 53;
        } else if(Math.abs(latitude) < 29.91135686) {return 52;
        } else if(Math.abs(latitude) < 31.77209708) {return 51;
        } else if(Math.abs(latitude) < 33.53993436) {return 50;
        } else if(Math.abs(latitude) < 35.22899598) {return 49;
        } else if(Math.abs(latitude) < 36.85025108) {return 48;
        } else if(Math.abs(latitude) < 38.41241892) {return 47;
        } else if(Math.abs(latitude) < 39.92256684) {return 46;
        } else if(Math.abs(latitude) < 41.38651832) {return 45;
        } else if(Math.abs(latitude) < 42.80914012) {return 44;
        } else if(Math.abs(latitude) < 44.19454951) {return 43;
        } else if(Math.abs(latitude) < 45.54626723) {return 42;
        } else if(Math.abs(latitude) < 46.86733252) {return 41;
        } else if(Math.abs(latitude) < 48.16039128) {return 40;
        } else if(Math.abs(latitude) < 49.42776439) {return 39;
        } else if(Math.abs(latitude) < 50.67150166) {return 38;
        } else if(Math.abs(latitude) < 51.89342469) {return 37;
        } else if(Math.abs(latitude) < 53.09516153) {return 36;
        } else if(Math.abs(latitude) < 54.27817472) {return 35;
        } else if(Math.abs(latitude) < 55.44378444) {return 34;
        } else if(Math.abs(latitude) < 56.59318756) {return 33;
        } else if(Math.abs(latitude) < 57.72747354) {return 31;
        } else if(Math.abs(latitude) < 58.84763776) {return 30;
        } else if(Math.abs(latitude) < 59.95459277) {return 30;
        } else if(Math.abs(latitude) < 61.04917774) {return 29;
        } else if(Math.abs(latitude) < 62.13216659) {return 28;
        } else if(Math.abs(latitude) < 63.20427479) {return 27;
        } else if(Math.abs(latitude) < 64.26616523) {return 26;
        } else if(Math.abs(latitude) < 65.31845310) {return 25;
        } else if(Math.abs(latitude) < 66.36171008) {return 24;
        } else if(Math.abs(latitude) < 67.39646774) {return 23;
        } else if(Math.abs(latitude) < 68.42322022) {return 22;
        } else if(Math.abs(latitude) < 69.44242631) {return 21;
        } else if(Math.abs(latitude) < 70.45451075) {return 20;
        } else if(Math.abs(latitude) < 71.45986473) {return 19;
        } else if(Math.abs(latitude) < 72.45884545) {return 18;
        } else if(Math.abs(latitude) < 73.45177442) {return 17;
        } else if(Math.abs(latitude) < 74.43893416) {return 16;
        } else if(Math.abs(latitude) < 75.42056257) {return 15;
        } else if(Math.abs(latitude) < 76.39684391) {return 14;
        } else if(Math.abs(latitude) < 77.36789461) {return 13;
        } else if(Math.abs(latitude) < 78.33374083) {return 12;
        } else if(Math.abs(latitude) < 79.29428225) {return 11;
        } else if(Math.abs(latitude) < 80.24923213) {return 10;
        } else if(Math.abs(latitude) < 81.19801349) {return 9;
        } else if(Math.abs(latitude) < 82.13956981) {return 8;
        } else if(Math.abs(latitude) < 83.07199445) {return 7;
        } else if(Math.abs(latitude) < 83.99173563) {return 6;
        } else if(Math.abs(latitude) < 84.89166191) {return 5;
        } else if(Math.abs(latitude) < 85.75541621) {return 4;
        } else if(Math.abs(latitude) < 86.53536998) {return 3;
        } else if(Math.abs(latitude) < 87.00000000) {return 2;
        } else {return 1;}
    }
    /**
     * Calculate Mode S Altitude
     * @see http://miniadsb.forumprofi.de/miniadsb-decoder-software-f10/decoding-df-17-extended-squitter-t41.html
     * @param w Altitude as bit raw data
     */
    public void decodeModeSAltitude(int w) {
        Integer result;
        int qbit = (w & 0x0010) >> 4;
        if(qbit == 0) { //100 feet resolution
            result = this.decodeAltitudeLessResolution(w);
        } else { //25 feet resolution
            int i = (((w & 0x1F80) >> 2) + ((w & 0x20) >> 1) + (w & 0xF)) * 25 - 1000; //This is for Mode-S
            result = i == -1000 ? null : i;
        }
        this.altitude = result;
    }
    /**
     * Calculate ADSB Altitude
     * @param w Altitude as bit raw data
     */
    public void decodeADSBAltitude(int w) {
        Integer result;
        int qbit = (w & 0x0010) >> 4;
        if(qbit == 0) { //100 feet resolution
            result = this.decodeAltitudeLessResolution(w);
        } else { //25 feet resolution
            int i = (((w & 0x000f) | ((w & 0x0FE0) >> 1)) * 25 - 1000); //This is for ADS-B
            result = i == -1000 ? null : i;
        }
        this.altitude = result;
    }
    /**
     * Calculates Altitude with less resolution. Today this format got very
     * uncommon and is mostly only used by the military.
     * @param w Altitude as bit raw data
     * @return Altitude in feet
     */
    private int decodeAltitudeLessResolution(int w) {
        //11  10  9  8  * 7  6   5   4 * 3  2  1  0
        //c1  a1  c2 a2 * c4 a4  b1  0 * b2 d2 b4 d4
        int a = ((w & 0x0400) >> 8) | ((w & 0x0100) >> 7) | ((w & 0x0040) >> 6);
        int b = ((w & 0x0020) >> 3) | ((w & 0x0008) >> 2) | ((w & 0x0002) >> 1);
        int c = ((w & 0x0800) >> 9) | ((w & 0x0200) >> 8) | ((w & 0x0080) >> 7);
        int d = ((w & 0x0004) >> 1) | (w & 0x0001);
        return this.squawkCtoAlt(a, b, c, d);
    }
    /**
     * Serve action for calculateAltitudeLessResolution
     */
    private int squawkCtoAlt(int a, int b,int c, int d) {
        int dab = this.grayToBinary(d << 6 + a << 3 + b);
        int i = dab & 0x1;
        dab = (dab * 500) - 1000;
        int result;
        if(i == 0) {
            switch(c) {
                case 4:  result = dab + 200;
                case 6:  result = dab + 100;
                case 2:  result = dab;
                case 3:  result = dab - 100;
                case 1:  result = dab - 200;
                default: result = 0; //Altitude could not be decoded
            }
        } else {
            switch(c) {
                case 4:  result = dab - 200;
                case 6:  result = dab - 100;
                case 2:  result = dab;
                case 3:  result = dab + 100;
                case 1:  result = dab + 200;
                default: result = 0; //Altitude could not be decoded
            }
        }
        return result;
    }
    /**
     * Serve action for calculateAltitudeLessResolution
     */
    private int grayToBinary(int g) {
        int result = g & 0x80;
        result = result | ((g & 0x40) ^ ((result & 0x80) >> 1));
        result = result | ((g & 0x20) ^ ((result & 0x40) >> 1));
        result = result | ((g & 0x10) ^ ((result & 0x20) >> 1));
        result = result | ((g & 0x08) ^ ((result & 0x10) >> 1));
        result = result | ((g & 0x04) ^ ((result & 0x08) >> 1));
        result = result | ((g & 0x02) ^ ((result & 0x04) >> 1));
        result = result | ((g & 0x01) ^ ((result & 0x02) >> 1));
        return result;
    }
    /**
     * Calculates squawk
     * @param data Bit raw data
     */
    public void decodeSquawk(int data) {
        int a = ((data & 0x080) >> 5) | ((data & 0x200) >> 8) | ((data & 0x800) >> 11);
        int b = ((data & 0x002) << 1) | ((data & 0x008) >> 2) | ((data & 0x020) >> 5);
        int c = ((data & 0x100) >> 6) | ((data & 0x400) >> 9) | ((data & 0x1000) >> 12);
        int d = ((data & 0x001) << 2) | ((data & 0x004) >> 1) | ((data & 0x010) >> 4);
        this.squawk = (short)(a * 1000 + b * 100 + c * 10 + d);
    }
    /**
     * Calculates groundspeed of airborne airplanes.
     * @param ewVelocity East/West velocity
     * @param nsVelocity North/South velocity
     * @param isSupersonic Indicates supersonic airplane
     */
    public void calculateAirborneGroudspeed(short ewVelocity, short nsVelocity, boolean isSupersonic) {
        this.groundspeed = new Float(Math.sqrt(Math.pow(ewVelocity * 8, 2) + Math.pow(nsVelocity * 8, 2)) * (isSupersonic ? 0.5 : 0.125));
    }
    /**
     * Calculates airspeed of airborne airplanes.
     * @param speed Speed bit raw data
     * @param isSupersonic Indicates supersonic airplane
     */
    public void calculateAirborneAirspeed(short speed, boolean isSupersonic) {
        if(speed == 0)
            this.airspeed = null;
        this.airspeed = (speed - 1) * (isSupersonic ? 4 : 1);
    }
    /**
     * Calculates groundspeed of airplanes on the ground.
     * @param speed Bit raw data
     */
    public void calculateSurfaceGroundspeed(short speed) {
        if(speed == 0) {
            this.groundspeed = null;
        } else if(speed == 1) {
            this.groundspeed = 0f;
        } else if(speed == 2) {
            this.groundspeed = 0.125f;
        } else if(speed <= 8) {
            this.groundspeed = 0.125f * (speed - 1);
        } else if(speed <= 12) {
            this.groundspeed = 1 + 0.25f * (speed - 9);
        } else if(speed <= 38) {
            this.groundspeed = 2 + 0.5f * (speed - 13);
        } else if(speed <= 93) {
            this.groundspeed = speed - 24f;
        } else if(speed <= 108) {
            this.groundspeed = 70 + 2f * (speed - 94);
        } else if(speed <= 123) {
            this.groundspeed = 100 + 5f * (speed - 109);
        } else if(speed <= 124) {
            this.groundspeed = 175f;
        }
    }
    /**
     * Calculates air track when the airplane is airborne.
     * @param ewDirection Direction of East/West move
     * @param ewVelocity Speed in East/West direction
     * @param nsDirection Direction of North/South move
     * @param nsVelocity Speed in North/South direction
     */
    public void calculateAirborneTrack(boolean ewDirection, short ewVelocity, boolean nsDirection, short nsVelocity) {
        float track = (float)(Math.toDegrees(Math.atan(ewVelocity / (double)nsVelocity)));
        if(!nsDirection && !ewDirection) {
            this.track = track;
        } else if(nsDirection && !ewDirection) {
            this.track = 180 - track;
        } else if(nsDirection && ewDirection) {
            this.track = track + 180;
        } else if(!nsDirection && ewDirection) {
            this.track = 360 - track;
        }
    }
    /**
     * Calculates air heading when the airplane is airborne.
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-18-DRAFT_DO-260B-V42.pdf Page 920
     * @param track Surface track bits
     */
    public void calculateAirborneHeading(short heading) {
        this.heading = heading * 0.3515625f;
    }
    /**
     * Calculates surface track when the airplane moves on ground.
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-18-DRAFT_DO-260B-V42.pdf
     * @param track Surface track bits
     */
    public void calculateSurfaceTrack(short track) {
        this.track = track * 2.8125f;
    }
    /**
     * Calculates and sets the vertical speed/rate in feet/min.
     * @param msb Most significant bit (0: Ascending, 1: Descending)
     * @param verticalRate Vertical rate raw data
     * @see * http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-18-DRAFT_DO-260B-V42.pdf Page 919
     */
    public void calculateVerticalRate(boolean msb, short verticalRate) {
        if(verticalRate == 0) //No vertical rate info
            this.verticalRate = null;
        this.verticalRate = (short)((msb ? -1 : 1) * 64 * (verticalRate - 1));
    }
    /**
     * Calculates and sets the difference between geometric and barometric
     * altitude in feet.
     * @param msb Most significant bit (0: Above barometric altitude, 1: Below barometric altitude)
     * @param difference Difference between geometric and barometric altitude
     * @see * http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-18-DRAFT_DO-260B-V42.pdf Page 919
     */
    public void calculateDifferenceFromBarometricAltitude(boolean msb, short difference) {
        if(difference == 0) //No info given
            this.differenceFromBarometricAltitude = null;
        this.differenceFromBarometricAltitude = (short)((msb ? -1 : 1) * 25 * (difference - 1));
    }
    public void decodeMaximumAirspeed(byte max) {
        if(max == 0) {
            this.ACASOperational = false;
        } else if(max >= 8) {
            this.ACASOperational = true;
            switch(max) {
                case 9:  this.maximumAirspeed = 75; break;
                case 10: this.maximumAirspeed = 150; break;
                case 11: this.maximumAirspeed = 300; break;
                case 12: this.maximumAirspeed = 600; break;
                case 13: this.maximumAirspeed = 1200; break;
                case 14: this.maximumAirspeed = Short.MAX_VALUE; break;
                case 8: //No data available
                case 15: //Not assigned
                    this.maximumAirspeed = null;
            }
        }
    }
    public void decodeSurveillance(byte surveillance) {
        switch(surveillance) {
            case 0: this.surveillance = Aircraft.NO_CONDITION_INFORMATION; break;
            case 1: this.surveillance = Aircraft.PERMANENT_ALERT; break;
            case 2: this.surveillance = Aircraft.TEMPORARY_ALERT; break;
            case 3: this.surveillance = Aircraft.SPI_CONDITION;
        }
    }
    public void decodeDownlinkRequest(byte downlinkRequest) {
        switch(downlinkRequest) {
            case 0: this.downlinkRequest = Aircraft.NO_DOWNLINK_REQUEST; break;
            case 1: this.downlinkRequest = Aircraft.REQUEST_TO_SEND_COMMB_MESSAGE; break;
            case 4: this.downlinkRequest = Aircraft.COMMB_BROADCAST_MESSAGE1_AVAILABLE; break;
            case 5: this.downlinkRequest = Aircraft.COMMB_BROADCAST_MESSAGE2_AVAILABLE; break;
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                this.downlinkRequest = Aircraft.ELM_REQUEST;
                break;
            default:
                this.downlinkRequest = null;
        }
    }
    public void decodeNavigationAccuracyForVelocity(byte navigationAccuracyForVelocity) {
        switch(navigationAccuracyForVelocity) {
            case 0: this.navigationAccuracyForVelocity = Aircraft.VELOCITY_ERROR_10M_AND_MORE; break;
            case 1: this.navigationAccuracyForVelocity = Aircraft.VELOCITY_ERROR_MAX_10M; break;
            case 2: this.navigationAccuracyForVelocity = Aircraft.VELOCITY_ERROR_MAX_3M; break;
            case 3: this.navigationAccuracyForVelocity = Aircraft.VELOCITY_ERROR_MAX_1M; break;
            case 4: this.navigationAccuracyForVelocity = Aircraft.VELOCITY_ERROR_MAX_0_3M;
        }
    }
    public void decodeEmergencyStatus(byte emergencyStatus) {
        switch(emergencyStatus) {
            case 0: this.emergencyStatus = Aircraft.NO_EMERGENCY; break;
            case 1: this.emergencyStatus = Aircraft.GENERAL_EMERGENCY; break;
            case 2: this.emergencyStatus = Aircraft.MEDICAL_EMERGENCY; break;
            case 3: this.emergencyStatus = Aircraft.MINIMUM_FUEL; break;
            case 4: this.emergencyStatus = Aircraft.NO_COMMUNICATIONS; break;
            case 5: this.emergencyStatus = Aircraft.UNLAWFUL_INTERFERENCE; break;
            case 6: this.emergencyStatus = Aircraft.DOWNED_AIRCRAFT; break;
            case 7: this.emergencyStatus = null;
        }
    }
    public void decodeAirspeedType(boolean airspeedType) {
        this.airspeedType = airspeedType ? Aircraft.TAS : Aircraft.IAS;
    }
    public void decodeVerticalRateSource(boolean verticalRateSource) {
        this.verticalRateSource = verticalRateSource ? Aircraft.BAROMETRIC : Aircraft.GEOMETRIC;
    }
    
    
    
    /* Calculate Serive methodes for GUI with return values */
    
    /**
     * Returns the parabolic distance along the earths ground from the airplane
     * to the base station.
     * @return Distance in Nautical Miles
     */
    public Float calculateParabolicDistance() {
        if(this.position == null || Airspace.getMyPosition() == null) //Position was not calculates yet or there is no position to the base station
            return null;
        Double distance = Math.acos(Math.sin(Math.toRadians(Airspace.getMyPosition().getLatitude())) * Math.sin(Math.toRadians(this.position.getLatitude())) + Math.cos(Math.toRadians(Airspace.getMyPosition().getLatitude())) * Math.cos(Math.toRadians(this.position.getLatitude())) * Math.cos(Math.toRadians(this.position.getLongitude()) - Math.toRadians(Airspace.getMyPosition().getLongitude()))) * 3443.844;
        return Math.round(distance * 10) / 10f;
    }
    /**
     * Returns the parabolic distance along the earths ground from a fix poit
     * to the base station.
     * @param latitude Latitude of the fix point
     * @param longitude Longitude of the fix point
     * @return Distance in Nautical Miles
     */
    public Double calculateParabolicDistance(Point point) {
        if(Airspace.getMyPosition() == null) //The base stations position is set
            return null;
        return Math.acos(Math.sin(Math.toRadians(Airspace.getMyPosition().getLatitude())) * Math.sin(Math.toRadians(point.getLatitude())) + Math.cos(Math.toRadians(Airspace.getMyPosition().getLatitude())) * Math.cos(Math.toRadians(point.getLatitude())) * Math.cos(Math.toRadians(point.getLongitude()) - Math.toRadians(Airspace.getMyPosition().getLongitude()))) * 6378.388;
    }
    /**
     * Returns the linear distance between the base station and the airplane
     * @return Distance in meters
     */
    public Long calculateLinearDistance() {
        if(this.position == null || this.altitude == null || Airspace.getMyPosition() == null) //Position was not calculatet yet or the position of the base station is not set
            return null;
        
        //Converting WGS84-coordinates to cartesian coordinates
        double[] my = Airspace.getMyPosition().getCartesianCoordinates(5);
        double[] air = this.position.getCartesianCoordinates(this.altitude);

        //Calculating linear distance
        return Math.round(Math.sqrt(Math.pow(my[0] - air[0], 2) + Math.pow(my[1] - air[1], 2) + Math.pow(my[2] - air[2], 2)));
    }
    /**
     * Calculates geodetic distance between two points specified by latitude/longitude using 
     * Vincenty inverse formula for ellipsoids
     * 
     * Vincenty Inverse Solution of Geodesics on the Ellipsoid (c) Chris Veness 2002-2012
     * from: Vincenty inverse formula - T Vincenty, "Direct and Inverse Solutions of Geodesics on the
     *       Ellipsoid with application of nested equations", Survey Review, vol XXII no 176, 1975
     *       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
     * 
     * @param   {Number} lat1, lon1: first point in decimal degrees
     * @param   {Number} lat2, lon2: second point in decimal degrees
     * @returns (Number} distance in metres between points
     */
    public Float calculateBearing() {
        if(this.position == null || Airspace.getMyPosition() == null) //Position was not calculatet yet or the position of the base station is not set
            return null;
        
        double f = 1/298.257223563d; // WGS-84 ellipsoid params
        double L = Math.toRadians(this.position.getLongitude() - Airspace.getMyPosition().getLongitude());
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(Airspace.getMyPosition().getLatitude())));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(this.position.getLatitude())));
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
    
    /* Statistics */
    public void incrementStatsModeS() {
        this.statsModeS++;
    }
    public void incrementStatsADSB() {
        this.statsADSB++;
    }
    public void incrementStatsMisc() {
        this.statsMisc++;
    }
    
    public void incrementStatsAcquisition() {
        this.statsAcquisition++;
    }
    public void incrementStatsSquawk() {
        this.statsSquawk++;
    }
    public void incrementStatsAltitudeModeS() {
        this.statsAltitudeModeS++;
    }
    public void incrementStatsAltitudeADSB() {
        this.statsAltitudeADSB++;
    }
    public void incrementStatsAltitudeMisc() {
        this.statsAltitudeMisc++;
    }
    public void incrementStatsPosition() {
        this.statsPosition++;
    }
    public void incrementStatsVelocity() {
        this.statsVelocity++;
    }
    public void incrementStatsACAS() {
        this.statsACAS++;
    }
    public void incrementStatsIdent() {
        this.statsIdent++;
    }
    public void incrementStatsADSR() {
        this.statsADSR++;
    }
    
    
    /* Serve actions - Do serve actions for decode- or calculate- methods */
    
    /**
     * Removes aircrafts properties
     */
    public void removeData() {
        this.oddPosition = new long[3];
        this.evenPosition = new long[3];
        
        this.squawk = null;
        this.flightIdent = null;

        this.category = null;
        this.position = null;
        this.altitude = null;
        
        this.antenna = null;
        this.surveillance = null;
        
        this.airspeed = null;
        this.heading = null;

        this.airspeedType = null;
        this.groundspeed = null;
        
        this.track = null;

        this.intentChangeFlag = null;

        this.navigationAccuracyForVelocity = null;
        this.verticalRateSource = null;
        this.verticalRate = null;
        this.differenceFromBarometricAltitude = null;

        this.time = null;

        this.specialPositionIdentification = null;
        this.alert = null;
        this.airborne = null;
        this.downlinkRequest = null;
        this.utilityMessage = null;

        this.continuationFlag = null;
        this.ACASCapability = null;
        this.modeSSubnetworkVersionNumber = null;
        this.transponderEnhancedProtocolIndicator = null;
        this.specificServicesCapability = null;
        this.uplinkELMCapability = null;
        this.downlinkELMCapability = null;
        this.aircraftIdentificationCapability = null;
        this.squitterCapabilitySubfield = null;
        this.surveillanceIdentifierCodeCapability = null;
        this.commonUsageGICBCapabilityReport = null;
        this.statusOfDTESubAdress = null;
        this.CDTIOperational = null;
        this.TCASOperational = null;
        this.ACASOperational = null;
        this.crossLinkCapability = null;
        this.sensityLevel = null;
        this.maximumAirspeed = null;

        this.lastChange = 0;
        
        this.receivers = new Hashtable<String,Long>(5);
    }
    /**
     * Sets timestamp for last change for a specific Receiver
     * @param receiver COM Port of receiver
     */
    public void updateLastChange(String receiver) {
        //Set last update
        this.lastChange = System.currentTimeMillis();
        
        //Insert Receiver
        this.receivers.put(receiver, this.lastChange);
    }
    /**
     * Returns a list of all active receivers which can receive the airplane.
     * @return Active receivers
     */
    public ArrayList<String> getActiveReceivers() {
        ArrayList<String> receivers = new ArrayList<String>(5); //Collector
        
        //Check for obsolete Receivers
        Set set = this.receivers.entrySet();
        Iterator iter = set.iterator();

        while (iter.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iter.next();
            
            if(this.isReceiverTimeout(entry)) //Receiver is obsolete
                continue;
            
            //Add Receiver to list
            receivers.add(entry.getKey().toString());
        }
        
        return receivers;
    }
    /**
     * Increments packet count
     * @param df Downlink format
     */
    public void incrementPacketCount(short df) {
        this.packetsCount[df]++;
    }
    /**
     * Sets the last receiver packet
     * @param lastPacket Squitter
     */
    public void setLastPacket(Squitter lastPacket) {
        this.lastPacket = lastPacket;
    }
    /**
     * Sets the table position. Thats important for table data update.
     * @param tablePosition Table position
     */
    public void setTablePosition(int tablePosition) {
        this.tablePosition = tablePosition;
    }
    /**
     * Resets the table position. Thats important that no updates can be written
     * into the table.
     */
    public void resetTablePosition() {
        this.tablePosition = -1;
    }
    /**
     * Promts an table update for this aircraft
     */
    public void updateAircraftInTable() {
        Receiver.getInstance().updateAircraft(this);
    }
    /**
     * Returns true if a data remove is necessary. Data will be removed, because
     * the airplane is probably clearly out of range and not only in a
     * receiving interference section.
     * @return Data remove necessary
     */
    public boolean isDataTimeout() {
        long timeOver = System.currentTimeMillis() - this.getLastChange(); //Age of last packet
        return timeOver > Aircraft.DATA_TIMEOUT * 1000 && this.getLastChange() != 0; //Additional performance check that the airplane is not deleted every second
    }
    /**
     * Returns true if it is necessary to remove the aircraft out of the table.
     * The airplane could be in a receiving interference section. The data will
     * not be removed, so the airplanes recent data is shown when receiving
     * a new packet until dataTimeout is reached.
     * @return Table remove necessary
     */
    public boolean isTableTimeout() {
        long timeOver = System.currentTimeMillis() - this.getLastChange(); //Age of last packet
        return timeOver > Aircraft.TABLE_TIMEOUT * 1000;
    }
    /**
     * Returns true if it is necessary to remove the airplane from the map. The
     * timeout depends on the last received packet. Even when altitude packets
     * are received but no new position could be decoded, the airplane should be
     * removed from the map.
     * @return Map remove necessary
     */
    public boolean isMapTimeout() {
        long timeOver = System.currentTimeMillis() - this.getLastPositionChange(); //Age of last packet
        return timeOver > Aircraft.MAP_TIMEOUT * 1000;
    }
    /**
     * Returns true, if the airplane is not received by this receiver anymore.
     * @param receiver COM Port of receiver
     * @return Receiver timeout
     */
    public boolean isReceiverTimeout(java.util.Map.Entry receiver) {
        return Long.parseLong(receiver.getValue().toString()) + Aircraft.RECEIVER_TIMEOUT * 1000 < System.currentTimeMillis();
    }
    
    public Integer getPositionTimeout() {
        if(this.getLastPositionChange() == 0)
            return null;
        return (int)(System.currentTimeMillis() - this.getLastPositionChange()); //Age of last packet
    }
    public int getTimeout() {
        return (int)(System.currentTimeMillis() - this.getLastChange()); //Age of last packet
    }
    
    /**
     * Sets up the length of the vectorline of the airplane. The end of the 
     * vector shows the predicted point of the airplane in x minutes.
     * @param vectorLength Length of vector
     */
    public static void setVectorLength(int vectorLength) {
        Aircraft.AIRPLANE_VECTOR_LENGTH = vectorLength;
    }
    
    
    
    /* Getter - Returns aircrafts data */
    
    public int getIcaoIdent() { return this.icaoIdent; }
    public String getFlightIdent() { return this.flightIdent; }
    public Short getSquawk() { return this.squawk; }
    public Character getCategory() { return this.category; }
    public Integer getAltitude() { return this.altitude; }
    public Point getPosition() { return this.position; }
    public Point getLastMapPosition() { return this.lastMapPosition; }
    public Float getGroundspeed() { return this.groundspeed; }
    public Integer getAirspeed() { return this.airspeed; }
    public Float getTrack() { return this.track; }
    public Float getHeading() { return this.heading; }
    public Short getVerticalRate() { return this.verticalRate; }
    public Boolean hasSignleAntenna() { return this.antenna; }
    public Integer getAirspeedType() { return this.airspeedType; }
    public Boolean getIntentChangeFlag() { return this.intentChangeFlag; }
    public Integer getNavigationAccuracyForVelocity() { return this.navigationAccuracyForVelocity; }
    public Integer getVerticalRateSource() { return this.verticalRateSource; }
    public Short getDifferenceFromBarometricAltitude() { return this.differenceFromBarometricAltitude; }
    public Boolean isSynchronized() { return this.time; }
    public Boolean hasSpecialPositionIdentification() { return this.specialPositionIdentification; }
    public Boolean hasAlert() { return this.alert; }
    public Boolean isAirborne() { return this.airborne; }
    public Integer getEmergencyStatus() { return this.emergencyStatus; }
    public Integer getDownlinkRequest() { return this.downlinkRequest; }
    public Integer getSurveillance() { return this.surveillance; }
    public Byte getUtilityMessage() { return this.utilityMessage; }
    public Boolean modeSAble() { return this.packetsCount[11] != 0 || this.packetsCount[17] != 0; }
    public Boolean ADSBAble() { return this.packetsCount[17] != 0; }
    public Boolean getContinuationFlag() { return this.continuationFlag; }
    public Byte getACASCapability() { return this.ACASCapability; }
    public Byte getModeSSubnetworkVersionNumber() { return this.modeSSubnetworkVersionNumber; }
    public Boolean getTransponderEnhancedProtocolIndicator() { return this.transponderEnhancedProtocolIndicator; }
    public Boolean getSpecificServicesCapability() { return this.specificServicesCapability; }
    public Byte getUplinkELMCapability() { return this.uplinkELMCapability; }
    public Byte getDownlinkELMCapability() { return this.downlinkELMCapability; }
    public Boolean getAircraftIdentificationCapability() { return this.aircraftIdentificationCapability; }
    public Boolean getSquitterCapabilitySubfield() { return this.squitterCapabilitySubfield; }
    public Boolean getSurveillanceIdentifierCodeCapability() { return this.surveillanceIdentifierCodeCapability; }
    public Boolean getCommonUsageGICBCapabilityReport() { return this.commonUsageGICBCapabilityReport; }
    public Integer getStatusOfDTESubAdress() { return this.statusOfDTESubAdress; }
    public Boolean getCDTIOperational() { return this.CDTIOperational; }
    public Boolean getTCASOperational() { return this.TCASOperational; }
    public Boolean getACASOperational() { return this.ACASOperational; }
    public Boolean hasCrossLinkCapability() { return this.crossLinkCapability; }
    public Byte getSensityLevel() throws ACASInoperativeException {
        if(this.sensityLevel != null && this.sensityLevel == 0)
            throw new ACASInoperativeException();
        return this.sensityLevel;
    }
    public Short getMaximumAirspeed() { return this.maximumAirspeed; }
    public boolean isMilitary() { return this.isMilitary; }
    
    //Packets
    public long getLastChange() { return this.lastChange; }
    public long getLastPositionChange() { return this.lastPositionChange; }
    public int getPacketCount(int df) { return this.packetsCount[df]; }
    public int getPacketCount() {
        int count = 0;
        for(int i = 0; i < 32; i++)
            count += this.packetsCount[i];
        return count;
    }
    public Squitter getLastPacket() { return this.lastPacket; }
    
    //Statistics
    public short getStatsModeS() { return this.statsModeS; }
    public short getStatsADSB() { return this.statsADSB; }
    public short getStatsMisc() { return this.statsMisc; }
    public short getStatsAcquisition() { return this.statsAcquisition; }
    public short getStatsSquawk() { return this.statsSquawk; }
    public short getStatsAltitudeModeS() { return this.statsAltitudeModeS; }
    public short getStatsAltitudeADSB() { return this.statsAltitudeADSB; }
    public short getStatsAltitudeMisc() { return this.statsAltitudeMisc; }
    public short getStatsPosition() { return this.statsPosition; }
    public short getStatsVelocity() { return this.statsVelocity; }
    public short getStatsACAS() { return this.statsACAS; }
    public short getStatsIdent() { return this.statsIdent; }
    public short getStatsADSR() { return this.statsADSR; }
    
    //GUI
    public int getTablePosition() { return this.tablePosition; }
    public DotCollection getDotCollection() { return this.dotCollection; }
    
    
    
    /* Paint - Paints the airplane to the map */
    
    @Override
    public void paint(Graphics g) {
        //Paint track
        if(Map.getInstance().displayWholeTrackTimeout())
            this.paintWholeTrack(g);
        
        if(this.isMapTimeout()) //Aircrafts last position packet is too old or aircraft has no position received
            return;
        
        //Paint track
        if(!Map.getInstance().displayWholeTrackTimeout() && Map.getInstance().displayWholeTrack())
            this.paintWholeTrack(g);
        
        //Get position
        Point position = this.getPosition();
        this.lastMapPosition = position.clone();
        
        //Out of view
        if(Math.abs(position.getX()-500) > 5000 || Math.abs(position.getY()-500) > 5000)
            return;
        
        //Set color
        g.setColor(this.isClicked ? new Color(255, 255, 255) : new Color(141, 205, 92));
        
        //Draw main dot
        g.drawRect(position.getX() - 3, position.getY() - 3, 6, 6);
        
        //Draw properties of the airplane in the upper right corner
        g.drawLine(position.getX() + 5, position.getY() - 5, position.getX() + 20, position.getY() - 20);
        
        Float speed = this.getGroundspeed();
        Float track = this.getTrack();
        Integer altitude = this.getAltitude();
        String flightIdent = this.getFlightIdent();
        String airborne = new String();
        Short verticalRate = this.getVerticalRate();
        
        if(this.isAirborne() != null)
            airborne = this.isAirborne() ? " A" : " S";
        
        if(verticalRate != null) {
            if(verticalRate > 300) { //Arrow up
                g.drawLine(position.getX() + 47, position.getY() - 40, position.getX() + 52, position.getY() - 45);
                g.drawLine(position.getX() + 52, position.getY() - 45, position.getX() + 49, position.getY() - 45);
                g.drawLine(position.getX() + 52, position.getY() - 45, position.getX() + 52, position.getY() - 42);
            } else if(verticalRate < -300) { //Arrow down
                g.drawLine(position.getX() + 47, position.getY() - 45, position.getX() + 52, position.getY() - 40);
                g.drawLine(position.getX() + 52, position.getY() - 40, position.getX() + 49, position.getY() - 40);
                g.drawLine(position.getX() + 52, position.getY() - 40, position.getX() + 52, position.getY() - 43);
            }
        }
        
        g.drawString(flightIdent != null ? flightIdent + airborne : "______" + airborne, position.getX() + 20, position.getY() - 51);
        if(altitude != null) {
            g.drawString("F" + this.numberFormat(altitude / 100, 3), position.getX() + 20, position.getY() - 38);
        } else {
            g.drawString("F___", position.getX() + 20, position.getY() - 38);
        }
        
        if(speed != null) {
            g.drawString("M" + this.numberFormat(new Float(speed).intValue(), 3), position.getX() + 20, position.getY() - 25);
        } else { //No speed value received yet
            g.drawString("M___", position.getX() + 20, position.getY() - 25);
        }
        
        //Draw dots in past
        for(Dot dot: this.getDotCollection().getAll())
            dot.paint(g);
        
        //Draw vector
        if(speed != null && track != null) {
            Point vectorPoint = position.calculatePoint(track, speed * Aircraft.AIRPLANE_VECTOR_LENGTH / 60f);
            
            if(!this.isClicked)
                g.setColor(new Color(116, 205, 248));
            g.drawLine(position.getX(), position.getY(), vectorPoint.getX(), vectorPoint.getY());
        }
    }
    private void paintWholeTrack(Graphics g) {
        g.setColor(new Color(200, 200, 200));
        for(Point point: this.positionCollection)
            g.fillRect(point.getX(), point.getY(), 1, 1);
    }
    public void setClicked() {
        for(Aircraft aircraft: Airspace.getInstance().getAllAircrafts())
            aircraft.isClicked = false;
        this.isClicked = true;
    }
}