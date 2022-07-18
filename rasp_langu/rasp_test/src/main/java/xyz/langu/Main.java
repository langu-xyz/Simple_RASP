package xyz.langu;

import java.io.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Main.main() in test project");

        for(int i = 1; i < 100; i++){
            Thread.sleep(3000);
            System.out.println("main start");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("ping", "www.baidu.com");
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            System.out.println(bufferedReader.readLine());

        }

        System.out.println("Main().main in test project exit()");
    }
}
