# DB-DIFF

## 1. Basic Introduction

    Allow you use java code compare the differences between databases and generate SQL statements.

## 2. Getting started

```java
public class Generator {
    public static void main(String[] args) {
        Db source = Db.builder()
                .url("jdbc:mysql://localhost/b?serverTimezone=GMT%2B8&tinyInt1isBit=false&useUnicode=true&characterEncoding=UTF-8&useSSL=false")
                .user("root")
                .pwd("pwd")
                .build();

        Db dest = Db.builder()
                .url("jdbc:mysql://localhost/a?serverTimezone=GMT%2B8&tinyInt1isBit=false&useUnicode=true&characterEncoding=UTF-8&useSSL=false")
                .user("root")
                .pwd("pwd")
                .build();
        SchemaSync schemaSync = new SchemaSync(source, dest);
        System.out.println(schemaSync.differenceSQL()); // get diff sql ~
    }
}
```
## 3. Enjoy it
