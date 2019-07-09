package net.dalamori.GMFriend.discord;

import lombok.Data;
import net.dalamori.GMFriend.config.InterpreterConfig;
import net.dalamori.GMFriend.config.Secrets;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;

@Data
@Component("discordClient")
public class DiscordClient {

    private JDABuilder jdaBuilder;
    private ListenerAdapter listenerAdapter;

    public DiscordClient(@Autowired Secrets secrets, @Autowired InterpreterConfig config) throws LoginException {
        jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setToken(secrets.getDiscordToken());

        DiscordListener listener = new DiscordListener();
        listener.setInterpreter(config.rootCommand());

        jdaBuilder.addEventListeners(listener);

        jdaBuilder.build();
    }
}
