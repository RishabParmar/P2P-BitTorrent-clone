package Utils;

import java.io.*;

public class FileUtils {
    public static void splitFile(String inputFile, String tmpPath) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            // Original large file
            fis = new FileInputStream(inputFile);
            System.out.println("The total file size: " + fis.getChannel().size() + " and the number of pieces possible are:" + (fis.getChannel().size() / 32678 ));
            // File read cache
            byte[] buffer = new byte[Constants.PIECE_SIZE_IN_BYTES];
            int len = 0;

            // File count after cutting (also file name)
            int fileNum = 0;

            // Large files cut into small files
            while ((len = fis.read(buffer)) != -1) {
                fos = new FileOutputStream(tmpPath + "/" + fileNum);
                fos.write(buffer, 0, len);
                fos.close();
                fileNum++;
            }
            System.out.println("Split file" + inputFile + "Complete, total build" + fileNum + "Files");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void mergeFile() {
        String tmpPath = Constants.FILE_DIRECTORY_PATH;
        String outputPath = Constants.FILE_DIRECTORY_PATH;
        Integer bufferSize = Constants.PIECE_SIZE_IN_BYTES*Constants.FILE_PIECES_COUNT;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            // Get the number of small files to cut
            File tempFilePath = new File(tmpPath);
            File[] files = tempFilePath.listFiles();
            if (files == null) {
                System.out.println("No file.");
                return;
            }
            int fileNum = files.length;

            // Restored large file path
            String outputFile = outputPath + "/result.jpeg";
            fos = new FileOutputStream(outputFile);

            // File read cache
            byte[] buffer = new byte[bufferSize];
            int len = 0;

            // Restore all cut small files to one large file
            for (int i = 0; i < fileNum; i++) {
                fis = new FileInputStream(tmpPath + "/" + i);
                len = fis.read(buffer);
                fos.write(buffer, 0, len);
            }
            System.out.println("Merge catalog file:" + tmpPath + "Complete, the generated file is:" + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void createFile(byte[] bytes, int index, int offset) {
        try {
            FileOutputStream out = new FileOutputStream(Constants.FILE_DIRECTORY_PATH + "/" + index);
            out.write(bytes, offset, bytes.length-offset);
        } catch (Exception e) {
            System.out.println("Error writing file! " + e);
        }
    }

    public static byte[] getBytes(int index) {
        byte[] buffer = null;
        try {
            FileInputStream in = new FileInputStream(Constants.FILE_DIRECTORY_PATH + "/" + index);
            buffer = new byte[(int)in.getChannel().size()];
            in.read(buffer);
        } catch (Exception e) {
            System.out.println("Error getting file bytes! " + e);
        }
        return buffer;
    }

//    public static void main(String[] args) {
//        FileSplitter fs = new FileSplitter();
//        fs.splitFile("image.jpeg", "paths", 32768);
//        fs.mergeFile("paths", "paths", 10 * 1024 * 1024);
//    }
}
