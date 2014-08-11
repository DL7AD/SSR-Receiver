package stream;

import decoder.Squitter;
import exception.MalformedSquitterException;
import exception.SquitterLengthException;
import gui.SquitterViewer;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class NetworkInputStream extends InputStream {
    
    private String connection;
    private Socket socket;
    private BufferedReader bufferedReader;
    
    private Long lastDataRateUpdateTime;
    private float lastDataRateUpdateValue;
    private int bitCounter;
    
    public NetworkInputStream(String host, int port) {
        
        this.connection = host + ":" + port;
        
        try {
            this.socket = new Socket(host, port);
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to server.", "Network Error", JOptionPane.INFORMATION_MESSAGE);
            this.close();
        }
    }
    public ArrayList<Squitter> getSquitter() {
        try {
            if(this.bufferedReader == null)
                return new ArrayList<Squitter>();
            
            if(!this.bufferedReader.ready())
                return new ArrayList<Squitter>();
            
            //Read one squitter of the network buffer
            String buffer = new String();
            
            do {
                while(this.bufferedReader.ready()) {
                    buffer += (char)bufferedReader.read();
                    buffer = buffer.trim();
                }
                
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {}
            } while(buffer.charAt(buffer.length() - 1) != ';');
            
            //Separate String into squitters
            ArrayList<String> squitterStringList = new ArrayList<String>();
            do {
                //Delete buffer from beginning until there is an *
                while(buffer.length() > 0 && buffer.charAt(0) != '*')
                    buffer = buffer.substring(1);

                if(buffer.length() == 0)
                    break;

                //Beginning of squitter found, log it
                buffer = buffer.substring(1); //Remove separator (*)
                String squitter = new String(); //Squitter message
                while(buffer.charAt(0) != ';') {
                    squitter += buffer.charAt(0);
                    buffer = buffer.substring(1);
                }
                squitterStringList.add(squitter); //Add squitter to list
                buffer = buffer.substring(1); //Remove separator (;)
            } while(buffer.length() > 0);
            
            ArrayList<Squitter> squitterList = new ArrayList<Squitter>();
            for(String squitterString: squitterStringList) {
                Squitter squitter = null;
                try {
                    squitter = new Squitter(squitterString, Squitter.NETWORK_INPUT, this.connection);
                    squitterList.add(squitter);
                    this.bitCounter += squitter.size();
                } catch(SquitterLengthException e) { //Squitter is malformed, it has an other number of characters than expected
                    SquitterViewer.getInstance().addPacket(squitter, "Squitter length error");
                } catch(StringIndexOutOfBoundsException e) { //Squitter is malformed, it has too less characters
                    SquitterViewer.getInstance().addPacket(squitter, "Malformed squitter error");
                } catch(MalformedSquitterException e) { //Number has other caracters than hexadecimal
                    SquitterViewer.getInstance().addPacket(squitter, "Malformed squitter error");
                }
            }
            
            return squitterList;
            
        } catch(IOException e) { //Network error
            this.close();
        }
        
        return new ArrayList<Squitter>();
    }
    public String getInputStreamName() {
        return this.connection;
    }
    public void close() {
        try {
            if(this.bufferedReader != null)
                this.bufferedReader.close();
            if(this.socket != null)
                this.socket.close();
        } catch(IOException f) {}
        this.setClosed();
    }
    public float getDataRate() {
        if(this.lastDataRateUpdateTime == null) {
            this.lastDataRateUpdateTime = System.currentTimeMillis();
            return 0;
        }
        if(this.lastDataRateUpdateTime + 3000 < System.currentTimeMillis()) {
            this.lastDataRateUpdateValue = this.bitCounter / 3000f;
            this.bitCounter = 0;
            this.lastDataRateUpdateTime += 3000;
        }
        return this.lastDataRateUpdateValue;
    }
}