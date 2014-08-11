package stream;

import decoder.Squitter;
import exception.MalformedSquitterException;
import exception.SquitterLengthException;
import gui.SquitterViewer;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import jssc.*;

public class SerialInputStream extends InputStream {
    
    private SerialPort serialPort;
    
    private Long lastDataRateUpdateTime;
    private float lastDataRateUpdateValue;
    private int bitCounter;
    
    public SerialInputStream(final String comPort) {
        this.serialPort = new SerialPort(comPort);
        
        try {
            //Open Serial Port
            this.serialPort.openPort();

            //Set Serial Port Configuration
            this.serialPort.setParams(9600, 8, 1, 0);
        } catch (SerialPortException e) {
            if(e.getExceptionType().equals("Port busy")) { //Port is already in use
                JOptionPane.showMessageDialog(null, "Port " + comPort + " is already used by another programm.", "COM Port Error", JOptionPane.INFORMATION_MESSAGE);
            } else if(e.getExceptionType().equals("Port not found")) { //Port not found
                JOptionPane.showMessageDialog(null, "Port " + comPort + " is not existing.", "COM Port Error", JOptionPane.INFORMATION_MESSAGE);
            } else { //General error
                JOptionPane.showMessageDialog(null, "A general error occured when opening Port " + comPort + ". (ERROR_MSG=" + e.getExceptionType() + ")", "COM Port Error", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    public ArrayList<Squitter> getSquitter() {
        try {
            
            if(this.serialPort.getInputBufferBytesCount() == 0)
                return new ArrayList<Squitter>();
            
            String buffer = new String();
            
            //Read serial buffer until last byte is an separator
            do {
                String read;
                while((read = serialPort.readString()) == null)
                    try {
                        Thread.sleep(1);
                    } catch(InterruptedException e) {}
                buffer += read.trim();
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
                    squitter = new Squitter(squitterString, Squitter.SERIAL_INPUT, this.serialPort.getPortName());
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
            
        } catch(SerialPortException e) { //Serial port error
            try {
                this.serialPort.closePort();
            } catch(SerialPortException f) {}
        }
        
        return new ArrayList<Squitter>();
    }
    public String getInputStreamName() {
        return this.serialPort.getPortName();
    }
    public void close() {
        try {
            this.serialPort.closePort();
        } catch(SerialPortException f) {}
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