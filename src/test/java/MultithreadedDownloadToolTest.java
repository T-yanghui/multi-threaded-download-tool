import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.Properties;

/**
 * @author yang
 * @version 1.0.0
 * @ClassName MultithreadedDownloadToolTest.java
 * @Description TODO
 * @createTime 2022/09/08
 */
public class MultithreadedDownloadToolTest {
    private Properties properties;
    private static final String URL="http://download.manjaro.org/kde/21.3.7/manjaro-kde-21.3.7-220816-linux515.iso";
    private static final String RANGEFORMATE="bytes=%s-%s";
    private MultithreadedDownloadTool multithreadedDownloadTool;
    @Before
    public void setUp() throws Exception{
        //multithreadedDownloadTool=new MultithreadedDownloadTool(URL,"/home/yang/picTest",1);
        properties=new Properties();
        properties.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64; rv:104.0) Gecko/20100101 Firefox/104.0");
//        properties.put("Range",String.format(RANGEFORMATE,0,multithreadedDownloadTool.getFileSize()));
    }

    @Test
    public void downLoadTest() {
    }
    @Test
    public void getHttpConnectionTest() throws IOException {
        HttpURLConnection conn=MultithreadedDownloadTool.HttpUtils.getHttpConnection(properties);
        System.out.println(conn.getContentLength());
        System.out.println(conn.getContent());
//        System.out.println(multithreadedDownloadTool.getFileSize());
//        byte[] data=MultithreadedDownloadTool.HttpUtils.getDataByte(conn);
//        System.out.println(data.length);
//        RandomAccessFile randomAccessFile=multithreadedDownloadTool.getRandomAccessFile();
//        randomAccessFile.write(data,0,data.length);
//        randomAccessFile.close();
    }
}