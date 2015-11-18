/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wepaht.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.Database;
import wepaht.domain.Task;
import wepaht.repository.DatabaseRepository;
import wepaht.repository.TaskRepository;

import javax.annotation.PostConstruct;

import wepaht.domain.Table;
import wepaht.service.DatabaseService;
import wepaht.service.TaskResultService;

@Controller
@RequestMapping("tasks")
public class TaskController {

    private Map<Long, String> queries;
    private String selectRegex = "select ([a-zA-Z0-9_]+){1}(, [a-zA-Z0-9_]+)* from [a-zA-Z0-9_]+( where [a-zA-Z0-9_]+='[a-zA-Z0-9_]+')?";

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    DatabaseRepository databaseRepository;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    TaskResultService taskResultService;

    @PostConstruct
    public void init() {
        queries = new HashMap<>();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String listTasks(Model model) {
        model.addAttribute("tasks", taskRepository.findAll());
        model.addAttribute("databases", databaseRepository.findAll());

        // queries = new HashMap<Long,String>();
        return "tasks";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createTask(RedirectAttributes redirectAttributes,
                             @ModelAttribute Task task,
                             @RequestParam Long databaseId) {
        if (task == null) {
            redirectAttributes.addFlashAttribute("messages", "Task creation has failed");
            return "redirect:/tasks";
        }

        Database db = databaseRepository.findOne(databaseId);
        task.setDatabase(db);

        if (task.getSolution() != null || !task.getSolution().isEmpty()) {
            if (!databaseService.isValidSelectQuery(db, task.getSolution())) {
                redirectAttributes.addFlashAttribute("messages", "Task creation failed due to invalid solution");
                return "redirect:/tasks";
            }
        }

        taskRepository.save(task);
        redirectAttributes.addFlashAttribute("messages", "Task has been created");

        return "redirect:/tasks";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getTask(@PathVariable Long id, Model model) throws Exception {
        Task task = taskRepository.findOne(id);

        if (queries.containsKey(id)) {
//          model.addAttribute("queryResults", databaseService.performSelectQuery(task.getDatabase().getId(), queries.get(id)));

        } else {
            model.addAttribute("queryResults", new Table("dummy"));
        }

        model.addAttribute("task", task);

        return "task";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/query")
    public String sendQuery(RedirectAttributes redirectAttributes, @RequestParam(required = false, defaultValue = "") String query, @PathVariable Long id) {
        Task task = taskRepository.findOne(id);

        queries.put(id, query);
        redirectAttributes.addFlashAttribute("messages", "Query sent.");

        if (task.getSolution() != null && taskResultService.evaluateSubmittedQueryStrictly(task, query)) {
            redirectAttributes.addFlashAttribute("messages", "Your answer is correct!");
        }

        Table queryResult = databaseService.performSelectQuery(task.getDatabase().getId(), query);

        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("queryResults", queryResult);
        return "redirect:/tasks/{id}";
    }
}
