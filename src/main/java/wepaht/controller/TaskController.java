/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wepaht.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.Database;
import wepaht.domain.Task;
import wepaht.repository.DatabaseRepository;
import wepaht.repository.TaskRepository;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import wepaht.domain.Table;
import wepaht.service.DatabaseService;
import wepaht.service.PastQueryService;
import wepaht.service.TaskResultService;
import wepaht.service.UserService;

@Controller
@RequestMapping("tasks")
public class TaskController {

    private Map<Long, String> queries;
    private String selectRegex = "select ([a-zA-Z0-9*_]+){1}(, [[a-zA-Z0-9*_]+)* from [[a-zA-Z0-9*_]+( where [[a-zA-Z0-9*_]+='[[a-zA-Z0-9*_]+')?";

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    DatabaseRepository databaseRepository;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    UserService userService;

    @Autowired
    PastQueryService pastQueryService;

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

    @Secured("ROLE_ADMIN")
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
            if (!databaseService.isValidQuery(db, task.getSolution())) {
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

        } else {
            model.addAttribute("queryResults", new Table("dummy"));
        }

        model.addAttribute("task", task);

        return "task";
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
    public String getTaskEditor(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskRepository.findOne(id));
        model.addAttribute("databases", databaseRepository.findAll());
        model.addAttribute("user", userService.getAuthenticatedUser());
        
        return "editTask";
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String removeTask(@PathVariable Long id, RedirectAttributes redirectAttributes) throws Exception {
        taskRepository.delete(id);
        // remove all connections here
        redirectAttributes.addFlashAttribute("messages", "Task deleted!");
        return "redirect:/tasks";
    }

    @Secured("ROLE_ADMIN")
    @Transactional
    @RequestMapping(value ="/{id}/edit", method = RequestMethod.POST)
    public String updateTask(@PathVariable Long id, RedirectAttributes redirectAttributes,
            @RequestParam Long databaseId,
            @RequestParam String name,
            @RequestParam String solution,
            @RequestParam String description){
        if (solution != null || !solution.isEmpty()) {
            if (!databaseService.isValidQuery(databaseRepository.findOne(databaseId), solution)) {
                redirectAttributes.addFlashAttribute("messages", "Task creation failed due to invalid solution");
                return "redirect:/tasks";
            }
        }
        Task oldtask = taskRepository.getOne(id);
        oldtask.setDatabase(databaseRepository.findOne(databaseId));
        oldtask.setDescription(description);
        oldtask.setName(name);
        oldtask.setSolution(solution);
        
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("messages", "Task modified!");
        return "redirect:/tasks/{id}";
    }
    

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/query")
    public String sendQuery(RedirectAttributes redirectAttributes, @RequestParam(required = false, defaultValue = "") String query, @PathVariable Long id) {
        Task task = taskRepository.findOne(id);

        queries.put(id, query);
        redirectAttributes.addFlashAttribute("messages", "Query sent.");

        if (task.getSolution() != null && taskResultService.evaluateSubmittedQueryStrictly(task, query)) {
            RedirectAttributes messages = redirectAttributes.addFlashAttribute("messages", "Your answer is correct!");
            pastQueryService.saveNewPastQuery(userService.getAuthenticatedUser().getUsername(), task.getId(),query,true);
        }else{
            pastQueryService.saveNewPastQuery(userService.getAuthenticatedUser().getUsername(), task.getId(),query,false);
        }

        Map<String, Table> queryResult = databaseService.performQuery(task.getDatabase().getId(), query);

        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("tables", queryResult);
        return "redirect:/tasks/{id}";
    }
}
