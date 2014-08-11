package stream;

import decoder.Squitter;
import java.util.ArrayList;

public class RTLInputStream extends InputStream {
    
    private int deviceID;
    
    public RTLInputStream(int deviceID) {
    }
    public ArrayList<Squitter> getSquitter() {
        return new ArrayList<Squitter>();
    }
    public String getInputStreamName() {
        return "RTL" + this.deviceID;
    }
    public void close() {}
}