package wepaht.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wepaht.domain.Database;
import wepaht.repository.DatabaseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {

    @Autowired
    private DatabaseRepository dbRepo;

    public boolean createDatabase(String name, String createTable) {
        try {
            Database db = new Database();

            db.setName(name);
            db.setDatabaseSchema(createTable);

            //Testing the connection
            Connection conn = createConnectionToDatabase(name, createTable);
            conn.close();

            dbRepo.save(db);

            return true;
        } catch (Exception e) {

        }
        return false;
    }

    public List<String> listDatabaseObjects(Long id) throws Exception {
        Database db = dbRepo.findOne(id);
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
}
