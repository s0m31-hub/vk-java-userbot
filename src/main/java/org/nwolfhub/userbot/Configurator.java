package org.nwolfhub.userbot;

import org.nwolfhub.vkUser.Vk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class Configurator {
    @Bean
    public Vk vk() throws IOException {
        File tokenFile = new File("token");
        if(!tokenFile.exists()) {
            tokenFile.createNewFile();
            System.out.println("File " + tokenFile.getName() + " was created at " + tokenFile.getAbsolutePath() + ", please insert your token here");
            System.exit(3);
        }
        String token = "";
        try (FileInputStream in = new FileInputStream(tokenFile)) {
            token = new String(in.readAllBytes());
        } catch (IOException e) {
            System.out.println("Failed to read file: " + e);
        }
        Vk vk = new Vk(token);
        vk.setV("5.131");
        return vk;
    }

    @Bean (name = "statuses")
    public String[] statuses(){
        File statusFile = new File("statuses.txt");
        if(statusFile.exists()) {
            try (FileInputStream in = new FileInputStream(statusFile)) {
                return new String(in.readAllBytes()).split("\n");
            } catch (IOException ignored) {}
        } else {
            System.out.println("Autostatus will be disabled");
        }
        return new String[0];
    }
}
