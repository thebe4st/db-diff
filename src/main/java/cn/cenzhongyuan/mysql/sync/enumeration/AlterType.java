package cn.cenzhongyuan.mysql.sync.enumeration;


import lombok.Getter;

@Getter
public enum AlterType {
    NO(0, "not change"),
    CREATE(1,"create"),
    DROP(2,"drop"),
    ALTER(3,"alter");

    private final int type;
    private final String msg;

    AlterType(int type,String msg) {
        this.type = type;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return this.msg;
    }
}
