/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wepaht.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wepaht.domain.Task;

/**
 *
 * @author Kake
 */
public interface TaskRepository extends JpaRepository<Task,Long> {
    
}
