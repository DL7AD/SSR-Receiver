package stream;

import decoder.Squitter;
import java.util.ArrayList;

public abstract class InputStream {
    private boolean isClosed;
    public abstract ArrayList<Squitter> getSquitter();
    public abstract String getInputStreamName();
    public abstract void close();
    public boolean isClosed() {
        return this.isClosed;
    }
    public void setClosed() {
        this.isClosed = true;
    }
}