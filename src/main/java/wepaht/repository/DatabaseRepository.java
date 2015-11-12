package wepaht.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wepaht.domain.Database;

import java.util.List;

public interface DatabaseRepository extends JpaRepository<Database, Long>{

    public List<Database> findByName(String name);
}
