package wepaht.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wepaht.domain.User;

public interface UserRepository extends JpaRepository<User, Long>{

    User findByUsername(String username);
}
