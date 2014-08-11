package decoder;

import stream.SerialInputStream;
import stream.OutputStream;
import stream.NetworkOutputStream;
import stream.InputStream;
import stream.FileInputStream;
import stream.FileOutputStream;
import exception.AircraftNotFoundException;
import exception.DuplicateSquitterException;
import exception.NotImplementedException;
import exception.ParityException;
import exception.UnknownDFException;
import exception.UnknownSubtypeException;
import exception.UnknownTCException;
import gui.Receiver;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import javax.swing.JTable;
import map.AircraftPanel;
import map.Point;
import gui.Map;
import gui.SquitterViewer;
import stream.NetworkInputStream;

/**
 * Decoder of SSR-Project. Represents a airspace containig all Airplanes in
 * a Hashtable. The class inizializes the remove of old airplanes, which are out
 * of range. The class is splitting received messages into different data
 * groups. Furthermore packets which cant be decoded are filtered.
 * 
 * @author Sven Steudte DL7AD DOK D20
 * @version 0.3a
 * 
 * @see http://www.modesbeast.com/resources/Koellner_Projekt-ADSB3.pdf
 * @see http://www.radarspotters.eu/forum/index.php?topic=5617.45
 * @see http://www.ll.mit.edu/mission/aviation/publications/publication-files/atc-reports/Gertz_1984_ATC-117_WW-15318.pdf
 */
public class Airspace {
    private static Point myPosition; //My position
    
    private Hashtable<Integer,Aircraft> aircrafts = new Hashtable<Integer,Aircraft>(500, 0.75f); //Contains all active Aircrafts in a hashtable
    private Squitter lastSquitter;
    private static Airspace instance;
    
    private static Thread tableUpdater;
    private static Thread mapUpdater;
    private static Thread inputStreamUpdater;
    
    private double maxDistance; //Distance highscore
    
    private ArrayList<InputStream> inputStreams = new ArrayList<InputStream>();
    private ArrayList<OutputStream> outputStreams = new ArrayList<OutputStream>();
    
    /**
     * Airspace is created new every time when starting a new decoding process.
     * Some values and the Table must be reset.
     */
    private Airspace() {
        Airspace.startGUIUpdater(); //Start monitor for old airplanes
    }
    public static Airspace getInstance() {
        if(Airspace.instance == null)
            Airspace.instance = new Airspace();
        return Airspace.instance;
    }
    
