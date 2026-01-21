package com.idear.fimpe.database;

import com.idear.fimpe.properties.PropertiesHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLServerDatabaseConnection {

    private SQLServerDatabaseConnection(){
    }

    public static Connection getConnection() throws SQLException {
        StringBuilder connectionString = new StringBuilder();
        connectionString.append("jdbc:sqlserver://" + PropertiesHelper.DB_HOST + ";")
        .append("database=" + PropertiesHelper.DB_NAME + ";")
        .append("trustServerCertificate=true;");
        return DriverManager.getConnection(
                    connectionString.toString(),
                    PropertiesHelper.DB_USER, PropertiesHelper.DB_PASSWORD);

    }

}
