package csw.subsync.user.dto;

import csw.subsync.user.model.Permission;
import csw.subsync.user.model.User;
import csw.subsync.user.model.role.AdminRole;
import csw.subsync.user.model.role.ManagerRole;
import csw.subsync.user.model.role.Role;
import csw.subsync.user.model.role.UserRole;

import java.util.Set;
import java.util.stream.Collectors;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String role,
        Set<String> permissions,
        String roleDescription
) {
    public UserProfileResponse(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getRole().permissions().stream()
                        .map(Permission::getPermission)
                        .collect(Collectors.toSet()),
                getRoleDescription(user.getRole())
        );
    }

    private static String getRoleDescription(Role role) {
        return switch (role) {
            case AdminRole a -> "System Administrator with full privileges";
            case ManagerRole m -> "Team Manager with resource management access";
            case UserRole u -> "Standard application user";
        };
    }
}