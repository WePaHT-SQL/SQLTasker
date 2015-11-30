package wepaht.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.jdbc.Expectation;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import wepaht.Application;
import wepaht.domain.Database;
import wepaht.domain.Task;
import wepaht.repository.DatabaseRepository;
import wepaht.repository.TaskRepository;

import java.util.List;

import static org.junit.Assert.*;

import org.springframework.test.web.servlet.MvcResult;
import wepaht.service.DatabaseService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import wepaht.domain.PastQuery;

import wepaht.domain.Table;
import wepaht.service.PastQueryService;


@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class TaskControllerTest {

    private final String API_URI = "/tasks";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private DatabaseRepository databaseRepository;
    
    @Autowired
    private PastQueryService pastQueryService;

    private MockMvc mockMvc = null;

    private Database database = null;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).apply(springSecurity()).build();

        databaseService.createDatabase("testDatabase4", "CREATE TABLE Persons"
                + "(PersonID int, LastName varchar(255), FirstName varchar(255), Address varchar(255), City varchar(255));"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)"
                + "VALUES (2, 'Raty', 'Matti', 'Rautalammintie', 'Helsinki');"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)"
                + "VALUES (1, 'Jaaskelainen', 'Timo', 'Jossakin', 'Heslinki');"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)"
                + "VALUES (3, 'Entieda', 'Kake?', 'Laiva', 'KJYR');");
        database = databaseRepository.findByName("testDatabase4").get(0);
    }

    private Task randomTask() {
        Task task = new Task();
        task.setName(RandomStringUtils.randomAlphanumeric(10));
        task.setDescription(RandomStringUtils.randomAlphabetic(30));
        task.setDatabase(database);
        return task;
    }

    @Test
    public void statusIsOkTest() throws Exception {
        mockMvc.perform(get(API_URI).with(user("user")))
                .andExpect(status().isOk());
    }

    @Test
    public void createSelectQuery() throws Exception {
        Task task = randomTask();
        task = taskRepository.save(task);

        String query = "select firstname, lastname from testdb";
        mockMvc.perform(post(API_URI + "/" + task.getId() + "/query").param("query", query).param("id", "" + task.getId()).with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/tasks/{id}"))
                .andExpect(flash().attribute("messages", "Query sent."))
                .andReturn();
    }

    @Test
    public void createTask() throws Exception {
        String taskName = "testTask";
        Long databaseId = database.getId();
        mockMvc.perform(post(API_URI).param("name", taskName)
                .param("description", "To test creation of a task with a database")
                .param("solution", "select * from persons;")
                .param("databaseId", databaseId.toString())
                .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        List<Task> tasks = taskRepository.findAll();

        assertTrue(tasks.stream().filter(task -> task.getName().equals(taskName)).findFirst().isPresent());
    }

    @Test
    public void querysTableIsSeen() throws Exception {
        Task testTask = randomTask();
        testTask = taskRepository.save(testTask);

        mockMvc.perform(post(API_URI + "/" + testTask.getId() + "/query").param("query", "SELECT * FROM persons;").with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("tables"))
                .andReturn();
    }

    @Test
    public void correctQueryIsAnnounced() throws Exception {
        Task testTask = randomTask();
        String solution = "SELECT firstname FROM persons;";
        testTask.setSolution(solution);
        testTask = taskRepository.save(testTask);

        mockMvc.perform(post(API_URI + "/" + testTask.getId() + "/query").param("query", solution).with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Your answer is correct!"))
                .andReturn();
    }



    @Test
    public void deleteTask() throws Exception {
        Task testTask = randomTask();
        taskRepository.save(testTask);
        assertNotNull(taskRepository.findOne(testTask.getId()));

        mockMvc.perform(delete(API_URI + "/" + testTask.getId()).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Task deleted!"))
                .andReturn();

        assertNull(taskRepository.findOne(testTask.getId()));
    }

    @Test
    public void editTask() throws Exception {

        Task testTask = randomTask();
        taskRepository.save(testTask);
        assertNotNull(taskRepository.findOne(testTask.getId()));

        mockMvc.perform(post(API_URI + "/" + testTask.getId() + "/edit")
                    .param("databaseId", "" + database.getId())
                    .param("name","Test")
                    .param("solution","SELECT * FROM persons;")
                    .param("description","It works")
                    .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Task modified!"))
                .andReturn();
        testTask=taskRepository.findOne(testTask.getId());
        assertEquals("Test", testTask.getName());
    }

    //Update-, delete-, drop-, insert- and create-queries use the same method
    @Test
    public void updateTypeQuery() throws Exception {
        Task testTask = randomTask();
        taskRepository.save(testTask);
        String sql = "UPDATE persons SET city='Helesinki' WHERE personid=3;";

        mockMvc.perform(post(API_URI + "/" + testTask.getId() + "/query")
                    .param("query", sql)
                    .with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Query sent."))
                .andReturn();
    }
    
    @Test
    public void pastQueryIsSaved() throws Exception {
        Task task = randomTask();
        task = taskRepository.save(task);

        String query = "select firstname, lastname from testdb";
        mockMvc.perform(post(API_URI + "/" + task.getId() + "/query").param("query", query).param("id", "" + task.getId()).with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Query sent."))
                .andReturn();
                
                assertNotNull(pastQueryService.returnQuery(null, task.getId(), null).get(0));        
    }
}
