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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.Database;
import wepaht.domain.Task;
import wepaht.repository.DatabaseRepository;
import wepaht.repository.TaskRepository;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping("tasks")
public class TaskController {
    
    private Map<Long,String> queries;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    DatabaseRepository databaseRepository;

    @PostConstruct
    public void init(){
        queries = new HashMap<Long,String>();
    }
    
    @RequestMapping(method=RequestMethod.GET)
    public String listTasks(Model model){
        model.addAttribute("tasks", taskRepository.findAll());
        model.addAttribute("databases", databaseRepository.findAll());
        
       // queries = new HashMap<Long,String>();
        
        return "tasks";
    }

    @RequestMapping(method=RequestMethod.POST)
    public String createTask(RedirectAttributes redirectAttributes,
                             @ModelAttribute Task task,
                             @RequestParam Long databaseId) {
        if (task == null) {
            redirectAttributes.addFlashAttribute("messages", "Task creation has failed");
            return "redirect:/tasks";
        }

        Database db = databaseRepository.findOne(databaseId);
        task.setDatabase(db);
        taskRepository.save(task);
        redirectAttributes.addAttribute("messages", "Task has been created");

        return "redirect:/tasks";
    }
    
    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public String getTask(@PathVariable Long id, Model model ){
        model.addAttribute("task", taskRepository.findOne(id));

        model.addAttribute("query", queries.get(id));
        
        return "task";
    }
    
    @RequestMapping(method=RequestMethod.POST, value = "/{id}/query")
    public String sendQuery(Model model, RedirectAttributes redirectAttributes
            , @RequestParam (required=false, defaultValue="") String query, @PathVariable Long id){
        // queryRepositoryRepository.findByTaskId(id).save(query);
        // Datatable table = databaseService.processQuery(query);
        // queries.solutions.add(id, table);
        queries.put(id, query);
        
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("messages", "Query sent.");
        return "redirect:/tasks/{id}";
    }


}
