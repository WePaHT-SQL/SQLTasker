package wepaht.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wepaht.domain.Database;
import wepaht.repository.DatabaseRepository;

import javax.annotation.PostConstruct;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
public class DatabaseService {

    @Autowired
    private DatabaseRepository databaseRepository;

    private HashSet<String> defaultTables = new HashSet<>();

    @PostConstruct
    private void init() {
        defaultTables.addAll(Arrays.asList("CATALOGS",
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

    public List<String> listDatabaseTables(Long databaseId) {
        Database db = databaseRepository.findOne(databaseId);
        List<String> tables = new ArrayList<String>();

        try {
            Connection conn = createConnectionToDatabase(db.getName(), db.getDatabaseSchema());

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "%", null);

            while (resultSet.next()) {
                String tableName = resultSet.getString(3);

                if (!defaultTables.contains(tableName)) {
                    tables.add(tableName);
                }
            }

            conn.close();
        } catch (Exception e) {
            tables.add("ERROR IN QUERY");
        }

        return tables;
    }

    public List<String> listTableColumns(Long databaseId, String tableName) {
        Database database = databaseRepository.findOne(databaseId);
        List<String> columns = new ArrayList<>();
        tableName = tableName.toUpperCase();

        try {
            Connection conn = createConnectionToDatabase(database.getName(), database.getDatabaseSchema());

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, tableName, null);

            while (resultSet.next()) {
                String columnName = resultSet.getString(4);

                columns.add(columnName);
            }

            conn.close();
        } catch (Exception e) {}

        return columns;
    }
}
