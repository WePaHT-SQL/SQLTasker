package wepaht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.PastQuery;
import wepaht.repository.TaskRepository;
import wepaht.service.PastQueryService;

import java.util.List;

@Controller
@RequestMapping("queries")
public class PastQueryController {


    @Autowired
    TaskRepository taskRepository;

    @Autowired
    PastQueryService pastQueryService;

    @RequestMapping(method = RequestMethod.GET)
    public String getPage(Model model){
        model.addAttribute("queries",taskRepository.findAll());
        return "query";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String getPastQuery(RedirectAttributes redirectAttributes,
                               @RequestParam Long taskId,
                               @RequestParam String username,
                               @RequestParam Boolean isCorrect){

         redirectAttributes.addFlashAttribute("messages", "Oboy!");
          List pastQueries = pastQueryService.returnQuery(username, taskId, isCorrect);
       redirectAttributes.addAttribute("pastQuery", pastQueries);
        return "redirect:/queries";
    }
}
