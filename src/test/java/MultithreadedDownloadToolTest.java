import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author yang
 * @version 1.0.0
 * @ClassName MultithreadedDownloadToolTest.java
 * @Description TODO
 * @createTime 2022/09/08
 */
public class MultithreadedDownloadToolTest {
    private Properties properties;
    private static final String requstURL="https://publish.u-tools.cn/version2/uTools-2.1.0.exe";
    private static final String RANGEFORMATE="bytes=%s-%s";
    private MultithreadedDownloadTool multithreadedDownloadTool;
    @Before
    public void setUp() throws Exception{
        multithreadedDownloadTool=new MultithreadedDownloadTool(requstURL,"/home/yang/picTest2",10);
        properties=new Properties();
        properties.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64; rv:104.0) Gecko/20100101 Firefox/104.0");
        properties.put("Range",String.format(RANGEFORMATE,0,multithreadedDownloadTool.getFileSize()));
    }

    @Test
    public void downLoadTest() throws IOException, InterruptedException {
        multithreadedDownloadTool.downLoad();
    }
    @Test
    public void getHttpConnectionTest() throws IOException, InterruptedException {
    }
    @Test
    public void processBarTest() throws InterruptedException {
        MultithreadedDownloadTool.ProgressBar.printProgressBar(0.123455);
    }
}