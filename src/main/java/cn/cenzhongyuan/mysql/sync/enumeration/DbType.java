package cn.cenzhongyuan.mysql.sync.enumeration;

public enum  DbType {

    SOURCE(0,"source"),
    DEST(1,"dest");

    private int code;
    private String type;

    DbType(int code, String type) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
