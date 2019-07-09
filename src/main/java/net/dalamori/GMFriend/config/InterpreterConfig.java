package net.dalamori.GMFriend.config;

import lombok.Data;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.InfoCommand;
import net.dalamori.GMFriend.interpreter.MapCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class InterpreterConfig {

    private AbstractCommand rootCommand;

    @Bean
    public AbstractCommand rootCommand() {
        if (rootCommand == null) {
            InfoCommand pong = new InfoCommand();
            pong.setInfo("Pong!");

            MapCommand root = new MapCommand();
            root.getMap().put(";;ping", pong);

            rootCommand = root;
        }

        return rootCommand;
    }
}
