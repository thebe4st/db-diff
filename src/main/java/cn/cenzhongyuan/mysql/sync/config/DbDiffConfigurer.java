package cn.cenzhongyuan.mysql.sync.config;

import cn.cenzhongyuan.mysql.sync.consts.SQLConst;
import cn.cenzhongyuan.mysql.sync.model.Db;
import lombok.Data;

@Data
public class DbDiffConfigurer {
    private boolean drop = false;
    private Db source;
    private Db dest;
    private SQLConst sqlConst;
}