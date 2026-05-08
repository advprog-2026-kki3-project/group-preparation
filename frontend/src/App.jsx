import { useEffect, useMemo, useState } from "react";
import { AuthPage } from "./auth/AuthPage.jsx";
import { authApi } from "./auth/authApi.js";
import { tokenStore } from "./auth/tokenStore.js";
import { CatalogPlaceholder } from "./catalog/CatalogPlaceholder.jsx";
import { AccountSecurity } from "./auth/AccountSecurity.jsx";
import { SessionsPage } from "./auth/SessionsPage.jsx";
import { AdminPage } from "./auth/AdminPage.jsx";

const tabs = [
  { id: "catalog", label: "Catalog" },
  { id: "security", label: "2FA" },
  { id: "sessions", label: "Sessions" },
  { id: "admin", label: "Admin", adminOnly: true }
];

export function App() {
  const [auth, setAuth] = useState(() => tokenStore.get());
  const [currentUser, setCurrentUser] = useState(null);
  const [activeTab, setActiveTab] = useState("catalog");
  const [message, setMessage] = useState(null);
  const [loadingUser, setLoadingUser] = useState(Boolean(auth.accessToken));

  const isAdmin = useMemo(
    () => currentUser?.authorities?.includes("auth:admin"),
    [currentUser]
  );

  useEffect(() => {
    if (!auth.accessToken) {
      setLoadingUser(false);
      return;
    }

    authApi.me()
      .then((user) => {
        setCurrentUser(user);
        setActiveTab("catalog");
      })
      .catch(() => {
        tokenStore.clear();
        setAuth(tokenStore.get());
        setCurrentUser(null);
      })
      .finally(() => setLoadingUser(false));
  }, [auth.accessToken]);

  function handleAuthenticated(tokens) {
    tokenStore.set(tokens);
    setAuth(tokenStore.get());
    setMessage({ type: "success", text: "Authenticated." });
  }

  function handleLogout() {
    tokenStore.clear();
    setAuth(tokenStore.get());
    setCurrentUser(null);
    setActiveTab("catalog");
    setMessage({ type: "success", text: "Logged out." });
  }

  if (loadingUser) {
    return <main className="page narrow"><div className="panel">Loading...</div></main>;
  }

  if (!auth.accessToken || !currentUser) {
    return (
      <AuthPage
        message={message}
        setMessage={setMessage}
        onAuthenticated={handleAuthenticated}
      />
    );
  }

  const visibleTabs = tabs.filter((tab) => !tab.adminOnly || isAdmin);

  return (
    <main className="page">
      <section className="topbar">
        <div>
          <p className="eyebrow">BidMart</p>
          <h1>Catalog</h1>
          <p className="muted">Signed in as {currentUser.principal}</p>
        </div>
        <button className="secondary" onClick={handleLogout}>Logout</button>
      </section>

      {message && <div className={`message ${message.type}`}>{message.text}</div>}

      <nav className="tabs">
        {visibleTabs.map((tab) => (
          <button
            key={tab.id}
            className={activeTab === tab.id ? "active" : ""}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {activeTab === "catalog" && <CatalogPlaceholder />}
      {activeTab === "security" && <AccountSecurity setMessage={setMessage} />}
      {activeTab === "sessions" && <SessionsPage setMessage={setMessage} />}
      {activeTab === "admin" && isAdmin && <AdminPage setMessage={setMessage} />}
    </main>
  );
}
