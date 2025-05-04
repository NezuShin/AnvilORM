package su.nezushin.anvil.orm.table.selector.storage;

import su.nezushin.anvil.orm.table.SqlColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ColumnPSStorage implements PSStorage {
    SqlColumn column;
    Object obj;
    Class<?> clazz;
    int pos;

    public ColumnPSStorage(SqlColumn column, Object obj, int pos, Class<?> clazz) {
        super();
        this.column = column;
        this.obj = obj;
        this.pos = pos;
        this.clazz = clazz;
    }

    @Override
    public void set(PreparedStatement stat) throws SQLException {
        column.type().set(stat, pos, obj, column.flags(), clazz);
    }

    @Override
    public String toString() {
        return "ColumnPSStorage{" +
                "column=" + column +
                ", obj=" + obj +
                ", clazz=" + clazz +
                ", pos=" + pos +
                '}';
    }
}
