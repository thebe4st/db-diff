package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.consts.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.enumeration.AlterType;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class SchemaSync {

    private boolean drop = false;

    private Db sourceDb;

    private Db destDb;

    public SchemaSync(Db source,Db dest) {
        this(source,dest,false);
    }

    public SchemaSync(Db source,Db dest,boolean drop) {
        this.drop = drop;
        this.sourceDb = source;
        this.destDb = dest;
    }

    public void tableAlter2SQLFile(List<TableAlter> data,String path) {
        if(data == null || StrUtil.isBlank(path)) {
            throw new RuntimeException("param is valid");
        }
        if(data.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (TableAlter datum : data) {
            String sql = datum.getSql();
            if(StrUtil.isNotBlank(sql)) {
                sb.append(sql);
                sb.append(StrUtil.LF);
            }
        }
        String sqls = sb.toString();
        if(StrUtil.isBlank(sqls)) {
            return;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(sqls.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<TableAlter> getAllDiff() {
        List<String> tableNames = this.sourceDb.getTableNames();
        List<TableAlter> ret = new ArrayList<>();

        for (String tableName : tableNames) {
            TableAlter test = this.getAlterDataByTable(tableName);
            if(StrUtil.isNotBlank(test.getSql())) {
                ret.add(test);
            }
        }
        return ret;
    }

    public List<String> getNewTableNames() {
        List<String> sourceTables = this.sourceDb.getTableNames();
        List<String> destTables = this.destDb.getTableNames();

        List<String> ret = new ArrayList<>();

        for (String sourceTable : sourceTables) {
            if(!destTables.contains(sourceTable)) {
                ret.add(sourceTable);
            }
        }
        return ret;
    }

    public TableAlter getAlterDataByTable(String table) {
        TableAlter alter = new TableAlter();
        alter.setTable(table);
        alter.setAlterType(AlterType.NO);

        String sourceTableSchema = this.getSourceDb().getTableSchema(table);
        String destTableSchema = this.getDestDb().getTableSchema(table);

        SchemaDiff schemaDiff = new SchemaDiff(table, sourceTableSchema, destTableSchema);
        alter.setSchemaDiff(schemaDiff);

        if(sourceTableSchema.equals(destTableSchema)) {
            return alter;
        }

        if(StrUtil.isBlank(sourceTableSchema)) {
            alter.setAlterType(AlterType.DROP);
            alter.setSql(String.format("drop table `%s`;",table));
            return alter;
        }

        if(StrUtil.isBlank(destTableSchema)) {
            alter.setAlterType(AlterType.CREATE);
            alter.setSql(sourceTableSchema + ";");
            return alter;
        }

        String diff = this.getSchemaDiff(alter);
        if(StrUtil.isNotBlank(diff)) {
            alter.setAlterType(AlterType.ALTER);
            alter.setSql(String.format("ALTER TABLE `%s`\n%s;", table, diff));
        }

        return alter;
    }

    public String getSchemaDiff(TableAlter alter) {
        Table sourceMyS = alter.getSchemaDiff().getSource();
        Table destMyS = alter.getSchemaDiff().getDest();
        String table = alter.getTable();

        List<String> alterLines = new ArrayList<>();

        //比对字段
        sourceMyS.getFields().forEach((name,dt) -> {
            //TODO: 忽略字段

            String alterSQL = "";
            if(destMyS.getFields().containsKey(name)) {
                String destDt = destMyS.getFields().get(name);
                if(!dt.equals(destDt)) {
                    alterSQL = String.format("CHANGE `%s` %s",name,dt);
                }
            } else {
                alterSQL =  "ADD " + dt;
            }

            if(StrUtil.isNotBlank(alterSQL)) {
                System.out.printf("trace check column.alter %s.%s alterSQL= %s%n", table, name, alterSQL);
                alterLines.add(alterSQL);
            } else {
                System.out.printf("trace check column.alter %s.%s no change%n", table, name);
            }
        });

        // source 库已经删除的字段
        if(drop) {
            destMyS.getFields().forEach((name,dest) -> {
                if(!sourceMyS.getFields().containsKey(name)) {
                    String alterSQL = String.format("drop `%s`", name);
                    alterLines.add(alterSQL);
                    System.out.printf("trace check column.alter %s.%s alterSQL= %s%n", table, name, alterSQL);
                } else {
                    System.out.printf("trace check column.alter %s.%s no change%n", table, name);
                }
            });
        }

        // 比对索引
        sourceMyS.getIndexAll().forEach((index,idx) -> {
            boolean has = destMyS.getIndexAll().containsKey(index);
            Index dIdx = destMyS.getIndexAll().get(index);
            System.out.printf("trace indexName---->[ %s.%s ] dest_has:%s\ndest_idx:%s\nsource_idx:%s%n", table, index, has, dIdx, idx);
            String alterSQL = "";
            if(has) {
                if(!idx.getSql().equals(dIdx.getSql())) {
                    alterSQL = idx.alterAddSql(true);
                }
            }else {
                alterSQL = idx.alterAddSql(false);
            }
            if(StrUtil.isNotBlank(alterSQL)) {
                alterLines.add(alterSQL);
                System.out.printf("trace check index.alter %s.%s alterSQL= %s%n", table, index, alterSQL);
            } else {
                System.out.printf("trace check index.alter %s.%s no change%n", table, index, alterSQL);
            }
        });

        // drop index


        // 比对外键

        // drop 外键

        return String.join(ProjectConstant.LINE_JOIN_DELIMITER,alterLines);
    }

}
