package wepaht.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wepaht.domain.Category;
import wepaht.domain.Task;
import wepaht.repository.CategoryRepository;
import wepaht.repository.TaskRepository;

import java.util.List;

@Service
public class CategoryService {


    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Adds task to categorys' task list.
     *
     * @param categoryId which category
     * @param task       which task
     */
    @Transactional
    public void setCategoryToTask(Long categoryId, Task task) {
        Category category = categoryRepository.findOne(categoryId);
        List<Task> taskList = category.getTaskList();
        if (!taskList.contains(task)) {
            taskList.add(task);
            category.setTaskList(taskList);
            task.addCategoryToList(category);
            taskRepository.save(task);
        }
    }

    public void removeTaskFromCategory(Task task) {
        List<Category> categoryList =task.getCategoryList();
        for (Category category : categoryList) {
            List<Task> taskList = category.getTaskList();
            taskList.remove(task);
            category.setTaskList(taskList);
            categoryRepository.save(category);
        }

        categoryList.clear();
        task.setCategoryList(categoryList);
        taskRepository.save(task);
    }


}
