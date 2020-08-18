# db-diff
compare database differences
## quick start
```java
    // 生成差异SQL
    SchemaSyncConfig config = new SchemaSyncConfig();
    config.setSourceDbUrl("jdbc:mysql://xxx/xxx?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false");
    config.setSourceDbUser("admin");
    config.setSourceDbPwd("123456");
    config.setDestDbUrl("jdbc:mysql://xxx/xxx?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false");
    config.setDestDbUser("admin");
    config.setDestDbPwd("123456");;

    String sqlPath = PathKit.getWebRootPath() + "/src/main/resources/db/migration";


    SchemaSync schemaSync = new SchemaSync(config);
    Integer lastVersion = schemaSync.getDestDb().getLastVersion();
    sqlPath +="/V" + (lastVersion + 1) + "__cust.sql";
    List<TableAlter> allDiff = schemaSync.getAllDiff();
    schemaSync.tableAlter2SQLFile(allDiff,sqlPath);
```
