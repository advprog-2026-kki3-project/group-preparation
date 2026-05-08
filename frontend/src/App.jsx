import { useEffect, useMemo, useState } from "react";
import { Routes, Route, Link, useLocation } from "react-router-dom";
import { AuthPage } from "./auth/AuthPage.jsx";
import { authApi } from "./auth/authApi.js";
import { tokenStore } from "./auth/tokenStore.js";
import { CatalogPage } from "./catalog/CatalogPage.jsx";
import { AccountSecurity } from "./auth/AccountSecurity.jsx";
import { SessionsPage } from "./auth/SessionsPage.jsx";
import { AdminPage } from "./auth/AdminPage.jsx";
import AuctionPage from "./auction/AuctionPage.jsx";

export function App() {
    // TEMPORARY ACCOUNT
    const [auth, setAuth] = useState({ accessToken: "fake-mock-token" });
    const [currentUser, setCurrentUser] = useState({ principal: "local-tester", authorities: ["auth:admin"] });
    const [loadingUser, setLoadingUser] = useState(false);

    const [message, setMessage] = useState(null);
    const location = useLocation();

    const isAdmin = useMemo(
        () => currentUser?.authorities?.includes("auth:admin"),
        [currentUser]
    );

// --- TEMPORARY HACK: Comment this out so it doesn't call the missing backend ---
    /*
    useEffect(() => {
        if (!auth.accessToken) {
            setLoadingUser(false);
            return;
        }

        authApi.me()
            .then((user) => {
                setCurrentUser(user);
            })
            .catch(() => {
                tokenStore.clear();
                setAuth(tokenStore.get());
                setCurrentUser(null);
            })
            .finally(() => setLoadingUser(false));
    }, [auth.accessToken]);
    */
    function handleAuthenticated(tokens) {
        tokenStore.set(tokens);
        setAuth(tokenStore.get());
        setMessage({ type: "success", text: "Authenticated." });
    }

    function handleLogout() {
        tokenStore.clear();
        setAuth(tokenStore.get());
        setCurrentUser(null);
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

    return (
        <main className="page">
            <section className="topbar">
                <div>
                    <p className="eyebrow">BidMart</p>
                    <h1>{location.pathname.includes('/auction') ? 'Bidding Room' : 'Dashboard'}</h1>
                    <p className="muted">Signed in as {currentUser.principal}</p>
                </div>
                <button className="secondary" onClick={handleLogout}>Logout</button>
            </section>

            {message && <div className={`message ${message.type}`}>{message.text}</div>}

            <nav className="tabs">
                <Link to="/" className={location.pathname === "/" ? "active" : ""}>
                    Catalog
                </Link>
                <Link to="/security" className={location.pathname === "/security" ? "active" : ""}>
                    2FA
                </Link>
                <Link to="/sessions" className={location.pathname === "/sessions" ? "active" : ""}>
                    Sessions
                </Link>
                {isAdmin && (
                    <Link to="/admin" className={location.pathname === "/admin" ? "active" : ""}>
                        Admin
                    </Link>
                )}
            </nav>

            <Routes>
                <Route path="/" element={<CatalogPage />} />
                <Route path="/security" element={<AccountSecurity setMessage={setMessage} />} />
                <Route path="/sessions" element={<SessionsPage setMessage={setMessage} />} />
                {isAdmin && <Route path="/admin" element={<AdminPage setMessage={setMessage} />} />}
                <Route path="/auctions/:listingId" element={<AuctionPage currentUser={currentUser} />} />
            </Routes>
        </main>
    );
}