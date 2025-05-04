package su.nezushin.anvil.orm.table.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import su.nezushin.anvil.orm.table.AnvilORMSerializable;
import su.nezushin.anvil.orm.table.AnvilORMSynchronizeRunnable;
import su.nezushin.anvil.orm.table.AnvilORMTable;
import su.nezushin.anvil.orm.table.ex.AnvilORMRuntimeException;
import su.nezushin.anvil.orm.table.log.Logger;

public class AnvilUrlORMTable<T extends AnvilORMSerializable> extends AnvilORMTable<T> {

    String url;
    String user, pwd;
    boolean synchronize;
    Object sync = new Object();


    public AnvilUrlORMTable(String url, String user, String pwd, boolean synchronize) {
        this.url = url;
        this.user = user;
        this.pwd = pwd;
        this.synchronize = synchronize;
    }


    @Override
    public Connection getConnection() {
        try {
            if (user != null && pwd != null)
                return DriverManager.getConnection(url, user, pwd);
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new AnvilORMRuntimeException(e);
        }
    }


    @Override
    public <A> A synchronize(AnvilORMSynchronizeRunnable<A> run) {
        if (synchronize) {
            synchronized (sync) {
                return run.run();
            }
        }
        return run.run();
    }


    @Override
    public boolean needCloseConnection() {
        return true;
    }

}
