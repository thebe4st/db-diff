package cn.cenzhongyuan.mysql.sync.enumeration;

public enum IndexType {
    PRIMARY(0,"PRIMARY"),
    INDEX(1,"INDEX"),
    FOREIGN(2,"FOREIGN KEY");

    private int code;
    private String type;

    IndexType(int code, String type) {
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
