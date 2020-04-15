package org.example;

import static org.junit.Assert.assertTrue;

import cn.cenzhongyuan.mysql.sync.SchemaSyncConfig;
import cn.cenzhongyuan.mysql.sync.model.DbIndex;
import cn.cenzhongyuan.mysql.sync.model.SchemaSync;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private static SchemaSyncConfig config = null;
    static
    {
        String sourceDbUrl = "jdbc:mysql://192.168.1.201/nlp_customer";
        String sourceDbUser = "admin";
        String sourceDbPwd = "123456";

        String destDbUrl = "jdbc:mysql://192.168.1.201/guest_replication";
        String destDbUser = "admin";
        String destDbPwd = "123456";
        config = new SchemaSyncConfig();
        config.setDestDbPwd(destDbPwd);
        config.setDestDbUrl(destDbUrl);
        config.setDestDbUser(destDbUser);

        config.setSourceDbPwd(sourceDbPwd);
        config.setSourceDbUser(sourceDbUser);
        config.setSourceDbUrl(sourceDbUrl);

    }
    /**
     * Rigorous Test :-)
     */
    @Test
    public void regTest()
    {
        String[] sqls = new String[] {
                "UNIQUE KEY `idx_a` (`a`) USING HASH COMMENT '注释'",
                "FULLTEXT KEY `c` (`c`)",
                "PRIMARY KEY (`d`)",
                "KEY `idx_e` (`e`),",
                "CONSTRAINT `busi_table_ibfk_1` FOREIGN KEY (`repo_id`) REFERENCES `repo_table` (`repo_id`)"
        };
        for (String sql : sqls) {
            DbIndex dbIndex = DbIndex.parseDbIndexLine(sql);
            if(dbIndex == null) {
                throw new RuntimeException();
            }
            System.out.println(dbIndex);
        }
    }

    @Test
    public void getVersionTest() {

        SchemaSync schemaSync = new SchemaSync(config);
        System.out.println(schemaSync.getSourceDb().getLastVersion());

    }
}
