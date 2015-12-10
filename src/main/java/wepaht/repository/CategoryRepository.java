package wepaht.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import wepaht.domain.Category;


import java.util.List;

@RestResource(exported = false)
public interface CategoryRepository extends JpaRepository<Category, Long>{


}
