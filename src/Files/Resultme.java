package Files;

public class Resultme{
    int userid=0;
    int chatid=0;
    String content=null;
    String remark=null;
    public Resultme(int userid,int chatid,String content,String remark){
        this.chatid=chatid;
        this.userid=userid;
        this.content=content;
        this.remark=remark;
    }

    public int getUserid() {
        return userid;
    }

    public int getChatid() {
        return chatid;
    }

    public String getContent() {
        return content;
    }

    public String getRemark() {
        return remark;
    }
}
