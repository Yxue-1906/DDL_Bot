package Interact;

import Files.DDL;
import Files.Resultme;
import Receive.Receive;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.sql.ResultSet;
import java.util.List;

public class Send {
    static TelegramBot bot= Receive.bot;
    public static boolean SendAlert(List<DDL> result, List<Integer> id){
        /**
         * 检查时间之后发现需要发送ddl提醒时调用这个方法
         * 传入一个包含目标
         */
        String tosend=null;
        SendMessage sendme=null;
        SendResponse response=null;
        for(int i=0;i<result.size();++i){
            tosend ="你应该完成:"+result.get(i).getContent()+"\n"+"你留下了这样的备注:"+result.get(i).getRemark()+"\n"+"而你的截至日期是"+result.get(i).getDate(true);
            sendme=new SendMessage(result.get(i).getChatid(),tosend).replyMarkup(new InlineKeyboardMarkup(
                    new InlineKeyboardButton[][]{
                            new InlineKeyboardButton[]{
                                    new InlineKeyboardButton("我完成啦哇哈哈!☺️").callbackData("complete "+id.get(i).toString())
                            },
                            new InlineKeyboardButton[]{
                                    new InlineKeyboardButton("再拖一天。。").callbackData("day "+id.get(i).toString()),
                                    new InlineKeyboardButton("再拖一周。。").callbackData("week "+id.get(i).toString())
                            },
                            new InlineKeyboardButton[]{
                                    new InlineKeyboardButton("我,我知道了...").callbackData("delete")
                            }
                    }
            ));
            response=bot.execute(sendme);
            if(!response.isOk()) System.out.println("Send alert failed!");
        }

        return true;
    }
    public static boolean SendError(long chatid,int errortype){
        switch (errortype){
            case 1:{
                SendMessage sendme=new SendMessage(chatid,"抱歉,没法识别你发送的内容!");
                SendResponse response=bot.execute(sendme);
                return  response.isOk();
            }
            case 2:{
                SendMessage sendme=new SendMessage(chatid,"无权用户!");
                SendResponse response=bot.execute(sendme);
                return  response.isOk();
            }
            case 3:{
                SendMessage sendme=new SendMessage(chatid,"未知错误!");
                return bot.execute(sendme).isOk();
            }
        }
        return false;
    }
    public static boolean SendHelp(long chatid){
        SendMessage sendme=new SendMessage(chatid,"这是一个用来记录DDL的bot\n" +
                "请按照以下格式向我发送您的DDL\n" +
                "`\n" +
                "日期:可以是yyyy年mm月dd日(号)hh点mm分 也可以是yyyy-mm-dd hh:mm的格式\n" +
                "内容:\n" +
                "重要性:只可以设定为重要|一般(默认)|低\n" +
                "备注:不是必需\n" +
                "`\n" +
                "`提供完ddl之后请记得发送end或者结束来告诉机器人来记录哦（其他可以缺省，但是日期和内容是必要的）`\n\n"+
                "顺序可以打乱 也可以分条发送或者缺省\n" +
                "要注意的是如果重复发送了同一个属性 例如重复发送了日期 则新日期会覆盖之前记录的日期\n" +
                "" +
                "如果你忘了怎么操作可以发送/help来再次获得帮助").parseMode(ParseMode.Markdown);
        SendResponse response=bot.execute(sendme);
        return response.isOk();
    }
    public static boolean SendMessage(long chatid,String text){
        SendMessage sendme = new SendMessage(chatid,text);
        return bot.execute(sendme).isOk();
    }
    public static boolean SendDDL(DDL ddl,int id_former,int id_latter,int id){
        SendMessage sendme=new SendMessage(ddl.getChatid(),
                "你应该完成:`"+ddl.getContent()+"`\n"+
                        "你留下了这样的备注:`"+ddl.getRemark()+"`\n"+
                        "而你的截至日期是:`"+ddl.getDate(true)+"`").replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton[][]{
                                        new InlineKeyboardButton[]{
                                                new InlineKeyboardButton("上一个").callbackData(Integer.toString(id_former)),
                                                new InlineKeyboardButton("下一个").callbackData(Integer.toString(id_latter))
                                        },
                                        new InlineKeyboardButton[]{
                                                new InlineKeyboardButton("已完成!").callbackData("complete "+Integer.toString(id))
                                        },
                                        new InlineKeyboardButton[]{
                                                new InlineKeyboardButton("我想修改..").callbackData("edit "+Integer.toString(id))
                                        }
                                }
        )).parseMode(ParseMode.MarkdownV2);
        return bot.execute(sendme).isOk();

    }
    public static boolean SendEdit(int messageid,DDL ddl,int[] id){
        EditMessageText edit=new EditMessageText(ddl.getChatid(),messageid,
                "你应该完成:`"+ddl.getContent()+"`\n"+
                        "你留下了这样的备注:`"+ddl.getRemark()+"`\n"+
                        "而你的截至日期是:`"+ddl.getDate(true)+"`").replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{
                        new InlineKeyboardButton[]{
                                new InlineKeyboardButton("上一个").callbackData(Integer.toString(id[0])),
                                new InlineKeyboardButton("下一个").callbackData(Integer.toString(id[2]))
                        },
                        new InlineKeyboardButton[]{
                                new InlineKeyboardButton("已完成!").callbackData("complete "+Integer.toString(id[1]))
                        },
                        new InlineKeyboardButton[]{
                                new InlineKeyboardButton("我想修改..").callbackData("edit "+Integer.toString(id[1]))
                        }
                }
        )).parseMode(ParseMode.MarkdownV2);
        return bot.execute(edit).isOk();
    }
    public static boolean CallAlert(String callbackid,String text,boolean type){
        AnswerCallbackQuery ans=new AnswerCallbackQuery(callbackid).showAlert(type).text(text);
        return bot.execute(ans).isOk();
    }
    public static boolean SendDelete(int messageid, Long chatid){
        DeleteMessage delete=new DeleteMessage(chatid,messageid);
        return bot.execute(delete).isOk();
    }
}
