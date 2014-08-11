package stream;

import decoder.Airspace;
import decoder.Squitter;
import java.util.ArrayList;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class ServerHandler extends IoHandlerAdapter {
    
    private ArrayList<IoSession> sessions = new ArrayList<IoSession>();
    
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        this.sessions.add(session);
        session.write("Welcome to SSR Receiver 1.0a Server");
        if(Airspace.getMyPosition() != null) {
            session.write("Position of this Receiver:");
            session.write("Latitude: " + Airspace.getMyPosition().getLatitude());
            session.write("Longitude: " + Airspace.getMyPosition().getLongitude());
        }
        session.write("--------------------------------------------------");
        session.write("Squitter stream is starting now");
        session.write("--------------------------------------------------");
    }
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        this.sessions.remove(session);
    }
    
    public void sendSquitter(Squitter squitter) {
        for(IoSession session: this.sessions)
            session.write("*" + squitter + ";");
    }
    public ArrayList<String> getClients() {
        ArrayList<String> clients = new ArrayList<String>();
        for(IoSession session: this.sessions) {
            long timeConnected = System.currentTimeMillis() - session.getCreationTime();
            String timeConnectedH = (timeConnected / 3600000 < 10 ? "0" : "") + Long.toString(timeConnected / 3600000);
            String timeConnectedM = ((timeConnected / 60000) % 3600 < 10 ? "0" : "") + Long.toString((timeConnected / 60000) % 3600);
            String timeConnectedS = ((timeConnected / 1000) % 60 < 10 ? "0" : "") + Long.toString((timeConnected / 1000) % 60);
            clients.add(session.getRemoteAddress().toString().substring(1) + " connected " + timeConnectedH + ":" + timeConnectedM + ":" + timeConnectedS);
        }
        return clients;
    }
}