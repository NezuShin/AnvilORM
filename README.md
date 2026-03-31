# AnvilORM

Object Relational Mapping tool designed to simplify the process of developing plugins for Bukkit.

## Installation

Currently, installation via a Maven repository is not available. To use this library, download the JAR file from the releases page or build it yourself using Gradle. Then add it as a local dependency by including the following code snippet in your `build.gradle` file:

```groovy
dependencies {
    compileOnly files('C:/your-dev-server/plugins/AnvilORM.jar')
}
```
---

### Example Usage

#### 1. Serializable Java Class

Create a simple Java class representing your arbitary data, which will later be mapped to a table in MySQL:

```java
public class ExampleAnvilORMClass implements AnvilORMSerializable {

    @SqlColumn(type = SqlType.INT, flags = {SqlFlag.AUTO_INCREMENT, SqlFlag.PRIMARY_KEY})
    private int id;

    @SqlColumn(type = SqlType.ENUM, name = "some_arbitrary_enum")
    private Material someArbitraryEnum;

    @SqlColumn(type = SqlType.VARCHAR, name = "user_name")
    private String userName;

    @SqlColumn(type = SqlType.BIGINT)
    private long timestamp;


    //There must be empty constructor for successful class deserialization
    public ExampleAnvilORMClass() {
    }

    public ExampleAnvilORMClass(Material someArbitraryEnum, String userName, long timestamp) {
        this.someArbitraryEnum = someArbitraryEnum;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    @Override
    public void onSerialize() {
        //prepare data before inserting to mysql table
    }

    @Override
    public void onDeserialize() {
        //prepare data after loading from mysql table
    }
    
    // Getters & Setters
}
```

#### 2. Connecting to MySQL and Creating Table

Set up connection details and create a new table structure corresponding to our serialized class:

```java
//CREATE TABLE IF NOT EXISTS my_table_name (`id` INT AUTO_INCREMENT PRIMARY KEY, `timestamp` BIGINT, `user_name` VARCHAR(255), `some_arbitrary_enum` VARCHAR(255));

//For mysql
AnvilORMTable<ExampleAnvilORMClass> mysqlOrmTable = AnvilORMFactory.factory().buildMysqlTable(ExampleAnvilORMClass.class, "my_table_name", "127.0.0.1", 3306, "dbname", "user", "password", true);

//For sqlite
AnvilORMTable<ExampleAnvilORMClass> sqliteOrmTable = AnvilORMFactory.factory().buildSqliteTable(ExampleAnvilORMClass.class, "my_table_name", new File(getDataFolder() + File.separator + "database.db"));

//Process all connections in one thread? Needed by sqlite
boolean synchronize = false;

//For any connections supported by loaded drivers
AnvilORMTable<ExampleAnvilORMClass> urlOrmTable = AnvilORMFactory.factory().buildTableFromUrl(ExampleAnvilORMClass.class, "my_table_name", "url", "username", "password", synchronize);
```

#### 3. Querying Database

Retrieve all records matching specific criteria:

```java
//SELECT * FROM my_table_name WHERE (`user_name`='NezuShin') LIMIT 1;
ExampleAnvilORMClass tableRow = ormTable.query().where("user_name", "NezuShin").completeAsOne();


//SELECT count(*) FROM my_table_name WHERE ((`timestamp`< 1746373243907) AND (`timestamp`>1743451263000)) OR ((`some_arbitrary_enum`='STONE'));
int rows = ormTable.query().combination(CombinationOperator.OR).where((query) -> {
    query.where("timestamp", "<", 1746373243907L).where("timestamp", ">", 1743451263000L);
}).where((query) -> {
    query.where("some_arbitrary_enum", Material.STONE);
}).completeAsCount();


//SELECT * FROM my_table_name LIMIT 100;
List<ExampleAnvilORMClass> firstHundredRows = ormTable.query().limit(100).completeAsList();
```

#### 4. Updating Records (`UPDATE SET`)

```java
//UPDATE my_table_name SET `some_arbitrary_enum`='GRASS_BLOCK' WHERE (`some_arbitrary_enum`='DIRT');
int rowsChanged = ormTable.update().set("some_arbitrary_enum", Material.GRASS_BLOCK).where("some_arbitrary_enum", Material.DIRT).complete();


//UPDATE my_table_name SET timestamp=timestamp+1
rowsChanged = ormTable.update().setRaw("timestamp=timestamp+?", 1L).complete();
```

#### 5. Inserting Records (`INSERT INTO`)

```java
//REPLACE INTO my_table_name (`id`,`timestamp`,`user_name`,`some_arbitrary_enum`) VALUES (?, ?, ?, ?)
ormTable.update().replace(new ExampleAnvilORMClass(Material.STONE, "NezuShin", System.currentTimeMillis()));


//REPLACE INTO my_table_name (`id`,`timestamp`,`user_name`,`some_arbitrary_enum`) VALUES (?, ?, ?, ?),(?, ?, ?, ?);
ormTable.update().replace(Lists.newArrayList(
        new ExampleAnvilORMClass(Material.DIRT, "AnotherPerson", 1),
        new ExampleAnvilORMClass(Material.STONE, "YetAnotherPerson", 2)
        ));
```



#### 6. Deleting Records (`DELETE FROM`)


```java
//DELETE FROM my_table_name WHERE (`id`=?);
int rowsChanged = ormTable.delete().where("id", 1).compete();


// DELETE FROM my_table_name LIMIT 1;
rowsChanged = ormTable.delete().limit(1).compete();
```
