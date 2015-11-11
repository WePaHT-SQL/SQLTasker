package wepaht.service;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wepaht.Application;
import wepaht.domain.Database;
import wepaht.repository.DatabaseRepository;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class DatabaseServiceTest {

    @Autowired
    DatabaseService dbService;

    @Autowired
    DatabaseRepository dbRepository;

    @Test
    public void databaseCanBeCreatedWithCorrectCreateTablesTest() {
        String testDbName = "TestDatabase";
        String testDbCreateTable = "CREATE TABLE Test(id integer);" +
                                    "INSERT INTO Test (id) VALUES (7);";

        dbService.createDatabase(testDbName, testDbCreateTable);

        List<Database> databases = dbRepository.findByName(testDbName);

        assertTrue(databases.stream().filter(db -> db.getDatabaseSchema().equals(testDbCreateTable)).findFirst().isPresent());
    }

    @Test
    public void databaseCanNotBeCreatedWithIncorrectCreateTablesTest() {
        String testDbName = "TestDatabase2";
        String testDbIncorrectCreateTable = "Haha, lol";

        dbService.createDatabase(testDbName, testDbIncorrectCreateTable);

        List<Database> databases = dbRepository.findByName(testDbName);

        assertFalse(databases.stream().filter(db -> db.getDatabaseSchema().equals(testDbIncorrectCreateTable)).findFirst().isPresent());
    }

    @Test
    public void listedDatabasesContainsCreatedDatabases() throws Exception {
        String testDbName = "TestDatabase3";
        String tableName = "testing";
        String testDbCreateTable = "CREATE TABLE " + tableName +"(id integer);" +
                "INSERT INTO " + tableName + " (id) VALUES (7);";

        dbService.createDatabase(testDbName, testDbCreateTable);
        Database testDb = dbRepository.findByName(testDbName).get(0);

        List<String> tableNames = dbService.listDatabaseObjects(testDb.getId());

        assertTrue(tableNames.contains(tableName.toUpperCase()));
    }
}
