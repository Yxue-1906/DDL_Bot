package Files;


import Interact.Send;
import com.pengrad.telegrambot.request.SendMessage;

import javax.xml.transform.Result;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Database {
    private String DBpath=null;
    private static String date=null;
    public static Connection connection=null;
    static PreparedStatement statement=null;
    static String operation=null;
    public Database(String DBpath){
        date=DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
        if(DBpath==null){
            this.DBpath="database.db";
            System.out.println("Use default database path!");
        }
        else this.DBpath=DBpath;
        connection=getConnection();
        try {
            /**
             * 创建每个用户的ddl表
             */
             operation="CREATE TABLE IF NOT EXISTS DDL"+
                     "(ID        INTEGER PRIMARY KEY  autoincrement NOT NULL," +
                     "ChatId     INT     NOT NULL," +
                     "DateYear   INT     NOT NULL," +
                     "DateMonth  INT     NOT NULL," +
                     "DateDay    INT     NOT NULL," +
                     "DateHour   INT     NOT NULL," +
                     "DateMinute INT     NOT NULL," +
                     "UserId     INT     NOT NULL," +
                     "Content    TEXT    NOT NULL," +
                     "Importance TEXT    NOT NULL," +
                     "Remark     TEXT    )";
             statement=connection.prepareStatement(operation);
             statement.executeUpdate();
             statement.close();
            /**
             * 创建offset表
             */
            operation="CREATE TABLE IF NOT EXISTS OffsetId" +
                    "(ID        INT     NOT NULL DEFAULT 1," +
                    "OffsetId   INT     NOT NULL );";
            statement=connection.prepareStatement(operation);
            statement.executeUpdate();
            operation="";
            /**
             * 建立数据表
             * 如果存在默认是符合要求的数据表
             */
            statement.close();
            operation=null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public Connection getConnection(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:"+DBpath);

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return connection;
    }

    public static boolean checkddl(){
        List<DDL> toAlert= new ArrayList<>();
        List<Integer> id=new ArrayList<>();
        /**
         * 查询数据库中需要提醒的ddl
         */
        if(!date.equals(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))))){
            date=DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
            DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
            String dateImportant=formatter.format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).minusMonths(1));
            String dateNormal=formatter.format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).minusWeeks(1));
            String dateLow=formatter.format(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).minusDays(3));
            String[] splitImportant=dateImportant.split("-");
            String[] splitNormal=dateNormal.split("-");
            String[] splitLow=dateLow.split("-");
            int[] important={0,0,0,0,0};
            int[] normal={0,0,0,0,0};
            int[] low={0,0,0,0,0};
            for(int i=0;i<5;i++){
                important[i]=Integer.parseInt(splitImportant[i]);
                normal[i]=Integer.parseInt(splitNormal[i]);
                low[i]=Integer.parseInt(splitLow[i]);
            }
            try {
                ResultSet temp=null;
                operation="SELECT * FROM DDL "+
                        "WHERE  Importance  == ? " +
                        "AND    DateYear    >= ? " +
                        "AND    DateMonth   >= ? " +
                        "AND    DateDay     >= ? " +
                        "AND    DateHour    >= ? " +
                        "AND    DateMinute  >= ? ;";
                statement=connection.prepareStatement(operation);
                statement.setObject(1,"重要");
                for(int j=0;j<5;j++){
                    statement.setObject(j+2,important[j]);
                }
                temp=statement.executeQuery();
                while(temp.next()){
                    DDL linshi=new DDL();
                    List<Integer> date=new ArrayList<>();
                    date.add(temp.getInt("DateYear"));
                    date.add(temp.getInt("DateMonth"));
                    date.add(temp.getInt("DateDay"));
                    date.add(temp.getInt("DateHour"));
                    date.add(temp.getInt("DateMinute"));
                    linshi.setDate(date);
                    linshi.setContent(temp.getString("Content"));
                    linshi.setRemark(temp.getString("Remark"));
                    toAlert.add(linshi);
                    id.add(temp.getInt("ID"));
                }
                statement.setObject(1,"一般");
                for(int j=0;j<5;j++){
                    statement.setObject(j+2,normal[j]);
                }
                temp=statement.executeQuery();
                while(temp.next()){
                    DDL linshi=new DDL();
                    List<Integer> date=new ArrayList<>();
                    date.add(temp.getInt("DateYear"));
                    date.add(temp.getInt("DateMonth"));
                    date.add(temp.getInt("DateDay"));
                    date.add(temp.getInt("DateHour"));
                    date.add(temp.getInt("DateMinute"));
                    linshi.setDate(date);
                    linshi.setContent(temp.getString("Content"));
                    linshi.setRemark(temp.getString("Remark"));
                    toAlert.add(linshi);
                    id.add(temp.getInt("ID"));
                }
                statement.setObject(1,"低");
                for(int j=0;j<5;j++){
                    statement.setObject(j+2,low[j]);
                }
                temp=statement.executeQuery();
                while(temp.next()){
                    DDL linshi=new DDL();
                    List<Integer> date=new ArrayList<>();
                    date.add(temp.getInt("DateYear"));
                    date.add(temp.getInt("DateMonth"));
                    date.add(temp.getInt("DateDay"));
                    date.add(temp.getInt("DateHour"));
                    date.add(temp.getInt("DateMinute"));
                    linshi.setDate(date);
                    linshi.setContent(temp.getString("Content"));
                    linshi.setRemark(temp.getString("Remark"));
                    toAlert.add(linshi);
                    id.add(temp.getInt("ID"));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            if(toAlert.size()!=0)Send.SendAlert(toAlert,id);
            return  true;
        }
        return true;
    }

    public static int getOffset(){
        int offset=0;
        ResultSet result=null;
        if(connection==null){
            System.out.println("Database fails loading!");
            System.exit(1);
        }
        else{

            try {
                operation="SELECT OffsetId FROM OffsetId";
                statement=connection.prepareStatement(operation);
                result=statement.executeQuery();
                if(result.next()){
                    offset=result.getInt("OffsetId");
                }
                else{
                    offset=1;
                    operation="INSERT INTO OffsetId (id,offsetid) values (1,1)";
                    statement= connection.prepareStatement(operation);
                    statement.executeUpdate();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return offset;
    }

    public static boolean updateOffset(int offset){
        operation="UPDATE OffsetId SET OffsetId=? where ID==1";
        try {
            statement=connection.prepareStatement(operation);
            statement.setObject(1,offset);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("Update failed!");
            throwables.printStackTrace();
        }
        return true;
    }

    public static boolean insertddl(DDL ddl){

        try {
            operation="CREATE TABLE IF NOT EXISTS DDL"+
                    "(ID        INTEGER PRIMARY KEY  autoincrement NOT NULL," +
                    "ChatId     INT     NOT NULL," +
                    "DateYear   INT     NOT NULL," +
                    "DateMonth  INT     NOT NULL," +
                    "DateDay    INT     NOT NULL," +
                    "DateHour   INT     NOT NULL," +
                    "DateMinute INT     NOT NULL," +
                    "UserId     INT     NOT NULL," +
                    "Content    TEXT    NOT NULL," +
                    "Importance TEXT    NOT NULL," +
                    "Remark     TEXT    )";
            statement=connection.prepareStatement(operation);
            statement.executeUpdate();
            operation="INSERT INTO DDL (ID,ChatId,DateYear,DateMonth,DateDay,DateHour,DateMinute,Content,Importance,Remark,userid) " +
                    "VALUES (null,?,?,?,?,?,?,?,?,?,?);";
            statement=connection.prepareStatement(operation);
            //statement.setObject(1, "'"+Integer.toString(ddl.getUserid())+"'");
            statement.setObject(1,ddl.getChatid());
            statement.setObject(2,ddl.getDate().get(0));
            statement.setObject(3,ddl.getDate().get(1));
            statement.setObject(4,ddl.getDate().get(2));
            statement.setObject(5,ddl.getDate().get(3));
            statement.setObject(6,ddl.getDate().get(4));
            statement.setObject(7,ddl.getContent());
            statement.setObject(8,ddl.getImportance());
            statement.setObject(9,ddl.getRemark());
            statement.setObject(10,ddl.getUserid());
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }
    public static boolean updateddl(DDL ddl,int id){
        String[] toset={"DateYear","DateMonth","DateDay","DateHour","DateMinute","Content","Remark","Importance"};
        try {
            for(int i=0;i<8;i++){
                String operation="UPDATE DDL set "+toset[i]+" =? where ID ==? ;";
                statement= connection.prepareStatement(operation);
                statement.setObject(2,id);
                if(i<5){
                    /**
                     * 更新日期
                     */
                    if(ddl.getDate()!=null){
                        statement.setObject(1,ddl.getDate().get(i));
                        statement.executeUpdate();
                    }
                    else{
                        continue;
                    }
                }
                else if(i==5){
                    /**
                     * 更新内容
                     */
                    if(ddl.getContent()!=null){
                        statement.setObject(1,ddl.getContent());
                        statement.executeUpdate();
                    }
                    else{
                        continue;
                    }
                }
                else if(i==6){
                    /**
                     * 更新备注
                     */
                    if(ddl.getRemark()!=null){
                        statement.setObject(1,ddl.getRemark());
                        statement.executeUpdate();
                    }
                    else{
                        continue;
                    }
                }
                else if(i==7){
                    /**
                     * 更新重要性
                     */
                    if(ddl.getImportance()!=null){
                        statement.setObject(1,ddl.getImportance());
                        statement.executeUpdate();
                    }
                    else{
                        continue;
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }
    public static boolean deleteddl(int id){
        try {
            operation="DELETE FROM ddl where ID=?";
            statement= connection.prepareStatement(operation);
            statement.setObject(1,id);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }
    public static boolean delayddl(boolean week,int userid,int id){
        Calendar calendar=Calendar.getInstance();
        calendar.clear();
        ResultSet result=null;
        int chatid=0;
        try {
            operation="SELECT * FROM '"+Integer.toString(userid)+"' WHERE ID==?";
            statement= connection.prepareStatement(operation);
            result=statement.executeQuery();
            if(!result.next()){
                System.out.println("ERROR! Can not find ddl with provided id!");
            }
            chatid=result.getInt("Chatid");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if(week){
            try {
                calendar.set(result.getInt("DateYear"),result.getInt("DateMonth")-1,
                        result.getInt("DateDay"),result.getInt("DateHour"),
                        result.getInt("DateMinute"));
                calendar.add(Calendar.DAY_OF_MONTH,7);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        else{
            try {
                calendar.set(result.getInt("DateYear"),result.getInt("DateMonth")-1,
                        result.getInt("DateDay"),result.getInt("DateHour"),
                        result.getInt("DateMinute"));
                calendar.add(Calendar.DAY_OF_MONTH,1);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String date=sdf.format(calendar.getTime());
        String[] temp=date.split("-");
        operation="UPDATE '?' SET ?=? WHERE ID = ?;";
        try {
            /**
             * 将更新后的日期写入数据库
             */
            statement= connection.prepareStatement(operation);
            statement.setObject(4,id);
            statement.setObject(1,userid);
            statement.setObject(2,"DateYear");
            statement.setObject(3,calendar.get(Calendar.YEAR));
            statement.executeUpdate();
            statement.setObject(2,"DateMonth");
            statement.setObject(3,1+calendar.get(Calendar.MONTH));
            statement.executeUpdate();
            statement.setObject(2,"DateDay");
            statement.setObject(3,calendar.get(Calendar.DAY_OF_MONTH));
            statement.executeUpdate();
            statement.setObject(2,"DateHour");
            statement.setObject(3,calendar.get(Calendar.HOUR));
            statement.executeUpdate();
            statement.setObject(2,"DateMinute");
            statement.setObject(3,calendar.get(Calendar.MINUTE));
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Send.SendMessage(chatid,"更新成功!");
    }

    public static int[] getddl(int userid,int order){
        ResultSet result=null;
        int[] id={0,0,0};
        try {
            /**
             * 如果有该用户的表
             * 可以是插入是新建的
             * 也可以是一开始allowuser时初始化创建的
             */
            if(order==1){
                operation="select * from DDL where id>=? and userid==?;";
            }
            else{
                operation="select * from DDL where id==? and userid==?;";
                /**
                 * 如果是命令的话需要得到第一个
                 * 不是命令的话会提供准确id 交给analyze处理
                 */
                }
            statement= connection.prepareStatement(operation);
            statement.setObject(1,order);
            statement.setObject(2,userid);
            result=statement.executeQuery();
            if(result.next()){
                /**
                 * 如果找到了ddl
                 */
                id[1]=result.getInt("id");
                operation="select * from DDL where id>? and userid==?;";
                statement= connection.prepareStatement(operation);
                statement.setObject(1,id[1]);
                statement.setObject(2,userid);
                result=statement.executeQuery();
                if(result.next()){
                    id[2]=result.getInt("id");
                }
                operation="select * from ddl where ? > id and userid==? ORDER BY ID DESC LIMIT 1;";
                statement= connection.prepareStatement(operation);
                statement.setObject(2,userid);
                statement.setObject(1,id[1]);
                result=statement.executeQuery();
                if(result.next()){
                    id[0]=result.getInt("id");
                }
                return id;
            }
            else{
                return null;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
    public static DDL getddl(int id,int userid,boolean useless){
        ResultSet result=null;
        List<Integer> teger=new ArrayList<>();
        DDL ddl=new DDL();
        try {
            operation="select * from DDL where id==? and userid==?";
            statement= connection.prepareStatement(operation);
            statement.setObject(1,id);
            statement.setObject(2,userid);
            result=statement.executeQuery();
            if(result.next()){
                for(int i=3;i<8;++i){
                    teger.add(result.getInt(i));
                }
                ddl.setDate(teger);
                ddl.setContent(result.getString("Content"));
                ddl.setRemark(result.getString("remark"));
                ddl.setChatid(result.getInt("chatid"));
                return ddl;
            }
            else{
                return null;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}

