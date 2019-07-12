package net.dalamori.GMFriend.discord;

import lombok.Data;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.config.InterpreterConfig;
import net.dalamori.GMFriend.config.Secrets;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;

@Data
@Component("discordClient")
public class DiscordClient {

    private DmFriendConfig config;
    private JDABuilder jdaBuilder;
    private ListenerAdapter listenerAdapter;

    @Autowired
    public DiscordClient(Secrets secrets, InterpreterConfig interpreter, DmFriendConfig config) throws LoginException {
        this.config = config;

        jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setToken(secrets.getDiscordToken());

        DiscordListener listener = new DiscordListener();
        listener.setInterpreter(interpreter.rootCommand());

        jdaBuilder.addEventListeners(listener);

        Activity status = Activity.listening(config.getInterpreterCommandPrefix().concat("? for help"));
        jdaBuilder.setActivity(status);

        jdaBuilder.build();

    }
}
