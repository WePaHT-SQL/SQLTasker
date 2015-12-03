package wepaht.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.Table;
import wepaht.domain.User;
import wepaht.repository.UserRepository;
import wepaht.service.PointService;
import wepaht.service.UserService;

@Controller
@RequestMapping("points")
public class PointsController {

    @Autowired
    PointService pointService;

    @Secured("ROLE_TEACHER")
    @RequestMapping(method = RequestMethod.GET)
    public String listPoints(Model model, RedirectAttributes redirectAttributes) {
        if (!pointService.getAllPoints().getRows().isEmpty()) {
            Table pointsTable = pointService.getAllPoints();
            Map<String, Table> tables = new HashMap<>();
            tables.put("pointsTable", pointsTable);
            model.addAttribute("tables", tables);
            return "points";
        }
        model.addAttribute("messages", "No points available.");
        return "points";
    }    
}
