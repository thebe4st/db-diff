package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.enumeration.AlterType;
import lombok.Data;

@Data
public class TableAlter {

    private String table;

    private AlterType alterType;

    private String sql;

    private SchemaDiff schemaDiff;

}
