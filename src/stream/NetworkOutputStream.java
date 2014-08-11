package stream;

import decoder.Squitter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class NetworkOutputStream extends OutputStream {
    
    private IoAcceptor acceptor;
    
    public NetworkOutputStream(int port) {
        if(port > 65535) {
            JOptionPane.showMessageDialog(null, "The port is set too high.", "Server Error", JOptionPane.INFORMATION_MESSAGE);
            this.setClosed();
            return;
        }
        
        this.acceptor = new NioSocketAcceptor();
        
        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        
        this.acceptor.setHandler(new ServerHandler());
        this.acceptor.getSessionConfig().setReadBufferSize(16);
        this.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        
        try {
            this.acceptor.bind(new InetSocketAddress(port));
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "The could not be started.", "Server Error", JOptionPane.INFORMATION_MESSAGE);
            this.setClosed();
        }
    }
    
    @Override
    public void writeSquitter(Squitter squitter) {
        ((ServerHandler)this.acceptor.getHandler()).sendSquitter(squitter);
    }
    
    public ArrayList<String> getConnectedClients() {
        return ((ServerHandler)this.acceptor.getHandler()).getClients();
    }

    @Override
    public void close() {
        this.acceptor.unbind();
        this.setClosed();
    }
}