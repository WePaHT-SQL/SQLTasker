package wepaht.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wepaht.domain.Database;
import wepaht.domain.Table;

import java.util.*;
import javax.annotation.PostConstruct;
import wepaht.domain.PastQuery;

import wepaht.domain.Task;
import wepaht.repository.TaskRepository;

@Service
public class PointService {

    @Autowired
    PastQueryService pastQueryService;
    
    @Autowired
    TaskRepository taskRepository;

    public Integer getPointsByUsername(String username) {
        
        List pastQueries = pastQueryService.returnQuery(username, null, "true");
        if(pastQueries.isEmpty()){
            return 0;
        }
        
        List<Long> tasksCompleted = new ArrayList<>();
        
        for(Object o: pastQueries){
            PastQuery query = (PastQuery) o;
            Long taskId = query.getTaskId();
            if(!tasksCompleted.contains(taskId)){
                tasksCompleted.add(taskId);
            }
        }
        
        return tasksCompleted.size();
    }
    
    public Table getAllPoints(){
        
        List pastQueries = pastQueryService.returnQuery("allUsers", null, "true");
        if(pastQueries.isEmpty()){
            return new Table("empty");
        }
        
        Table pointsTable = new Table("points");
        List<String> columns = new ArrayList<>();
        columns.add("username");
        columns.add("points");
        pointsTable.setColumns(columns);
        pointsTable.setRows(new ArrayList<>());
        
        Map<String,List> userData = new HashMap<>();
        
        for(Object o: pastQueries){
            PastQuery query = (PastQuery) o;
            String username = query.getUsername();
            if(!userData.containsKey(username)){
                List<Long> tasksCompleted = new ArrayList<>();
                userData.put(username, tasksCompleted);
            }
            List<Long> tasksCompleted = userData.get(username);
            Long taskId = query.getTaskId();
            
            if(!tasksCompleted.contains(taskId)){
                tasksCompleted.add(taskId);
            }
        }
        
        Set<String> usernames = userData.keySet();
        for(String username: usernames){
            List<String> row = new ArrayList<>();
            row.add(username);
            row.add(""+userData.get(username).size());
            pointsTable.getRows().add(row);
        }
        return pointsTable;
    }
    

}
