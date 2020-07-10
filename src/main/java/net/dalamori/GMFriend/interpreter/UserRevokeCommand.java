package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.exceptions.UserException;
import net.dalamori.GMFriend.models.User;
import net.dalamori.GMFriend.models.enums.UserRole;
import net.dalamori.GMFriend.services.UserService;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class UserRevokeCommand extends AbstractCommand {

    private UserService userService;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        String targetName = getCurrentCommandPart(context, 0);
        String gameName = getCurrentCommandPart(context, 1);

        if (context.getRole().compareTo(UserRole.ROLE_GAME_MASTER) < 0) {
            context.setResponse("Only the Game Master can remove permissions.");
        }

        if (targetName.isEmpty()) {
            context.setResponse("Revoke a role from whom?");
            return;
        }

        if (gameName.isEmpty()) {
            userService.deleteAllByOwner(targetName);
        } else {
            if (userService.exists(targetName, gameName)) {
                try {
                    User luser = userService.read(targetName, gameName);

                    userService.delete(luser);

                    context.setResponse("OK");
                } catch (UserException ex) {
                    context.setResponse("Error: " + ex.getMessage());
                }
            }
        }
    }
}

