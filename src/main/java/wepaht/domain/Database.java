package wepaht.domain;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class Database {

    private String name;

    @Lob
    private String databaseSchema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabaseSchema() {
        return databaseSchema;
    }

    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }
}
