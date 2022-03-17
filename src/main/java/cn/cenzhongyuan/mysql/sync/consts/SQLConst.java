package cn.cenzhongyuan.mysql.sync.consts;

public interface SQLConst {
    String tableName();

    String tableSchema(String tableName);

    String dropTable(String tableName);

    String alertTable(String tableName, String subSQL);

    String alertTableChangeFieldSubSQL(String fieldName, String subSQL);

    String alertTableAddFieldSubSQL(String subSQL);

    String alertTableDelFieldSubSQL(String subSQL);

    String alertTableAddIndexSubSQL(String subSQL);

    String alertTableDelPrimaryIndexSubSQL(String subSQL);

    String alertTableDelForeignIndexSubSQL(String subSQL);

    String alertTableDelIndexSubSQL(String subSQL);
}
