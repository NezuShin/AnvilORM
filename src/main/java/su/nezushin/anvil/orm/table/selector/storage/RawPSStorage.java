package su.nezushin.anvil.orm.table.selector.storage;

import su.nezushin.anvil.orm.SqlFlag;
import su.nezushin.anvil.orm.SqlType;
import su.nezushin.anvil.orm.table.SqlColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RawPSStorage implements PSStorage {

    Object obj;
    int pos;

    public RawPSStorage(Object obj, int pos) {
        super();
        this.obj = obj;
        this.pos = pos;
    }

    @Override
    public void set(PreparedStatement stat) throws SQLException {
        SqlType.getSqlType(obj).set(stat, pos, obj, new SqlFlag[0], obj.getClass());
    }

    @Override
    public String toString() {
        return "RawPSStorage{" +
                "obj=" + obj +
                ", pos=" + pos +
                '}';
    }
}
