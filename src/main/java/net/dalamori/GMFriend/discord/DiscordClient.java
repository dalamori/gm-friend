package net.dalamori.GMFriend.discord;

import lombok.Data;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@Data
public class DiscordClient {

    private JDABuilder jdaBuilder;
    private ListenerAdapter listenerAdapter;

    public DiscordClient() {
        jdaBuilder = new JDABuilder(AccountType.BOT);

    }
}
