package cn.cenzhongyuan.mysql.sync.model;


import lombok.Data;

import java.util.List;

@Data
public class SchemaDiff {

    private String table;
    private Table source;
    private Table dest;

    public SchemaDiff(String table,String source,String dest) {
        this.table = table;
        this.source = Table.parseSchema(source);
        this.dest = Table.parseSchema(dest);
    }

    public List<String> getRelationTables() {
        return this.source.getRelationTables();
    }
}
