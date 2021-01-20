package Receive;

import Analyze.Analyze;
import Files.Database;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

import java.sql.Connection;
import java.util.List;

public class Receive {
    public static TelegramBot bot=null;
    public static Connection databaseConnection=Database.connection;

    private Analyze analyze=null;
    private List<Update> updates=null;
    private int offset=0;

    public Receive(String BotToken, Database database){
        bot=new TelegramBot(BotToken);
        analyze=new Analyze();
    }
    public void getUpdate(){
        offset=Database.getOffset();
        GetUpdates getupdates=new GetUpdates().limit(1).offset(offset).timeout(0);
        GetUpdatesResponse response=bot.execute(getupdates);
        if(response.isOk()){
            List<Update> update=response.updates();
            if(update.size()!=0){
                Database.updateOffset(update.get(update.size()-1).updateId()+1);
                analyze.analyze(update);
            }
        }
        else {
            System.out.println("Update failed!");
        }
    }
}
