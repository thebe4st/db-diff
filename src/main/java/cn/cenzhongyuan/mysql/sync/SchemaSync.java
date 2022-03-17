package cn.cenzhongyuan.mysql.sync;

import cn.cenzhongyuan.mysql.sync.consts.MysqlSQLConst;
import cn.cenzhongyuan.mysql.sync.consts.SQLConst;
import cn.cenzhongyuan.mysql.sync.model.*;
import cn.cenzhongyuan.mysql.sync.util.ConfigHelper;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class SchemaSync {


    public SchemaSync(Db source,Db dest) {
        this(source,dest,true);
    }

    public SchemaSync(Db source,Db dest,boolean drop) {
        this(source,dest,new MysqlSQLConst(),drop);
    }

    public SchemaSync(Db source,Db dest,SQLConst sqlConst,boolean drop) {
        ConfigHelper.get().setSource(source);
        ConfigHelper.get().setDest(dest);
        ConfigHelper.get().setDrop(drop);
        ConfigHelper.get().setSqlConst(sqlConst);
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
        List<String> tableNames = ConfigHelper.get().getSource().getTableNames();
        List<TableAlter> ret = new ArrayList<>();
        for (String tableName : tableNames) {
            TableAlter tableAlter = new TableAlter(tableName);
            if(StrUtil.isNotBlank(tableAlter.getSql())) {
                ret.add(tableAlter);
            }
        }
        return ret;
    }

    private List<String> getIncrTableNames() {
        List<String> sourceTables = ConfigHelper.get().getSource().getTableNames();
        List<String> destTables = ConfigHelper.get().getDest().getTableNames();

        List<String> ret = new ArrayList<>();

        for (String sourceTable : sourceTables) {
            if(!destTables.contains(sourceTable)) {
                ret.add(sourceTable);
            }
        }
        return ret;
    }
}
