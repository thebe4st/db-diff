package cn.cenzhongyuan.mysql.sync.enumeration;


public enum AlterType {
    NO(0, "not change"),
    CREATE(1,"create"),
    DROP(2,"drop"),
    ALTER(3,"alter");

    private int type;
    private String msg;

    AlterType(int type,String msg) {
        this.type = type;
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }
}
