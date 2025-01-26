package csw.subsync.user.model.role;

import csw.subsync.user.model.Permission;

import java.util.Set;

public record UserRole() implements Role {
    @Override public String name() { return "USER"; }
    @Override public Set<Permission> permissions() { return Set.of(); }
}
