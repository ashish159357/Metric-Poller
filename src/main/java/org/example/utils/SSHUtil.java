package org.example.utils;


import com.jcraft.jsch.*;

import java.io.InputStream;

public class SSHUtil {

    public static int executeCommand(String username, String host, String password, String command) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            // Create SSH session
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);

            // Avoid asking for key confirmation
            session.setConfig("StrictHostKeyChecking", "no");

            // Connect to the server
            session.connect();

            // Open SSH channel
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // Get the input stream
            InputStream in = channel.getInputStream();

            // Connect the channel
            channel.connect();

            // Read the output from the command
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("Exit status: " + channel.getExitStatus());
                    return channel.getExitStatus();
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return 0;
    }
}
