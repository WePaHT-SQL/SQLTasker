/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wepaht.service;

import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wepaht.domain.Database;
import wepaht.domain.Table;
import wepaht.repository.DatabaseRepository;

import javax.annotation.PostConstruct;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.*;
import javax.persistence.Column;
import wepaht.domain.Task;

/**
 *
 * @author Kake
 */
@Service
public class TaskResultService {
    
    @Autowired
    private DatabaseService databaseService;
    
    public boolean evaluateSubmittedQuery(Task task, String query){
        Database database = task.getDatabase();
        Table queryResult = databaseService.performSelectQuery(database.getId(), query);
        Table correctResult = databaseService.performSelectQuery(database.getId(), task.getSolution());        
    
        if(queryResult.getColumns().size()!=correctResult.getColumns().size() ){
            return false;
        }
        if(queryResult.getRows().size()!=correctResult.getRows().size()){
            return false;
        }
//  compare the contents of the columns...
        return true;
    }
}
