package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.util.ProjectUtils;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Table {

    private String schemaRaw;

    private Map<String,String> fields = new HashMap<>();

    private Map<String, Index> indexAll = new HashMap<>();

    private Map<String, Index> foreignAll = new HashMap<>();

    public Table(String schemaRaw) {
        this.schemaRaw = schemaRaw;
    }

    public static Table parseSchema(String schema) {
        schema = schema.trim();
        String[] lines = schema.split(StrUtil.LF);
        Table ret = new Table(schema);

        for(int i = 1; i < lines.length - 1; i++) {
            String line = lines[i].trim();
            if(StrUtil.isBlank(line)) {
                continue;
            }
            line = ProjectUtils.trimRight(line,CharUtil.COMMA);
            if(line.charAt(0) == '`') {
                int index = line.substring(1).indexOf('`');
                String name = line.substring(1,index + 1);
                ret.getFields().put(name,line);
            } else {
                Index dbIndex = Index.parseDbIndexLine(line);
                if(dbIndex == null) {
                    continue;
                }
                switch (dbIndex.getIndexType()) {
                    case FOREIGN:
                        ret.getForeignAll().put(dbIndex.getName(),dbIndex);
                    default:
                        ret.getIndexAll().put(dbIndex.getName(),dbIndex);
                }
            }

        }
        return ret;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Fields:\n");
        fields.forEach((key,val)->{
            sb.append(String.format("%s : %s",key,val));
        });

        sb.append("Index:\n");
        indexAll.forEach((key,val)->{
            sb.append(String.format("%s : %s",key,val.getSql()));
        });

        sb.append("Foreign:\n");
        foreignAll.forEach((key,val)->{
            sb.append(String.format("%s : %s",key,val.getSql()));
        });

        return sb.toString();
    }

    public List<String> getFieldNames() {
        List<String> ret = new ArrayList<>();
        this.fields.forEach((key,val) -> {
            ret.add(key);
        });
        return ret;
    }

    public List<String> getRelationTables() {
        List<String> ret = new ArrayList<>();
        this.foreignAll.forEach((key,val) -> {
            ret.addAll(val.getRelationTables());
        });
        return ret;
    }

}
