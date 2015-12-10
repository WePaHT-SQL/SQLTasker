package wepaht.domain;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class AuthenticationToken extends AbstractPersistable<Long>{

    @Column(unique = true)
    private String token;

    @OneToOne
    private User user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = RandomStringUtils.randomAlphabetic(30);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
