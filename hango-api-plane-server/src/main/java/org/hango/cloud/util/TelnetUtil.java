package org.hango.cloud.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/9/22
 */
public class TelnetUtil {

    private static final Logger logger = LoggerFactory.getLogger(TelnetUtil.class);


    private static TelnetClient build(String ip, Integer port, int connectTimeout) throws IOException {
        TelnetClient telnetClient = new TelnetClient();
        telnetClient.setConnectTimeout(connectTimeout);
        telnetClient.connect(ip, port);
        return telnetClient;
    }

    private static void destroy(TelnetClient telnetClient) throws IOException {
        telnetClient.disconnect();
    }

    private static void write(OutputStream out, String command) throws IOException {
        PrintStream print = new PrintStream(out);
        print.println(command);
        print.flush();
        // print.close();
    }


    public static String read(TelnetClient telnetClient, String pattern) throws IOException {
        StringBuilder builder = new StringBuilder();
        while (telnetClient.isAvailable()) {
            int read = telnetClient.getInputStream().read();
            if (read == -1){
                break;
            }
            builder.append((char) read);
            if (builder.toString().endsWith(pattern)) {
                break;
            }
        }
        return builder.toString().replace(pattern, StringUtils.EMPTY);
    }

    /**
     * 发起telnet命令
     *
     * @param ip
     * @param port
     * @param command 命令
     * @param pattern 结束语
     * @return
     */
    public static String sendCommand(String ip, Integer port,Integer connectTimeout, String command, String pattern) {
        String info = StringUtils.EMPTY;
        try {
            logger.info("telent info , ip = {} ,port = {}", ip, port);
            TelnetClient telnetClient = build(ip, port,connectTimeout);
            write(telnetClient.getOutputStream(), command);
            info = read(telnetClient, pattern);
            destroy(telnetClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;
    }

}
