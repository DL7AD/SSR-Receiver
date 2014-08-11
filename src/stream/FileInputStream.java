package stream;

import decoder.Squitter;
import exception.MalformedSquitterException;
import exception.SquitterLengthException;
import gui.Preferences;
import gui.SquitterViewer;
import java.io.*;
import java.util.ArrayList;

public class FileInputStream extends InputStream {
    
    private File file;
    private BufferedReader bufferedReader;
    private int currentLineNumber;
    private int maxLineNumber;
    private Long lastSquitterAtTime;
    private Long timePerSquitter;
    
    public FileInputStream(String filename) {
        this.file = new File(filename);
        
        try {
            
            //Set Max lines of file in GUI
            LineNumberReader lnr = new LineNumberReader(new FileReader(this.file));
            lnr.skip(Long.MAX_VALUE);
            this.maxLineNumber = lnr.getLineNumber();
            
            //Open file
            java.io.FileInputStream fstream = new java.io.FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            this.bufferedReader = new BufferedReader(new InputStreamReader(in));
            
            //Set Replay speed
            this.timePerSquitter = (long)(1000000000 / new Double(Preferences.getInstance().getSquittersPerSecond()));
            
        } catch(IOException e) {}
    }
    public ArrayList<Squitter> getSquitter() {
        if(this.lastSquitterAtTime == null)
            this.lastSquitterAtTime = System.nanoTime();
        
        //Calculate how much squitters must be generated
        int squitters = (int)((System.nanoTime() - this.lastSquitterAtTime) / this.timePerSquitter);
        if(squitters < 1)
            return new ArrayList<Squitter>();
        this.lastSquitterAtTime += squitters * this.timePerSquitter;
        
        ArrayList<Squitter> squitterList = new ArrayList<Squitter>();
        
        try {
            String strLine;
            while(squitters > 0 && (strLine = this.bufferedReader.readLine()) != null) {
                
                this.currentLineNumber++;
                Squitter squitter = null;
                
                try {
                    
                    squitterList.add(new Squitter(
                        strLine.substring(1, strLine.length() - 1),
                        Squitter.FILE_INPUT,
                        this.file.getName()
                    )); //Create new Squitter
                    
                } catch(SquitterLengthException e) { //Squitter is malformed, it has an other number of characters than expected
                    SquitterViewer.getInstance().addPacket(squitter, "Squitter length error");
                } catch(StringIndexOutOfBoundsException e) { //Squitter is malformed, it has too less characters
                    SquitterViewer.getInstance().addPacket(squitter, "Malformed squitter error");
                } catch(MalformedSquitterException e) { //Number has other caracters than hexadecimal
                    SquitterViewer.getInstance().addPacket(squitter, "Malformed squitter error");
                }
                
                if(this.currentLineNumber == this.maxLineNumber) //Finished reading file
                    this.close();
                
                return squitterList;
            }
        } catch(IOException e) {
            this.close();
        }
        
        return new ArrayList<Squitter>();
    }
    public String getInputStreamName() {
        return this.file.getName();
    }
    public int getLines() {
        return this.maxLineNumber;
    }
    public int getCurrentLine() {
        return this.currentLineNumber;
    }
    public void close() {
        try {
            this.bufferedReader.close();
        } catch(IOException f) {}
        this.setClosed();
    }
}