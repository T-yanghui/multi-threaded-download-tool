# ForkJoinPool实现多线程下载

## 一、前言

ForkJoinPool给我们提供了分治 的解决方案，类似于大数据计算中的MapReduce。借助JDK提供的ForkJoinPool,我们可以把大型的计算任务分为小任务去执行，借助多线程的优势，更快的获取到结果。与ThreadPoolExecutor不一样的是，ForkJoinPool使用工作窃取算法，除了公共工作队列外，还维护了一个线程的工作队列，当线程空闲时，会去“窃取”繁忙线程的任务执行，提高工作效率。
当我们下载大文件时，可以使用ForkJoinPool可以将文件划分为更小的块去执行，利用多线程，建立多个Http连接，加快文件的下载速度。
## 二、准备工作
### 项目依赖:
- JDK11
- Maven

## 三、实现
### 1. 文件分块写入
文件分块写入使用RandomAccessFile实现。RandomAccessFile提供seek()方法，可以指定文件写入的位置，类比C语言中的seek()函数。由于RandomAccessFile本身不是线程安全的，我们使用ThreadLocal包装RandomAccessFile，每个线程持有各自的RandomAccessFile类。
### 2.Http连接
```java
/*
**使用JDK提供的Http连接
*/
        public static HttpURLConnection getHttpConnection(Properties properties) throws IOException {
            URL url = new URL(requestURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            properties.entrySet().forEach(
                    e -> connection.setRequestProperty(e.getKey().toString(), e.getValue().toString())
            );
            return connection;
        }

```
### 3. ForkJoinPool实现任务分块下载
```java
    private static class workers extends RecursiveAction{
    /*
    ** start end 分别代表文件分块的起始位置和结束位置
    ** end-start > threshold 则利用分支，将其拆分为更小的任务，直到
    ** end-start<= threshold
    */
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

```
### 4. 实验结果
	本地运行程序，多线程下载速度明显快于单个线程。

![ksnip_20220908-211955](https://www.qiuming.top/upload/2022/09/ksnip_20220908-211955-cca8b79103eb427ead5c4606e39d03c5.png)

[项目地址 Github](https://github.com/T-yanghui/multi-threaded-download-tool)