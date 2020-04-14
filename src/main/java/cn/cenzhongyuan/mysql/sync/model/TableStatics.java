package cn.cenzhongyuan.mysql.sync.model;

import lombok.Data;

@Data
public class TableStatics {

    private String table;

    private TableAlter alter;

    private String schemaAfter;

    public TableStatics(String table, TableAlter alter) {
        this.table = table;
        this.alter = alter;
    }
}
