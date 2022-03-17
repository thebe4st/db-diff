package cn.cenzhongyuan.mysql.sync.consts;

public class MysqlSQLConst implements SQLConst {

    @Override
    public String tableName() {
        return "show table status";
    }

    @Override
    public String tableSchema(String tableName) {
        return String.format("show create table `%s`",tableName);
    }
}