    /**
     * Decodes a squitter message and insert the data to airplane objects.
     * Important: The squitter should only contain the Bits (Hexadecimal) in a String.
     * True is returned in case of the squitters parity is correct and airplane
     * could be found in case of Adress is overlaid with Parity. This method throws
     * Squitter too short if squitter does not contain 3 Chars. In case the squitter
     * has other Characters than A-Z and 0-9, NumberFormatException will be thrown.
     * 
     * DF=00 ACAS (56bit)
     * DF=04 Altitude (56bit)
     * DF=05 Squawk (56bit)
     * DF=11 Acquisition Squitter (56bit)
     * DF=16 ACAS (112bit)
     * DF=17 ADS-B (112bit)
     * DF=18 TIS-B (112bit)
     * DF=20 Altitude and 56bit Message (112bit)
     * DF=21 Squawk and 56bit Message (112bit)
     * 
     * @param squitter The completly received 56bit or 112bit message
     * @return Returns True if squitter passes the parity check
     * @throws UnknownDFException Thrown when Downlink Format is unknown
     * @throws UnknownTCException Thrown when Type in DF=17 Squitter is unknown
     * @throws UnknownSubtypeException Thrown when Subtype in DF=17 Squitter is unknown
     * 
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-04R1-Draft%20Summary%20of%20DO-260B%20ADS-B%20Messages.pdf
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-18-DRAFT_DO-260B-V42.pdf Units on page 203/204
     * @see http://adsb.tc.faa.gov/WG3_Meetings/Meeting29/1090-WP29-14-%20Additional%20NIC%20for%20ES%20Surface%20position%20format.pdf
     * @see ICAO Annex 10 Volume 4
     */
    public Update squitterUpdate(Squitter squitter) throws UnknownDFException,UnknownTCException,UnknownSubtypeException,DuplicateSquitterException,AircraftNotFoundException,ParityException,NotImplementedException {
        //Check for duplicate Input
        if(this.lastSquitter != null && squitter.toString().equals(lastSquitter.toString()))
            throw new DuplicateSquitterException();
        
        this.lastSquitter = squitter;
        
        //Control Block (Downlink Format)
        int control = Integer.valueOf(squitter.toString().substring(0,2), 16).intValue();
        byte df = (byte)(control >>> 3); //Downlink Format: First 5 bits of control block
        
        //Find Identity and check parity
        Aircraft aircraft;
        switch(df) {
            case 0:
            case 4:
            case 5:
            case 16:
            case 20:
            case 21: //There is a parity overlaid with the adress of the airplane
                //Find aircraft
                try {
                    aircraft = this.getAircraft(squitter); //Aircraft was found when no exception was thrown
                } catch(AircraftNotFoundException e) {
                    throw new AircraftNotFoundException(); //Airplane was not found or squitter is incorrect
                }
                break;
            default: //Normal parity check
                if(!squitter.isValid())
                    throw new ParityException(); //Parity is incorrect
                //Find aircraft or create a new one
                int icaoIdent = Integer.valueOf(squitter.toString().substring(2,8), 16).intValue();
                aircraft = this.getAircraft(icaoIdent);
        }
        
        //Check for data remove timeout
        if(aircraft.isDataTimeout()) //Aircraft is out of range
            aircraft.removeData(); //Data remove
        
        //Initialize new update object
        Update update = new Update(aircraft.getIcaoIdent());
        update.setDF(df); //Update
        
        long data;
        String meBits,msgBits;
        
        //Selection by Downlink Format
        switch(df) {
            case 4: //Altitude 56bit
            case 20: //Altitude 112bit
                data = Integer.valueOf(squitter.toString().substring(1,5), 16).intValue();
                this.decodeModeSHeader(aircraft, ((int)data & 0x7FFE) >> 1); //Decode Mode S Header
                data = Integer.valueOf(squitter.toString().substring(2,8), 16).intValue();
                aircraft.decodeModeSAltitude((int)data); //Decode Altitude
                
                if(aircraft.getAltitude() != null)
                    update.setAltitude(aircraft.getAltitude()); //Update
                
                //Statistics
                aircraft.incrementStatsAltitudeModeS();
                aircraft.incrementStatsModeS();
                
                if(df == 4)
                    break;
                
                data = Long.valueOf(squitter.toString().substring(8,22), 16).longValue();
                this.decodeModeSMessage(aircraft, data); //Decode Mode S message
                break;
            
            case 5: //Squawk 56bit
            case 21: //Squawk 112bit
                data = Integer.valueOf(squitter.toString().substring(1,5), 16).intValue();
                this.decodeModeSHeader(aircraft, ((int)data & 0x7FFE) >> 1); //Decode Mode S Header
                data = Integer.valueOf(squitter.toString().substring(4,8), 16).intValue() & 0x1FFF;
                aircraft.decodeSquawk((int)data); //Decode Squawk
                
                update.setSquawk(aircraft.getSquawk()); //Update
                
                //Statistics
                aircraft.incrementStatsSquawk();
                aircraft.incrementStatsModeS();
                
                if(df == 5)
                    break;
                
                data = Long.valueOf(squitter.toString().substring(8,22), 16).longValue();
                this.decodeModeSMessage(aircraft, data); //Decode Mode S message
                break;
            
            case 11: //Mode S
                //This packet does not contain any data
                //It announces that this aircraft is able to transmit Mode S
                //One packet is transmitted each second
                
                //Statistics
                aircraft.incrementStatsAcquisition();
                aircraft.incrementStatsModeS();
                
                //Update
                update.setModeS();
                break;
            
            case 17: //ADS-B
                //Capability
                byte ca = (byte)(control & 0x5);
                aircraft.setTCASOperational(ca == 0 || ca == 1);
                aircraft.setCDTIOperational(ca == 1 || ca == 3);
                
                data = Long.valueOf(squitter.toString().substring(8,22), 16).longValue();
                
                byte tc = (byte)(data >>> 51); //First 5 bits
                byte subtype = (byte)((data >> 48) & 3); //Next 3 bits

                //Making message to a binary String and fills it up to 56 bit
                meBits = Long.toBinaryString(data);
                meBits = numberFormat(meBits, 56);
                
                //Selection by 
                switch(tc) {
                    case 0: //Airborne and Surface Position Message
                        throw new NotImplementedException("[DF=17] Airborne and Surface Position Message", squitter);
                    
                    case 1: //Category D
                    case 2: //Category C
                    case 3: //Category B
                    case 4: //Category A
                        aircraft.setCategory(Aircraft.CATEGORY[tc-1]); //Category
                        long ident = Long.parseLong(meBits.substring(8, 56), 2); //Bit 9 - 56
                        aircraft.decodeFlightIdent(ident); //Flight number or Callsign
                        aircraft.incrementStatsIdent(); //Statistics
                        update.setFlightIdent(aircraft.getFlightIdent()); //Update
                        break;
                    
                    //Surface and Airborne Position message
                             //         NIC     R_C                 NIC (Navigation Integrity Category)
                    case 5:  //GROUND   0-0     < 7.5m              11      none
                    case 6:  //GROUND   0-0     < 25m               10      none
                    case 7:  //GROUND   1-0     < 75m               09      none
                             //         1-1     < 0.1NM (185.2m)    08      none
                    case 8:  //GROUND   1-1     > 0.1NM (185.2m)    00      none
                    case 9:  //AIR      00-     < 7.5m              11      Barometric
                    case 10: //AIR      00-     < 25m               10      Barometric
                    case 11: //AIR      11-     < 75m               09      Barometric
                             //         00-     < 0.1NM (185.2m)    08      Barometric
                    case 12: //AIR      00-     < 0.2NM (370.4m)    07      Barometric
                    case 13: //AIR      01-     < 0.3NM (555.6m)    06      Barometric
                             //         00-     < 0.5NM (926m)      06      Barometric
                             //         11-     < 0.6NM (1111.2m)   06      Barometric
                    case 14: //AIR      00-     < 1.0NM (1852m)     05      Barometric
                    case 15: //AIR      00-     < 2.0NM (3704m)     04      Barometric
                    case 16: //AIR      11-     < 4.0NM (7.408km)   03      Barometric
                             //         00-     < 8.0NM (14.816km)  02      Barometric
                    case 17: //AIR      00-     < 20.0NM (37.04km)  01      Barometric
                    case 18: //AIR      00-     > 20.0NM (37.04km)  00      Barometric
                    case 20: //AIR      00-     < 7.5m              11      GNSS Height
                    case 21: //AIR      00-     < 25m               10      GNSS Height
                    case 22: //AIR      00-     > 25 or unknown     00      GNSS Height
                        
                        boolean cprFormat = meBits.substring(21, 22).equals("1"); //Bit 22
                        int latitude = Integer.parseInt(meBits.substring(22, 39), 2); //Bit 23 - 39
                        int longitude = Integer.parseInt(meBits.substring(39, 56), 2); //Bit 40 - 56
                        aircraft.setTime(meBits.substring(20,21).equals("1")); //Time (Bit 21)
                        aircraft.calculatePosition(
                            cprFormat,
                            latitude,
                            longitude,
                            tc <= 8 ? Aircraft.SURFACE : Aircraft.AIRBORNE,
                            meBits.substring(20,21).equals("1")
                        ); //Calculate position
                        
                        //Highscore distance
                        Float dist = aircraft.calculateParabolicDistance();
                        if(dist != null)
                            Airspace.getInstance().tryMaxDistance(dist);
                        
                        if(tc <= 8) { //Surface
                            aircraft.calculateSurfaceGroundspeed(Short.parseShort(meBits.substring(5, 12), 2)); //Calculate movement (groundspeed) (Bit 6 - 12)
                            if(meBits.substring(12, 13).equals("1")) { //Bit 13 (Track Status Bit), In case of 0, ground track is not valid
                                short track = Short.parseShort(meBits.substring(13, 20), 2); //Bit 14 - 20
                                aircraft.calculateSurfaceTrack(track);
                                update.setTrack(aircraft.getTrack()); //Update
                            } //Calculate ground track
                            update.setGroundspeed(aircraft.getGroundspeed()); //Update
                        } else { //Airborne
                            aircraft.decodeSurveillance(Byte.parseByte(meBits.substring(5, 7), 2)); //Surveillance status (Bit 6 - 7)
                            aircraft.setAntenna(meBits.substring(7, 8).equals("1")); //Antenna (Bit 8)
                            aircraft.decodeADSBAltitude(Short.parseShort(meBits.substring(8, 20), 2)); //Altitude (Bit 9 - 20)
                            update.setAltitude(aircraft.getAltitude()); //Update
                        }
                        
                        aircraft.setAirborne(tc > 8);
                        
                        //Statistics
                        aircraft.incrementStatsPosition();
                        aircraft.incrementStatsAltitudeADSB();
                        
                        //Update
                        update.setPosition(aircraft.getPosition());
                        break;

                    case 19: //Airborne velocity message
                        
                        aircraft.setIntentChangeFlag(meBits.substring(8, 9).equals("1")); //Bit 9
                        aircraft.decodeNavigationAccuracyForVelocity(Byte.parseByte(meBits.substring(10, 13), 2)); //Bit 11 - 13
                        aircraft.decodeVerticalRateSource(meBits.substring(35, 36).equals("1")); //Bit 36

                        aircraft.calculateDifferenceFromBarometricAltitude(
                            meBits.substring(48, 49).equals("1"),       //Bit 49
                            Byte.parseByte(meBits.substring(49, 56), 2) //Bit 50 - 56
                        ); //Difference from barometric Altitude

                        aircraft.calculateVerticalRate(
                            meBits.substring(36, 37).equals("1"),         //Bit 37
                            Short.parseShort(meBits.substring(37, 46), 2) //Bit 38 - 46
                        ); //Vertical Rate
                        
                        switch(subtype) {
                            case 1: //Velocity over ground: Normal
                            case 2: //Velocity over ground: Supersonic
                                //Calculate Groundspeed and Track
                                boolean ewDirection = meBits.substring(13, 14).equals("1");       //Bit 14
                                short ewVelocity = Short.parseShort(meBits.substring(14, 24), 2); //Bit 15 - 24
                                boolean nsDirection = meBits.substring(24, 25).equals("1");       //Bit 14
                                short nsVelocity = Short.parseShort(meBits.substring(25, 35), 2); //Bit 26 - 35

                                aircraft.calculateAirborneGroudspeed(ewVelocity, nsVelocity, subtype == 2);
                                aircraft.calculateAirborneTrack(ewDirection, ewVelocity, nsDirection, nsVelocity);

                                //Update
                                update.setGroundspeed(aircraft.getGroundspeed());
                                update.setTrack(aircraft.getTrack());
                                break;
                            case 3: //Airspeed and Heading: Normal
                            case 4: //Airspeed and Heading: Supersonic
                                //Calculate heading
                                if(meBits.substring(13, 14).equals("1")) { //Bit 14 (Status Bit), In case of 0, heading is not available
                                    aircraft.calculateAirborneHeading(Short.parseShort(meBits.substring(14, 24), 2)); //Bit 15 - 24
                                    update.setHeading(aircraft.getHeading()); //Update
                                }
                                
                                //Calculate Airspeed
                                aircraft.decodeAirspeedType(meBits.substring(24, 25).equals("1")); //Bit 25
                                aircraft.calculateAirborneAirspeed(
                                    Short.parseShort(meBits.substring(25, 35), 2), //Bit 26 - 35
                                    subtype == 4
                                );
                                
                                update.setAirspeed(aircraft.getAirspeed()); //Update
                                break;
                            default:
                                throw new UnknownSubtypeException(tc, subtype, df);
                        }
                        
                        aircraft.incrementStatsVelocity(); //Statistics
                        update.setVerticalRate(aircraft.getVerticalRate()); //Update
                        break;

                    case 23:
                        if(subtype == 0) { //Test message
                            throw new NotImplementedException("[DF=17] Test message", squitter);
                        } else {
                            throw new UnknownSubtypeException(tc, subtype, df); //Other subtypes are not specified
                        }

                    case 24:
                        if(subtype == 1) { //Surface System Status
                            throw new NotImplementedException("[DF=17] Surface System Status", squitter);
                        } else {
                            throw new UnknownSubtypeException(tc, subtype, df); //Other subtypes are not specified
                        }

                    case 28: //ADS-R Aircraft Status Message (Emergency/Priority Status)
                        switch(subtype) {
                            case 1: //Extended Squitter Aircraft Status Message (Emergency/ Priority Status)
                                aircraft.decodeEmergencyStatus(Byte.parseByte(meBits.substring(8, 11), 2)); //Emergency Status (Bit 9 - 11)
                                aircraft.decodeSquawk(Short.parseShort(meBits.substring(11, 24), 2)); //Squawk (Bit 12 - 24)
                                update.setSquawk(aircraft.getSquawk()); //Update
                                break;
                            case 2: //Extended Squitter Aircraft Status Message (1090ES TCAS RA Message)
                                throw new NotImplementedException("[DF=17] Extended Squitter Aircraft Status Message (1090 Emergency Status TCAS RA Message)", squitter);
                            default:
                                throw new UnknownSubtypeException(tc, subtype, df); //Other subtypes are not specified
                        }
                        aircraft.incrementStatsADSR(); //Statistics
                        break;

                    case 29: //ADS-R Target State and Status Message
                        switch(subtype) {
                            case 0: //Target State and Status Message (ADS-B Version Number=1, defined in RTCA DO-260A)
                                throw new NotImplementedException("[DF=17] Target State and Status Message (ADS-B Version Number=1, defined in RTCA DO-260A)", squitter);
                            case 1: //Target State and Status Message (ADS-B Version Number=2, defined in these MOPS, RTCA DO-260B)
                                throw new NotImplementedException("[DF=17] Target State and Status Message (ADS-B Version Number=2, defined in these MOPS, RTCA DO-260B)", squitter);
                            default:
                                throw new UnknownSubtypeException(tc, subtype, df); //Other subtypes are not specified
                        }
                        //break; Temporary disabled due to compiler error

                    case 31: //ADS-R Aircraft Operational Status Message
                        if(subtype == 0 || subtype == 1) { //Aircraft Operational Status Message
                            throw new NotImplementedException("[DF=17] Aircraft Operational Status Message", squitter);
                        } else {
                            throw new UnknownSubtypeException(tc, subtype, df); //Other subtypes are not specified
                        }

                    case 25: //Reserved
                    case 26: //Reserved
                    case 27: //Reserved
                    case 30: //Reserved
                        throw new UnknownTCException(tc, df);
                }
                
                aircraft.incrementStatsADSB(); //Statistics
                break;
            
            case 0: //ACAS 56bit
            case 16: //ACAS 112bit
                msgBits = Long.toBinaryString(Long.valueOf(squitter.toString().substring(0,8), 16).longValue());
                msgBits = numberFormat(msgBits, 32);
                
                aircraft.setAirborne(!msgBits.substring(5, 6).equals("1")); //Vertical Status (Bit 6)
                aircraft.setSensityLevel(Byte.parseByte(msgBits.substring(8, 11), 2)); //Bit 9-11
                aircraft.decodeMaximumAirspeed(Byte.parseByte(msgBits.substring(13, 17), 2)); //Bit 14-17
                aircraft.decodeModeSAltitude(Short.parseShort(msgBits.substring(19, 32), 2)); //Altitude Bit 20-32
                
                if(df == 0) { //DF=0
                    aircraft.setCrossLinkCapability(msgBits.substring(6, 7).equals("1")); //Cross Link Capability (Bit 7)
                } else { //DF=16
                    msgBits = numberFormat(new BigInteger(squitter.toString().substring(0,22), 16).toString(2), 80);
                    //TODO: Decode ACAS message
                    //See ICAO Annex 10 Volume 4 Page 4-22 - 4-27 (160-165)
                    throw new NotImplementedException("[DF=16] Partial ACAS Message", squitter);
                }
                
                //Statistics
                aircraft.incrementStatsACAS();
                aircraft.incrementStatsAltitudeMisc();
                aircraft.incrementStatsMisc();
                
                //Update
                update.setAltitude(aircraft.getAltitude());
                break;
            
            case 18: //TIS-B
                throw new NotImplementedException("[DF=18] TIS-B", squitter);
            
            case 24: //ELM
                throw new NotImplementedException("[DF=24] ELM", squitter);
            
            case 19: //Military cannot be decoded
                System.exit(aircraft.getIcaoIdent());
                aircraft.setMilitary();
                update.setMilitary(); //Update
                break;
            
            default: //Downlink Format is not known
                throw new UnknownDFException(df, squitter);
        }
        
        //Some serve actions
        aircraft.incrementPacketCount(df); //Increment Packets count
        aircraft.updateLastChange(squitter.getInputStreamName()); //Update last change for timeout remove
        aircraft.setLastPacket(squitter); //Set last packet
        aircraft.updateAircraftInTable(); //Update table
        
        return update;
    }
    /**
     * This method decodes the Mode S Header of DF=4, DF=5, DF=20 and DF=21
     * http://adsb.tc.faa.gov/WG3_Meetings/Meeting30/1090-WP30-18-DRAFT_DO-260B-V42.pdf
     * @param data The first 14 Bits of the Message Field
     */
    public void decodeModeSHeader(Aircraft airplane, int data) { //TODO: Untestet
        byte flightStatus = (byte)((data & 0x3800) >> 11);
        airplane.setSpecialPositionIdentification(flightStatus == 4 || flightStatus == 5);
        airplane.setAlert(flightStatus == 2 || flightStatus == 3 || flightStatus == 4);
        if(flightStatus != 4 && flightStatus != 5) //Has not ambiguous information
            airplane.setAirborne(flightStatus == 0 || flightStatus == 2);
        
        airplane.decodeDownlinkRequest((byte)((data & 0x7C0) >> 6));
        airplane.setUtilityMessage((byte)(data & 0x3F));
    }
    /**
     * Decodes message containing transponder capabilities
     * @param data Raw data bits
     */
    public void decodeModeSMessage(Aircraft aircraft, long data) {
        //Making message to a binary String and fills it up to 56 bit
        String bits = Long.toBinaryString(data);
        bits = numberFormat(bits, 56);
        
        aircraft.setContinuationFlag(bits.substring(8, 9).equals("1")); //Bit 9
        aircraft.setACASCapability(Byte.parseByte(bits.substring(15, 16) + bits.substring(36, 40), 2)); //Bit 16 & 37-40
        aircraft.setModeSSubnetworkVersionNumber(Byte.parseByte(bits.substring(16, 23), 2)); //Bit 17-23
        aircraft.setTransponderEnhancedProtocolIndicator(bits.substring(23, 24).equals("1")); //Bit 24
        aircraft.setSpecificServicesCapability(bits.substring(24, 25).equals("1")); //Bit 25
        aircraft.setUplinkELMCapability(Byte.parseByte(bits.substring(25, 28), 2)); //Bit 26-28
        aircraft.setDownlinkELMCapability(Byte.parseByte(bits.substring(28, 32), 2)); //Bit 29-32
        aircraft.setAircraftIdentificationCapability(bits.substring(32, 33).equals("1")); //Bit 33
        aircraft.setSquitterCapabilitySubfield(bits.substring(33, 34).equals("1")); //Bit 34
        aircraft.setSurveillanceIdentifierCodeCapability(bits.substring(34, 35).equals("1")); //Bit 35
        aircraft.setCommonUsageGICBCapabilityReport(bits.substring(35, 36).equals("1")); //Bit 36
        aircraft.setStatusOfDTESubAdress(Integer.parseInt(bits.substring(40, 56), 2)); //Bit 41-56
    }
    /**
     * Find Aircraft Object by its unique ident. Creates a new Object if Aircraft
     * is not found.
     * @param icaoIdent ICAO ident
     * @return Aircraft Object
     */
    public Aircraft getAircraft(int icaoIdent) {
        //Search Airplane in Hashtable or create new one
        Aircraft aircraft;
        if(this.aircrafts.containsKey(icaoIdent)) { //Aicraft existing
            aircraft = this.aircrafts.get(icaoIdent);
        } else { //Aircraft not found
            aircraft = new Aircraft(icaoIdent); //Create a new object
            this.aircrafts.put(icaoIdent, aircraft); //Insert into Hashtable
        }
        return aircraft;
    }
    /**
     * Resets the Aircraft Hashtable when Receiving or FileDecoding Process was stopped.
     */
    public void resetAircrafts() {
        this.aircrafts = new Hashtable<Integer,Aircraft>(500, 0.75f); //Generates a new Hashtable
        
    }
    /**
     * The Parity of the Squitter does contain the Adress of the Airplane. This
     * is made in DF=00, DF=04, DF=05, DF=20 and DF=21. The addition (XOR) of
     * Parity and ICAO-Ident should be zero.
     * @param squitter The complete received 56bit or 112bit message
     * @return Aircraft Object
     * @throws AircraftNotFoundException Thrown when aircraft was not found
     */
    private Aircraft getAircraft(Squitter squitter) throws AircraftNotFoundException {
        int parity = squitter.calculateParity(); //Calculate parity of squitter
        
        for(Aircraft aircraft: this.getAllAircrafts()) {
            int result = parity ^ aircraft.getIcaoIdent(); //Calculate result in combination of the Aircraft ident
            if(result == 0)
                return aircraft; //Aircraft was found
        }
        throw new AircraftNotFoundException(); //Aircraft was not found
    }
    /**
     * Returns a collection of all aircrafts. First it clones the hashtable
     * to prevent an concurrent modification error.
     * @return Aircrafts
     */
    public Collection<Aircraft> getAllAircrafts() {
        return ((Hashtable<Integer,Aircraft>)this.aircrafts.clone()).values();
    }
    
