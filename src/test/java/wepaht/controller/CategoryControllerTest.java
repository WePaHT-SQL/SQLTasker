package wepaht.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import wepaht.Application;
import wepaht.domain.Category;
import wepaht.domain.Database;
import wepaht.domain.Task;
import wepaht.repository.CategoryRepository;
import wepaht.repository.TaskRepository;
import wepaht.service.DatabaseService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class CategoryControllerTest {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WebApplicationContext webAppContext;

    private final String URI = "/categories";
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Database database;
    private MockMvc mockMvc;

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
    }

    private Task randomTask() {
        Task task = new Task();
        task.setName(RandomStringUtils.randomAlphanumeric(10));
        task.setDescription(RandomStringUtils.randomAlphabetic(30));
        task.setDatabase(database);
        return taskRepository.save(task);
    }

    private Category createCategory() throws Exception{
        Date today = new Date();

        Category category = new Category();
        category.setName("create new category");
        category.setDescription("test");
        category.setTaskList(new ArrayList<>());
        category.setStartDate(sdf.parse("" + today.getYear() + "-" + today.getMonth() + "-" + today.getDay()));
        int nextYear = today.getYear() + 1;
        category.setExpiredDate(sdf.parse("" + nextYear + "-" + today.getMonth() + "-" + today.getDay()));

        return category;
    }

    @Test
    public void statusIsOk() throws Exception{
        mockMvc.perform(get(URI).with(user("user")))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void adminCanCreateNewCategory() throws Exception{
        Category category = createCategory();

        mockMvc.perform(post(URI)
                    .param("name", category.getName())
                    .param("description", category.getDescription())
                    .param("startDate", sdf.format((Date) category.getStartDate()))
                    .param("expiredDate", sdf.format((Date) category.getExpiredDate()))
                    .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Category has been created!"))
                .andReturn();

        List<Category> categoryList = categoryRepository.findAll();
        assertTrue(categoryList.stream().filter(cat -> cat.getName().equals("create new category")).findFirst().isPresent());
    }

    @Test
    public void categoryCannotBeCreatedWihtoutAdmin() throws Exception {
        Category category = createCategory();
        category.setName("creation fails without permissions");

        mockMvc.perform(post(URI)
                    .param("name", category.getName())
                    .param("description", category.getDescription())
                    .param("startDate", sdf.format((Date) category.getStartDate()))
                    .param("expiredDate", sdf.format((Date) category.getExpiredDate()))
                    .with(user("teacher").roles("TEACHER")).with(csrf()))
                .andExpect(status().isForbidden())
                .andReturn();

        List<Category> categoryList = categoryRepository.findAll();
        assertFalse(categoryList.stream().filter(cat -> cat.getName().equals("creation fails without permissions")).findFirst().isPresent());
    }

    @Test
    public void adminCanEditCreatedCategory() throws Exception {
        Category category = createCategory();
        category.setName("is editing possible?");
        category = categoryRepository.save(createCategory());

        mockMvc.perform(post(URI + "/" + category.getId() + "/edit")
                    .param("name", "editing is possible")
                    .param("description", category.getDescription())
                    .param("startDate", sdf.format((Date) category.getStartDate()))
                    .param("expiredDate", sdf.format((Date) category.getExpiredDate()))
                    .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Category modified!"))
                .andReturn();

        assertTrue(categoryRepository.findOne(category.getId()).getName().equals("editing is possible"));
    }

    @Test
    @Transactional
    public void adminCanCategorizeTasks() throws Exception {
        Category category = createCategory();
        category.setName("categorized tasks");
        Task task1 = randomTask();
        Task task2 = randomTask();
        Task task3 = randomTask();

        mockMvc.perform(post(URI)
                    .param("name", category.getName())
                    .param("description", category.getDescription())
                    .param("startDate", sdf.format((Date) category.getStartDate()))
                    .param("expiredDate", sdf.format((Date) category.getExpiredDate()))
                    .param("taskIds", task1.getId().toString())
                    .param("taskIds", task2.getId().toString())
                    .param("taskIds", task3.getId().toString())
                    .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messages", "Category has been created!"))
                .andReturn();

        List<Category> categoryList = categoryRepository.findAll();
        Category created = categoryList.stream().filter(cat -> cat.getName().equals("categorized tasks")).findFirst().get();
        List<Task> tasks = created.getTaskList();

        assertTrue(tasks.containsAll(Arrays.asList(task1, task2, task3)));
    }
}
