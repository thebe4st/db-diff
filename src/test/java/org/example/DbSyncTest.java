package org.example;

import cn.cenzhongyuan.mysql.sync.model.Db;
import cn.cenzhongyuan.mysql.sync.model.Index;
import cn.cenzhongyuan.mysql.sync.model.SchemaSync;
import cn.cenzhongyuan.mysql.sync.model.TableAlter;
import org.junit.Test;

import java.util.List;

/**
 * Unit test for simple App.
 */
public class DbSyncTest
{
    static Db source;
    static Db dest;
    static
    {
        source = Db.builder()
                .url("jdbc:mysql://localhost/b?serverTimezone=GMT%2B8&tinyInt1isBit=false&useUnicode=true&characterEncoding=UTF-8&useSSL=false")
                .user("root")
                .pwd("pwd")
                .build();

        dest = Db.builder()
                .url("jdbc:mysql://localhost/a?serverTimezone=GMT%2B8&tinyInt1isBit=false&useUnicode=true&characterEncoding=UTF-8&useSSL=false")
                .user("root")
                .pwd("pwd")
                .build();
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
            Index dbIndex = Index.parseDbIndexLine(sql);
            if(dbIndex == null) {
                throw new RuntimeException();
            }
            System.out.println(dbIndex);
        }
    }

    @Test
    public void getVersionTest() {
        SchemaSync schemaSync = new SchemaSync(source,dest);
        List<TableAlter> allDiff = schemaSync.getAllDiff();
        schemaSync.tableAlter2SQLFile(allDiff,"/Users/cenzhongyuan/Desktop/hengonda/diff2.sql");
    }
}
