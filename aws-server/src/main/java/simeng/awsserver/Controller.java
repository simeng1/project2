package simeng.awsserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class Controller {

    @Autowired
    private RecordDao recordDao;

    @ResponseBody
    @RequestMapping(value="/{userId}/{day}/{timeInterval}/{stepCount}",method=RequestMethod.POST)
    public void createRecord(@PathVariable int userId, @PathVariable int day, @PathVariable int timeInterval, @PathVariable int stepCount){
        Record record = new Record();
        record.setUserId(userId);
        record.setDay(day);
        record.setTimeInterval(timeInterval);
        record.setStepCount(stepCount);
        try {
            recordDao.save(record);
        }catch (Exception e){
        }
    }

    @ResponseBody
    @RequestMapping(value="/current/{userId}",method=RequestMethod.GET)
    public int stepForRecent(@PathVariable int userId){
        List<Record> list = recordDao.findAllByUserId(userId);
        Collections.sort(list,(a, b)->b.getDay()-a.getDay());
        int count = 0;
        if (list.size()==0) return count;
        for (int i=0;i<list.size();i++){
            if (i==0 || list.get(i).getDay()==list.get(i-1).getDay()) count+=list.get(i).getStepCount();
            else break;
        }
        return count;
    }

    @ResponseBody
    @RequestMapping(value="/single/{userId}/{day}",method=RequestMethod.GET)
    public int stepForSpecific(@PathVariable int userId,@PathVariable int day){
        List<Record> list = recordDao.findAllByUserIdAndDay(userId,day);
        int count = 0;
        for (Record r:list) count+=r.getStepCount();
        return count;
    }

    @ResponseBody
    @RequestMapping(value="/range/{userId}/{startDay}/{numDays}",method=RequestMethod.GET)
    public int[] stepForRange(@PathVariable int userId,@PathVariable int startDay,@PathVariable int numDays){
        int[] steps = new int[numDays];
        for (int i=0;i<numDays;i++){
            steps[i] = stepForSpecific(userId,startDay+i);
        }
        return steps;
    }
}
