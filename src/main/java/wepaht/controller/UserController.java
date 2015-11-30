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
import wepaht.service.PointService;
import wepaht.service.UserService;

@Controller
public class UserController {

    private String[] roles = {"STUDENT", "TEACHER", "ADMIN"};

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;
    
    @Autowired
    PointService pointService;

    @Secured("ROLE_TEACHER")
    @RequestMapping(value = "users", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", roles);
        return "users";
    }

    @Secured("ROLE_TEACHER")
    @RequestMapping(value = "users/{id}", method = RequestMethod.GET)
    public String getUser(Model model, @PathVariable Long id) {
        User editedUser = userRepository.findOne(id);
        model.addAttribute("editedUser", editedUser);
        model.addAttribute("roles", roles);
        model.addAttribute("points", pointService.getPointsByUsername(editedUser.getUsername()));
        return "user";
    }

    @RequestMapping(value = "profile", method = RequestMethod.GET)
    public String getProfile(Model model) {
        User user = userService.getAuthenticatedUser();
        model.addAttribute("editedUser", user);
        model.addAttribute("roles", roles);
        model.addAttribute("user", user);
        model.addAttribute("points", pointService.getPointsByUsername(user.getUsername()));

        return "user";
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public String create(@ModelAttribute User newUser, RedirectAttributes redirectAttributes) {
        if (newUser.getRole() == null) newUser.setRole("STUDENT");
        userRepository.save(newUser);
        redirectAttributes.addFlashAttribute("messages", "User created succesfully, please log in.");
        if (userService.getAuthenticatedUser() != null && userService.getAuthenticatedUser().getRole().equals("ADMIN")) {
            return "redirect:/users";
        }
        return "redirect:/";
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "users/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (user.getId() == id) {
            redirectAttributes.addFlashAttribute("messages", "Admin users cannot delete themselves.");
            return "redirect:/users";
        }
        userRepository.delete(id);
        redirectAttributes.addFlashAttribute("messages", "User deleted.");

        return "redirect:/users";
    }


    @Transactional
    @RequestMapping(value = "users/{id}/edit", method = RequestMethod.POST)
    public String update(@PathVariable Long id, RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String repassword) {
        
        if (password != null && !password.equals(repassword)) {
            redirectAttributes.addFlashAttribute("messages", "Passwords didn't match");
            return "redirect:/";
        }

        User loggedUser = userService.getAuthenticatedUser();
        User userToBeEdited = userRepository.getOne(id);

        String loggedRole = loggedUser.getRole();

        if (loggedRole.equals("ADMIN") || loggedUser.getId().equals(userToBeEdited.getId())) {
            if(loggedUser.getRole().equals("ADMIN") && loggedUser.getId().equals(userToBeEdited.getId()) && !loggedUser.getRole().equals(role)){
                redirectAttributes.addFlashAttribute("messages", "Admins cannot demote themselves");
            }

            if (loggedUser.getRole().equals("ADMIN")) {
                userToBeEdited.setRole(role);
            }

            if (username != null || !username.isEmpty()) userToBeEdited.setUsername(username);
            if (password != null || !password.isEmpty()) userToBeEdited.setPassword(password);


            redirectAttributes.addAttribute("id", id);
            redirectAttributes.addFlashAttribute("messages", "User modified!");
        } else {
            redirectAttributes.addFlashAttribute("messages", "User modification failed!");
        }
        

        return "redirect:/";
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String viewRegister(Model model) {
        model.addAttribute("user", userService.getAuthenticatedUser());
        return "register";
    }
}
