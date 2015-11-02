/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wepaht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wepaht.repository.TaskRepository;

/**
 *
 * @author Kake
 */
@Controller
@RequestMapping("tasks")
public class TaskController {
    
    @Autowired
    TaskRepository taskRepository;
    
    @RequestMapping(method=RequestMethod.GET)
    public String listTasks(Model model){
        model.addAttribute("tasks", taskRepository.findAll());
        
        return "tasks";
    }
    
    @RequestMapping(value="/{id}", method=RequestMethod.GET)
    public String getTask(@PathVariable Long id, Model model){
        model.addAttribute("task", taskRepository.findOne(id));
        
        return "task";
    }
}
