package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.config.DiffContext;
import cn.cenzhongyuan.mysql.sync.consts.CharConst;
import cn.cenzhongyuan.mysql.sync.consts.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.enums.AlterType;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class TableAlter {

    private final String table;

    private AlterType alterType;

    private String sql;

    private DiffContext diffContext;

    private final SchemaDiff schema;

    public TableAlter(String table, DiffContext diffContext) {
        this.table = table;
        this.diffContext = diffContext;
        this.alterType = AlterType.NO;

        this.schema = new SchemaDiff(table,
                this.diffContext.getSource().getTable(table,diffContext),
                this.diffContext.getDest().getTable(table,diffContext)
        );

        String SSQL = this.schema.getSource().getOriginSQL();
        String DSQL = this.schema.getDest().getOriginSQL();

        if (StrUtil.isBlank(SSQL)) {
            this.alterType = AlterType.DROP;
            this.sql = this.diffContext.getSqlConst().dropTable(table);
            return;
        }

        if (StrUtil.isBlank(DSQL)) {
            this.alterType = AlterType.CREATE;
            this.sql = SSQL + CharConst.SEMICOLON;
            return;
        }

        String diff = this.getSchemaDiff();
        if (StrUtil.isNotBlank(diff)) {
            this.alterType = AlterType.ALTER;
            this.sql = this.diffContext.getSqlConst().alertTable(table, diff);
        }
    }

    private String getSchemaDiff() {
        Table sourceTable = this.getSchema().getSource();
        Table destTable = this.getSchema().getDest();
        String tableName = this.getTable();

        List<String> alterLines = new ArrayList<>();

        //比对字段
        sourceTable.getFields().forEach((sourceFieldName,sourceFieldSQL) -> {
            String alterSQL = "";
            if(destTable.getFields().containsKey(sourceFieldName)) {
                String destFieldSQL = destTable.getFields().get(sourceFieldName);
                if(!sourceFieldSQL.equals(destFieldSQL)) {
                    alterSQL = this.diffContext.getSqlConst().alertTableChangeFieldSubSQL(sourceFieldName,sourceFieldSQL);
                }
            } else {
                alterSQL =  this.diffContext.getSqlConst().alertTableAddFieldSubSQL(sourceFieldSQL);
            }
            if(StrUtil.isNotBlank(alterSQL)) {
                log.info("trace check column.alter {}.{} alterSQL= [{}]", tableName, sourceFieldName, alterSQL);
                alterLines.add(alterSQL);
            }
        });

        // source 库已经删除的字段
        if(this.diffContext.isDrop()) {
            destTable.getFields().forEach((sourceFieldName,sourceFieldSQL) -> {
                if(!sourceTable.getFields().containsKey(sourceFieldName)) {
                    String alterSQL = this.diffContext.getSqlConst().alertTableDelFieldSubSQL(sourceFieldName);
                    alterLines.add(alterSQL);
                    log.info("trace check column.alter {}.{} alterSQL= {}", tableName, sourceFieldName, alterSQL);
                }
            });
        }

        // 比对索引
        sourceTable.getIndexAll().forEach((index,idx) -> {
            boolean has = destTable.getIndexAll().containsKey(index);
            Index dIdx = destTable.getIndexAll().get(index);
            log.info("trace indexName---->[ {}.{} ] dest_has:[{}],dest_idx:[{}],source_idx:[{}]", tableName, index, has, dIdx, idx);
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
            }
        });

        // drop index
        if(this.diffContext.isDrop()) {
            destTable.getIndexAll().forEach((destIndexName,destIndex) -> {
                if(!sourceTable.getIndexAll().containsKey(destIndexName)) {
                    String alterSQL = destIndex.alterDropSQL();
                    alterLines.add(alterSQL);
                    log.info("trace check column.alter {}.{} alterSQL= {}", tableName, destIndexName, alterSQL);
                }
            });
        }


            // 比对外键

        // drop 外键

        return String.join(ProjectConstant.LINE_JOIN_DELIMITER,alterLines);
    }
}
