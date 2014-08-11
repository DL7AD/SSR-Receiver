package exception;

import decoder.Squitter;

public class UnknownDFException extends Exception {
    private byte df;
    public UnknownDFException(byte df, Squitter squitter) {
        System.err.println("Unknown DF" + df + ": " + squitter);
        this.df = df;
        this.printStackTrace();
    }
    public byte getDF() {
        return this.df;
    }
}