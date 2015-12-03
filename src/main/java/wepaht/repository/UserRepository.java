package wepaht.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import wepaht.domain.User;

@RestResource(exported = false)
public interface UserRepository extends JpaRepository<User, Long>{

    User findByUsername(String username);
}
