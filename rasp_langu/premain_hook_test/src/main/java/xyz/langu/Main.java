package xyz.langu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("main start");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("ping", "www.baidu.com");
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        System.out.println(bufferedReader.readLine());
    }
}