    /**
     * Returns the number of all active aircrafts in the hashtable with a
     * position and with no position. The timeout depends on the table remove
     * timeout.
     * 
     * @return 0: All aircrafts
     * @return 1: Aircrafts without position
     * @return 2: Aircrafts containing position
     */
    private short[] getNumberOfAircrafts() {
        short[] count = new short[3]; //Number of Aircrafts (0: without position, 0: with position)
        for(Aircraft aircraft: this.getAllAircrafts()) {
            if(!aircraft.isTableTimeout()) //Aircraft is active
                count[aircraft.getPosition() != null ? 2 : 1]++; //Increment number of active aircrafts with and with no position
            
            count[0]++; //Increment number of all aircrafts received in past
        }
        return count;
    }
    /**
     * Returns the number a ADSB and ModeS airplanes which can be received.
     * @return 0: Mode S able
     * @return 1: ADS-B able
     */
    private short[] getTransponderInformation() {
        short[] count = new short[3]; //Number of Aircrafts (0: without position, 0: with position)
        for(Aircraft aircraft: this.getAllAircrafts()) {
            if(!aircraft.isTableTimeout()) { //Aircraft is active
                if(aircraft.modeSAble()) count[0]++; //ModeS able
                if(aircraft.ADSBAble()) count[1]++; //ADSB able
            }
        }
        return count;
    }
    /**
     * Returns the nearest aircraft below. The calculation depends on the linear
     * distance.
     * @return 
     */
    private Aircraft getNearestAircraft() {
        Aircraft nearest = null;
        
        for(Aircraft curr: this.getAllAircrafts()) {
            if(curr.isTableTimeout()) //Aircraft is out of receive
                continue;
            
            if(curr.calculateLinearDistance() != null && (nearest == null || nearest.calculateLinearDistance() > curr.calculateLinearDistance()))
                nearest = curr;
        }
        return nearest;
    }
    /**
     * Returns the number of unique aircrafts received
     * @return Number of unique Aircrafts
     */
    public short uniqueAircrafts() {
        return (short)this.aircrafts.size();
    }
    /**
     * Returns the highscore distance
     * @return Distance between airplane and base station
     */
    public double getMaxDistance() {
        return this.maxDistance;
    }
    /**
     * Tries to set a new highscore distance
     * @param distance Distance between airplane and base station
     */
    public void tryMaxDistance(double distance) {
        if(this.maxDistance < distance)
            this.maxDistance = distance;
    }
    private static void startGUIUpdater() {
        
        //Table updater
        
        if(Airspace.tableUpdater == null) { //Table updater is not initialized yet
            
            //Start table updater
            Airspace.tableUpdater = new Thread() {public void run() {
                //Get instances
                Receiver receiver = Receiver.getInstance();
                JTable aircraftTable = receiver.getAircraftTable();
                
                //Start endless loop
                while(true) {
                    
                    //Get current airspace
                    Airspace airspace = Airspace.getInstance();
                    if(airspace != null) {
                        
                        //Remove all aircrafts which are out of range
                        if(airspace.getAllAircrafts() != null) {
                            for(Aircraft aircraft: airspace.getAllAircrafts()) {
                                //Calculate age of last packet
                                long timeOver = System.currentTimeMillis() - aircraft.getLastChange();
                                
                                if(aircraft.getTablePosition() != -1) {
                                    if(aircraft.isTableTimeout()) { //Aircraft is out of receive
                                        receiver.removeAircraftFromTable(aircraft); //Table remove
                                    } else { //Aircraft is in range
                                        aircraftTable.setValueAt(Math.round(timeOver / 100d) / 10d, aircraft.getTablePosition(), 12); //Update timeout in aircraft table
                                    }
                                }
                            }
                        }

                        //Update distance highscore
                        if(Airspace.getInstance().maxDistance != 0)
                            receiver.setMaxDistance(Airspace.getInstance().maxDistance);

                        //Update aircrafts statistics
                        short[] airplanesNumber = airspace.getNumberOfAircrafts();
                        receiver.setActiveAircrafts(
                            airplanesNumber[0],
                            (short)(airplanesNumber[1] + airplanesNumber[2]),
                            airplanesNumber[2]
                        );

                        //Update transponder statistics
                        short[] transponder = airspace.getTransponderInformation();
                        receiver.updateTransponderStatistics(transponder[0], transponder[1]);

                        //Update nearest airplane
                        receiver.updateNearestAircraft(airspace.getNearestAircraft());
                    
                    }
                    
                    //Updater interruption but interrupt one time minimum
                    do {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {}
                    } while(!Airspace.getInstance().hasInputStreams());
                }
            }};
            Airspace.tableUpdater.start(); //Start Updater thread
        }
        
        //Map updater
        
        if(Airspace.mapUpdater == null) { //Table updater is not initialized yet
            
            //Start map updater
            Airspace.mapUpdater = new Thread() {public void run() {
                while(true) {
                    //Repaint aircrafts on map
                    AircraftPanel.getInstance().repaintAircrafts();
                    
                    //Updater interruption but interrupt one time minimum
                    do {
                        try {
                            Thread.sleep((int)(Map.getInstance().getUpdateInterval() * 1000));
                        } catch (InterruptedException e) {}
                    } while(!Airspace.getInstance().hasInputStreams());
                }
            }};
            Airspace.mapUpdater.start(); //Start Updater thread
        }
        
        if(Airspace.inputStreamUpdater == null) { //Input stream updater is not initialized yet
            
            //Start map updater
            Airspace.inputStreamUpdater = new Thread() {public void run() {
                while(true) {
                    
                    //Check for update
                    synchronized(Airspace.getInstance().inputStreams) {
                        
                        Float dataRate = null;
                        ArrayList<String> receivers = new ArrayList<String>();
                        
                        //Query each input stream
                        for(InputStream inputStream: Airspace.getInstance().inputStreams) {
                            
                            if((inputStream instanceof SerialInputStream || inputStream instanceof NetworkInputStream) && dataRate == null)
                                dataRate = 0f;
                            
                            for(Squitter squitter: inputStream.getSquitter()) {
                                try {
                                    
                                    //Output stream
                                    for(OutputStream outputStream: Airspace.getInstance().outputStreams)
                                        outputStream.writeSquitter(squitter);
                                    
                                    //try {
                                        //Insert squitter into Airspace
                                        Update update = Airspace.getInstance().squitterUpdate(squitter);
                                        
                                        //Squitter viwer update
                                        SquitterViewer.getInstance().addPacket(squitter, update);
                                        
                                    //} catch(RuntimeException e) {
                                    //    System.err.println("Error in decode:");
                                    //    System.err.println("Squitter: " + squitter);
                                    //    e.getStackTrace();
                                    //}
                                    
                                } catch(UnknownDFException e) { //The Downlink Format is unknown
                                    SquitterViewer.getInstance().addPacket(squitter, "Unknown DF", e.getDF());
                                } catch(UnknownTCException e) { //The Capability Mode ist unknown
                                    SquitterViewer.getInstance().addPacket(squitter, "Unknown TC error");
                                } catch(UnknownSubtypeException e) { //The Subtype is unknown
                                    SquitterViewer.getInstance().addPacket(squitter, "Unknown subtype error");
                                } catch(DuplicateSquitterException e) { //The Squitter is duplicate
                                    SquitterViewer.getInstance().addPacket(squitter, "Duplicate squitter");
                                } catch(StringIndexOutOfBoundsException e) { //Squitter is malformed, it has too less characters
                                    SquitterViewer.getInstance().addPacket(squitter, "Malformed squitter error");
                                } catch(AircraftNotFoundException e) { //The aircraft was not found or parity is incorrect
                                    SquitterViewer.getInstance().addPacket(squitter, "Aircraft not found");
                                } catch(ParityException e) { //The parity check was incorrect
                                    SquitterViewer.getInstance().addPacket(squitter, "Parity error");
                                } catch(NotImplementedException e) { //The function was not implemented yet
                                    SquitterViewer.getInstance().addPacket(squitter, "Not implemented yet");
                                }
                            }
                            
                            //Update data rate/ Step
                            if(inputStream instanceof FileInputStream) { //File input
                                Receiver.getInstance().setLineStep(((FileInputStream)inputStream).getCurrentLine(), ((FileInputStream)inputStream).getLines());
                            } else if(inputStream instanceof SerialInputStream) { //Serial input
                                dataRate += ((SerialInputStream)inputStream).getDataRate();
                            } else if(inputStream instanceof NetworkInputStream) { //Network input
                                dataRate += ((NetworkInputStream)inputStream).getDataRate();
                            }
                            receivers.add(inputStream.getInputStreamName());
                            
                            if(inputStream.isClosed()) {
                                Airspace.getInstance().inputStreams.remove(inputStream);
                                break;
                            }
                        }
                        
                        //Input name
                        String receiversString = new String();
                        for(String receiver: receivers)
                            receiversString += (receiversString.length() == 0) ? receiver : "/ " + receiver;
                        Receiver.getInstance().setInput(receiversString.equals(new String()) ? "None" : receiversString);
                        
                        //Data rate
                        if(dataRate != null) //Network or serial input
                            Receiver.getInstance().setDataRate(dataRate);
                        
                    }
                    
                    //Updater interruption
                    do {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {}
                    } while(!Airspace.getInstance().hasInputStreams());
                }
            }};
            Airspace.inputStreamUpdater.start(); //Start Updater thread
        }
    }
    
