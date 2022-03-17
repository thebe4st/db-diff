package org.example;

import cn.cenzhongyuan.mysql.sync.SchemaSync;
import cn.cenzhongyuan.mysql.sync.model.Db;
import cn.cenzhongyuan.mysql.sync.model.Index;
import cn.hutool.core.io.FileUtil;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class GeneratorTest {
    static Db source;
    static Db dest;

    static {
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

    @Test
    public void genDiff() {
        String path = "/Users/cenzhongyuan/Desktop/hengonda/diff2.sql";
        SchemaSync schemaSync = new SchemaSync(source, dest);
        FileUtil.writeString(schemaSync.differenceSQL(), path, StandardCharsets.UTF_8);
    }
}
