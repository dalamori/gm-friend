package net.dalamori.GMFriend.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource(value = "classpath:/secrets.properties", ignoreResourceNotFound = true)
public class Secrets {

    @Value("${discord.token:DISCORD_TOKEN_NOT_SET_IN_SECRETS.PROPERTIES}")
    private String discordToken;
}