    //Serve
    
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
    
    public static void setMyPosition(Point position) {
        Airspace.myPosition = position;
    }
    public static Point getMyPosition() {
        return Airspace.myPosition;
    }
    
    //Streams
    public void addInputStream(InputStream inputStream) {
        synchronized(this.inputStreams) {
            if(this.inputStreams.isEmpty())
                this.reset();
            this.inputStreams.add(inputStream);
        }
    }
    public ArrayList<InputStream> getInputStreams(Class inputStreamClass) {
        ArrayList<InputStream> streams = new ArrayList<InputStream>();
        for(InputStream stream: this.inputStreams) {
            if(stream.getClass() == inputStreamClass)
                streams.add(stream);
        }
        return streams;
    }
    public void removeInputStream(String inputStreamName) {
        synchronized(this.inputStreams) {
            for(InputStream inputStream: this.inputStreams)
                if(inputStream.getInputStreamName().equals(inputStreamName)) {
                    inputStream.close();
                    return;
                }
        }
    }
    public void removeInputStreamGroup(Class streamClass) {
        synchronized(this.inputStreams) {
            for(InputStream inputStream: this.inputStreams) {
                if(inputStream.getClass() == streamClass)
                    inputStream.close();
            }
        }
    }
    public void removeAllInputStreams() {
        synchronized(this.inputStreams) {
            for(InputStream inputStream: this.inputStreams)
                inputStream.close();
        }
    }
    public boolean hasInputStreams() {
        return this.inputStreams.size() != 0;
    }
    public boolean hasInputStream(String inputStreamName) {
        for(InputStream stream: this.inputStreams)
            if(stream.getInputStreamName().equals(inputStreamName))
                return true;
        return false;
    }
    public boolean hasInputStreamGroupStreams(Class inputStreamGroup) {
        for(InputStream stream: this.inputStreams)
            if(stream.getClass() == inputStreamGroup)
                return true;
        return false;
    }
    
