package stream;

import decoder.Squitter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

public class FileOutputStream extends OutputStream {
    
    private File file;
    private BufferedWriter bufferedWriter;
    private FileWriter fileStream;
    
    public FileOutputStream(String logFolder) {
        //Create filename
        DateFormat dateFormatFile = new SimpleDateFormat("dd.MM.yyyy HHmmss");
        DateFormat dateFormatInFile = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
        Date date = new Date();
        this.file = new File(logFolder + "/" + dateFormatFile.format(date) + ".log");
        
        //Open file
        try {
            this.fileStream = new FileWriter(this.file);
            this.bufferedWriter = new BufferedWriter(fileStream);
            
            this.bufferedWriter.write("SSR log file\r\n");
            this.bufferedWriter.write("Date: " + dateFormatInFile.format(date) + "\r\n");
            this.bufferedWriter.write("----------------------------------------\r\n");
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Could not write squitter to file. Is the output folder set?", "Log Error", JOptionPane.INFORMATION_MESSAGE);
            this.close();
        }
    }
    
    public void writeSquitter(Squitter squitter) {
        if(this.bufferedWriter == null)
            return;
        
        try {
            this.bufferedWriter.write("*" + squitter + ";\r\n");
            this.bufferedWriter.flush();
        } catch(IOException e) {}
    }

    @Override
    public void close() {
        try {
            if(this.bufferedWriter != null)
                this.bufferedWriter.close();
            if(this.fileStream != null)
                this.fileStream.close();
        } catch(IOException e) {}
        this.setClosed();
    }
}