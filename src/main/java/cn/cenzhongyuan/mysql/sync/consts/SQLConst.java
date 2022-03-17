package cn.cenzhongyuan.mysql.sync.consts;

public interface SQLConst {
    String tableName();

    String tableSchema(String tableName);

    String dropTable(String tableName);

    String alertTable(String tableName, String subSQL);

    String alertTableChangeSubSQL(String fieldName, String subSQL);

    String alertTableAddSubSQL(String subSQL);
}
