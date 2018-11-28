package simeng.awsserver;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordDao extends JpaRepository<Record, Integer> {
    public List<Record> findAllByUserId(int userId);
    public List<Record> findAllByUserIdAndDay(int userId,int day);

}
