/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package profile;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import wepaht.domain.Task;
import wepaht.repository.TaskRepository;
import wepaht.service.DatabaseService;

/**
 *
 * @author Kake
 */
@Configuration
@Profile(value = {"dev", "default"})
public class DevProfile {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DatabaseService databaseService;

    @PostConstruct
    public void init(){
        
        for(int i=0; i<10; i++){
            taskRepository.save(randomTask());
        }

        databaseService.createDatabase("persons", "CREATE TABLE Persons" +
                "(PersonID int, LastName varchar(255), FirstName varchar(255), Address varchar(255), City varchar(255));" +
                "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)" +
                                "VALUES (2, 'Raty', 'Matti', 'Rautalammintie', 'Helsinki');" +
                "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)" +
                                "VALUES (1, 'Jaaskelainen', 'Timo', 'Jossakin', 'Heslinki');" +
                "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)" +
                                "VALUES (3, 'Entieda', 'Kake?', 'Laiva', 'KJYR');");
    }
    
    public Task randomTask(){
        Task task = new Task();
        task.setName(RandomStringUtils.randomAlphanumeric(10));
        task.setDescription(RandomStringUtils.randomAlphabetic(30));
        task.setStatus("Uncomplete");
        return task;
    }


}

