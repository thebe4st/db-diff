package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.config.DiffContext;
import cn.cenzhongyuan.mysql.sync.consts.CharConst;
import cn.cenzhongyuan.mysql.sync.consts.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.enums.IndexType;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
@ToString
public class Index {

    private IndexType indexType;

    private String name;

    private String sql;

    private DiffContext diffContext;

    // 相关联的表
    private List<String> relationTables = new ArrayList<>();

    public Index(String sql, DiffContext context) {
        this.diffContext = context;
        this.sql = sql;
    }

    public static Index parseDbIndexLine(String line,DiffContext context) {
        line = line.trim();
        Index idx = new Index(line,context);

        if (line.startsWith(IndexType.PRIMARY.getPrefix())) {
            idx.setIndexType(IndexType.PRIMARY);
            idx.setName(IndexType.PRIMARY.getName());
            return idx;
        }

        if (Pattern.matches(ProjectConstant.INDEX_REG, line)) {
            List<String> arr = StrUtil.split(line, CharConst.BACKQUOTE);
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
                alterSQL.add(diffContext.getSqlConst().alertTableAddIndexSubSQL(sql));
                break;
            case FOREIGN:
            case INDEX:
                alterSQL.add(diffContext.getSqlConst().alertTableAddIndexSubSQL(sql));
                break;
            default:
               log.error("unknown indexType {}", this.indexType);
        }
        return String.join(ProjectConstant.LINE_JOIN_DELIMITER, alterSQL);
    }

    public String alterDropSQL() {
        switch (this.indexType) {
            case PRIMARY:
                return diffContext.getSqlConst().alertTableDelPrimaryIndexSubSQL(StrUtil.EMPTY);
            case INDEX:
                return diffContext.getSqlConst().alertTableDelIndexSubSQL(this.name);
            case FOREIGN:
                return diffContext.getSqlConst().alertTableDelForeignIndexSubSQL(this.name);
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
}
