package wepaht.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wepaht.domain.PastQuery;
import wepaht.repository.PastQueryRepository;

import java.util.Date;
import java.util.List;


@Service
public class PastQueryService {

    @Autowired
    PastQueryRepository pastQueryRepository;

    public void saveNewPastQuery(String username, Long taskId, String query, boolean isCorrect){
        PastQuery pastQuery = new PastQuery(username,taskId,query,isCorrect);
        pastQueryRepository.save(pastQuery);

    }

    public List returnAllPastQuery(){
        return pastQueryRepository.findAll();
    }


    public List returnPastQueriesByTaskId(Long taskId){
        return pastQueryRepository.findByTaskId(taskId);
    }
    public List returnPastQueriesByUserName(String username){
        return pastQueryRepository.findByUsername(username);
    }

    public List returnAllIncorrectQueries(boolean wasCorrect){
        return pastQueryRepository.findByWasCorrect(wasCorrect);
    }
}
