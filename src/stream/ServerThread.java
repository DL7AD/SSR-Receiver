package stream;

import decoder.Squitter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class ServerThread extends Thread {
    
    private ArrayList<Squitter> buffer = new ArrayList<Squitter>();
    private int port;
    private boolean isOnline;
    private ObjectOutputStream output;
    private Socket socket;
    private ServerSocket serverSocket;
    private boolean externalClosed;
    
    public ServerThread(int port) {
        this.port = port;
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                try {
                    
                    //Try to close old socket if there are one
                    try {
                        if(this.serverSocket != null)
                            this.serverSocket.close();
                    } catch (IOException e) {}
                    
                    //Open new socket
                    this.serverSocket = new ServerSocket(this.port);
                    
                } catch (IOException e) { //Failed to open new socket
                    
                    JOptionPane.showMessageDialog(null, "Could not open Server on port " + this.port, "Server Error", JOptionPane.INFORMATION_MESSAGE);
                    this.close();
                    return;
                    
                }
                
                //Wait for connection
                this.socket = this.serverSocket.accept();
                
                //Set thread is connected
                this.isOnline = true;
                
                //Get stream
                this.output = new ObjectOutputStream(this.socket.getOutputStream());
                
                //Transmit data
                while(true) {
                    String transmit = new String();
                    synchronized(this.buffer) {
                        for(Squitter squitter: this.buffer)
                            transmit += "*" + squitter + ";\n";
                        this.buffer = new ArrayList<Squitter>();
                    }
                    this.output.write(transmit.getBytes());
                    this.output.flush();

                    try {
                        Thread.sleep(100);
                    } catch(InterruptedException e) {}
                }
                
            } catch(IOException e) {
                if(this.externalClosed)
                    return;
            } finally {
                //Set thread is not connected
                this.isOnline = false;
                
                //Close connection
                this.clientClose();
            }
        }
    }
    public void clientClose() {
        try {
            if(this.output != null) {
                this.output.write(new String("*close;").getBytes());
                this.output.flush();
            }
        } catch(IOException e) {}
        try {
            if(this.serverSocket != null)
                this.serverSocket.close();
            if(this.socket != null)
                this.socket.close();
            if(this.output != null)
                this.output.close();
        } catch (IOException e) {}
    }
    public void close() {
        this.externalClosed = true;
        this.clientClose();
    }
    public void addBuffer(Squitter squitter) {
        if(!this.isOnline)
            return;
        synchronized(this.buffer) {
            this.buffer.add(squitter);
        }
    }
}