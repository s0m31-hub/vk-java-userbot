package org.nwolfhub.userbot;

import org.nwolfhub.vkUser.Vk;
import org.nwolfhub.vkUser.longpoll.LongPoll;
import org.nwolfhub.vkUser.longpoll.updates.NewMessageUpdate;
import org.nwolfhub.vkUser.longpoll.updates.Update;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class UpdateListener {
    public static void initialize() {
        ApplicationContext context = new AnnotationConfigApplicationContext(Configurator.class);
        Vk vk = context.getBean(Vk.class);
        UpdateHandler.initialize(vk);
        new Thread(() -> listen(vk)).start();
    }

    private static void listen(Vk vk) {
        LongPoll lp;
        try {
            lp = vk.initializeLongPoll();
        } catch (IOException e) {
            System.out.println("Failed to create longpoll instance: " + e);
            System.exit(1);
            return;
        }
        while (true) {
            try {
                for(Update preUpdate:lp.getUpdatesByType(String.valueOf(LongPoll.Type.message))) {
                    NewMessageUpdate update = new NewMessageUpdate(preUpdate);
                    System.out.println("(" + update.peer_id + ") " + update.from_id + ": " + update.text);
                    try {
                        UpdateHandler.processUpdate(update);
                    } catch (Exception e) {
                        System.out.println("Failed to fetch update: " + e);
                    }
                }
            } catch (IOException e) {
                System.out.println("Failed to fetch updates: " + e);
            }
        }
    }
}
