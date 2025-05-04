package su.nezushin.anvil.orm.table.selector.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import su.nezushin.anvil.orm.table.SqlColumn;

public interface PSStorage {



	public void set(PreparedStatement stat) throws SQLException;

}

