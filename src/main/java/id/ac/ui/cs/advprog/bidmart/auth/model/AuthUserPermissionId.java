package id.ac.ui.cs.advprog.bidmart.auth.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class AuthUserPermissionId implements Serializable {
    private UUID userId;
    private UUID permissionId;

    public AuthUserPermissionId() {
    }

    public AuthUserPermissionId(UUID userId, UUID permissionId) {
        this.userId = userId;
        this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuthUserPermissionId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, permissionId);
    }
}
