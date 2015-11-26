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

    public void saveNewPastQuery(String username, Long taskId, String query, boolean wasCorrect) {
        PastQuery pastQuery = new PastQuery();
        pastQuery.setPastQuery(query);
        pastQuery.setUsername(username);
        pastQuery.setTaskId(taskId);
        pastQuery.setWasCorrect(wasCorrect);
        pastQueryRepository.save(pastQuery);

    }

    public List returnQuery(String username, Long taskId, String isCorrect) {

        if (taskId != null) {
            return pastQueryRepository.findByTaskId(taskId);
        }
        if (isCorrect.equals("true")) {
            return pastQueryRepository.findByWasCorrect(true);
        } else if (isCorrect.equals("false")) {
            return pastQueryRepository.findByWasCorrect(false);
        }
        if (!username.equals("allUsers")) {
            return pastQueryRepository.findByUsername(username);
        }
        return pastQueryRepository.findAll();
    }
}
