import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yang
 * @version 1.0.0
 * @ClassName multithreadedDownloadTool.java
 * @Description TODO
 * @createTime 2022/09/07
 */
public class MultithreadedDownloadTool {
    private static long fileSize;
    private static final long splitThreshold=6*1024*1024L;
    private static File file;
    private static ThreadLocal<RandomAccessFile> randomAccessFileThreadLocal=new ThreadLocal<>();
    private static String requestURL;
    private static int threadNum;
    private static AtomicInteger count;
    private static final String RANGEFORMATE="bytes=%s-%s";
    private static final String USERAGENT="Mozilla/5.0 (X11; Linux x86_64; rv:104.0) Gecko/20100101 Firefox/104.0";
    private static final String FILE_ACCESS_MODE="rwd";
    public MultithreadedDownloadTool(String requestURL,String storePath,int threadNum) throws IOException {
        this.requestURL=requestURL.trim();
        this.file=new File(storePath.trim());
        this.fileSize=HttpUtils.getHttpConnection(new Properties()).getContentLengthLong();
        this.threadNum=threadNum;
        this.count=new AtomicInteger(0);
    }
    public void downLoad() throws IOException, InterruptedException {
        int availableThread=Runtime.getRuntime().availableProcessors();
        ForkJoinPool forkJoinPool=new ForkJoinPool(availableThread<threadNum?availableThread:threadNum);
        forkJoinPool.submit(new workers((int)fileSize));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.DAYS);
    }
    public long getFileSize(){
        return fileSize;
    }
    public int getProcess(){
        return (int)(count.get()/fileSize);
    }
    public File getFile(){
        return this.file;
    }
    public static class HttpUtils {
        public static HttpURLConnection getHttpConnection(Properties properties) throws IOException {
            URL url = new URL(requestURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            properties.entrySet().forEach(
                    e -> connection.setRequestProperty(e.getKey().toString(), e.getValue().toString())
            );
            return connection;
        }

        public static byte[] getDataByte(HttpURLConnection httpURLConnection) throws IOException {
            InputStream inputStream = httpURLConnection.getInputStream();
            return readInputStream(inputStream);
        }

        private static byte[] readInputStream(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[1024];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            buffer = null;
            return bos.toByteArray();
        }
    }
    private static class workers extends RecursiveAction{
        private int start;
        private int end;
        public workers(int end) throws FileNotFoundException {
            this(0,end);
        }
        public workers(int start,int end) throws FileNotFoundException {
            this.start=start;
            this.end=end;
        }
        @Override
        protected void compute() {
            if(end-start<=splitThreshold){
                try {
                    Properties properties = new Properties(2);
                    properties.put("User-Agent", USERAGENT);
                    properties.put("Range", String.format(RANGEFORMATE, start, end));
                    HttpURLConnection httpURLConnection = HttpUtils.getHttpConnection(properties);
                    byte[] data= HttpUtils.getDataByte(httpURLConnection);
                    if(null==randomAccessFileThreadLocal.get()){
                        randomAccessFileThreadLocal.set(new RandomAccessFile(file,FILE_ACCESS_MODE));
//                        System.out.println("randomFile is null...fix it"+"***"+Thread.currentThread().getId());
                    }
                    randomAccessFileThreadLocal.get().seek(start);
                    randomAccessFileThreadLocal.get().write(data,0, data.length);
                    count.addAndGet(data.length);
                }catch (Exception e){
                    System.out.println("something wrong...");
                    e.printStackTrace();
                }
            }else{
                try {
                    int mid = start + (end - start >> 1);
                    workers worker1 = new workers(start, mid);
                    workers worker2 = new workers(mid, end);
                    invokeAll(worker1, worker2);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    static class ProgressBar extends Thread {
        private static final String printFormat = "progress: %.2f %%";
        /**
         * 打印进度条（长度变化）
         * @throws InterruptedException 抛出异常
         */
        static void printProgressBar(double percent) throws InterruptedException {
            percent*=100;
            String out=String.format(printFormat,percent);
            System.out.print(out);
            Thread.sleep(500);
            System.out.print("\b".repeat(out.length()));
        }

        @Override
        public void run() {
            while(true) {
                try {
                    printProgressBar( 1.0*count.get() / fileSize);
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner in=new Scanner(System.in);
        String url;
        String path;
        int threadNum;
        System.out.println("URL:");
        url=in.nextLine();
        System.out.println("store path:");
        path=in.nextLine();
        System.out.println("threadNum:");
        threadNum=in.nextInt();
        in.close();
        long startTime=System.currentTimeMillis();
        MultithreadedDownloadTool multithreadedDownloadTool=new MultithreadedDownloadTool(url,path,threadNum);
        Thread daemendThread=new Thread(new ProgressBar());
        daemendThread.setDaemon(true);
        daemendThread.start();
        multithreadedDownloadTool.downLoad();
        long endTime=System.currentTimeMillis();
        System.out.printf("\nfinished... total time: %d s",(endTime-startTime)/1000);
        System.out.println("\nfile path: "+path);
    }
}
