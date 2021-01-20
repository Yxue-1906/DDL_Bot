package Main;

import Files.Config;
import Files.Database;
import Receive.Receive;

import java.io.IOException;
import java.util.Properties;


public class Main {
    /**
     * 应该持有的全局变量
     * @param args
     */
    public static Receive receive=null;
    public static Config config=null;
    public static Database database;
    public static void main(String[] args){
        Main main = new Main();
        main.initial();
        loopHandle loop=new loopHandle(receive,database);
        Thread thread=new Thread(loop);
        thread.start();
    }

    /**
     * 初始化
     * 载入config->传递Config对象给Receive和Analyze
     * 链接数据库->给Analyze和Files
     * 创建bot->给到receive
     */
    void initial(){
        Properties default_config=new Properties();
        try {
            default_config.load(getClass().getResourceAsStream("/default.properties"));
        } catch (IOException e) {
            System.out.println("Load default config failed!");

            e.printStackTrace();
            System.exit(1);
        }/*
        try {
            default_config.load(new FileInputStream("config/default.properties"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1);
        }*/
        config=new Config(default_config);

        database=new Database(config.getProperty("DBpath"));
        receive=new Receive(config.getBotToken(),database);
    }
}
class loopHandle implements Runnable {
    Receive receive=null;
    Database database=null;
    public loopHandle(Receive receive,Database database){
        this.receive=receive;
        this.database=database;
    }
    @Override
    public void run() {
        while (true){
            receive.getUpdate();
            Database.checkddl();
            /**
             * 定时接受信息和查看ddl
             */
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * 改变思路
 * 将检查ddl写入主线程
 * 定时检查
 */
/*
class checkDDL implements Runnable{
    //暂时不写数据库 改为在主线程里检查
    Database database=null;
    public checkDDL(Database database){
        this.database=database;
    }
    @Override
    public void run() {
        if(Database.checkddl(Config.getAllowedUser())){
            System.out.println("Check ok!");
        }
        else{
            System.out.println("Check DDL failed!");
        }
    }
}

 */
