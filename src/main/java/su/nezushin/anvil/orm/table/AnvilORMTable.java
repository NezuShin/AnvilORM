package su.nezushin.anvil.orm.table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import java.util.Map.Entry;

import su.nezushin.anvil.orm.SqlFlag;
import su.nezushin.anvil.orm.table.ex.AnvilORMRuntimeException;
import su.nezushin.anvil.orm.table.log.Logger;
import su.nezushin.anvil.orm.table.query.Query;
import su.nezushin.anvil.orm.table.update.DeleteUpdate;
import su.nezushin.anvil.orm.table.update.Update;

public abstract class AnvilORMTable<T extends AnvilORMSerializable> {

    protected String tableName;
    protected Class<T> target;
    protected Logger logger;
    protected boolean debug;

    protected Map<Field, SqlColumn> fields = new HashMap<>();

    protected AnvilORMTable() {
        this.logger = (str) -> {
            if (debug) {
                System.out.println(str);
            }
        };
    }

    public AnvilORMTable<T> debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public AnvilORMTable<T> logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public void log(String log) {
        logger.log(log);
    }

    public Map<Field, SqlColumn> getFields() {
        return fields;
    }

    public String getTableName() {
        return tableName;
    }

    public Class<T> getTarget() {
        return target;
    }

    public void init(Class<T> target, String tableName) {

        this.target = target;
        this.tableName = tableName;

        for (Field f : target.getDeclaredFields()) {

            SqlColumn sqlColumn = f.getAnnotation(SqlColumn.class);

            if (sqlColumn == null)
                continue;

            f.setAccessible(true);

            fields.put(f, sqlColumn);

        }

    }


    public abstract boolean needCloseConnection();


    public abstract <A> A synchronize(AnvilORMSynchronizeRunnable<A> run);

    public Update<T> update() {
        return new Update<>(this);
    }

    public Query<T> query() {
        return new Query<>(this);
    }

    public DeleteUpdate<T> delete() {
        return new DeleteUpdate<>(this);
    }

    public abstract Connection getConnection();

    public void createTable() {

        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

        int i = 0;
        for (Entry<Field, SqlColumn> s : getFields().entrySet()) {
            Field f = s.getKey();
            SqlColumn c = s.getValue();
            String name = c.name().equalsIgnoreCase(SqlColumn.defaultName) ? f.getName() : c.name();

            if (i != 0)
                sb.append(", ");

            sb.append("`").append(name).append("`").append(" ");

            sb.append(c.type().getName());

            for (SqlFlag flag : c.flags()) {
                sb.append(" ").append(flag.getSqlName());
            }

            i++;
        }

        sb.append(");");

        log(sb.toString());

        synchronize(() -> {

            try (Connection c = getConnection(); Statement s = c.createStatement();) {
                return s.executeUpdate(sb.toString());
            } catch (Throwable e) {
                throw new AnvilORMRuntimeException(e);
            }
        });

    }

}
