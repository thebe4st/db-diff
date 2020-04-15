package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.SchemaSyncConfig;
import cn.cenzhongyuan.mysql.sync.enumeration.AlterType;
import cn.cenzhongyuan.mysql.sync.enumeration.DbType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class SchemaSync {

    private SchemaSyncConfig config;

    private MyDb sourceDb;

    private MyDb destDb;

    public SchemaSync(SchemaSyncConfig config) {
        this.config = config;
        this.sourceDb = new MyDb(config.getSourceDbUser(),config.getSourceDbPwd(),config.getSourceDbUrl(), DbType.SOURCE);
        this.destDb = new MyDb(config.getDestDbUser(),config.getDestDbPwd(),config.getDestDbUrl(), DbType.DEST);
    }

    public void tableAlter2SQLFile(List<TableAlter> data,String path) {
        if(data == null || StringUtils.isBlank(path)) {
            throw new RuntimeException("param is valid");
        }
        if(data.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (TableAlter datum : data) {
            String sql = datum.getSql();
            if(StringUtils.isNotBlank(sql)) {
                sb.append(sql);
                sb.append("\n");
            }
        }
        String sqls = sb.toString();
        if(StringUtils.isBlank(sqls)) {
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
            if(StringUtils.isNotBlank(test.getSql())) {
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

        if(StringUtils.isBlank(sourceTableSchema)) {
            alter.setAlterType(AlterType.DROP);
            alter.setSql(String.format("drop table `%s`;",table));
            return alter;
        }

        if(StringUtils.isBlank(destTableSchema)) {
            alter.setAlterType(AlterType.CREATE);
            alter.setSql(sourceTableSchema + ";");
            return alter;
        }

        String diff = this.getSchemaDiff(alter);
        if(StringUtils.isNotBlank(diff)) {
            alter.setAlterType(AlterType.ALTER);
            alter.setSql(String.format("ALTER TABLE `%s`\n%s;", table, diff));
        }

        return alter;
    }

    public String getSchemaDiff(TableAlter alter) {
        TableSchema sourceMyS = alter.getSchemaDiff().getSource();
        TableSchema destMyS = alter.getSchemaDiff().getDest();
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

            if(StringUtils.isNotBlank(alterSQL)) {
                System.out.println(String.format("trace check column.alter %s.%s alterSQL= %s", table, name, alterSQL));
                alterLines.add(alterSQL);
            } else {
                System.out.println(String.format("trace check column.alter %s.%s no change", table, name));
            }
        });

        // source 库已经删除的字段
        if(this.config.isDrop()) {
            destMyS.getFields().forEach((name,dest) -> {
                if(!sourceMyS.getFields().containsKey(name)) {
                    String alterSQL = String.format("drop `%s`", name);
                    alterLines.add(alterSQL);
                    System.out.println(String.format("trace check column.alter %s.%s alterSQL= %s", table, name, alterSQL));
                } else {
                    System.out.println(String.format("trace check column.alter %s.%s no change", table, name));
                }
            });
        }

        // 比对索引
        sourceMyS.getIndexAll().forEach((index,idx) -> {
            boolean has = destMyS.getIndexAll().containsKey(index);
            DbIndex dIdx = destMyS.getIndexAll().get(index);
            System.out.println(String.format("trace indexName---->[ %s.%s ] dest_has:%s\ndest_idx:%s\nsource_idx:%s", table, index, has, dIdx, idx));
            String alterSQL = "";
            if(has) {
                if(!idx.getSql().equals(dIdx.getSql())) {
                    alterSQL = idx.alterAddSql(true);
                }
            }else {
                alterSQL = idx.alterAddSql(false);
            }
            if(StringUtils.isNotBlank(alterSQL)) {
                alterLines.add(alterSQL);
                System.out.println(String.format("trace check index.alter %s.%s alterSQL= %s", table, index, alterSQL));
            } else {
                System.out.println(String.format("trace check index.alter %s.%s no change", table, index, alterSQL));
            }
        });

        // drop index


        // 比对外键

        // drop 外键


        return StringUtils.join(alterLines.toArray(new String[0]),",\n");
    }

}
