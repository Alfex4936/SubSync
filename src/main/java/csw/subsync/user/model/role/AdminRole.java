package csw.subsync.user.model.role;

import csw.subsync.user.model.Permission;

import java.util.Set;

// Role implementations
public record AdminRole() implements Role {
    @Override public String name() { return "ADMIN"; }
    @Override public Set<Permission> permissions() {
        return Set.of(
                Permission.ADMIN_READ,
                Permission.ADMIN_UPDATE,
                Permission.ADMIN_CREATE,
                Permission.ADMIN_DELETE,
                Permission.MANAGER_READ,
                Permission.MANAGER_UPDATE,
                Permission.MANAGER_CREATE,
                Permission.MANAGER_DELETE
        );
    }
}
