package cn.cenzhongyuan.mysql.sync.model;


import lombok.Data;

import java.util.List;

@Data
public class SchemaDiff {

    private String table;
    private Table source;
    private Table dest;

    public SchemaDiff(String table,Table source,Table dest) {
        this.table = table;
        this.source = source;
        this.dest = dest;
    }

    public List<String> getRelationTables() {
        return this.source.getRelationTables();
    }
}
