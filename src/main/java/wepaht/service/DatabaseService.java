package wepaht.service;

import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wepaht.domain.Database;
import wepaht.domain.Table;
import wepaht.repository.DatabaseRepository;

import javax.annotation.PostConstruct;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.*;

@Service
public class DatabaseService {

    @Autowired
    private DatabaseRepository databaseRepository;

    private HashSet<String> defaultTables = new HashSet<>();

    @PostConstruct
    private void init() {
        defaultTables.addAll(Arrays.asList(
                "CATALOGS",
                "COLLATIONS",
                "COLUMNS",
                "COLUMN_PRIVILEGES",
                "CONSTANTS",
                "CONSTRAINTS",
                "CROSS_REFERENCES",
                "DOMAINS",
                "FUNCTION_ALIASES",
                "FUNCTION_COLUMNS",
                "HELP",
                "INDEXES",
                "IN_DOUBT",
                "LOCKS",
                "QUERY_STATISTICS",
                "RIGHTS",
                "ROLES",
                "SCHEMATA",
                "SEQUENCES",
                "SESSIONS",
                "SESSION_STATE",
                "SETTINGS",
                "TABLES",
                "TABLE_PRIVILEGES",
                "TABLE_TYPES",
                "TRIGGERS",
                "TYPE_INFO",
                "USERS",
                "VIEWS"));
    }

    public boolean createDatabase(String name, String createTable) {
        try {
            Database db = new Database();

            db.setName(name);
            db.setDatabaseSchema(createTable);

            //Testing the connection
            Connection conn = createConnectionToDatabase(name, createTable);
            conn.close();

            databaseRepository.save(db);

            return true;
        } catch (Exception e) {

        }
        return false;
    }

    public List<String> listDatabaseObjects(Long id) throws Exception {
        Database db = databaseRepository.findOne(id);
        List<String> objects = new ArrayList<String>();

        Connection conn = createConnectionToDatabase(db.getName(), db.getDatabaseSchema());

        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, "%", null);

        while (resultSet.next()) {
            objects.add(resultSet.getString(3));
        }

        conn.close();
        return objects;
    }

    public Map<String, Table> listDatabase(Long databaseId) {
        HashMap<String, Table> listedDatabase = new HashMap<>();
        Database database = databaseRepository.findOne(databaseId);

        try {
            Connection connection = createConnectionToDatabase(database.getName(), database.getDatabaseSchema());

            List<String> tables = listDatabaseTables(connection);

            tables.parallelStream().forEach(tableName -> {
                Table table = new Table(tableName);
                table.setColumns(listTableColumns(tableName, connection));
                table.setRows(listTableRows(tableName, table.getColumns(), connection));
                listedDatabase.put(tableName, table);
            });

            connection.close();
        } catch (Exception e) {
            listedDatabase.put("ERROR", null);
        }

        return listedDatabase;
    }

    public Table performSelectQuery(Long databaseId, String sqlQuery) {
        Table queryResult = new Table("query");
        Database database = databaseRepository.findOne(databaseId);
        Statement statement = null;

        try {
            Connection connection = createConnectionToDatabase(database.getName(), database.getDatabaseSchema());
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            queryResult.setColumns(listQueryColumns(resultSet));
            //queryResult.setRows(listQueryRows(resultSet, queryResult.getColumns()));

            connection.close();
        } catch (Exception e) {
            queryResult.setName("ERROR");
        }

        return queryResult;
    }

    private List<List<String>> listQueryRows(ResultSet resultSet, List<String> columns) throws Exception {
        List<List<String>> rows = new ArrayList<>();

        while (resultSet.next()) {
            List<String> row = new ArrayList<>();
            for (String column : columns) {
                row.add(resultSet.getString(column));
            }
            rows.add(row);
        }


        return rows;
    }

    private List<String> listQueryColumns(ResultSet resultSet) throws Exception {
        HashSet<String> columns = new HashSet<>();

        while(resultSet.next()) {
            columns.add(resultSet.getString(3));
        }

        return new ArrayList<String>(columns);
    }

    private List<String> listDatabaseTables(Connection connection) {
        ArrayList<String> tables = new ArrayList<>();

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "%", null);

            while (resultSet.next()) {
                String tableName = resultSet.getString(3);

                if (!defaultTables.contains(tableName)) {
                    tables.add(tableName);
                }
            }
        } catch (Exception e) {}

        return tables;
    }

    private List<String> listTableColumns(String tableName, Connection connection) {
        ArrayList<String> columns = new ArrayList<>();

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, tableName, null);

            while (resultSet.next()) {
                String columnName = resultSet.getString(4);

                columns.add(columnName);
            }
        } catch (Exception e) {}

        return columns;
    }

    private List<List<String>> listTableRows(String tableName, List<String> columns, Connection connection) {
        List<List<String>> rows = new ArrayList<>();
        Statement statement = null;
        String selectQuery = "SELECT * FROM " + tableName + ";";

        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery);

            while (resultSet.next()) {
                ArrayList<String> row = new ArrayList<>();

                for (String column : columns) {
                    row.add(resultSet.getString(column));
                }

                rows.add(row);
            }
        } catch (Exception e) {}

        return rows;
    }

    private Connection createConnectionToDatabase(String databaseName, String sql) throws Exception {
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:mem:" + databaseName;
        String user = "lol";
        String pwds = "hah";

        Connection conn = DriverManager.getConnection(url, user, pwds);

        Statement statement = conn.createStatement();
        int result = statement.executeUpdate(sql);

        return conn;
    }
}
