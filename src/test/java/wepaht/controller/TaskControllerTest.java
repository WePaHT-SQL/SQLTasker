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
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import wepaht.Application;
import wepaht.domain.Task;
import wepaht.repository.TaskRepository;
import java.util.List;

import static org.junit.Assert.assertTrue;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by tatti on 6.11.2015.
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class TaskControllerTest {

    private final String API_URI = "/tasks";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private TaskRepository taskRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    }

    private Task randomTask() {
        Task task = new Task();
        task.setName(RandomStringUtils.randomAlphanumeric(10));
        task.setDescription(RandomStringUtils.randomAlphabetic(30));
        task.setStatus("Uncomplete");
        return task;
    }

    @Test
    public void statusIsOkTest() throws Exception {
        mockMvc.perform(get(API_URI))
                .andExpect(status().isOk());
    }

    @Test
    public void createQuery() throws Exception {
        Task task = randomTask();
        taskRepository.save(task);

        String query ="Jee";          
        mockMvc.perform(post(API_URI).param("query", query).param("id",""+ task.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/tasks/{id}"))
                .andExpect(flash().attributeExists("messages"))
                .andReturn();

    }
}
