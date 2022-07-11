package org.nwolfhub.userbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.Response;
import org.nwolfhub.vk.requests.GroupsIsMember;
import org.nwolfhub.vk.requests.MessageSend;
import org.nwolfhub.vk.requests.UsersGet;
import org.nwolfhub.vkUser.Vk;
import org.nwolfhub.vkUser.longpoll.updates.NewMessageUpdate;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class UpdateHandler {
    private static Vk vk;

    public static void initialize(Vk vk) {
        UpdateHandler.vk = vk;
    }

    public static void who(NewMessageUpdate update, String[] parsed) throws IOException {
        Integer id = 0;
        if(parsed[1].contains("[")) {
            try {
                id = Integer.valueOf(parsed[1].split("\\[id")[1].split("\\|")[0]);
            } catch (NumberFormatException e) {
                vk.makeRequest(new MessageSend(update.peer_id, "Failed to parse id from " + parsed[1] + ": " + e));
            }
        }
        JsonObject response = JsonParser.parseString(vk.makeRequest(new UsersGet(id.toString(), "counters,common_count", "nom"))).getAsJsonObject().get("response").getAsJsonArray().get(0).getAsJsonObject();
        String regdate = "failed to obtain";
        try {
            Response r = vk.client.newCall(new Request.Builder().url("https://vk.com/foaf.php?id=" + id).build()).execute();
            String body = r.body().string();
            r.close();
            regdate = body.split("<ya:created dc:date=\"")[1].split("T")[0];
        } catch (Exception ignored){}
        System.out.println(response);
        String subs;
        try {
            subs = response.get("counters").getAsJsonObject().get("followers").getAsString();
        } catch (NullPointerException e) {
            subs = "Failed to fetch";
        }
        vk.makeRequest(new MessageSend(update.peer_id, "Report for user [id" + id + "|" + response.get("first_name").getAsString() + "]:\nVK id: " + id +
                "\nProfile closed: " + response.get("is_closed").getAsBoolean() +
                "\nCan get full profile info: " + response.get("can_access_closed").getAsBoolean() +
                "\nFriends: " + response.get("counters").getAsJsonObject().get("friends").getAsInt() +
                "\nOnline friends: " + response.get("counters").getAsJsonObject().get("online_friends").getAsInt() +
                "\nCommon (mutual) friends: " + response.get("common_count").getAsInt() +
                "\nSubscribers: " + subs +
                "\nAmount of pending friend requests (aka following): " + response.get("counters").getAsJsonObject().get("subscriptions").getAsInt() +
                "\nAmount of groups: " + response.get("counters").getAsJsonObject().get("pages").getAsInt() +
                "\nRegistration date: " + regdate +
                "\nEnd of report"));
    }

    public static void processUpdate(NewMessageUpdate update) throws IOException, InterruptedException { //getting all functions in 1 class is a shitcode, but I don't really care tbh
        String command = update.text.toLowerCase(Locale.ROOT);
        if(update.from_me) {
            String[] parsed = command.split(" ");
            //scang
            if(command.contains("scang")) {
                if(parsed.length > 3) {
                    if(parsed[0].equals("scang")) {
                        String next = "";
                        int chat = 0;
                        int user = 0;
                        String groups = "";
                        for(String arg:parsed) {
                            switch (next) {
                                case "chat" -> {
                                    chat = Integer.parseInt(arg);
                                    next = "";
                                }
                                case "user" -> {
                                    user = Integer.parseInt(arg);
                                    next = "";
                                }
                                case "groups" -> {
                                    groups = arg;
                                    next = "";
                                }
                            }
                            next = switch (arg) {
                                case "--groups", "-g" -> "groups";
                                case "--user", "-u" -> "user";
                                case "--chat", "-c" -> "chat";
                                default -> next;
                            };
                        }
                        if((chat == 0 && user==0) || groups.equals("")) vk.makeRequest(new MessageSend(update.peer_id, "Incorrect usage of scang. Detected params:\nchat: " + chat + "\nuser: " + user + "\ngroups: " + groups));
                        else {
                            System.out.println("Retrieving subscribers from " + groups);
                            StringBuilder result = new StringBuilder();
                            if(chat==0) {
                                String name = JsonParser.parseString(vk.makeRequest(new UsersGet(user))).getAsJsonObject().get("response").getAsJsonArray().get(0).getAsJsonObject().get("first_name").getAsString();
                                result.append("Report for user [id" + user + "|" + name + "]").append(":\n");
                                for(String group:groups.split(",")) {
                                    String rawResponse = vk.makeRequest(new GroupsIsMember(Integer.valueOf(group), user));
                                    JsonObject response = JsonParser.parseString(rawResponse).getAsJsonObject();
                                    Integer isMember = response.get("response").getAsInt();
                                    result.append(group).append(": ").append(isMember.toString().replace("0", "not a member").replace("1", "member"));
                                    result.append("\n");
                                }
                            } else {
                                result.append("WIP\n");
                            }
                            result.append("End of report");
                            vk.makeRequest(new MessageSend(update.peer_id, result.toString()));
                        }
                    }
                }
            }
            //ping
            if(command.equals("ping")) {
                vk.makeRequest(new MessageSend(update.peer_id, "Pong!"));
            }
            //who
            if(command.contains("who")) {
                if(parsed.length == 2) {
                    if(parsed[0].equals("who")) {
                        who(update, parsed);
                    }
                }
            }
            //chatwho
            if (command.contains("chatwho")) {
                if(parsed.length == 2) {
                    if (parsed[0].equals("chatwho")) {
                        String response = vk.makeRequest(new org.nwolfhub.vk.requests.Request("messages.getChat", "chat_id=" + Integer.valueOf(parsed[1])));
                        JsonArray users = JsonParser.parseString(response).getAsJsonObject().get("response").getAsJsonObject().get("users").getAsJsonArray();
                        for(JsonElement userElement:users) {
                            Integer id = userElement.getAsInt();
                            if(id>0) {
                                who(update, new String[]{"", "[id" + id + "|"});
                                Thread.sleep(new Random().nextInt(5000));
                            }
                        }
                        Thread.sleep(1000);
                        vk.makeRequest(new MessageSend(update.peer_id, "End of chat report"));
                    }
                }
            }
        }
    }
}
