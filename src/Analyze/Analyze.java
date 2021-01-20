package Analyze;

import Files.Config;
import Files.DDL;
import Files.Database;
import Interact.Send;
import com.pengrad.telegrambot.model.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analyze {
    List<DDL> ddls=new ArrayList<>();
    List<Integer> userid_toupdate=new ArrayList<>(),id_toupdate=new ArrayList<>();
    class bool {
        boolean mark_date,mark_content;
    }
    List<bool> mark=new ArrayList<>();
    public boolean analyze(List<Update> results){
        /**
         * 首先检查是不是命令
         * start和help返回帮助信息
         * ddl返回该用户的一条ddl
         * 否则检查用户的输入
         */
        int size=results.size();
        for(int i=0;i<size;++i){
            if(!checkIsPermitted(results.get(i))){
                Send.SendError(results.get(i).message().chat().id(),2);
                continue;
            }
            if(checkIsReturn(results.get(i))){
                continue;
            }
            else if(checkIsCommand(results.get(i))){
                continue;
            }
            else if(userid_toupdate.size()!=0){
                int k=0;
                for(;k< userid_toupdate.size();++k){
                    if(results.get(i).message().from().id().equals(userid_toupdate.get(k)))break;
                }
                if(k==userid_toupdate.size()){
                    Send.SendError(results.get(i).message().chat().id(),3);
                    continue;
                }
                DDL ddl=new DDL();
                List<Integer> temp_date=checkDate(results.get(i));
                if(temp_date!=null){
                    ddl.setDate(temp_date);
                    temp_date=null;
                }
                String temp_string=checkContent(results.get(i));
                if(temp_string!=null){
                    ddl.setContent(temp_string);
                    temp_string=null;
                }
                temp_string=checkRemark(results.get(i));
                if(temp_string!=null){
                    ddl.setRemark(temp_string);
                    temp_string=null;
                }
                temp_string=checkImportance(results.get(i));
                if(temp_string!=null){
                    ddl.setImportance(temp_string);
                    temp_string=null;
                }
                if(ddl.isnull()){
                    Send.SendError(results.get(i).message().chat().id(),1);
                    continue;
                }
                Database.updateddl(ddl,id_toupdate.get(k));
                Send.SendMessage(results.get(i).message().chat().id(),"修改成功!");
                id_toupdate.remove(k);
                userid_toupdate.remove(k);
            }
            else{
                int j=0;
                //凑齐日期和内容就会写入数据库
                if(ddls.size()!=0){
                    for(j=0;j< ddls.size();++j){
                        if(ddls.get(j).getChatid()==results.get(i).message().chat().id()){
                            break;
                        }
                        else if(j==ddls.size()-1){
                            ddls.add(new DDL());
                            j++;
                            ddls.get(j).setUserid(results.get(i).message().from().id());
                            ddls.get(j).setChatid(results.get(i).message().chat().id());
                        }
                    }
                }
                else{
                    ddls.add(new DDL());
                    ddls.get(j).setUserid(results.get(i).message().from().id());
                    ddls.get(j).setChatid(results.get(i).message().chat().id());
                    mark.add(new bool());
                    mark.get(j).mark_content=mark.get(j).mark_date=false;
                }
                if(checkIsEnd(results.get(i))){
                    if(mark.get(j).mark_content&&mark.get(j).mark_date){
                        if(ddls.get(j).getImportance()==null){
                            ddls.get(j).setImportance("一般");
                        }
                        Database.insertddl(ddls.get(j));
                        ddls.remove(j);
                        Send.SendMessage(results.get(i).message().chat().id(),"我确实收到你的ddl了!");
                        continue;
                    }
                    else{
                        if(mark.get(j).mark_content){
                            Send.SendMessage(results.get(i).message().from().id(),"您还未提供日期!");
                            continue;
                        }
                        else{
                            Send.SendMessage(results.get(i).message().from().id(),"您还未提供ddl内容!");
                            continue;
                        }
                    }
                }
                List<Integer> temp_date=checkDate(results.get(i));
                if(temp_date!=null){
                    ddls.get(j).setDate(temp_date);
                    temp_date=null;
                }
                if(ddls.get(j).getDate()!=null){
                    mark.get(j).mark_date=true;
                }
                String temp_string=checkContent(results.get(i));
                if(temp_string!=null){
                    ddls.get(j).setContent(temp_string);
                    temp_string=null;
                }
                if(ddls.get(j).getContent()!=null){
                    mark.get(j).mark_content=true;
                }
                temp_string=checkRemark(results.get(i));
                if(temp_string!=null){
                    ddls.get(j).setRemark(temp_string);
                    temp_string=null;
                }
                temp_string=checkImportance(results.get(i));
                if(temp_string!=null){
                    ddls.get(j).setImportance(temp_string);
                    temp_string=null;
                }
                if(ddls.get(j).isnull()){
                    Send.SendError(results.get(i).message().chat().id(),1);
                }
            }
        }
        return true;
    }
    List<Integer> checkDate(Update update){
        String text=update.message().text();
        List<Integer> temp =new ArrayList<>();
        String tomatch="^ *" +
                "((日期|时间|((D|d)(A|a)(T|t)(E|e)))(:|：| *)){0,1}" +
                "((?<year>[0-9]{4})(年|-| +)){0,1}" +
                "((?<month>([1-9])|(1[0-2]))(月|-| +)){0,1}" +
                "((?<day>(([1-9]))|([1-2][0-9])|(3[0-1]))(日|号| +|$)){0,1}" +
                "((?<hour>[0-9]|(1[0-9])|(2[0-3]))(时|点| +|:|：)){0,1}" +
                "((?<minute>[1-9]|([1-5][0-9]))(分| +|$)){0,1} *$";
        Pattern pattern = Pattern.compile(tomatch,Pattern.MULTILINE);
        Matcher matcher= pattern.matcher(text);
        int[] date={0,0,0,0,0};
        if(matcher.find()){
            date[0]=Integer.parseInt(matcher.group("year")==null?"2020":matcher.group("year"));
            date[1]=Integer.parseInt(matcher.group("month")==null?"12":matcher.group("month"));
            date[2]=Integer.parseInt(matcher.group("day")==null?"31":matcher.group("day"));
            date[3]=Integer.parseInt(matcher.group("hour")==null?"23":matcher.group("hour"));
            date[4]=Integer.parseInt(matcher.group("minute")==null?"59":matcher.group("minute"));
            for(int i=0;i<5;++i){
                temp.add(date[i]);
            }
            return temp;
        }
        return null;
    }
    String checkContent(Update update){
        String text=update.message().text();
        String tomatch="^ *(((d|D)(d|D)(L|l))|内容)( +|:|：)(?<DDL>.*)$";
        Pattern pattern = Pattern.compile(tomatch,Pattern.MULTILINE);
        Matcher matcher= pattern.matcher(text);
        String content=null;
        if(matcher.find()){
            content=matcher.group("DDL");
            return content;
        }
        else return null;
    }
    String checkRemark(Update update){
        String text=update.message().text();
        String tomatch="^ *(remark|备注)( +|:|：)(?<Remark>.*)$";
        Pattern pattern = Pattern.compile(tomatch,Pattern.MULTILINE);
        Matcher matcher= pattern.matcher(text);
        String remark=null;
        if(matcher.find()){
            remark=matcher.group("Remark");
            return remark;
        }
        else return null;
    }
    String checkImportance(Update update){
        String text=update.message().text();
        String tomatch="((^重要性( +|:|：))|(^ *))(?<improtance>(重要|一般|低))$";
        Pattern pattern = Pattern.compile(tomatch,Pattern.MULTILINE);
        Matcher matcher= pattern.matcher(text);
        String improtance=null;
        if(matcher.find()){
            improtance=matcher.group("improtance");
            return improtance;
        }
        else return null;
    }
    boolean checkIsCommand(Update update){
        String text=update.message().text();
        if(text.equals("/start")||text.equals("/help")){
            Send.SendHelp(update.message().chat().id());
            return true;
        }
        else if(text.equals("/ddl")){
            int[] id=Database.getddl(update.message().from().id(),1);
            if(id!=null){
                Send.SendDDL(Database.getddl(id[1],update.message().from().id(),true),id[0],id[2],id[1]);
                return true;
            }
            else{
                Send.SendMessage(update.message().chat().id(),"你还没有添加ddl哦!");
                return true;
            }
        }
        else return false;
    }
    boolean checkIsReturn(Update update){
        if(update.callbackQuery()!=null){
            String callback=update.callbackQuery().data();
            String pattern_number=".*(?<number>[0-9]+)";
            String pattern_toupdate="(?<update>edit|day|week)";
            String pattern_todelete="(?<delete>complete)";
            Pattern number=Pattern.compile(pattern_number,Pattern.MULTILINE);
            Pattern toupdate=Pattern.compile(pattern_toupdate,Pattern.MULTILINE);
            Pattern todelete=Pattern.compile(pattern_todelete,Pattern.MULTILINE);
            Matcher number_matcher=number.matcher(callback);
            Matcher toupdate_matcher=toupdate.matcher(callback);
            Matcher todelete_matcher=todelete.matcher(callback);
            if(toupdate_matcher.find()){
                String to_update_type=toupdate_matcher.group("update");
                if(to_update_type.equals("edit")){
                    userid_toupdate.add(update.callbackQuery().from().id());
                    if(number_matcher.find()){
                        id_toupdate.add(Integer.parseInt(number_matcher.group("number")));
                    }
                    else{
                        Send.SendMessage(update.callbackQuery().from().id(),"未知错误!");
                    }
                    if(Database.getddl(id_toupdate.get(id_toupdate.size()-1),userid_toupdate.get(userid_toupdate.size()-1),true)==null){
                        Send.SendMessage(update.callbackQuery().from().id(),"你是不是点击了历史遗留信息呢?..");
                        Send.SendDelete(update.callbackQuery().message().messageId(),update.callbackQuery().message().chat().id());
                        return true;
                    }
                    Send.SendDelete(update.callbackQuery().message().messageId(),update.callbackQuery().message().chat().id());
                    Send.SendMessage(update.callbackQuery().message().chat().id(),"请输入你要修改的内容 格式与正常发送ddl相同\n" +
                            "可以仅包含你想修改的项,缺省项会使用原有内容\n" +
                            "请注意发送之后会直接修改 也就是说不想点很多次修改按钮的话就在同一条消息内提供所有需要修改的东西吧...");
                }
                else if(to_update_type.equals("week")){
                    if(number_matcher.find()){
                        Send.CallAlert(update.callbackQuery().id(),"遇到困难睡大觉~\uD83D\uDE34",false);
                        int id=Integer.parseInt(number_matcher.group("number"));
                        int userid=update.callbackQuery().from().id();
                        if(Database.getddl(id,userid,true)==null){
                            Send.SendMessage(update.callbackQuery().from().id(),"你是不是点击了历史遗留信息呢?..");
                            Send.SendDelete(update.callbackQuery().message().messageId(),update.callbackQuery().message().chat().id());
                            return true;
                        }
                        Database.delayddl(true,userid,id);
                    }
                    else{
                        Send.SendMessage(update.callbackQuery().from().id(),"未知错误!");
                    }
                }
                else if(to_update_type.equals("day")){
                    if(number_matcher.find()){
                        Send.CallAlert(update.callbackQuery().id(),"遇到困难睡大觉~\uD83D\uDE34",false);
                        int id=Integer.parseInt(number_matcher.group("number"));
                        int userid=update.callbackQuery().from().id();
                        if(Database.getddl(id,userid,true)==null){
                            Send.SendMessage(update.callbackQuery().from().id(),"你是不是点击了历史遗留信息呢?..");
                            Send.SendDelete(update.callbackQuery().message().messageId(),update.callbackQuery().message().chat().id());
                            return true;
                        }
                        Database.delayddl(false,update.callbackQuery().from().id(),Integer.parseInt(number_matcher.group("number")));
                    }
                    else{
                        Send.SendMessage(update.callbackQuery().from().id(),"未知错误!");
                    }
                }
                else if(to_update_type.equals("delete")){
                    Send.SendDelete(update.callbackQuery().message().messageId(),update.callbackQuery().message().chat().id());
                    Send.CallAlert(update.callbackQuery().id(),"要确实记得完成哦?..",false);
                }
            }
            else if(todelete_matcher.find()){
                if(number_matcher.find()){
                    if(Database.getddl(Integer.parseInt(number_matcher.group("number")),update.callbackQuery().from().id(),true)==null){
                        Send.SendMessage(update.callbackQuery().from().id(),"你是不是点击了历史遗留信息呢?..");
                        Send.SendDelete(update.callbackQuery().message().messageId(),update.callbackQuery().message().chat().id());
                        return true;
                    }
                    Send.CallAlert(update.callbackQuery().id(),"恭喜完成!\uD83C\uDF89",false);
                    Send.SendDelete(update.callbackQuery().message().messageId(),update.callbackQuery().message().chat().id());
                    Database.deleteddl(Integer.parseInt(number_matcher.group("number")));
                }
                else{
                    Send.SendMessage(update.callbackQuery().from().id(),"未知错误!");
                }
            }
            else if(number_matcher.find()){
                String out=number_matcher.group("number");
                int out1=Integer.parseInt(out);
                if(out1==0){
                    Send.CallAlert(update.callbackQuery().id(),"没有了哦~",false);
                }
                else{
                    int[] id=Database.getddl(update.callbackQuery().from().id(),out1);
                    Send.SendEdit(update.callbackQuery().message().messageId(),Database.getddl(out1,update.callbackQuery().from().id(),true),id);
                }
            }
            else{
                Send.SendError(update.message().chat().id(),3);
            }
            return true;
        }
        else return false;
    }
    boolean checkIsPermitted(Update update){
        String[] allowed=Config.getAllowedUser();
        if(allowed==null){
            return true;
        }
        else {
            for(int i=0;i<allowed.length;++i){
                if(update.message().from().id()==Integer.parseInt(allowed[i])){
                    return true;
                }
            }
            return false;
        }
    }
    boolean checkIsEnd(Update update){
        String text=update.message().text();
        String pattern_end="(?<end>^ *(结束|((E|e)(N|n)(D|d))) *$)";
        Pattern pattern=Pattern.compile(pattern_end);
        Matcher matcher_end=pattern.matcher(text);
        if(matcher_end.find()){
            return true;
        }
        else{
            return false;
        }
    }
}
