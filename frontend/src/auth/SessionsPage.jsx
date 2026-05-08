import { useEffect, useState } from "react";
import { authApi } from "./authApi.js";

export function SessionsPage({ setMessage }) {
    const [sessions, setSessions] = useState([]);
    const [busy, setBusy] = useState(false);

    useEffect(() => {
        loadSessions();
    }, []);

    async function loadSessions() {
        try {
            setSessions(await authApi.listSessions());
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        }
    }

    async function revoke(sessionId) {
        setBusy(true);
        try {
            await authApi.revokeSession(sessionId);
            await loadSessions();
            setMessage({ type: "success", text: "Session revoked." });
        } catch (error) {
            setMessage({ type: "error", text: error.message });
        } finally {
            setBusy(false);
        }
    }

    return (
        <section className="panel">
            <div className="section-heading">
                <h2>Active Sessions</h2>
                <button className="secondary" onClick={loadSessions}>Refresh</button>
            </div>

            {sessions.length === 0 && <div className="empty-state">No active sessions.</div>}

            <div className="list">
                {sessions.map((session) => (
                    <div className="list-item" key={session.id}>
                        <div>
                            <p>{session.userAgent || "Unknown device"}</p>
                            <small>{session.ipAddress || "Unknown IP"} · Expires {formatDate(session.expiresAt)}</small>
                        </div>
                        <button disabled={busy} className="secondary" onClick={() => revoke(session.id)}>Revoke</button>
                    </div>
                ))}
            </div>
        </section>
    );
}

function formatDate(value) {
    return value ? new Date(value).toLocaleString() : "unknown";
}