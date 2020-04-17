package cn.cenzhongyuan.mysql.sync.model;

import cn.cenzhongyuan.mysql.sync.core.ProjectConstant;
import cn.cenzhongyuan.mysql.sync.enumeration.DbType;
import lombok.Cleanup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Db {

    private String user;
    private String pwd;
    private String url;
    private DbType dbType;

    public Db(String user, String pwd, String url, DbType dbType) {
        this.user = user;
        this.pwd = pwd;
        this.url = url;
        this.dbType = dbType;
    }

    static {
        try {
            Class.forName(ProjectConstant.DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

     public Connection getConnection() {
        Connection ret = null;
        try {
            ret = DriverManager.getConnection(this.url,this.user,this.pwd);
        } catch (SQLException e) {
            e.printStackTrace();
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
            res = statement.executeQuery("show table status");
            while (res.next()) {
                ResultSetMetaData rsmd = res.getMetaData();
                ret.add(res.getString(rsmd.getColumnLabel(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            res = statement.executeQuery(String.format("show create table `%s`",name));
            if(res.next()) {
                ResultSetMetaData rsmd = res.getMetaData();
                return res.getString(rsmd.getColumnLabel(2));
            }else {
                throw new RuntimeException(String.format("get table %s 's schema failed", name));
            }
        } catch (SQLException e) {
        }
        System.out.println(String.format("get table %s 's schema failed", name));
        return "";
    }

    public Integer getLastVersion() {
        try {
            @Cleanup Connection connection = null;
            @Cleanup Statement statement = null;
            @Cleanup ResultSet res = null;
            connection = getConnection();
            statement = connection.createStatement();
            res = statement.executeQuery(String.format("SELECT MAX(`version`) FROM `flyway_schema_history`"));
            if(res.next()) {
                ResultSetMetaData rsmd = res.getMetaData();
                return Optional.ofNullable(res.getInt(rsmd.getColumnLabel(1))).orElse(1);
            }else {
                throw new RuntimeException(String.format("get last version failed"));
            }
        } catch (SQLException e) {
        }
        System.out.println(String.format("get last version failed"));
        return 1;
    }

}
