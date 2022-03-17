package cn.cenzhongyuan.mysql.sync;

import cn.cenzhongyuan.mysql.sync.consts.MysqlSQLConst;
import cn.cenzhongyuan.mysql.sync.consts.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.consts.SQLConst;
import cn.cenzhongyuan.mysql.sync.enumeration.AlterType;
import cn.cenzhongyuan.mysql.sync.model.*;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Data
public class SchemaSync {

    private boolean drop;

    private Db sourceDb;

    private Db destDb;

    private SQLConst sqlConst;

    public SchemaSync(Db source,Db dest) {
        this(source,dest,false);
    }

    public SchemaSync(Db source,Db dest,boolean drop) {
        this(source,dest,new MysqlSQLConst(),drop);
    }

    public SchemaSync(Db source,Db dest,SQLConst sqlConst,boolean drop) {
        this.drop = drop;
        this.sourceDb = source;
        this.destDb = dest;
        this.sqlConst = sqlConst;
    }


    public String differenceSQL() {
        List<TableAlter> difference = this.difference();
        if(difference.isEmpty()) {
            log.info("All consistent, You don't need to generate");
            return StrUtil.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (TableAlter diff : difference) {
            String sql = diff.getSql();
            if(StrUtil.isNotBlank(sql)) {
                sb.append(sql);
                sb.append(StrUtil.LF);
            }
        }
        return sb.toString();
    }

    public List<TableAlter> difference() {
        List<String> tableNames = this.sourceDb.getTableNames();
        List<TableAlter> ret = new ArrayList<>();
        for (String tableName : tableNames) {
            TableAlter tableAlter = this.diffByTableName(tableName);
            if(StrUtil.isNotBlank(tableAlter.getSql())) {
                ret.add(tableAlter);
            }
        }
        return ret;
    }

    public List<String> getIncrTableNames() {
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

    public TableAlter diffByTableName(String table) {
        TableAlter alter = new TableAlter();
        alter.setTable(table);
        alter.setAlterType(AlterType.NO);

        Table sourceT = sourceDb.getTable(table);
        Table destT = destDb.getTable(table);

        SchemaDiff schemaDiff = new SchemaDiff(table, sourceT, destT);
        alter.setSchemaDiff(schemaDiff);

        if(Objects.equals(sourceT.getOriginSQL(),destT.getOriginSQL())) {
            return alter;
        }

        if(StrUtil.isBlank(sourceT.getOriginSQL())) {
            alter.setAlterType(AlterType.DROP);
            alter.setSql(String.format("drop table `%s`;",table));
            return alter;
        }

        if(StrUtil.isBlank(destT.getOriginSQL())) {
            alter.setAlterType(AlterType.CREATE);
            alter.setSql(sourceT + ";");
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
        Table sourceT = alter.getSchemaDiff().getSource();
        Table destT = alter.getSchemaDiff().getDest();
        String tableName = alter.getTable();

        List<String> alterLines = new ArrayList<>();

        //比对字段
        sourceT.getFields().forEach((name,dt) -> {
            //TODO: 忽略字段
            String alterSQL = "";
            if(destT.getFields().containsKey(name)) {
                String destDt = destT.getFields().get(name);
                if(!dt.equals(destDt)) {
                    alterSQL = String.format("CHANGE `%s` %s",name,dt);
                }
            } else {
                alterSQL =  "ADD " + dt;
            }

            if(StrUtil.isNotBlank(alterSQL)) {
                log.info("trace check column.alter {}.{} alterSQL= [{}]", tableName, name, alterSQL);
                alterLines.add(alterSQL);
            } else {
                log.info("trace check column.alter {}.{} no change", tableName, name);
            }
        });

        // source 库已经删除的字段
        if(drop) {
            destT.getFields().forEach((name,dest) -> {
                if(!sourceT.getFields().containsKey(name)) {
                    String alterSQL = String.format("drop `%s`", name);
                    alterLines.add(alterSQL);
                    log.info("trace check column.alter {}.{} alterSQL= {}", tableName, name, alterSQL);
                } else {
                    log.info("trace check column.alter {}.{} no change", tableName, name);
                }
            });
        }

        // 比对索引
        sourceT.getIndexAll().forEach((index,idx) -> {
            boolean has = destT.getIndexAll().containsKey(index);
            Index dIdx = destT.getIndexAll().get(index);
            log.info("trace indexName---->[ {}.{} ] dest_has:{}\ndest_idx:{}\nsource_idx:{}", tableName, index, has, dIdx, idx);
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
                log.info("trace check index.alter {}.{} alterSQL= {}", tableName, index, alterSQL);
            } else {
                log.info("trace check index.alter {}.{} no change", tableName, index);
            }
        });

        // drop index


        // 比对外键

        // drop 外键

        return String.join(ProjectConstant.LINE_JOIN_DELIMITER,alterLines);
    }

}
