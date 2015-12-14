package wepaht.repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import wepaht.domain.User;

@RestResource(exported = false)
public interface UserRepository extends JpaRepository<User, Long>{

    User findByUsername(String username);
    List findByRole(String role);
}
