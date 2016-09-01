package cn.knight.gsw;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;


/**
 * Hello world!
 */
public class JsonClient {
    private static final Logger LOG = LoggerFactory.getLogger(JsonClient.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: method url  file");
        }
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setMaxTotal(1);
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setConnectionManager(httpClientConnectionManager);
        CloseableHttpClient httpClient = builder.build();
        String method = args[0];
        String url = args[1];
        LOG.info("method={}, url={}", method, url);
        System.out.println();
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        if(httpMethod == null) {
            for (HttpMethod temp : HttpMethod.values()) {
                LOG.info("method name={}", temp.name());
            }
        }
        CloseableHttpResponse response = null;
        try {
            switch (httpMethod) {
                case GET:
                    HttpGet httpGet = new HttpGet(url);
                    response = httpClient.execute(httpGet);
                    break;
                case POST:
                    HttpPost httpPost = new HttpPost(url);
                    String file = args[2];
                    RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
                    FileChannel fileChannel = accessFile.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
                    fileChannel.read(buffer);
                    String body = new String(buffer.array());
                    httpPost.setEntity(new StringEntity(body, Charset.forName("utf-8")));
                    LOG.info("body={}", body);
                    httpPost.addHeader(new BasicHeader("Content-Type", "application/json"));
                    response = httpClient.execute(httpPost);
                    break;
                default:
                    break;
            }
            if(response != null) {
                String responseStr = EntityUtils.toString(response.getEntity());
                LOG.info("response = {}", responseStr);
            }
        } catch (Exception e) {
            LOG.error("http request fail.", e);
        }
    }

    enum HttpMethod {
        GET,
        POST
    }
}
