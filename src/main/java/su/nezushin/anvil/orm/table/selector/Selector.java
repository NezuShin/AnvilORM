package su.nezushin.anvil.orm.table.selector;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import su.nezushin.anvil.orm.table.SqlColumn;
import su.nezushin.anvil.orm.table.AnvilORMSerializable;
import su.nezushin.anvil.orm.table.AnvilORMTable;
import su.nezushin.anvil.orm.table.ex.AnvilORMRuntimeException;
import su.nezushin.anvil.orm.table.query.OrderBy;
import su.nezushin.anvil.orm.table.selector.storage.ColumnPSStorage;
import su.nezushin.anvil.orm.table.selector.storage.PSStorage;
import su.nezushin.anvil.orm.table.selector.storage.RawPSStorage;

@SuppressWarnings("unchecked")
public abstract class Selector<T extends AnvilORMSerializable, R extends Selector<T, R>> {

    protected AnvilORMTable<T> table;

    public Selector(AnvilORMTable<T> table) {
        this.table = table;
    }

    protected Entry<String, OrderBy> orderBy;
    protected Map<String, Object> where = new HashMap<>();
    protected List<String> whereNull = new ArrayList<>();
    protected List<String> whereNotNull = new ArrayList<>();
    protected List<Entry<String, List<Object>>> whereRaw = new ArrayList<>();
    protected Map<String, Entry<ComparisonOperator, Object>> whereOperator = new HashMap<>();
    protected Map<String, Object> notWhere = new HashMap<>();
    protected Map<String, Entry<Object, Object>> between = new HashMap<>();
    protected Map<String, Entry<Object, Object>> notBetween = new HashMap<>();
    protected Map<String, Object> like = new HashMap<>();
    protected List<SimpleSelector<T>> selectors = new ArrayList<>();
    protected int limit = -1;
    protected CombinationOperator combinationOperator = CombinationOperator.AND;

    public R combination(CombinationOperator combinationOperator) {
        this.combinationOperator = combinationOperator;
        return (R) this;
    }

    public R where(AnvilSelectorCallback<T> callback) {
        SimpleSelector<T> selector = new SimpleSelector<>(this.table);

        selectors.add(selector);
        callback.run(selector);
        return (R) this;
    }

    public R orderBy(OrderBy orderBy, String parm) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");

