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

    /**
     * Lists tables of a single Database-entity. Example of use found in database.html-resource file and
     * DatabaseController.
     * @param databaseId ID of selected database
     * @return A map in which String-object indicates the name of certain table, and Table contains its' columns
     * and rows in separate lists. In case of broken database, the only returned table name is "ERROR".
     */
    public Map<String, Table> listDatabase(Long databaseId) {
        HashMap<String, Table> listedDatabase = new HashMap<>();
        Database database = databaseRepository.findOne(databaseId);
        Connection connection = null;

        try {
            connection = createConnectionToDatabase(database.getName(), database.getDatabaseSchema());

            List<String> tables = listDatabaseTables(connection);

            final Connection finalConnection = connection;
            tables.parallelStream().forEach(tableName -> {
                Table table = new Table(tableName);
                table.setColumns(listTableColumns(tableName, finalConnection));
                table.setRows(listTableRows(tableName, table.getColumns(), finalConnection));
                listedDatabase.put(tableName, table);
            });

            finalConnection.close();
        } catch (Exception e) {
            listedDatabase.put("ERROR", null);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {}
            }
        }

        return listedDatabase;
    }

    /**
     * Performs a SELECT-query in the selected database. Example of use found in database.html-resource file and
     * DatabaseController.
     * @param databaseId ID of the selected database
     * @param sqlQuery the query. Syntax must be correct in order this to work!
     * @return a table-object, which contains separately its' columns and rows. In case of syntax error, table-object's
     * will be named "ERROR".
     */
    public Table performSelectQuery(Long databaseId, String sqlQuery) {
        Table queryResult = new Table("query");
        Database database = databaseRepository.findOne(databaseId);
        Statement statement = null;
        Connection connection = null;

        try {
            connection = createConnectionToDatabase(database.getName(), database.getDatabaseSchema());
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            queryResult.setColumns(listQueryColumns(resultSet));
            queryResult.setRows(listQueryRows(resultSet, queryResult.getColumns()));
        } catch (Exception e) {
            queryResult.setName("ERROR");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
        ResultSetMetaData metaData = resultSet.getMetaData();
        int numberOfColumns = metaData.getColumnCount();

        for (int i = 1; i < numberOfColumns + 1; i++) {
            String columnName = metaData.getColumnName(i);
            columns.add(columnName);
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
