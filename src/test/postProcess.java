package test;

import java.io.*;

public class postProcess {
    public static void main(String[] args) {
        String prefixPath = "Datasets/Timestamp_data/Node_6_Device_10_STK_0.15/"; // 替换为实际文件路径
        int linesToRead = 100000; // 需要读取的行数

        for (int i = 0; i < 60; i++) {
            String filePath = prefixPath + i + ".txt";
            boolean flag = false;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + ".tmp"))) {

                String line;
                int lineCount = 0;

                while ((line = reader.readLine()) != null && lineCount < linesToRead) {
                    // 处理每一行的内容
                    writer.write(line);
                    writer.newLine();
                    lineCount++;
                }
                if ((line = reader.readLine()) == null && lineCount<linesToRead){
                    System.out.println("error" + i);
                    continue;
                }

//            // 处理剩余的行数
//            while ((line = reader.readLine()) != null) {
//                writer.write(line);
//                writer.newLine();
//            }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 删除原始文件，并将临时文件重命名为原始文件
            File originalFile = new File(filePath);
            File tempFile = new File(filePath + ".tmp");
            if (tempFile.renameTo(originalFile)) {
                System.out.println("File updated successfully."+ i);
            } else {
                System.out.println("Error updating file."+ i);
            }
        }
    }
}

