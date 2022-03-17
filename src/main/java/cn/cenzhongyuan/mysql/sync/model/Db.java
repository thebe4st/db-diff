package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.consts.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.consts.MysqlSQLConst;
import cn.cenzhongyuan.mysql.sync.consts.SQLConst;
import cn.hutool.core.util.StrUtil;
import lombok.Builder;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Builder
public class Db {

    private String user;
    private String pwd;
    private String url;

    private SQLConst sqlConst;

    static {
        try {
            Class.forName(ProjectConstant.DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            log.error("", e);
        }
    }

    public Connection getConnection() {
        Connection ret = null;
        try {
            ret = DriverManager.getConnection(this.url, this.user, this.pwd);
        } catch (SQLException e) {
            log.error("", e);
        }
        return ret;
    }

    public List<String> getTableNames() {
        List<String> ret = new ArrayList<>();
        try {
            @Cleanup Connection connection = null;
            @Cleanup Statement statement = null;
            @Cleanup ResultSet res = null;
            connection = getConnection();
            statement = connection.createStatement();
            res = statement.executeQuery(sql().tableName());
            while (res.next()) {
                ResultSetMetaData rsmd = res.getMetaData();
                ret.add(res.getString(rsmd.getColumnLabel(1)));
            }
        } catch (SQLException e) {
            log.error("", e);
        }
        return ret;
    }

    public String getTableSchema(String name) {
        try {
            @Cleanup Connection connection = null;
            @Cleanup Statement statement = null;
            @Cleanup ResultSet res = null;
            connection = getConnection();
            statement = connection.createStatement();
            res = statement.executeQuery(sql().tableSchema(name));
            if (res.next()) {
                ResultSetMetaData rsmd = res.getMetaData();
                return res.getString(rsmd.getColumnLabel(2));
            } else {
                throw new RuntimeException(String.format("get table %s 's schema failed", name));
            }
        } catch (SQLException ignored) {
            log.error("get table {} 's schema failed", name);
        }
        return StrUtil.EMPTY;
    }

    private SQLConst sql() {
        if (Objects.isNull(this.sqlConst)) {
            this.sqlConst = new MysqlSQLConst();
        }
        return this.sqlConst;
    }
}
