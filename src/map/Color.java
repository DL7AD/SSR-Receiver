package map;
import java.util.Hashtable;

public class Color {
    private static Hashtable<String,java.awt.Color> list = new Hashtable<String,java.awt.Color>(25, 0.75f);
    
    public static java.awt.Color getColor(String colorName) {
        if(colorName == null || !Color.list.containsKey(colorName.toLowerCase())) {
            return new java.awt.Color(116, 116, 116);
        }
        return Color.list.get(colorName.toLowerCase());
    }
    public static void resetAll() {
        Color.list.clear();
    }
    public static void add(String colorName, int color) {
        Color.list.put(colorName.toLowerCase(), new java.awt.Color( new java.awt.Color(color).getBlue(), new java.awt.Color(color).getGreen(), new java.awt.Color(color).getRed() ));
    }
}