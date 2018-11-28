package simeng.awsserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "records")
public class Record {

    @Id
    @GeneratedValue
    @Column(name = "record_id", nullable = false)
    private int recordId;

    @Basic
    @Column(name = "user_id", nullable = false)
    private int userId;

    @Basic
    @Column(name = "day", nullable = false)
    private int day;

    @Basic
    @Column(name = "time_interval", nullable = false)
    private int timeInterval;

    @Basic
    @Column(name = "step_count", nullable = false)
    private int stepCount;
}