    public void addOutputStream(OutputStream outputStream) {
        //Check for existing output stream
        for(OutputStream o: this.outputStreams) {
            if(outputStream.getClass() == o.getClass()) { //Streams equals each other compared to original class
                //Close stream
                o.close();
                
                //Wait to be closed
                do {
                    try {
                        Thread.sleep(1);
                    } catch(InterruptedException e) {}
                } while(!o.isClosed());
                
                break;
            }
        }
        
        //Add new output stream
        this.outputStreams.add(outputStream);
    }
    public ArrayList<OutputStream> getOutputStreams() {
        return this.outputStreams;
    }
    public void removeOutputStream(boolean removeFileStream) {
        Class c = removeFileStream ? FileOutputStream.class : NetworkOutputStream.class;
        for(OutputStream o: this.outputStreams) {
            if(c == o.getClass()) {
                o.close();
                this.outputStreams.remove(o);
                return;
            }
        }
    }
    
    /**
     * This method performs a full reset of the airspace.
     */
    private void reset() {
        this.maxDistance = 0; //Reset highscore
        this.aircrafts = new Hashtable<Integer,Aircraft>(500, 0.75f);
        this.lastSquitter = null;
        Receiver.getInstance().resetTable(); //Reset table
        Receiver.getInstance().resetGUI(); //Reset GUI
    }
}