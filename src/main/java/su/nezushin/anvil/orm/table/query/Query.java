package su.nezushin.anvil.orm.table.query;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import su.nezushin.anvil.orm.table.SqlColumn;
import su.nezushin.anvil.orm.table.AnvilORMSerializable;
import su.nezushin.anvil.orm.table.AnvilORMTable;
import su.nezushin.anvil.orm.table.ex.AnvilORMRuntimeException;
import su.nezushin.anvil.orm.table.selector.Selector;

public class Query<T extends AnvilORMSerializable> extends Selector<T, Query<T>> {

    protected String columns = "*";
    protected Set<String> selectedColumns = null;

    public Query(AnvilORMTable<T> table) {
        super(table);
    }

    public Query<T> columns(String... columns) {
        if (columns == null)
            throw new IllegalArgumentException("Columns cannot be null");
        int i = 0;
        StringBuilder sb = new StringBuilder();
        Set<String> selected = new HashSet<>();
        for (String s : columns) {
            if (i != 0)
                sb.append(", ");
            sb.append("`").append(s).append("`");
            selected.add(s.toLowerCase(Locale.ROOT));
            i++;
        }
        this.columns = sb.toString();
        this.selectedColumns = selected;

        return this;
    }

    public T completeAsOne() {
        return table.synchronize(() -> {
            limit(1);
            Connection c = table.getConnection();

            try (PreparedStatement ps = generateStatement("SELECT " + columns + " FROM " + table.getTableName(), 0, new ArrayList<>(), c);
                 ResultSet rs = ps.executeQuery();) {

                if (rs.next()) {
                    return collectFromResultSet(rs);
                }
                return null;
            } catch (Throwable e) {
                throw new AnvilORMRuntimeException(e);
            } finally {
                if (table.needCloseConnection()) {
                    try {
                        c.close();
                    } catch (Exception e2) {
                    }
                }
            }
        });
    }

    protected T collectFromResultSet(ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        T t = table.getTarget().getConstructor().newInstance();

        for (Entry<Field, SqlColumn> e : table.getFields().entrySet()) {
            Field field = e.getKey();
            SqlColumn column = e.getValue();

            String name = column.name().equalsIgnoreCase(SqlColumn.defaultName) ? field.getName() : column.name();

            if (selectedColumns != null && !selectedColumns.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }

            field.set(t, column.type().get(rs, name, column.flags(), field.getType()));

        }

        t.onDeserialize();

        return t;

    }

    public List<T> completeAsList() {
        return table.synchronize(() -> {
            Connection c = table.getConnection();
            try (PreparedStatement ps = generateStatement("SELECT " + columns + " FROM " + table.getTableName(), 0, new ArrayList<>(), c);
                 ResultSet rs = ps.executeQuery();) {

                List<T> list = new ArrayList<>();

                while (rs.next()) {
                    list.add(collectFromResultSet(rs));
                }

                return list;
            } catch (Throwable e) {
                throw new AnvilORMRuntimeException(e);
            } finally {
                if (table.needCloseConnection()) {
                    try {
                        c.close();
                    } catch (Exception e2) {
                    }
                }
            }
        });
    }

    public int completeAsCount() {

        return table.synchronize(() -> {
            Connection c = table.getConnection();
            try (PreparedStatement ps = generateStatement("SELECT count(*) FROM " + table.getTableName(), 0, new ArrayList<>(), c);
                 ResultSet rs = ps.executeQuery();) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            } catch (Throwable e) {
                throw new AnvilORMRuntimeException(e);
            } finally {
                if (table.needCloseConnection()) {
                    try {
                        c.close();
                    } catch (Exception e2) {
                    }
                }
            }
        });
    }

    public List<Map<String, Object>> completeAsMaps() {
        return table.synchronize(() -> {
            Connection c = table.getConnection();
            try (PreparedStatement ps = generateStatement("SELECT " + columns + " FROM " + table.getTableName(), 0, new ArrayList<>(), c);
                 ResultSet rs = ps.executeQuery();) {

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                List<Map<String, Object>> rows = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }

                return rows;
            } catch (Throwable e) {
                throw new AnvilORMRuntimeException(e);
            } finally {
                if (table.needCloseConnection()) {
                    try {
                        c.close();
                    } catch (Exception e2) {
                    }
                }
            }
        });
    }

}
