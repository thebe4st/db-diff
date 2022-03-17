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

    @Override
    public String dropTable(String tableName) {
        return String.format("drop table `%s`;",tableName);
    }

    @Override
    public String alertTable(String tableName, String subSQL) {
        return String.format("ALTER TABLE `%s`\n%s;", tableName, subSQL);
    }

    @Override
    public String alertTableChangeFieldSubSQL(String fieldName, String subSQL) {
        return String.format("CHANGE `%s` %s",fieldName,subSQL);
    }

    @Override
    public String alertTableAddFieldSubSQL(String subSQL) {
        return "ADD " + subSQL;
    }

    @Override
    public String alertTableDelFieldSubSQL(String subSQL) {
        return String.format("drop `%s`", subSQL);
    }
}
