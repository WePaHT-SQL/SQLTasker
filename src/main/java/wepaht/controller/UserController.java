package wepaht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.User;
import wepaht.repository.UserRepository;
import wepaht.service.UserService;

@Controller
public class UserController {

    private String[] roles = {"STUDENT","TEACHER","ADMIN"};

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Secured("ROLE_ADMIN")
    @RequestMapping(value="users", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", roles);
        model.addAttribute("user", userService.getAuthenticatedUser());
        return "users";
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value="users/{id}", method = RequestMethod.GET)
    public String getUser(Model model, @PathVariable Long id) {
        model.addAttribute("user", userRepository.findOne(id));
        model.addAttribute("roles", roles);
        model.addAttribute("user", userService.getAuthenticatedUser());
        return "user";
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value="users", method = RequestMethod.POST)
    public String create(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("messages", "User created succesfully.");
        return "redirect:/";
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value="users/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.delete(id);
        redirectAttributes.addFlashAttribute("messages", "User deleted.");

        return "redirect:/users";
    }

    @Secured("ROLE_ADMIN")
    @Transactional
    @RequestMapping(value ="users/{id}/edit", method = RequestMethod.POST)
    public String update(@PathVariable Long id, RedirectAttributes redirectAttributes,
                         @RequestParam(required = false) String username,
                         @RequestParam(required = false) String role,
                         @RequestParam(required = false) String password,
                         @RequestParam(required = false) String repassword){

        if(!password.equals(repassword)){
            redirectAttributes.addFlashAttribute("messages", "Passwords didn't match");
            return "redirect:/users/{id}";
        }

        User olduser = userRepository.getOne(id);
        olduser.setUsername(username);
        olduser.setRole(role);
        olduser.setPassword(password);

        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("messages", "User modified!");
        return "redirect:/users/{id}";
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String viewRegister(Model model) {
        model.addAttribute("user", userService.getAuthenticatedUser());
        return "register";
    }
}
