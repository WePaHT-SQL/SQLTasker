package wepaht.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wepaht.domain.Database;
import wepaht.domain.PastQuery;

import java.util.Date;
import java.util.List;

public interface PastQueryRepository extends JpaRepository<PastQuery, Long>{


    List findByTaskIdAndCorrectnessAndUsername(Long taskId, boolean correctness, String username);

    List findByTaskIdAndCorrectness(Long taskId, boolean correctness);

    List findByTaskIdAndUsername(Long taskId, String username);

    List findByCorrectnessAndUsername(boolean correctness, String username);


    List findByCorrectness(boolean correctness);
    List findByTaskId(Long taskId);
    List findByUsername(String username);
}

