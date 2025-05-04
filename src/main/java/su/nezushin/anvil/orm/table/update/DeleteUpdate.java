package su.nezushin.anvil.orm.table.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import su.nezushin.anvil.orm.table.AnvilORMSerializable;
import su.nezushin.anvil.orm.table.AnvilORMTable;
import su.nezushin.anvil.orm.table.ex.AnvilORMRuntimeException;
import su.nezushin.anvil.orm.table.selector.Selector;

public class DeleteUpdate<T extends AnvilORMSerializable> extends Selector<T, DeleteUpdate<T>> {

    public DeleteUpdate(AnvilORMTable<T> table) {
        super(table);
    }

    public int compete() {
        return table.synchronize(() -> {
            Connection c = table.getConnection();
            try (PreparedStatement ps = generateStatement("DELETE FROM " + table.getTableName(), 0, new ArrayList<>(), c);) {

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

}
