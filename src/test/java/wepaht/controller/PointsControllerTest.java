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
import wepaht.domain.User;
import wepaht.repository.UserRepository;

import static junit.framework.TestCase.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import wepaht.service.PastQueryService;

@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class PointsControllerTest {
    
    @Autowired
    private WebApplicationContext webAppContext;
    
    @Autowired
    private PastQueryService pastQueryService;

    private MockMvc mockMvc = null;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).apply(springSecurity()).build();
        pastQueryService.deleteAllPastQueries();
    }

    @Test
    public void noPointslistWithoutPoints() throws Exception {
        
        mockMvc.perform(get("/points").with(user("user").roles("TEACHER")).with(csrf()))
                .andExpect(view().name("points"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("messages", "No points available."))
                .andReturn();
    }
    
    @Test
    public void returnsTable() throws Exception {
        
        for(Long l=0l; l<5; l++){
        pastQueryService.saveNewPastQuery("student", l, "select firstname from persons", true);
        }
        mockMvc.perform(get("/points").with(user("user").roles("TEACHER")).with(csrf()))
                .andExpect(view().name("points"))
                .andExpect(model().attributeExists("tables"))
                .andExpect(status().isOk());
    }
}
