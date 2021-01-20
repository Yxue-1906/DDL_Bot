package Files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config extends Properties {
    static String BotToken=null;
    static String DBpath=null;
    static String[]  AllowedUser=null;

    public Config(Properties default_config){
        super(default_config);
        try {
            this.load(new FileInputStream("config.properties"));
            if((BotToken=this.getProperty("BotToken",null))==null){
                System.out.println("Please set BotToken!");
                System.exit(1);
            }
            DBpath=this.getProperty("DBpath","database.db");
            if(DBpath.equals("database.db")){
                System.out.println("Use default database path.");
            }
            else{
                System.out.println(DBpath);
            }
            if(this.getProperty("AllowedUser").equals("")){
                System.out.println("You didn't set any permitted user,everyone can access your bot!");
            }
            else{
                AllowedUser=this.getProperty("AllowedUser").split(",");
            }
        } catch (IOException e) {
            System.out.println("Read external config failed! Please check");
            e.printStackTrace();
            System.exit(1);
        }
    }
    public String getBotToken(){
        return BotToken;
    }
    public String getDBpath(){
        return DBpath;
    }
    public static String[] getAllowedUser(){
        return AllowedUser;
    }
}
