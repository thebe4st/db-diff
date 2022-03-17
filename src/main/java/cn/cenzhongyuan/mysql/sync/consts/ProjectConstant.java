package cn.cenzhongyuan.mysql.sync.consts;


public class ProjectConstant {

    public static final String INDEX_REG = "([A-Z]+\\s)?KEY\\s.*";

    public static final String FOREIGN_REG = "CONSTRAINT `(.+)` FOREIGN KEY.+ REFERENCES `(.+)` ";

    public static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    public static final String LINE_JOIN_DELIMITER = ",\n";
}
