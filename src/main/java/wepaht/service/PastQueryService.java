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
    public List returnQuery(String username, Long taskId, Boolean isCorrect){
            if(!taskId.equals("null")){
                return pastQueryRepository.findByTaskId(taskId);
            }

                if(isCorrect=true){
                 return   pastQueryRepository.findByWasCorrect(isCorrect);
                }else if(isCorrect=false){
                    return pastQueryRepository.findByWasCorrect(isCorrect);
                }



        return pastQueryRepository.findAll();
    }

}
