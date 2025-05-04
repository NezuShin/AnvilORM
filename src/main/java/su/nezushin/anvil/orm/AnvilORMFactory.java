package su.nezushin.anvil.orm;

import java.io.File;

import su.nezushin.anvil.orm.table.AnvilORMSerializable;
import su.nezushin.anvil.orm.table.AnvilORMTable;
import su.nezushin.anvil.orm.table.impl.AnvilUrlORMTable;
import su.nezushin.anvil.orm.table.log.Logger;

public class AnvilORMFactory {

    private static AnvilORMFactory main = new AnvilORMFactory();

    public AnvilORMFactory() {

    }

    public static AnvilORMFactory factory() {
        return main;
    }

    public <T extends AnvilORMSerializable> AnvilORMTable<T> buildTableFromUrl(Class<T> target, String tableName,
                                                                               String url, boolean synchronize) {
        return buildTableFromUrl(target, tableName, url, null, null, synchronize);
    }


    /**
     * @param target
     * @param tableName
     * @param url
     * @param user
     * @param password
     * @param synchronize - needed for sqlite.
     * @param <T>
     * @return
     */
    public <T extends AnvilORMSerializable> AnvilORMTable<T> buildTableFromUrl(Class<T> target, String tableName,
                                                                               String url, String user, String password, boolean synchronize) {
        if (target == null)
            throw new IllegalArgumentException("Target class cannot be null");
        if (url == null)
            throw new IllegalArgumentException("Url cannot be null");
        if (tableName == null)
            throw new IllegalArgumentException("Table name cannot be null");
        AnvilORMTable<T> table = new AnvilUrlORMTable<>(url, user, password, synchronize);

        table.init(target, tableName);
        table.createTable();

        return table;
    }

    public <T extends AnvilORMSerializable> AnvilORMTable<T> buildSqliteTable(Class<T> target, String tableName,
                                                                              File file) {
        if (target == null)
            throw new IllegalArgumentException("Target class cannot be null");
        if (tableName == null)
            throw new IllegalArgumentException("Table name cannot be null");
        if (file == null)
            throw new IllegalArgumentException("File cannot be null");

        AnvilORMTable<T> table = new AnvilUrlORMTable<>("jdbc:sqlite:" + file, null, null, true);

        table.init(target, tableName);

        table.createTable();

        return table;
    }

    public <T extends AnvilORMSerializable> AnvilORMTable<T> buildMysqlTable(Class<T> target, String tableName,
                                                                             String ip, int port, String dbname, String user, String password, boolean useSSL) {
        if (target == null)
            throw new IllegalArgumentException("Target class cannot be null");
        if (ip == null)
            throw new IllegalArgumentException("Ip cannot be null");
        if (dbname == null)
            throw new IllegalArgumentException("Dbname cannot be null");
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (password == null)
            throw new IllegalArgumentException("Password cannot be null");
        if (tableName == null)
            throw new IllegalArgumentException("Table name cannot be null");
        AnvilORMTable<T> table = new AnvilUrlORMTable<>(
                "jdbc:mysql://" + ip + ":" + port + "/" + dbname + "?useSSL=" + useSSL, user, password, false);

        table.init(target, tableName);

        table.createTable();

        return table;
    }


}
