package hu.webarticum.holodb.hibernate.lab;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "example")
@Access(value = AccessType.FIELD)
public class ExampleEntity {

    @Id
    @Column(name = "example_id", unique = true, nullable = false)
    //@GeneratedValue(generator = "gen")
    //@GenericGenerator(name = "gen", strategy = "foreign", parameters = { @Parameter(name = "property", value = "employee") })
    private long id;

    @Column(name = "label", nullable = false)
    private String label;
    

    public ExampleEntity() {
    }
    
    public ExampleEntity(long id, String label) {
        this.id = id;
        this.label = label;
    }

    
    public long getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return String.format("%d:%s", id, label);
    }
}
