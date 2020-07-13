package net.dalamori.GMFriend.interpreter;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dalamori.GMFriend.models.enums.UserRole;

@Data
@AllArgsConstructor
public class MapSubcommand {
    private UserRole requiredRole;
    private String helpHint;
    private AbstractCommand command;
}
