package wepaht.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
public class PastQuery extends AbstractPersistable<Long> {

    private String username;

    private Long taskId;

    @Lob
    private String pastQuery;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    public PastQuery(String username, Long taskId , String pastQuery, boolean wasCorrect) {
        this.username = username;
        this.wasCorrect = wasCorrect;
        this.pastQuery = pastQuery;
        this.taskId = taskId;
        this.date=new Date();
    }

    private boolean wasCorrect;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getPastQuery() {
        return pastQuery;
    }

    public void setPastQuery(String pastQuery) {
        this.pastQuery = pastQuery;
    }

    public boolean isWasCorrect() {
        return wasCorrect;
    }

    public void setWasCorrect(boolean wasCorrect) {
        this.wasCorrect = wasCorrect;
    }
}
