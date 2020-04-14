package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.core.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.enumeration.IndexType;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class DbIndex {

    private IndexType indexType;

    private String name;

    private String sql;

    // 相关联的表
    private List<String> relationTables = new ArrayList<>();

    public DbIndex(String sql) {
        this.sql = sql;
    }

    public static DbIndex parseDbIndexLine(String line) {
        line = line.trim();
        DbIndex idx = new DbIndex(line);

        if (line.startsWith("PRIMARY")) {
            idx.setIndexType(IndexType.PRIMARY);
            idx.setName("PRIMARY KEY");
            return idx;
        }

        if(Pattern.matches(ProjectConstant.INDEX_REG,line)) {
            String[] arr = StringUtils.split(line, "`");
            idx.setIndexType(IndexType.INDEX);
            idx.setName(arr[1]);
            return idx;
        }
        Pattern foreignReg = Pattern.compile(ProjectConstant.FOREIGN_REG);
        Matcher m = foreignReg.matcher(line) ;
        if(m.find()) {
            idx.setIndexType(IndexType.FOREIGN);
            idx.setName(m.group(1));
            idx.addRelationTable(m.group(2));
            return idx;
        }

        System.out.println(String.format("db_index parse failed,unsupported,line: %s", line));
        return null;
    }


    public String alterAddSql(boolean drop) {
        List<String> alterSQL = new ArrayList<>();
        if(drop) {
            String dropSQL = this.alterDropSQL();
            if(StringUtils.isNotBlank(dropSQL)) {
                alterSQL.add(dropSQL);
            }
        }

        switch (this.indexType) {
            case PRIMARY:
                alterSQL.add(String.format("ADD %s",sql));
                break;
            case FOREIGN:
            case INDEX:
                alterSQL.add(String.format("ADD %s",sql));
                break;
            default:
                System.out.println(String.format("unknow indexType %s", this.indexType));
        }
        return StringUtils.join(alterSQL.toArray(new String[0]),",\n");
    }

    private String alterDropSQL() {
        switch (this.indexType) {
            case PRIMARY:
                return "DROP PRIMARY KEY";
            case INDEX:
                return String.format("DROP INDEX `%s`",this.name);
            case FOREIGN:
                return String.format("DROP FOREIGN KEY `%s`",this.name);
            default:
                String.format("unknown indexType %s", this.indexType);
        }
        return "";
    }

    public void addRelationTable(String table) {
        table = table.trim();
        if(StringUtils.isNotBlank(table)) {
            this.relationTables.add(table);
        }
    }

    public String toString() {
        return JSON.toJSONString(this);
    }

}
