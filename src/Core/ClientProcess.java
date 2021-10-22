package Core;

import java.io.*;
import java.net.Socket;

public class ClientProcess {
    public static final String outputFilePath = "src\\Output\\result_image.JPG";
    public static void main(String[] args) {
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        try{
            socket = new Socket("localhost", 8089);
            System.out.println("Connection Established!");
        } catch (Exception e) {
            System.out.println("Error establishing connection to server!");
            return;
        }
        try {
            in = socket.getInputStream();
        } catch (IOException ex) {
            System.out.println("Can't get socket input stream. ");
        }

        try {
            File f = new File(outputFilePath);
            f.createNewFile();
            out = new FileOutputStream(outputFilePath);
        } catch (Exception ex) {
            System.out.println("File not found. ");
        }

        try{
            byte[] bytes = new byte[10*1024];
            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
        } catch (Exception e) {
            System.out.println("Error getting file!!!");
        }

        try {
            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Error closing connection!!!");
        }
    }
}