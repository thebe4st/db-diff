package cn.cenzhongyuan.mysql.sync;


import lombok.Data;

@Data
public class SchemaSyncConfig {

    private   String sourceDbUrl = "";
    private   String sourceDbUser = "";
    private   String sourceDbPwd = "";

    private   String destDbUrl = "";
    private   String destDbUser = "";
    private   String destDbPwd = "";

    private boolean drop = true;
}
