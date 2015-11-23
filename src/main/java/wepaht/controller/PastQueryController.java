package wepaht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wepaht.repository.TaskRepository;

@Controller
@RequestMapping("queries")
public class PastQueryController {


    @Autowired
    TaskRepository taskRepository;


    @RequestMapping(method = RequestMethod.GET)
    public String getPage(Model model){
        model.addAttribute("queries",taskRepository.findAll());
        return "query";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String getPastQuery(){


        return "redirect:/queries";
    }
}
