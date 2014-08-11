package file;

import exception.DecodingCoordinateException;
import exception.IntersectionNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import map.*;

public class Import {
    
    public Import(String filename) {
        File file;
        FileReader fr = null;
        String data;
        
        try {
            //Open file
            file = new File(filename);
            fr = new FileReader(file);
            byte b;
            int i = 0;
            
            //Read file
            char[] c = new char[(int)file.length()];
            while((b = (byte)fr.read()) != -1)
                c[i++] = (char)b;
            data = new String(c);
            
            //Remove commands and empty lines
            ArrayList<String> lines = new ArrayList<String>(10000);
            String dataLines[] = data.split("\n");
            for(int u = 0; u < dataLines.length; u++) {
                String line = dataLines[u].split(";").length != 0 ? dataLines[u].split(";")[0].trim() : "";
                if(!line.equals(""))
                    lines.add(line.replaceAll("\\s+", " "));
            }
            
            String objectType = null;
            
            for(String line: lines) {
                switch(line.charAt(0)) {
                    case '#':
                        this.decodeColor(line);
                        break;
                    case '[':
                        objectType = line.split("\\[")[1].split("\\]")[0].toUpperCase();
                        break;
                    default:
                        if(objectType != null) {
                            if(objectType.equals("INFO")) {
                                
                            } else if(objectType.equals("VOR")) {
                                this.decodeVOR(line);
                                
                            } else if(objectType.equals("NDB")) {
                                this.decodeNDB(line);
                                
                            } else if(objectType.equals("FIXES")) {
                                this.decodeFIX(line);
                                
                            } else if(objectType.equals("AIRPORT")) {
                                this.decodeAirport(line);
                                
                            } else if(objectType.equals("RUNWAY")) {
                                this.decodeRunway(line);
                                
                            } else if(objectType.equals("SID")) {
                                this.decodeSID(line);
                                
                            } else if(objectType.equals("STAR")) {
                                this.decodeSTAR(line);
                                
                            } else if(objectType.equals("ARTCC")) {
                                this.decodeARTCC(line, ARTCC.MEDIUM);
                                
                            } else if(objectType.equals("ARTCC HIGH")) {
                                this.decodeARTCC(line, ARTCC.HIGH);
                                
                            } else if(objectType.equals("ARTCC LOW")) {
                                this.decodeARTCC(line, ARTCC.LOW);
                                
                            } else if(objectType.equals("HIGH AIRWAY")) {
                                this.decodeAirway(line, Airway.HIGH);
                                
                            } else if(objectType.equals("LOW AIRWAY")) {
                                this.decodeAirway(line, Airway.LOW);
                                
                            } else if(objectType.equals("GEO")) {
                                this.decodeGEO(line);
                            }
                        }
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch(IOException e) {}
        }
    }
    private void decodeGEO(String line) {
        String[] data = line.split(" ");
        try {
            GeoLine.add(new GeoLine(data[0], data[1], data[2], data[3], data[4]));
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeAirway(String line, int type) {
        String[] data = line.split(" ");
        try {
            if(data.length == 4) { //Airway continuation
                try { //Try Coordinates
                    Airway.addLineToLastAirway(this.decodeLine(data[0], data[1], data[2], data[3]));
                } catch(DecodingCoordinateException e) { //Try named intersection
                    Airway.addLineToLastAirway(this.decodeLine(data[0], data[2]));
                }
            } else if(data.length == 5) {
                try { //Try Coordinates
                    Airway.tryAddNewAirway(new Airway(data[0], type), this.decodeLine(data[1], data[2], data[3], data[4]));
                } catch(DecodingCoordinateException e) { //Try named intersection
                    Airway.tryAddNewAirway(new Airway(data[0], type), this.decodeLine(data[1], data[3]));
                }
            }
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(IntersectionNotFoundException e) {} //One of the points or both point were not found or could be not decoded
    }
    private void decodeARTCC(String line, int type) {
        String[] data = line.split(" ");
        try {
            if(data.length == 4) {
                ARTCC.add(new ARTCC(data[0], data[1], data[2], data[3], type));
            } else if(data.length == 5) {
                ARTCC.add(new ARTCC(data[1], data[2], data[3], data[4], type));
            }
        } catch(NumberFormatException e) {
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeSTAR(String line) {
        String[] data = line.split(" ");
        try {
            if(data.length == 2) { //STAR continuation with named intersections
                STAR.addSTAR(this.decodeLine(data[0], data[1]));
            } else if(data.length == 5) { //New STAR with Name with named intersections
                STAR.addSTAR(data[2], data[0], data[1], this.decodeLine(data[3], data[4]));
            } else if(data.length == 7) { //Coordinate values
                STAR.addSTAR(data[2], data[0], data[1], this.decodeLine(data[3], data[4], data[5], data[6]));
            }
        } catch(IntersectionNotFoundException e) { //One of the points or both point were not found or could be not decoded
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeSID(String line) {
        String[] data = line.split(" ");
        try {
            if(data.length == 2) { //SID continuation with named intersections
                SID.addSID(this.decodeLine(data[0], data[1]));
            } else if(data.length == 5) { //New SID with Name with named intersections
                SID.addSID(data[2], data[0], data[1], this.decodeLine(data[3], data[4]));
            } else if(data.length == 7) { //Coordinate values
                SID.addSID(data[2], data[0], data[1], this.decodeLine(data[3], data[4], data[5], data[6]));
            }
        } catch(IntersectionNotFoundException e) { //One of the points or both point were not found or could be not decoded
        } catch(DecodingCoordinateException e) {}
    }
    private Line decodeLine(String lat1, String lon1, String lat2, String lon2) throws DecodingCoordinateException {
        Point pointACoord = new Point(lat1, lon1);
        Point pointBCoord = new Point(lat2, lon2);
        return new Line(pointACoord, pointBCoord);
    }
    private Line decodeLine(String intersectionA, String intersectionB) throws IntersectionNotFoundException {
        Point pointA = Intersection.getIntersection(intersectionA);
        Point pointB = Intersection.getIntersection(intersectionB);
        if(pointA == null || pointB == null) //One of the intersection was not found, so the values does not contain coordinates
            throw new IntersectionNotFoundException();
        
        return new Line(pointA, pointB);
    }
    private void decodeRunway(String line) {
        String[] data = line.split(" ");
        
        try {
            Runway.add(new Runway(data[0], data[1], data[4], data[5], data[6], data[7], Integer.parseInt(data[2]), Integer.parseInt(data[3])));
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeAirport(String line) {
        String[] data = line.split(" ");
        try {
            Float frequency = Float.parseFloat(data[1]);
            Airport.add(new Airport(data[0], frequency, data[2], data[3]));
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(NumberFormatException e) {
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeFIX(String line) {
        String[] data = line.split(" ");
        try {
            Fix.add(new Fix(data[0], data[1], data[2]));
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeNDB(String line) {
        String[] data = line.split(" ");
        try {
            NDB.add(new NDB(data[0], Float.parseFloat(data[1]), data[2], data[3]));
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeVOR(String line) {
        String[] data = line.split(" ");
        try {
            VOR.add(new VOR(data[0], Float.parseFloat(data[1]), data[2], data[3]));
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(DecodingCoordinateException e) {}
    }
    private void decodeColor(String line) {
        String[] data = line.split(" ");
        try {
            Color.add(data[1], Integer.parseInt(data[2]));
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(NumberFormatException e) {}
    }
}