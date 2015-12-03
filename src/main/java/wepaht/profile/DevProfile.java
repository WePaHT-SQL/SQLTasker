/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wepaht.profile;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import wepaht.domain.User;
import wepaht.domain.Task;
import wepaht.repository.UserRepository;
import wepaht.repository.DatabaseRepository;
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

    @Autowired
    private DatabaseRepository databaseRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {

        databaseService.createDatabase("persons", "CREATE TABLE Persons"
                + "(PersonID int, LastName varchar(255), FirstName varchar(255), Address varchar(255), City varchar(255));"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)"
                + "VALUES (2, 'Raty', 'Matti', 'Rautalammintie', 'Helsinki');"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)"
                + "VALUES (1, 'Jaaskelainen', 'Timo', 'Jossakin', 'Heslinki');"
                + "INSERT INTO PERSONS (PERSONID, LASTNAME, FIRSTNAME, ADDRESS, CITY)"
                + "VALUES (3, 'Entieda', 'Kake?', 'Laiva', 'KJYR');");

        for (int i = 0; i < 10; i++) {
            taskRepository.save(randomTask());
        }

        User student = new User();
        student.setUsername("0123456789");
        student.setPassword("opiskelija");
        student.setRole("STUDENT");

        User teacher = new User();
        teacher.setUsername("avihavai");
        teacher.setPassword("vihainen");
        teacher.setRole("ADMIN");

        User assistant = new User();
        assistant.setUsername("assistant");
        assistant.setPassword("iassist");
        assistant.setRole("TEACHER");

        userRepository.save(student);
        userRepository.save(teacher);
        userRepository.save(assistant);
    }

    public Task randomTask() {
        Task task = new Task();
        task.setName(RandomStringUtils.randomAlphanumeric(10));
        task.setDescription(RandomStringUtils.randomAlphabetic(30));
        task.setDatabase(databaseRepository.findAll().get(0));
        task.setSolution("select address from persons");
        return task;
    }
}
