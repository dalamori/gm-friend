/*
    GameMaster's Friend - a mechanics-agnostic tabletop gaming assistant Discord bot.
    Copyright (C) 2019  A. Koelewyn - https://github.com/dalamori

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.dalamori.GMFriend;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.discord.DiscordClient;
import net.dalamori.GMFriend.services.SimpleCrudeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"net.dalamori.GMFriend.models"})
@ComponentScan(basePackageClasses = {DmFriendConfig.class, DiscordClient.class, SimpleCrudeService.class})
public class GmFriendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmFriendApplication.class, args);
	}

}
