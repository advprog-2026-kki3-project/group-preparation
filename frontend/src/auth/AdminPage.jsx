import { useEffect, useState } from "react";
import { authApi } from "./authApi.js";

const defaultPolicy = {
    maxConcurrentSessions: 3,
    concurrentSessionPolicy: "REVOKE_OLDEST",
    loginAttemptLimit: 5,
    loginAttemptWindowSeconds: 900,
    otpAttemptLimit: 5,
    otpTtlSeconds: 300
};

export function AdminPage({ setMessage }) {
    const [policy, setPolicy] = useState(defaultPolicy);
    const [roles, setRoles] = useState([]);
    const [permissions, setPermissions] = useState([]);
    const [roleForm, setRoleForm] = useState({ name: "", description: "" });
    const [permissionForm, setPermissionForm] = useState({ name: "", description: "" });
    const [rolePermissionForm, setRolePermissionForm] = useState({ roleId: "", permissionId: "" });
    const [userRoleForm, setUserRoleForm] = useState({ userId: "", roleId: "" });
    const [userPermissionForm, setUserPermissionForm] = useState({ userId: "", permissionId: "" });
    const [disableUserId, setDisableUserId] = useState("");
    const [busy, setBusy] = useState(false);

    useEffect(() => {
        loadAdmin();
    }, []);

    async function loadAdmin() {
        try {
            const [policyResponse, rolesResponse, permissionsResponse] = await Promise.all([
                authApi.getPolicy(),
                authApi.listRoles(),
                authApi.listPermissions()
            ]);
            setPolicy(policyResponse);
            setRoles(rolesResponse);
            setPermissions(permissionsResponse);
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        }
    }

    async function savePolicy(event) {
        event.preventDefault();
        setBusy(true);
        try {
            await authApi.updatePolicy({
                maxConcurrentSessions: Number(policy.maxConcurrentSessions),
                concurrentSessionPolicy: policy.concurrentSessionPolicy,
                loginAttemptLimit: Number(policy.loginAttemptLimit),
                loginAttemptWindowSeconds: Number(policy.loginAttemptWindowSeconds),
                otpAttemptLimit: Number(policy.otpAttemptLimit),
                otpTtlSeconds: Number(policy.otpTtlSeconds)
            });
            setMessage({ type: "success", text: "Policy saved." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    async function createRole(event) {
        event.preventDefault();
        setBusy(true);
        try {
            await authApi.createRole(roleForm);
            setRoleForm({ name: "", description: "" });
            setRoles(await authApi.listRoles());
            setMessage({ type: "success", text: "Role created." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    async function createPermission(event) {
        event.preventDefault();
        setBusy(true);
        try {
            await authApi.createPermission(permissionForm);
            setPermissionForm({ name: "", description: "" });
            setPermissions(await authApi.listPermissions());
            setMessage({ type: "success", text: "Permission created." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    async function assignPermissionToRole(event) {
        event.preventDefault();
        setBusy(true);
        try {
            await authApi.assignPermissionToRole(rolePermissionForm.roleId, rolePermissionForm.permissionId);
            setRolePermissionForm({ roleId: "", permissionId: "" });
            setMessage({ type: "success", text: "Permission assigned to role." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    async function assignRoleToUser(event) {
        event.preventDefault();
        setBusy(true);
        try {
            await authApi.assignRoleToUser(userRoleForm.userId, userRoleForm.roleId);
            setUserRoleForm({ userId: "", roleId: "" });
            setMessage({ type: "success", text: "Role assigned to user." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    async function assignPermissionToUser(event) {
        event.preventDefault();
        setBusy(true);
        try {
            await authApi.assignPermissionToUser(userPermissionForm.userId, userPermissionForm.permissionId);
            setUserPermissionForm({ userId: "", permissionId: "" });
            setMessage({ type: "success", text: "Permission assigned to user." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    async function disableUser(event) {
        event.preventDefault();
        setBusy(true);
        try {
            await authApi.disableUser(disableUserId);
            setDisableUserId("");
            setMessage({ type: "success", text: "User disabled and sessions invalidated." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    return (
        <section className="panel">
            <div className="section-heading">
                <h2>Admin</h2>
                <button className="secondary" onClick={loadAdmin}>Refresh</button>
            </div>

            <div className="grid two">
                <form className="subform" onSubmit={savePolicy}>
                    <h3>Runtime Policy</h3>
                    <label>Max Sessions
                        <input type="number" min="1" value={policy.maxConcurrentSessions} onChange={(event) => setPolicy({ ...policy, maxConcurrentSessions: event.target.value })} required />
                    </label>
                    <label>Session Policy
                        <select value={policy.concurrentSessionPolicy} onChange={(event) => setPolicy({ ...policy, concurrentSessionPolicy: event.target.value })}>
                            <option value="REVOKE_OLDEST">Revoke oldest</option>
                            <option value="REJECT_NEW">Reject new</option>
                        </select>
                    </label>
                    <label>Login Attempt Limit
                        <input type="number" min="1" value={policy.loginAttemptLimit} onChange={(event) => setPolicy({ ...policy, loginAttemptLimit: event.target.value })} required />
                    </label>
                    <label>Login Window Seconds
                        <input type="number" min="1" value={policy.loginAttemptWindowSeconds} onChange={(event) => setPolicy({ ...policy, loginAttemptWindowSeconds: event.target.value })} required />
                    </label>
                    <label>OTP Attempt Limit
                        <input type="number" min="1" value={policy.otpAttemptLimit} onChange={(event) => setPolicy({ ...policy, otpAttemptLimit: event.target.value })} required />
                    </label>
                    <label>OTP TTL Seconds
                        <input type="number" min="1" value={policy.otpTtlSeconds} onChange={(event) => setPolicy({ ...policy, otpTtlSeconds: event.target.value })} required />
                    </label>
                    <button disabled={busy}>Save Policy</button>
                </form>

                <div className="stack">
                    <form className="subform" onSubmit={createPermission}>
                        <h3>Create Permission</h3>
                        <label>Name
                            <input placeholder="auction:create" value={permissionForm.name} onChange={(event) => setPermissionForm({ ...permissionForm, name: event.target.value })} required />
                        </label>
                        <label>Description
                            <input value={permissionForm.description} onChange={(event) => setPermissionForm({ ...permissionForm, description: event.target.value })} />
                        </label>
                        <button disabled={busy}>Create Permission</button>
                    </form>

                    <form className="subform" onSubmit={createRole}>
                        <h3>Create Role</h3>
                        <label>Name
                            <input placeholder="MODERATOR" value={roleForm.name} onChange={(event) => setRoleForm({ ...roleForm, name: event.target.value })} required />
                        </label>
                        <label>Description
                            <input value={roleForm.description} onChange={(event) => setRoleForm({ ...roleForm, description: event.target.value })} />
                        </label>
                        <button disabled={busy}>Create Role</button>
                    </form>
                </div>
            </div>

            <div className="grid two">
                <SummaryList title="Roles" items={roles} renderMeta={(role) => `${role.description || "No description"}${role.systemRole ? " · system" : ""}`} />
                <SummaryList title="Permissions" items={permissions} renderMeta={(permission) => permission.description || "No description"} />
            </div>

            <div className="grid two">
                <form className="subform" onSubmit={assignPermissionToRole}>
                    <h3>Assign Permission to Role</h3>
                    <label>Role ID
                        <input value={rolePermissionForm.roleId} onChange={(event) => setRolePermissionForm({ ...rolePermissionForm, roleId: event.target.value })} required />
                    </label>
                    <label>Permission ID
                        <input value={rolePermissionForm.permissionId} onChange={(event) => setRolePermissionForm({ ...rolePermissionForm, permissionId: event.target.value })} required />
                    </label>
                    <button disabled={busy}>Assign</button>
                </form>

                <form className="subform" onSubmit={assignRoleToUser}>
                    <h3>Assign Role to User</h3>
                    <label>User ID
                        <input value={userRoleForm.userId} onChange={(event) => setUserRoleForm({ ...userRoleForm, userId: event.target.value })} required />
                    </label>
                    <label>Role ID
                        <input value={userRoleForm.roleId} onChange={(event) => setUserRoleForm({ ...userRoleForm, roleId: event.target.value })} required />
                    </label>
                    <button disabled={busy}>Assign</button>
                </form>

                <form className="subform" onSubmit={assignPermissionToUser}>
                    <h3>Assign Permission to User</h3>
                    <label>User ID
                        <input value={userPermissionForm.userId} onChange={(event) => setUserPermissionForm({ ...userPermissionForm, userId: event.target.value })} required />
                    </label>
                    <label>Permission ID
                        <input value={userPermissionForm.permissionId} onChange={(event) => setUserPermissionForm({ ...userPermissionForm, permissionId: event.target.value })} required />
                    </label>
                    <button disabled={busy}>Assign</button>
                </form>

                <form className="subform" onSubmit={disableUser}>
                    <h3>Disable User</h3>
                    <label>User ID
                        <input value={disableUserId} onChange={(event) => setDisableUserId(event.target.value)} required />
                    </label>
                    <button disabled={busy}>Disable User</button>
                </form>
            </div>
        </section>
    );
}

function SummaryList({ title, items, renderMeta }) {
    return (
        <div>
            <h3>{title}</h3>
            <div className="list">
                {items.map((item) => (
                    <div className="list-item" key={item.id}>
                        <div>
                            <p>{item.name}</p>
                            <small>{item.id} · {renderMeta(item)}</small>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}