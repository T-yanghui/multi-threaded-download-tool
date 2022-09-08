import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * @author yang
 * @version 1.0.0
 * @ClassName multithreadedDownloadTool.java
 * @Description TODO
 * @createTime 2022/09/07
 */
public class MultithreadedDownloadTool {
    private static long fileSize;
    private static final long splitThreshold=1L*1024L*1024L;
    private static RandomAccessFile randomAccessFile;
    private static String requestURL;
    private static int threadNum;
    private static final String RANGEFORMATE="bytes=%s-%s";
    private static final String USERAGENT="Mozilla/5.0 (X11; Linux x86_64; rv:104.0) Gecko/20100101 Firefox/104.0";
    private static final String FILE_ACCESS_MODE="rwd";
    public MultithreadedDownloadTool(String requestURL,String storePath,int threadNum) throws IOException {
        this.requestURL=requestURL.trim();
        File file=new File(storePath.trim());
        this.randomAccessFile=new RandomAccessFile(file,FILE_ACCESS_MODE);
        this.fileSize=HttpUtils.getHttpConnection(new Properties()).getContentLength();
        System.out.println(fileSize);
        this.randomAccessFile.setLength(fileSize);
        this.threadNum=threadNum;
    }
    public void downLoad() throws IOException {
        int availableThread=Runtime.getRuntime().availableProcessors();
        ForkJoinPool forkJoinPool=new ForkJoinPool(availableThread<threadNum?availableThread:threadNum);
        forkJoinPool.submit(new workers((int)fileSize));
        forkJoinPool.shutdown();
        randomAccessFile.close();
    }
    public long getFileSize(){
        return fileSize;
    }
    public RandomAccessFile getRandomAccessFile(){
        return randomAccessFile;
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
        private int count;
        public workers(int end){
           this(0,end);
        }
        public workers(int start,int end){
            this.start=start;
            this.end=end;
            this.count=0;
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
                   randomAccessFile.write(data,start, data.length);
               }catch (Exception e){
                   System.out.println("something wrong...");
                   e.printStackTrace();
               }
           }else{
               int mid=start+(end-start>>1);
               workers worker1=new workers(start,mid);
               workers worker2=new workers(mid+1,end);
               worker1.fork();
               worker2.fork();
           }
        }
    }

    public static void main(String[] args) throws IOException {
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
        MultithreadedDownloadTool multithreadedDownloadTool=new MultithreadedDownloadTool(url,path,threadNum);
        multithreadedDownloadTool.downLoad();
    }
}
