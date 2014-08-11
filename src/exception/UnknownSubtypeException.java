package exception;

public class UnknownSubtypeException extends Exception {
    private byte df;
    public UnknownSubtypeException(byte tc, byte subtype, byte df) {
        System.err.println("Unknown Subtype (TC=" + tc + "): " + subtype);
        this.df = df;
        this.printStackTrace();
    }
    public byte getDF() {
        return this.df;
    }
}