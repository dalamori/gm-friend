package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dalamori.GMFriend.exceptions.InterpreterException;

@Data
@EqualsAndHashCode(callSuper = false)
public class InfoCommand extends AbstractCommand {

    String info;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        context.setResponse(info);
    }
}
