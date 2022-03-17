package cn.cenzhongyuan.mysql.sync.util;

import cn.cenzhongyuan.mysql.sync.config.DbDiffConfigurer;

import java.util.Objects;

public class ConfigHelper {
    private static final ThreadLocal<DbDiffConfigurer> t = new ThreadLocal<>();

    public static DbDiffConfigurer get() {
        if (Objects.isNull(t.get())) {
            synchronized (ConfigHelper.class) {
                if (Objects.isNull(t.get())) {
                    t.set(new DbDiffConfigurer());
                }
            }
        }
        return t.get();
    }
}
