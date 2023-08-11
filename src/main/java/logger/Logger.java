package logger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;

public class Logger {
    private String fileName;
    private static final String DEFAULT_FILE_NAME = "resources/log.txt";

    public Logger(){
        this.fileName = DEFAULT_FILE_NAME;
    }

    public Logger(String fileName){
        this.fileName = fileName;
    }

    public void log(String message){
        try {
            FileWriter fileWriter = new FileWriter(fileName,true);
            Formatter formatter = new Formatter(fileWriter);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            String time = dtf.format(LocalDateTime.now());
            formatter.format("Time: "+ time+ " -  " +message + "\n");
            formatter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
