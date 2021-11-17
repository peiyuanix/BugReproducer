# BugReproducer

## Build & Package
```
maven assembly:assembly
```

Then you can get jar file from maven output directory.
There are two jar files, one is packaged without dependcies, the other with.

## Usage
java -jar BugReproducer.jar ${CONFIG_PATH}

## Config
Config is a description of the bug reproduction process.  

A config file consists of `connUrl`, `initList`, `operationList` and so on.

Here is an example.

```yml
initUrl: "jdbc:mysql://127.0.0.1:4000?serverTimezone=UTC&useServerPrepStmts=true&cachePrepStmts=true"
connUrl: "jdbc:mysql://127.0.0.1:4000/reproduceDB?serverTimezone=UTC&useServerPrepStmts=true&cachePrepStmts=true"
database: "reproduceDB"
user: "root"
password: ""
initList:
  - "create table table_7_2(a int primary key, b int, c double);"
  - "insert into table_7_2 values(676, 5012153, 2240641.4);"
operationList:
  - trxId: "739"
    sql: "update table_7_2 set b=-5012153, c=2240641.4 where a=676;"
  - trxId: "723"
    sql: "update table_7_2 set b=852150 where a=676;"
  - trxId: "739"
    sql: "commit;"
  - trxId: "1000"
    sql: "select * from table_7_2;"
  - trxId: "1000"
    sql: "commit;"
```

Comments:  

`initURL`: A JDBC_URL **without** database name.

`connURL`: A JDBC_URL **with** database name.

`database`: A database name, which don't need exist before runing BugReproducer.

`user`: Username

`password`: Password

`initList`: A sequence of sqls used to init the test database.

`operationList`: A sequence of sqls used to test the database. The `trxId` is transaction ID, and the `sql` is just a excutable SQL.
