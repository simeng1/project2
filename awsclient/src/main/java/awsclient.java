import com.csvreader.CsvWriter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class awsclient {
    private static int numberOfThreads;
    private static String ipAddress;
    private static int day;
    private static int population;
    private static int iteration;
    private static String URL;
    private static int responseReceived;
    private static Vector<Double> responseTime;
    private static int timeStart;
    private static int timeEnd;
    private static long record0;
    private static long record1;
    private static long record2;
    private static long record3;
    private static long record4;
    private static Map<Integer, Integer> timeRecord;

    public static void main(String[] args) {
        numberOfThreads = Integer.parseInt(args[0]);
        ipAddress = args[1];
        day = Integer.parseInt(args[2]);
        population = Integer.parseInt(args[3]);
        iteration = Integer.parseInt(args[4]);
        timeRecord = new HashMap<>();
        URL = "http://" + ipAddress + ":8080";
        responseTime = new Vector<Double>();
        record0 = System.currentTimeMillis();
        warmup();
        loading();
        peak();
        cooldown();
        int maxNumOfRequest = 5 * (int) (numberOfThreads * 11 + Math.ceil(numberOfThreads * 0.1) * 3
                + Math.ceil(numberOfThreads * 0.5) * 5 + Math.ceil(numberOfThreads * 0.25) * 5) * iteration;
        System.out.println("Response rate: " + (double) responseReceived / maxNumOfRequest);
        double wallTime = (record4 - record0) / 1000.0;
        System.out.println("Test Wall Time: " + wallTime + " seconds.");
        System.out.println("Overall throughput across all phases: " + maxNumOfRequest / wallTime);
        Collections.sort(responseTime);
        double sum = 0;
        for (double d : responseTime) sum += d;
        double mean = sum / responseTime.size();
        double median = 0;
        if (responseTime.size() % 2 == 1) {
            median = responseTime.get(responseTime.size() / 2);
        } else {
            median = (responseTime.get(responseTime.size() / 2 - 1) + responseTime.get(responseTime.size() / 2)) / 2;
        }
        System.out.println("Mean latencies: " + mean + " seconds");
        System.out.println("Median latencies: " + median + " seconds");
        System.out.println("95th percentile latency: " + responseTime.get((int) Math.floor(responseTime.size() * 0.95)) + " seconds");
        System.out.println("99th percentile latency: " + responseTime.get((int) Math.floor(responseTime.size() * 0.99)) + " seconds");
        String csvFilePath = "C:\\output\\128load.csv";
        try {
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.defaultCharset());
            int second = 0;
            while (!timeRecord.isEmpty()) {
                int count = 0;
                if (timeRecord.containsKey(second)) {
                    count = timeRecord.get(second);
                    timeRecord.remove(second);
                }
                String[] csvContent = {second + "", count + ""};
                csvWriter.writeRecord(csvContent);
                second++;
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized static void responseIncrease() {
        responseReceived++;
        int current = (int) ((System.currentTimeMillis() - record0) / 1000);
        timeRecord.put(current, timeRecord.getOrDefault(current, 0) + 1);
        if (responseReceived%100==0) System.out.println(responseReceived);
    }

    public static void warmup() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        timeStart = 0;
        timeEnd = 2;
        for (int i = 0; i < numberOfThreads * 0.1; i++) {
            MyThread myThread = new MyThread();
            cachedThreadPool.submit((myThread));
        }
        cachedThreadPool.shutdown();
        try {
            cachedThreadPool.awaitTermination(6000, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        record1 = System.currentTimeMillis();
        System.out.println("Warmup phase complete: Time " + (record1 - record0) / 1000.0 + " seconds.");
    }

    public static void loading() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        timeStart = 3;
        timeEnd = 7;
        for (int i = 0; i < numberOfThreads * 0.5; i++) {
            MyThread myThread = new MyThread();
            cachedThreadPool.submit((myThread));
        }
        cachedThreadPool.shutdown();
        try {
            cachedThreadPool.awaitTermination(6000, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        record2 = System.currentTimeMillis();
        System.out.println("Loading phase complete: Time " + (record2 - record1) / 1000.0 + " seconds");
    }

    public static void peak() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        timeStart = 8;
        timeEnd = 18;
        for (int i = 0; i < numberOfThreads * 1; i++) {
            MyThread myThread = new MyThread();
            cachedThreadPool.submit((myThread));
        }
        cachedThreadPool.shutdown();
        try {
            cachedThreadPool.awaitTermination(6000, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        record3 = System.currentTimeMillis();
        System.out.println("Peak phase complete: Time " + (record3 - record2) / 1000.0 + " seconds.");
    }

    public static void cooldown() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        timeStart = 19;
        timeEnd = 23;
        for (int i = 0; i < numberOfThreads * 0.25; i++) {
            MyThread myThread = new MyThread();
            cachedThreadPool.submit((myThread));
        }
        cachedThreadPool.shutdown();
        try {
            cachedThreadPool.awaitTermination(6000, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        record4 = System.currentTimeMillis();
        System.out.println("Cooldown phase complete: Time " + (record4 - record3) / 1000.0 + " seconds.");
    }

    public static class MyThread extends Thread {
        public void run() {
            Random rand = new Random();
            for (int i = 0; i < iteration * (timeEnd - timeStart + 1); i++) {
                int randomUserId1 = rand.nextInt(population);
                int randomTimeInterval1 = timeStart + rand.nextInt(timeEnd - timeStart + 1);
                int randomStepCount1 = rand.nextInt(5001);
                sendPostRequest(randomUserId1, 1, randomTimeInterval1, randomStepCount1);
                int randomUserId2 = rand.nextInt(population);
                int randomTimeInterval2 = timeStart + rand.nextInt(timeEnd - timeStart + 1);
                int randomStepCount2 = rand.nextInt(5001);
                sendPostRequest(randomUserId2, 1, randomTimeInterval2, randomStepCount2);
                sendGetCurrentRequest(randomUserId1);
                sendGetSingleRequest(randomUserId2, 1);
                int randomUserId3 = rand.nextInt(population);
                int randomTimeInterval3 = timeStart + rand.nextInt(timeEnd - timeStart + 1);
                int randomStepCount3 = rand.nextInt(5001);
                sendPostRequest(randomUserId3, 1, randomTimeInterval3, randomStepCount3);
            }
        }
    }

    public static void sendGetCurrentRequest(int userId) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpGet httpGetRequest = new HttpGet(URL + "/current/" + userId);
            long timeBefore = System.currentTimeMillis();
            HttpResponse httpResponse = httpClient.execute(httpGetRequest);
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code >= 200 && code < 300) {
                long timeAfter = System.currentTimeMillis();
                responseIncrease();
                responseTime.add((timeAfter - timeBefore) / 1000.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
            }
        }
    }

    public static void sendGetSingleRequest(int userId, int day) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpGet httpGetRequest = new HttpGet(URL + "/single/" + userId + "/" + day);
            long timeBefore = System.currentTimeMillis();
            HttpResponse httpResponse = httpClient.execute(httpGetRequest);
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code >= 200 && code < 300) {
                long timeAfter = System.currentTimeMillis();
                responseIncrease();
                responseTime.add((timeAfter - timeBefore) / 1000.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
            }
        }
    }

    public static void sendPostRequest(int userId, int day, int timeInterval, int stepCount) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost httpPostRequest = new HttpPost(URL + "/" + userId + "/" + day + "/" + timeInterval + "/" + stepCount);
            long timeBefore = System.currentTimeMillis();
            HttpResponse httpResponse = httpClient.execute(httpPostRequest);
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code >= 200 && code < 300) {
                long timeAfter = System.currentTimeMillis();
                responseIncrease();
                responseTime.add((timeAfter - timeBefore) / 1000.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
            }
        }
    }

}