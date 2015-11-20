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
    
    public boolean evaluateSubmittedQueryStrictly(Task task, String query){
        Database database = task.getDatabase();
        Table queryResult = databaseService.performSelectQuery(database.getId(), query);
        Table correctResult = databaseService.performSelectQuery(database.getId(), task.getSolution());        
    

        return compareColumns(queryResult.getColumns(), correctResult.getColumns()) &&
                compareRows(queryResult.getRows(), correctResult.getRows());
    }

    private boolean compareColumns(List<String> query, List<String> answer) {
        if (query.size()!=answer.size()) return false;

        for (String column : answer) {
            if (!query.contains(column)) return false;
        }

        return true;
    }

    private boolean compareRows(List<List<String>> query, List<List<String>> answer) {
        if (query.size() != answer.size() || query.get(0).size() != answer.get(0).size()) return false;

        for (int i = 0; i < answer.size(); i++) {
            for (String cell : answer.get(i)) {
                if (!query.get(i).contains(cell)) return false;
            }
        }

        return true;
    }
}
