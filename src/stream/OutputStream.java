package stream;

import decoder.Squitter;
import java.util.ArrayList;

public abstract class OutputStream {
    private boolean isClosed;
    public abstract void writeSquitter(Squitter squitter);
    public abstract void close();
    public boolean isClosed() {
        return this.isClosed;
    }
    public void setClosed() {
        this.isClosed = true;
    }
}