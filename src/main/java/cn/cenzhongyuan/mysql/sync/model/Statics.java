package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.SchemaSyncConfig;
import cn.cenzhongyuan.mysql.sync.enumeration.AlterType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Statics {

    private SchemaSyncConfig config;
    private List<TableStatics> tableStatics = new ArrayList<>();

    public Statics(SchemaSyncConfig config) {
        this.config = config;
    }

    public TableStatics addTableStatics(String table, TableAlter tableAlter) {
        TableStatics ret = new TableStatics(table, tableAlter);
        if(tableAlter.getAlterType() != AlterType.NO) {
            this.tableStatics.add(ret);
        }
        return ret;
    }

    public int alterFailedNum() {

        return 0;
    }
}
