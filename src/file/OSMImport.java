package file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import map.GeoLine;
import map.Point;

public class OSMImport {
    
    String xml = new String();
    HashMap<Long,Point> nodes = new HashMap<Long,Point>();
    
    public OSMImport(Point leftTop, Point rightBottom) {
        double left = rightBottom.getLongitude();
        double bottom = leftTop.getLatitude();
        double right = leftTop.getLongitude();
        double top = rightBottom.getLatitude();
        
        BufferedReader in = null;
        try {
            URL url = new URL("http://www.openstreetmap.org/api/0.6/map?bbox=" + left +"," + bottom + "," + right + "," + top);
            URLConnection conn = url.openConnection();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) 
                this.xml += inputLine;
            
            this.decodeNodes();
            this.decodeWays();
            
        } catch(MalformedURLException e) {
        } catch(IOException e) {
        } finally {
            try {
                in.close();
            } catch(IOException e) {}
        }
        
    }
    private void decodeWays() {
        String pattern = "<way(.*?)>(.*?)</way>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.xml);
        while(m.find()) {
            String patternND = "<nd(.*?)[/]?>";
            Pattern rND = Pattern.compile(patternND);
            Matcher mND = rND.matcher(m.group(2));
            Point last = null;
            while(mND.find()) {
                long id = Long.parseLong(mND.group(1).split("ref=\"")[1].substring(0, mND.group(1).split("ref=\"")[1].length() - 1));
                Point curr = this.nodes.get(id);
                if(last != null)
                    GeoLine.add(new GeoLine(curr, last, null));
                last = curr;
            }
        }
    }
    private void decodeNodes() {
        String pattern = "<node(.*?)[/]?>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(this.xml);
        while(m.find()) {
            HashMap<String,String> params = new HashMap<String,String>();
            String[] paramsA = m.group(1).trim().split(" ");
            for(int i = 0; i < paramsA.length; i++) {
                params.put(
                    paramsA[i].split("=")[0],
                    paramsA[i].split("=")[1].substring(1, paramsA[i].split("=")[1].length() - 1)
                );
            }
            
            long id = Long.parseLong(params.get("id"));
            float lat = Float.parseFloat(params.get("lat"));
            float lon = Float.parseFloat(params.get("lon"));
            
            this.nodes.put(id, new Point(lat, lon));
        }
    }
}
