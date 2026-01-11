package com.cmov;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final String COM_PORT = "COM4";
    private static final String WRITE_KEY = "1PN6LGGAPLCZRAUN";
    private static final String READ_KEY = "5N7I7LBILL9TFC8M";
    private static final String CHANNEL_ID = "3224963";

    private static String lastCommand = "";

    public static void main(String[] args) throws Exception {

        SerialPort port = SerialPort.getCommPort(COM_PORT);
        port.setBaudRate(9600);
        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                2000,
                0
        );

        if (!port.openPort()) {
            System.out.println("Error opening Serial Port");
            return;
        }

        InputStream serialIn = port.getInputStream();
        OutputStream serialOut = port.getOutputStream();

        StringBuilder buffer = new StringBuilder();

        System.out.println("Active Gateway...");

        while (true) {

            // -------- LER HUMIDADE (BYTE-A-BYTE) --------
            while (serialIn.available() > 0) {
                char c = (char) serialIn.read();

                if (c == '\n') {
                    String line = buffer.toString().trim();
                    buffer.setLength(0);

                    if (line.startsWith("HUM=")) {
                        String value = line.substring(4).replaceAll("[^0-9]", "");
                        int hum = Integer.parseInt(value);
                        sendHumidity(hum);
                    }
                } else {
                    buffer.append(c);
                }
            }

            // -------- LER COMANDO --------
            String cmd = getCommand();
            if (!cmd.equals(lastCommand)) {
                serialOut.write((cmd + "\n").getBytes());
                serialOut.flush();
                lastCommand = cmd;
                System.out.println("Command sent: " + cmd);
            }

            Thread.sleep(1000);

        }
    }

    private static void sendHumidity(int hum) {
        try {
            String data = "api_key=" + WRITE_KEY + "&field1=" + hum;
            URL url = new URL("https://api.thingspeak.com/update");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
            conn.getInputStream().close();

            System.out.println("Humidity sent: " + hum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCommand() {
        try {
            URL url = new URL(
                    "https://api.thingspeak.com/channels/" + CHANNEL_ID +
                            "/fields/2/last.txt?api_key=" + READ_KEY
            );

            byte[] data = url.openStream().readAllBytes();
            String value = new String(data).trim();

            return "1".equals(value) ? "ON" : "OFF";

        } catch (Exception e) {
            return "OFF";
        }
    }
}
