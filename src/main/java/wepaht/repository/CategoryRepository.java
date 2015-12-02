package wepaht.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wepaht.domain.Category;


import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>{


}
