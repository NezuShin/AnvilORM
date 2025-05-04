package su.nezushin.anvil.orm.table.update;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.Map.Entry;

import su.nezushin.anvil.orm.SqlFlag;
import su.nezushin.anvil.orm.table.SqlColumn;
import su.nezushin.anvil.orm.table.AnvilORMSerializable;
import su.nezushin.anvil.orm.table.AnvilORMTable;
import su.nezushin.anvil.orm.table.ex.AnvilORMRuntimeException;
import su.nezushin.anvil.orm.table.selector.Selector;
import su.nezushin.anvil.orm.table.selector.storage.ColumnPSStorage;
import su.nezushin.anvil.orm.table.selector.storage.PSStorage;
import su.nezushin.anvil.orm.table.selector.storage.RawPSStorage;

public class Update<T extends AnvilORMSerializable> extends Selector<T, Update<T>> {

    public Update(AnvilORMTable<T> table) {
        super(table);
    }

    protected Map<String, Object> set = new HashMap<>();
    protected List<Entry<String, List<Object>>> setRaw = new ArrayList<>();

    public Update<T> set(String parm, Object obj) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");

        set.put(parm, obj);
        return this;
    }

    public Update<T> setRaw(String expression) {
        if (expression == null) throw new IllegalArgumentException("Expression cannot be null");

        setRaw.add(new AbstractMap.SimpleEntry<>(expression, Arrays.asList()));
        return this;
    }

    public Update<T> setRaw(String expression, Object... obj) {
        if (expression == null) throw new IllegalArgumentException("Expression cannot be null");

        setRaw.add(new AbstractMap.SimpleEntry<>(expression, Arrays.asList(obj)));
        return this;
    }


    protected int prepareSetSql(StringBuilder builder, int positionPointer, List<PSStorage> list) {

        for (Entry<String, Object> e : set.entrySet()) {

            builder.append(positionPointer == 0 ? "" : ", ").append("`").append(e.getKey()).append("`=?");
            list.add(new RawPSStorage(e.getValue(), positionPointer += 1));
        }

        for (Entry<String, List<Object>> raw : setRaw) {

            builder.append(positionPointer == 0 ? "" : ", ").append(raw.getKey());
            for (Object obj : raw.getValue())
                list.add(new RawPSStorage(obj, positionPointer += 1));
        }

        return positionPointer;
    }

    public int complete() {
        return table.synchronize(() -> {
            Connection c = table.getConnection();
            int positionPointer = 0;
            List<PSStorage> list = new ArrayList<>();

            StringBuilder builder = new StringBuilder();
            positionPointer = this.prepareSetSql(builder, positionPointer, list);

            try (PreparedStatement ps = generateStatement("UPDATE " + table.getTableName() + " SET " + builder, positionPointer, list, c);) {
                return ps.executeUpdate();
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

    public int replace(T... itemsToReplace) {
        return replace(Arrays.asList(itemsToReplace));
    }

    public int replace(List<T> itemsToReplace) {
        for (T t : itemsToReplace)
            t.onSerialize();
        if (itemsToReplace.isEmpty()) return 0;
        return table.synchronize(() -> {
            try {

                List<PSStorage> list = new ArrayList<>();

                StringBuilder sb = new StringBuilder("REPLACE INTO ").append(table.getTableName()).append(" (");

                int pointer = 0;
                List<Entry<Field, SqlColumn>> fieldList = new ArrayList<>(table.getFields().entrySet());
                {
                    boolean entry = false;
                    for (Entry<Field, SqlColumn> e : fieldList) {
                        Field field = e.getKey();
                        SqlColumn column = e.getValue();
                        String name = column.name().equalsIgnoreCase(SqlColumn.defaultName) ? field.getName() : column.name();


                        if (entry) sb.append(",");
                        entry = true;
                        sb.append("`").append(name).append("`");
                    }
                }
                sb.append(") VALUES ");
                int i = 0;
                for (T t : itemsToReplace) {

                    for (Entry<Field, SqlColumn> e : fieldList) {
                        Field field = e.getKey();
                        SqlColumn column = e.getValue();
                        String name = column.name().equalsIgnoreCase(SqlColumn.defaultName) ? field.getName() : column.name();

                        Object value = field.get(t);

                        if (value == null) {
                            if (Arrays.asList(column.flags()).contains(SqlFlag.NOT_NULL))
                                throw new IllegalArgumentException("NOT_NULL Field " + field.getName() + " is null");
                        }
                        list.add(new ColumnPSStorage(column, value, pointer += 1, field.getType()));
                    }
                    i++;
                    sb.append("(").append(genKeys(fieldList.size())).append(")").append(i == itemsToReplace.size() ? ";" : ",");
                }

                table.log(sb.toString());
                Connection c = table.getConnection();
                try (PreparedStatement ps = c.prepareStatement(sb.toString());) {
                    for (PSStorage l : list)
                        l.set(ps);

                    return ps.executeUpdate();
                } catch (Throwable e) {
                    throw e;
                } finally {
                    if (table.needCloseConnection()) {
                        try {
                            c.close();
                        } catch (Exception ignored) {
                        }
                    }
                }

            } catch (Throwable e) {
                throw new AnvilORMRuntimeException(e);
            }
        });
    }

    private String genKeys(int i) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            if (j != 0) sb.append(", ");
            sb.append("?");
        }
        return sb.toString();
    }

}
