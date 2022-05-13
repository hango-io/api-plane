package org.hango.cloud.web.filter;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
public class CacheHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public CacheHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            body = StreamUtils.copyToByteArray(request.getInputStream());
        } catch (IOException ex) {
            body = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }
}
