package decoder;

import exception.MalformedSquitterException;
import exception.SquitterLengthException;
import java.math.BigInteger;

public class Squitter {
    private static boolean[] polynom = {true,true,true,true,true,true,true,true,
        true,true,true,true,true,false,true,false,false,false,false,false,false,
        true,false,false,true}; //Generator Polynom
    
    private String squitter;
    private int inputStreamType;
    private String inputStreamName;
    
    public static final int SERIAL_INPUT = 1;
    public static final int NETWORK_INPUT = 2;
    public static final int FILE_INPUT = 3;
    
    /**
     * Constructor for a squitter. Checks the squitter generally onto length and
     * hexadecimal characters.
     * @param squitter Received squitter
     * @throws MalformedSquitterException Thrown when Squitter has other elements than only A-Z and 0-9
     * @throws SquitterTooShort Thrown when squitter has less that 3 Characters
     */
    public Squitter(String squitter, int inputStreamType, String inputStreamName) throws SquitterLengthException,MalformedSquitterException {
        //Check if squitter is a valid squitter by checking length
        if(squitter.length() != 14 && squitter.length() != 28)
            throw new SquitterLengthException();
        
        //Check if squitter is a valid hexadecimal String
        if(!this.isHexDigit(squitter))
            throw new MalformedSquitterException(); //Number has other caracters than hexadecimal
        
        //Import squittter
        this.squitter = squitter;
        this.inputStreamType = inputStreamType;
        this.inputStreamName = inputStreamName;
    }
    /**
     * Returns the validity of a hexadecimal String.
     * @param hexDigit Hexadecimal String
     * @return Returns True if String is hexadecimal
     * @see http://www.velocityreviews.com/forums/t144530-check-if-a-character-is-hex.html
     */
    private boolean isHexDigit(String hexDigit) {
        char[] hexDigitArray = hexDigit.toCharArray();
        int hexDigitLength = hexDigitArray.length;
        
        for(int i = 0; i < hexDigitLength; i++) {
            if(Character.digit(hexDigitArray[i], 16) == -1)
                return false;
        }
        return true;
    }
    /**
     * Calculates the Rest of a Polynomdivition. This method provides the parity
     * check with more than a 64bit message
     * @param squitter The completly received 56bit or 112bit message
     * @return Rest of polynomdivition
     */
    public int calculateParity() {
        //Convert Data to a boolean Array
        boolean[] datastream = this.hexStringToBinaryArray(this.squitter.toString());
        for(int i = 0; i <= datastream.length - Squitter.polynom.length; i++) {
            //Find beginning of current number
            if(!datastream[i])
                continue;
            //Make Polynomdivision
            for(int u = 0; u < Squitter.polynom.length; u++)
                datastream[i+u] = datastream[i+u] ^ Squitter.polynom[u];
        }
        
        //Calculate rest of dvisition
        String parity = "";
        for(int i = datastream.length - Squitter.polynom.length + 1; i < datastream.length; i++)
            parity += datastream[i] ? "1" : "0";
        
        return Integer.parseInt(parity, 2);
    }
    /**
     * Converts a hexadecimal String to a binary Array. The Array has 24 or more indexes.
     * @param hex Hexadecimal String
     * @return Boolean Array of hexadecimal String
     */
    private boolean[] hexStringToBinaryArray(String hex) {
        BigInteger squitter = new BigInteger(hex, 16); //Initializes a BigInteger
        
        //Convert it to a binary String
        String bitsString = squitter.toString(2);
        
        //Convert to binary Array
        boolean[] bits = new boolean[bitsString.length()];
        for(int i = 0; i < bits.length; i++)
            bits[i] = bitsString.charAt(i) == '1';
        
        if(bits.length < 24) { //Array has less than 24 bits
            boolean[] bits24 = new boolean[24];
            boolean[] zerobits = new boolean[24-bits.length]; //Create some 0 bits
            //Concatenate Zero bits with calculated bits
            System.arraycopy(zerobits, 0, bits24, 0, zerobits.length);
            System.arraycopy(bits, 0, bits24, zerobits.length, bits.length);
            return bits24;
        }
        
        return bits;
    }
    /**
     * The method does make a parity check and gives back the validation of
     * the parity check
     * @return Squitter is valid
     */
    public boolean isValid() {
        return calculateParity() == 0;
    }
    /**
     * Returns squitter message
     * @return Squitter
     */
    @Override
    public String toString() {
        return this.squitter.toString();
    }
    public int getInputStreamType() {
        return this.inputStreamType;
    }
    public String getInputStreamName() {
        return this.inputStreamName;
    }
    public int size() {
        return this.squitter.length() * 4;
    }
}