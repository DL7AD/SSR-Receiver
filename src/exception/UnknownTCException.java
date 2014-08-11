package exception;

public class UnknownTCException extends Exception {
    private byte df;
    public UnknownTCException(byte tc, byte df) {
        System.err.println("Unknown TC " + tc);
        this.df = df;
        this.printStackTrace();
    }
    public byte getDF() {
        return this.df;
    }
}