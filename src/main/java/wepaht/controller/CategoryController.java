package wepaht.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wepaht.domain.Category;
import wepaht.domain.Task;
import wepaht.repository.CategoryRepository;
import wepaht.repository.TaskRepository;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("categories")
@Secured("ROLE_ADMIN")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String getCategories(Model model){
        model.addAttribute("categories",categoryRepository.findAll());
        model.addAttribute("tasks", taskRepository.findAll());
        return "categories";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createCategory(RedirectAttributes redirectAttributes,
                             @Valid @ModelAttribute Category category, BindingResult result,
                                 @RequestParam List<Long> taskIds){

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("messages", "Errors in field! Check name, start date and expiration date.");

            return "redirect:/categories";
        }
        if (category == null) {
            redirectAttributes.addFlashAttribute("messages", "Category creation has failed");
            return "redirect:/categories";
        }

        if (category.getExpiredDate().before(category.getStartDate())){
            redirectAttributes.addFlashAttribute("messages", "Expired date is before starting date! Creation has failed!");
            return "redirect:/categories";
        }
        List<Task> tasks = new ArrayList<>();
        for (Long taskId :taskIds){
            tasks.add(taskRepository.findOne(taskId));
        }
        category.setTaskList(tasks);
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("messages", "Category has been created!");
        return "redirect:/categories";
    }
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getCategory(@PathVariable Long id, Model model) throws Exception {
        model.addAttribute("category", categoryRepository.findOne(id));
        return "category";
    }

    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String removeCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) throws Exception {
        categoryRepository.delete(id);
        redirectAttributes.addFlashAttribute("messages", "Category deleted!");
        return "redirect:/categories";
    }


    @Transactional
    @RequestMapping(value ="/{id}/edit", method = RequestMethod.POST)
    public String updateCategory(@PathVariable Long id, RedirectAttributes redirectAttributes,
                             @RequestParam String name,
                             @RequestParam Date startDate,
                             @RequestParam Date expiredDate,
                             @RequestParam String description){
        Category oldCategory = categoryRepository.findOne(id);
        oldCategory.setName(name);
        oldCategory.setDescription(description);
        oldCategory.setExpiredDate(expiredDate);
        oldCategory.setStartDate(startDate);
        redirectAttributes.addFlashAttribute("messages", "Category modified!");
        return "redirect:/categories/{id}";
    }

    @RequestMapping(value ="/{id}/edit", method = RequestMethod.GET)
    public String getEditCategoryPage(@PathVariable Long id, Model model){
        model.addAttribute("category", categoryRepository.findOne(id));

        return "categoryEdit";
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
    }
}
