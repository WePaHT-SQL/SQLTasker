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


    private boolean correctness;

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

    public boolean getCorrectness() {
        return correctness;
    }

    public void setCorrectness(boolean correctness) {
        this.correctness = correctness;
    }
    public void setDate(Date date){
        this.date=date;
    }
    public Date getDate(){
        return date;
    }
}
