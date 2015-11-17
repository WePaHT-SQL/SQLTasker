package wepaht.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import wepaht.Application;
import wepaht.domain.Database;
import wepaht.repository.DatabaseRepository;
import wepaht.repository.TaskRepository;

import java.util.List;
import junit.framework.Assert;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class DatabaseControllerTest {

    private final String API_URI = "/databases";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private DatabaseRepository dbRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    }

    @Test
    public void statusIsOkTest() throws Exception{
        mockMvc.perform(get(API_URI))
                .andExpect(status().isOk());
    }

    @Test
    public void createDatabaseTest() throws Exception {
        String dbName = "suchDB";
        String dbSchema = "CREATE TABLE WOW(id integer);" +
                            "INSERT INTO WOW (id) VALUES (7);";

        mockMvc.perform(post(API_URI).param("name", dbName).param("databaseSchema", dbSchema))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("messages"));

        List<Database> databases = dbRepository.findByName(dbName);

        assertTrue(databases.stream().filter(db -> db.getDatabaseSchema().equals(dbSchema)).findFirst().isPresent());
    }
    
    @Test
    public void databaseViewHasCorrectTables() throws Exception{
        String dbSchema = "CREATE TABLE Persons(PersonID int, LastName varchar(255), FirstName varchar(255));"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME)"
                + "VALUES (2, 'Raty', 'Matti');"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME)"
                + "VALUES (1, 'Jaaskelainen', 'Timo');";
        
        mockMvc.perform(post(API_URI).param("name", "testDb").param("databaseSchema", dbSchema))
                .andReturn();
        
        Database testDb = dbRepository.findByName("testDb").get(0);
        
        mockMvc.perform(get(API_URI+testDb.getId()))
                .andReturn();
        
//        Assert.assertEquals("3", databaseTables.getRows.size());
//        Assert.assertEquals("2", databaseTables.getColumns.size());
    }
}
