package exception;

import decoder.Squitter;

public class NotImplementedException extends Exception {
    public NotImplementedException(String msg, Squitter squitter) {
        System.err.println("Not implemented: " + msg);
        System.err.println("Squitter: " + squitter);
        this.printStackTrace();
    }
}