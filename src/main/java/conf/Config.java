package conf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("resources/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  
    public static final String token = properties.getProperty("token");
  
    public static final String username = properties.getProperty("username");
}
