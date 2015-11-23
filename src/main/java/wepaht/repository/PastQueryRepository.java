package wepaht.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wepaht.domain.Database;
import wepaht.domain.PastQuery;

import java.util.Date;
import java.util.List;

public interface PastQueryRepository extends JpaRepository<PastQuery, Long>{

    List findByTaskId(Long taskId);
    List findByUsername(String username);



    List findByWasCorrect(boolean wasCorrect);
}
