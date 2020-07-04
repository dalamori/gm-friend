package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.UserRole;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CommandContext {

    @NotNull
    private List<@NotBlank String> command;

    @PositiveOrZero
    private int index;

    @NotBlank
    private String owner;

    @NotBlank
    private UserRole role;

    @NotBlank
    private String response;

    @NotNull
    private Map<String, Object> data = new HashMap<>();
}
