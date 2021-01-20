package Files;

import java.util.List;

public class DDL {
    List<Integer> date=null;
    int userid=0;
    long chatid=0;
    String content=null;
    String remark=null;
    String importance="normal";
    public void setDate(List<Integer> date){
        this.date=date;
    }
    public void setUserid(int userid){
        this.userid=userid;
    }
    public void setChatid(long chatid){
        this.chatid=chatid;
    }
    public void setContent(String content){
        this.content=content;
    }
    public void setRemark(String remark){
        this.remark=remark;
    }
    public void setImportance(String importance){
        this.importance=importance;
    }

    public String getRemark() {
        return remark;
    }

    public String getContent() {
        return content;
    }

    public long getChatid() {
        return chatid;
    }

    public int getUserid() {
        return userid;
    }

    public List<Integer> getDate() {
        return date;
    }

    public String getImportance() {
        return importance;
    }
    public String getDate(boolean format){
        String date=this.date.get(0).toString()+"年"+this.date.get(1).toString()+"月"+this.date.get(2).toString()+"号"+
                this.date.get(3).toString()+"点"+this.date.get(4).toString()+"分";
        return date;
    }
    public boolean isnull(){
        if(getDate()!=null){
            return false;
        }
        else if(getContent()!=null){
            return false;
        }
        else if(getRemark()!=null){
            return false;
        }
        else if(getImportance()!=null){
            return false;
        }
        else return true;
    }
}