        this.orderBy = new SimpleEntry<String, OrderBy>(parm, orderBy);
        return (R) this;
    }

    public R whereRaw(String expression) {
        if (expression == null) throw new IllegalArgumentException("Parm cannot be null");
        whereRaw.add(new SimpleEntry<>(expression, Arrays.asList()));
        return (R) this;
    }

    public R whereRaw(String expression, Object... obj) {
        if (expression == null) throw new IllegalArgumentException("Parm cannot be null");
        whereRaw.add(new SimpleEntry<>(expression, Arrays.asList(obj)));
        return (R) this;
    }

    public R whereNull(String parm) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");
        whereNull.add(parm);
        return (R) this;
    }

    public R whereNotNull(String parm) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");
        whereNotNull.add(parm);
        return (R) this;
    }

    public R where(String parm, Object obj) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");
        where.put(parm, obj);

        return (R) this;
    }

    public R where(String parm, ComparisonOperator operator, Object obj) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");

        if (operator == null) throw new IllegalArgumentException("ComparisonOperator cannot be null");

        whereOperator.put(parm, new SimpleEntry<ComparisonOperator, Object>(operator, obj));
        return (R) this;
    }

    public R where(String parm, String stringOperator, Object obj) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");

        if (stringOperator == null) throw new IllegalArgumentException("ComparisonOperator cannot be null");

        ComparisonOperator operator = ComparisonOperator.getByOperator(stringOperator);

        if (operator == null) throw new IllegalArgumentException("Illegal ComparisonOperator: " + stringOperator);


        whereOperator.put(parm, new SimpleEntry<ComparisonOperator, Object>(operator, obj));
        return (R) this;
    }

    public R notWhere(String parm, Object obj) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");
        notWhere.put(parm, obj);

        return (R) this;
    }

    public R between(String parm, Object obj1, Object obj2) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");
        if (obj1 == null || obj2 == null) throw new IllegalArgumentException("Objects cannot be null");
        between.put(parm, new SimpleEntry<>(obj1, obj2));
        return (R) this;
    }

    public R like(String parm, Object obj) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");
        if (obj == null) throw new IllegalArgumentException("Object cannot be null");
        like.put(parm, obj);

        return (R) this;
    }

    public R notBetween(String parm, Object obj1, Object obj2) {
        if (parm == null) throw new IllegalArgumentException("Parm cannot be null");
        if (obj1 == null || obj2 == null) throw new IllegalArgumentException("Objects cannot be null");

        notBetween.put(parm, new SimpleEntry<>(obj1, obj2));
        return (R) this;
    }

    public R limit(int limit) {
        if (limit < 1) throw new IllegalArgumentException("limit cannot be < 1");
        this.limit = limit;

        return (R) this;
    }

    protected int generateSql(String prefix, StringBuilder builder, int positionPointer, List<PSStorage> list) {

        builder.append(prefix);
        int startPositionPointer = positionPointer;

        for (SimpleSelector<T> selector : selectors) {
            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(");
            positionPointer = selector.generateSql("", builder, positionPointer, list);
            builder.append(")");
        }

        for (String key : whereNull) {
            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(`").append(key).append("` IS NULL)");
        }

        for (String key : whereNotNull) {
            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(`").append(key).append("` IS NOT NULL)");
        }

        for (Entry<String, List<Object>> e : whereRaw) {
            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(").append(e.getKey()).append(")");
            for (Object value : e.getValue())
                list.add(new RawPSStorage(value, positionPointer += 1));
        }

        for (Entry<String, Object> e : where.entrySet()) {
            String key = e.getKey();

            Object value = e.getValue();

            Entry<Field, SqlColumn> field = getField(key);
            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(`").append(key).append("`=?)");
            list.add(new ColumnPSStorage(field.getValue(), value, positionPointer += 1, field.getKey().getType()));

        }

        for (Entry<String, Entry<ComparisonOperator, Object>> e : whereOperator.entrySet()) {
            String key = e.getKey();
            ComparisonOperator co = e.getValue().getKey();
            Object obj = e.getValue().getValue();

            Entry<Field, SqlColumn> field = getField(key);

            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(`").append(key).append("`").append(co.getOperators().get(0)).append("?)");
            list.add(new ColumnPSStorage(field.getValue(), obj, positionPointer += 1, field.getKey().getType()));

        }

        for (Entry<String, Object> e : notWhere.entrySet()) {
            String key = e.getKey();

            Object value = e.getValue();

            Entry<Field, SqlColumn> field = getField(key);
            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(`").append(key).append("`<>?)");

            list.add(new ColumnPSStorage(field.getValue(), value, positionPointer += 1, field.getKey().getType()));
        }

        for (Entry<String, Entry<Object, Object>> e : between.entrySet()) {
            String key = e.getKey();
            Object obj1 = e.getValue().getKey();
            Object obj2 = e.getValue().getValue();

            whereAnd(builder, positionPointer - startPositionPointer);

            builder.append("(`").append(key).append("` BETWEEN ? AND ?)");

            Entry<Field, SqlColumn> field = getField(key);

            list.add(new ColumnPSStorage(field.getValue(), obj1, positionPointer += 1, field.getKey().getType()));
            list.add(new ColumnPSStorage(field.getValue(), obj2, positionPointer += 1, field.getKey().getType()));

        }

        for (Entry<String, Entry<Object, Object>> e : notBetween.entrySet()) {
            String key = e.getKey();
            Object obj1 = e.getValue().getKey();
            Object obj2 = e.getValue().getValue();

            whereAnd(builder, positionPointer - startPositionPointer);

            builder.append("(`").append(key).append("` NOT BETWEEN ? AND ?)");

            Entry<Field, SqlColumn> field = getField(key);

            list.add(new ColumnPSStorage(field.getValue(), obj1, positionPointer += 1, field.getKey().getType()));
            list.add(new ColumnPSStorage(field.getValue(), obj2, positionPointer += 1, field.getKey().getType()));

        }

        for (Entry<String, Object> e : like.entrySet()) {
            String key = e.getKey();

            Object value = e.getValue();

            Entry<Field, SqlColumn> field = getField(key);
            whereAnd(builder, positionPointer - startPositionPointer);
            builder.append("(`").append(key).append("`=?)");

            list.add(new ColumnPSStorage(field.getValue(), value, positionPointer += 1, field.getKey().getType()));
        }

        if (orderBy != null)
            builder.append(" ORDER BY ").append(orderBy.getKey()).append(" ").append(orderBy.getValue().name());

        if (limit != -1) builder.append(" LIMIT ").append(limit);

        return positionPointer;
    }


    protected PreparedStatement generateStatement(String prefix, int positionPointer, List<PSStorage> list, Connection c) {
        try {
            StringBuilder builder = new StringBuilder();
            generateSql(prefix, builder, positionPointer, list);

            builder.append(";");
            String sql = builder.toString();

            PreparedStatement ps = c.prepareStatement(sql);

            table.log(sql);

            for (PSStorage s : list)
                s.set(ps);

            return ps;
        } catch (Throwable e) {
            throw new AnvilORMRuntimeException(e);
        }
    }

    protected void whereAnd(StringBuilder sb, int position) {
        if (position == 0) sb.append(" WHERE ");
        else sb.append(" ").append(combinationOperator.getOperator()).append(" ");
    }

    protected Entry<Field, SqlColumn> getField(String key) {

        for (Entry<Field, SqlColumn> e : table.getFields().entrySet()) {
            Field a = e.getKey();
            SqlColumn b = e.getValue();
            String name = b.name().equalsIgnoreCase(SqlColumn.defaultName) ? a.getName() : b.name();

            if (name.equalsIgnoreCase(key)) return e;
        }
        return null;
    }
}
