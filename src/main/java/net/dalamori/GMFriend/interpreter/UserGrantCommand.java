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
public class UserGrantCommand extends AbstractCommand {

    private UserService userService;

    // TODO: handle cross-game escalation

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        String targetName = getCurrentCommandPart(context, 0);
        String newRoleName = getCurrentCommandPart(context, 1);
        String gameName = getCurrentCommandPart(context, 2);
        UserRole newRole = UserRole.fromInputString(newRoleName);

        if (newRoleName.isEmpty()) {
            context.setResponse("Grant which role?");
            return;
        }

        if (newRole == null || context.getRole().compareTo(newRole) < 0) {
            context.setResponse("Unable grant a role to others which you yourself do not have.");
            return;
        }

        if (targetName.isEmpty()) {
            context.setResponse("Grant a role to whom?");
            return;
        }

        if (gameName.isEmpty()) {
            gameName = User.GLOBAL_GAME_ID;
        }

        try {
            if (userService.exists(targetName, gameName)) {
                User existingUser = userService.read(targetName, gameName);
                existingUser.setRole(newRole);

                userService.update(existingUser);
            } else {
                User newUser = new User();
                newUser.setRole(newRole);
                newUser.setOwner(targetName);
                newUser.setGame(gameName);

                userService.create(newUser);
            }
            context.setResponse("OK");
        } catch (UserException ex) {
            log.error("Unexpected fail while adding user role.", ex);
            context.setResponse("Internal Error while adding role.");
        }
    }
}
