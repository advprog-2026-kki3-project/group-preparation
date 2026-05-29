package id.ac.ui.cs.advprog.bidmart.auth.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class AuthUserRoleId implements Serializable {
    private UUID userId;
    private UUID roleId;

    public AuthUserRoleId() {
    }

    public AuthUserRoleId(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuthUserRoleId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
