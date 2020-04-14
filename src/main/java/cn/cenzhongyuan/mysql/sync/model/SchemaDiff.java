package cn.cenzhongyuan.mysql.sync.model;


import lombok.Data;

import java.util.List;

@Data
public class SchemaDiff {

    private String table;
    private TableSchema source;
    private TableSchema dest;

    public SchemaDiff(String table,String source,String dest) {
        this.table = table;
        this.source = TableSchema.parseSchema(source);
        this.dest = TableSchema.parseSchema(dest);
    }

    public List<String> getRelationTables() {
        return this.source.getRelationTables();
    }
}
