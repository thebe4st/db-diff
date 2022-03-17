package org.example;

import cn.cenzhongyuan.mysql.sync.model.Index;
import org.junit.Test;

public class RegTest
{
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
}
