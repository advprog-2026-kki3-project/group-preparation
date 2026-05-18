import { tokenStore } from "./tokenStore.js";

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  if (options.auth !== false) {
    const { accessToken } = tokenStore.get();
    if (accessToken) {
      headers.Authorization = `Bearer ${accessToken}`;
    }
  }

  const response = await fetch(path, {
    method: options.method || "GET",
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  });

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(data?.message || `Request failed with status ${response.status}`);
  }

  return data;
}

export const authApi = {
  register(body) {
    return request("/auth/register", { method: "POST", body, auth: false });
  },

  login(body) {
    return request("/auth/login", { method: "POST", body, auth: false });
  },

  verifyLogin2fa(body) {
    return request("/auth/2fa/verify", { method: "POST", body, auth: false });
  },

  me() {
    return request("/api/auth/me");
  },

  getTwoFactorSettings() {
    return request("/api/auth/2fa");
  },

  beginTwoFactor(action, method = "EMAIL_OTP") {
    const path = action === "disable" ? "/api/auth/2fa/disable" : `/api/auth/2fa/${action}`;
    const body = action === "disable" ? undefined : { method };
    return request(path, { method: "POST", body });
  },

  confirmTwoFactor(action, body) {
    return request(`/api/auth/2fa/${action}/confirm`, { method: "POST", body });
  },

  listSessions() {
    return request("/api/auth/sessions");
  },

  revokeSession(sessionId) {
    return request(`/api/auth/sessions/${sessionId}`, { method: "DELETE" });
  },

  revokeCurrentSession() {
    return request("/api/auth/sessions/current", { method: "DELETE" });
  },

  getPolicy() {
    return request("/api/auth/admin/policy");
  },

  updatePolicy(body) {
    return request("/api/auth/admin/policy", { method: "PUT", body });
  },

  listRoles() {
    return request("/api/auth/admin/roles");
  },

  createRole(body) {
    return request("/api/auth/admin/roles", { method: "POST", body });
  },

  listPermissions() {
    return request("/api/auth/admin/permissions");
  },

  createPermission(body) {
    return request("/api/auth/admin/permissions", { method: "POST", body });
  },

  assignPermissionToRole(roleId, permissionId) {
    return request(`/api/auth/admin/roles/${roleId}/permissions/${permissionId}`, { method: "POST" });
  },

  assignRoleToUser(userId, roleId) {
    return request(`/api/auth/admin/users/${userId}/roles/${roleId}`, { method: "POST" });
  },

  assignPermissionToUser(userId, permissionId) {
    return request(`/api/auth/admin/users/${userId}/permissions/${permissionId}`, { method: "POST" });
  },

  disableUser(userId) {
    return request(`/api/auth/admin/users/${userId}/disable`, { method: "POST" });
  }
};
