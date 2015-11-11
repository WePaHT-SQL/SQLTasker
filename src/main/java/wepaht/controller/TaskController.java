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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.repository.TaskRepository;

import javax.annotation.PostConstruct;

/**
 *
 * @author Kake
 */
@Controller
@RequestMapping("tasks")
public class TaskController {
    
    private Map<Long,String> queries;

    @PostConstruct
    public void init(){
        queries = new HashMap<Long,String>();
    }
    @Autowired
    TaskRepository taskRepository;
    
    @RequestMapping(method=RequestMethod.GET)
    public String listTasks(Model model){
        model.addAttribute("tasks", taskRepository.findAll());
        
       // queries = new HashMap<Long,String>();
        
        return "tasks";
    }
    
    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public String getTask(@PathVariable Long id, Model model ){
        model.addAttribute("task", taskRepository.findOne(id));

     model.addAttribute("query", queries.get(id));
        
        return "task";
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public String sendQuery(Model model, RedirectAttributes redirectAttributes
            , @RequestParam (required=false, defaultValue="") String query, @RequestParam Long id){
        // queryRepositoryRepository.findByTaskId(id).save(query);
        // Datatable table = databaseService.processQuery(query);
        // queries.solutions.add(id, table);
        queries.put(id, query);
        
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("messages", "Query sent.");
        return "redirect:/tasks/{id}";
    }
}
