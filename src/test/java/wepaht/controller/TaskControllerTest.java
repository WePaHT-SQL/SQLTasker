package wepaht.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import wepaht.Application;
import wepaht.domain.*;
import wepaht.repository.DatabaseRepository;
import wepaht.repository.TaskRepository;

import java.util.List;

import static org.junit.Assert.*;

import wepaht.repository.UserRepository;
import wepaht.service.DatabaseService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import wepaht.service.PastQueryService;
import wepaht.service.UserService;


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

    @Autowired
    UserRepository userRepository;

    @Mock
    UserService userServiceMock;

    @InjectMocks
    TaskController taskController;

    private MockMvc mockMvc = null;
    private Database database = null;
    private User admin = null;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

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
        admin = new User();
        admin.setUsername("test");
        admin.setPassword("test");
        admin.setRole("ADMIN");
        admin = userRepository.save(admin);
        when(userServiceMock.getAuthenticatedUser()).thenReturn(admin);        
    }

    @After
    public void tearDown() {
        if (admin != null) {
            userRepository.delete(admin);
        }
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
        mockMvc.perform(get(API_URI).with(user("user").roles("TEACHER")))
                .andExpect(status().isOk());
    }

    @Test
    public void createSelectQuery() throws Exception {
        Task task = randomTask();
        task = taskRepository.save(task);

        String query = "select firstname, lastname from testdb";

        mockMvc.perform(post(API_URI + "/" + task.getId() + "/query").param("query", query).param("id", "" + task.getId()).with(user("test")).with(csrf()))
                .andExpect(status().is3xxRedirection())
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
                    .with(user("test").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        List<Task> tasks = taskRepository.findAll();

        assertTrue(tasks.stream().filter(task -> task.getName().equals(taskName)).findFirst().isPresent());
    }
    
    @Test
    public void studentCanSuggestTask() throws Exception {
        User student = new User();
        student.setUsername("student");
        student.setPassword("student");
        student.setRole("STUDENT");
        student = userRepository.save(student);
        when(userServiceMock.getAuthenticatedUser()).thenReturn(student);
        
        String taskName = "testTask";
        Long databaseId = database.getId();
        mockMvc.perform(post(API_URI).param("name", taskName)
                    .param("description", "To test suggestion")
                    .param("solution", "select * from persons;")
                    .param("databaseId", databaseId.toString())
                    .with(user("student")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        assertNotNull(taskRepository.findByName("SUGGESTION: "+taskName).get(0));
    }


    @Test
    public void querysTableIsSeen() throws Exception {
        Task testTask = randomTask();
        testTask = taskRepository.save(testTask);

        mockMvc.perform(post(API_URI + "/" + testTask.getId() + "/query").param("query", "SELECT * FROM persons;").with(user("test")).with(csrf()))
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

        mockMvc.perform(post(API_URI + "/" + testTask.getId() + "/query").param("query", solution).with(user("test")).with(csrf()))
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
                    .with(user("test")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Query sent."))
                .andReturn();
    }
    
    @Test
    public void pastQueryIsSaved() throws Exception {
        Task task = randomTask();
        task = taskRepository.save(task);

        String query = "select firstname, lastname from testdb";
        mockMvc.perform(post(API_URI + "/" + task.getId() + "/query").param("query", query).param("id", "" + task.getId()).with(user("test")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Query sent."))
                .andReturn();

        assertNotNull(pastQueryService.returnQuery("allUsers", task.getId(), "allAnswers").get(0));
    }
}
