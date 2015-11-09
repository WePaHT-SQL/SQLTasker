package wepaht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.Database;
import wepaht.repository.DatabaseRepository;
import wepaht.service.DatabaseService;

import java.util.List;

@Controller
@RequestMapping("databases")
public class DatabaseController {

    @Autowired
    DatabaseService dbService;

    @Autowired
    DatabaseRepository dbRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String listDatabases(Model model) {
        List<Database> databases = dbRepository.findAll();

        model.addAttribute("databases", databases);

        return "databases";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String viewDatabase(Model model, @PathVariable Long id) throws Exception {
        Database database = dbRepository.findOne(id);
        List<String> objects = dbService.listDatabaseObjects(id);

        model.addAttribute("database", database);
        model.addAttribute("objects", objects);

        return "database";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createDatabase(RedirectAttributes redirectAttributes, @ModelAttribute Database database) {
        if (dbService.createDatabase(database.getName(), database.getDatabaseSchema())) {
            redirectAttributes.addFlashAttribute("messages", "Database created!");
        } else {
            redirectAttributes.addFlashAttribute("messages", "Database creation failed!");
        }

        return "redirect:/databases";
    }
}
