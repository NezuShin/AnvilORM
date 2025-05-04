package su.nezushin.anvil.orm;

public enum SqlFlag {
	
	PRIMARY_KEY("PRIMARY KEY"),
	UNIQUE("UNIQUE"),
	NOT_NULL("NOT NULL"),
	AUTO_INCREMENT("AUTO_INCREMENT");
	
	private String sqlName;
	
	private SqlFlag(String sqlName) {
		this.sqlName = sqlName;
	}

	public String getSqlName() {
		return sqlName;
	}
	
	
	
}
