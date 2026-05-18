package id.ac.ui.cs.advprog.bidmart.auth.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class AuthRolePermissionId implements Serializable {
    private UUID roleId;
    private UUID permissionId;

    public AuthRolePermissionId() {
    }

    public AuthRolePermissionId(UUID roleId, UUID permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuthRolePermissionId that)) {
            return false;
        }
        return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}
