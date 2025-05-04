package su.nezushin.anvil.orm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public enum SqlType {

    INT(Types.INTEGER, "INT"),

    BIGINT(Types.BIGINT, "BIGINT"),

    FLOAT(Types.FLOAT, "FLOAT"),

    DOUBLE(Types.DOUBLE, "DOUBLE"),

    BOOLEAN(Types.BOOLEAN, "BOOLEAN"),

    BLOB(Types.BLOB, "BLOB"),

    TEXT(Types.BLOB, "TEXT"),

    VARCHAR(Types.VARCHAR, "VARCHAR(255)"),

    ENUM(Types.VARCHAR, "VARCHAR(255)");

    static Map<SqlType, Getter> getters = new HashMap<>();
    static Map<SqlType, Setter> setters = new HashMap<>();
    static Map<SqlType, Checker> checkers = new HashMap<>();

    static {
        setters.put(SqlType.INT,
                (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {
                    int obj = (int) object;

                    ps.setInt(position, obj);

                });

        setters.put(BIGINT, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {
            long obj = (long) object;

            ps.setLong(position, obj);

        });

        setters.put(FLOAT, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {
            float obj = (float) object;

            ps.setFloat(position, obj);
        });

        setters.put(DOUBLE, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {
            double obj = (double) object;

            ps.setDouble(position, obj);
        });
        setters.put(BOOLEAN, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {
            ps.setBoolean(position, (boolean) object);
        });
        setters.put(BLOB, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {

            if (object == null) {
                ps.setNull(position, BLOB.getType());
                return;
            }
            if (object instanceof byte[])
                ps.setBytes(position, (byte[]) object);
            else if (object instanceof InputStream) {
                ps.setBlob(position, (Blob) object);
            } else {
                throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
            }
        });

        setters.put(TEXT, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {

            if (object == null) {
                ps.setNull(position, TEXT.getType());
                return;
            }
            if (object instanceof String) {
                ps.setString(position, (String) object);
            } else {
                throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
            }
        });
        setters.put(VARCHAR, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {

            if (object == null) {
                ps.setNull(position, VARCHAR.getType());
                return;
            }
            if (object instanceof String) {
                ps.setString(position, (String) object);
            } else {
                throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
            }
        });
        setters.put(ENUM, (PreparedStatement ps, int position, Object object, SqlFlag[] flags, Class<?> type) -> {

            if (object == null) {
                ps.setNull(position, ENUM.getType());
                return;
            }
            if (object instanceof Enum) {
                ps.setString(position, ((Enum<?>) object).name());
            } else {
                throw new IllegalArgumentException("Unsupported type: " + object.getClass().getName());
            }
        });

        getters.put(INT, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            return rs.getInt(name);
        });
        getters.put(BIGINT, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            return rs.getLong(name);
        });
        getters.put(FLOAT, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            return rs.getFloat(name);
        });
        getters.put(DOUBLE, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            return rs.getDouble(name);
        });
        getters.put(BOOLEAN, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            return rs.getBoolean(name);
        });
        getters.put(BLOB, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            if (InputStream.class.isAssignableFrom(type))
                return new ByteArrayInputStream(rs.getBytes(name));
            return rs.getBytes(name);
        });
        getters.put(TEXT, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            return rs.getString(name);
        });
        getters.put(VARCHAR, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            return rs.getString(name);
        });
        getters.put(ENUM, (ResultSet rs, String name, SqlFlag[] flags, Class<?> type) -> {
            @SuppressWarnings("rawtypes")
            Class<? extends Enum> clazz = (Class<? extends Enum>) type;
            String s = rs.getString(name);
            if (s == null)
                return null;
            return Enum.valueOf(clazz, s);
        });
        checkers.put(SqlType.INT,
                (Object object) -> object == null || object instanceof Integer);

        checkers.put(BIGINT, (Object object) -> object == null || object instanceof Long);

        checkers.put(FLOAT, (Object object) -> object == null || object instanceof Float);

        checkers.put(DOUBLE, (Object object) -> object == null || object instanceof Double);
        checkers.put(BOOLEAN, (Object object) -> object == null || object instanceof Boolean);
        checkers.put(BLOB, (Object object) -> object == null || object instanceof byte[] || object instanceof InputStream);
        checkers.put(VARCHAR, (Object object) -> object == null || object instanceof String);
        checkers.put(ENUM, (Object object) -> object == null || object instanceof Enum<?>);
        checkers.put(TEXT, (Object object) -> false);
        //Not needed for now ^^^

    }

    int type;
    String name;

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    private SqlType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Check is object class acceptable for this type
     */
    public boolean check(Object object) {
        return checkers.get(this).check(object);
    }

    public void set(PreparedStatement stat, int position, Object obj, SqlFlag[] flags, Class<?> type) throws SQLException {
        setters.get(this).set(stat, position, obj, flags, type);
    }

    public Object get(ResultSet rs, String name, SqlFlag[] flags, Class<?> type) throws SQLException {
        return getters.get(this).get(rs, name, flags, type);
    }

    public static SqlType getSqlType(Object obj) {
        return Arrays.stream(SqlType.values()).filter(i -> i.check(obj)).findFirst().orElse(null);
    }

    public static interface Setter {
        public void set(PreparedStatement stat, int position, Object object, SqlFlag[] flags, Class<?> type)
                throws SQLException;
    }

    public static interface Getter {
        public Object get(ResultSet rs, String name, SqlFlag[] flags, Class<?> type) throws SQLException;
    }

    public static interface Checker {
        public boolean check(Object object);
    }

}
