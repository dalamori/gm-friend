package net.dalamori.GMFriend;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.discord.DiscordClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"net.dalamori.GMFriend.models"})
@ComponentScan(basePackageClasses = {DmFriendConfig.class, DiscordClient.class})
public class GmFriendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmFriendApplication.class, args);
	}

}
