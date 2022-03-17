package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.consts.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.enums.IndexType;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
public class Index {

    private IndexType indexType;

    private String name;

    private String sql;

    // 相关联的表
    private List<String> relationTables = new ArrayList<>();

    public Index(String sql) {
        this.sql = sql;
    }

    public static Index parseDbIndexLine(String line) {
        line = line.trim();
        Index idx = new Index(line);

        if (line.startsWith("PRIMARY")) {
            idx.setIndexType(IndexType.PRIMARY);
            idx.setName("PRIMARY KEY");
            return idx;
        }

        if (Pattern.matches(ProjectConstant.INDEX_REG, line)) {
            List<String> arr = StrUtil.split(line, "`");
            idx.setIndexType(IndexType.INDEX);
            idx.setName(arr.get(1));
            return idx;
        }
        Pattern foreignReg = Pattern.compile(ProjectConstant.FOREIGN_REG);
        Matcher m = foreignReg.matcher(line);
        if (m.find()) {
            idx.setIndexType(IndexType.FOREIGN);
            idx.setName(m.group(1));
            idx.addRelationTable(m.group(2));
            return idx;
        }

        log.error("db_index parse failed,unsupported,line: {}", line);
        return null;
    }


    public String alterAddSql(boolean drop) {
        List<String> alterSQL = new ArrayList<>();
        if (drop) {
            String dropSQL = this.alterDropSQL();
            if (StrUtil.isNotBlank(dropSQL)) {
                alterSQL.add(dropSQL);
            }
        }

        switch (this.indexType) {
            case PRIMARY:
                alterSQL.add(String.format("ADD %s", sql));
                break;
            case FOREIGN:
            case INDEX:
                alterSQL.add(String.format("ADD %s", sql));
                break;
            default:
               log.error("unknown indexType {}", this.indexType);
        }
        return String.join(ProjectConstant.LINE_JOIN_DELIMITER, alterSQL);
    }

    private String alterDropSQL() {
        switch (this.indexType) {
            case PRIMARY:
                return "DROP PRIMARY KEY";
            case INDEX:
                return String.format("DROP INDEX `%s`", this.name);
            case FOREIGN:
                return String.format("DROP FOREIGN KEY `%s`", this.name);
            default:
                log.error("unknown indexType {}", this.indexType);
        }
        return "";
    }

    public void addRelationTable(String table) {
        table = table.trim();
        if (StrUtil.isNotBlank(table)) {
            this.relationTables.add(table);
        }
    }

    public String toString() {
        return JSON.toJSONString(this);
    }

}
