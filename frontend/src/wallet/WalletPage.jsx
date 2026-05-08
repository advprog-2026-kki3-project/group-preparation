import React, { useState, useEffect } from 'react';
import { tokenStore } from "../auth/tokenStore.js";

const WalletPage = () => {
    const [wallet, setWallet] = useState(null);
    const [history, setHistory] = useState([]);
    const [topUpAmount, setTopUpAmount] = useState('');
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        const token = tokenStore.get().accessToken;
        const headers = { 'Authorization': `Bearer ${token}` };

        try {
            // Using relative paths to utilize the Vite proxy
            const [walletRes, historyRes] = await Promise.all([
                fetch('/api/wallet', { headers }),
                fetch('/api/wallet/history', { headers })
            ]);

            if (walletRes.ok && historyRes.ok) {
                setWallet(await walletRes.json());
                setHistory(await historyRes.json());
                setError(null);
            } else {
                setError("Failed to fetch data. Are you logged in?");
            }
        } catch (err) {
            console.error("Integration Error:", err);
            setError("Connection to backend lost.");
        }
    };

    const handleTopUp = async (e) => {
        e.preventDefault();
        const token = tokenStore.get().accessToken;

        try {
            const response = await fetch('/api/wallet/topup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ amount: parseInt(topUpAmount) })
            });

            if (response.ok) {
                setTopUpAmount('');
                fetchAllData(); // Refresh the UI instantly
            }
        } catch (err) {
            console.error("Top-up failed:", err);
        }
    };

    // Helper to fix the "Invalid Date" issue
    const formatDate = (dateInput) => {
        if (!dateInput) return "Just now";

        // If Spring Boot sends a date array [2026, 5, 8...], convert it
        if (Array.isArray(dateInput)) {
            const [year, month, day] = dateInput;
            return new Date(year, month - 1, day).toLocaleDateString();
        }

        const date = new Date(dateInput);
        return date.toString() === "Invalid Date" ? "Just now" : date.toLocaleDateString();
    };

    if (error) return <div className="panel"><p style={{ color: 'red' }}>{error}</p></div>;
    if (!wallet) return <div className="panel">Loading your secure wallet...</div>;

    return (
        <div style={{ padding: '20px' }}>
            {/* Balance Overview */}
            <div className="panel" style={{ marginBottom: '20px' }}>
                <h2>Wallet Balance</h2>
                <p className="muted">Account: {wallet.userId}</p>
                <div style={{ display: 'flex', gap: '40px', marginTop: '10px' }}>
                    <div>
                        <p className="eyebrow">Available</p>
                        <h2 style={{ color: '#2ecc71' }}>Rp {wallet.availableBalance.toLocaleString()}</h2>
                    </div>
                    <div>
                        <p className="eyebrow">Held (Escrow)</p>
                        <h2>Rp {wallet.heldBalance.toLocaleString()}</h2>
                    </div>
                </div>
            </div>

            {/* Top Up Section */}
            <div className="panel" style={{ marginBottom: '20px' }}>
                <h3>Quick Top Up</h3>
                <form onSubmit={handleTopUp} style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                    <input
                        type="number"
                        value={topUpAmount}
                        onChange={(e) => setTopUpAmount(e.target.value)}
                        placeholder="Amount (e.g. 50000)"
                        required
                    />
                    <button type="submit" className="primary">Add Funds</button>
                </form>
            </div>

            {/* History Table */}
            <div className="panel">
                <h3>Transaction History</h3>
                <table style={{ width: '100%', marginTop: '10px', borderCollapse: 'collapse' }}>
                    <thead>
                    <tr style={{ textAlign: 'left' }}>
                        <th style={{ padding: '8px', borderBottom: '1px solid #eee' }}>Type</th>
                        <th style={{ padding: '8px', borderBottom: '1px solid #eee' }}>Amount</th>
                        <th style={{ padding: '8px', borderBottom: '1px solid #eee' }}>Date</th>
                    </tr>
                    </thead>
                    <tbody>
                    {history.length > 0 ? (
                        history.map((tx, index) => (
                            <tr key={tx.id || index}>
                                <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                                    {/* Matches either 'type' or 'transactionType' from JSON */}
                                    <span className="badge">{tx.type || tx.transactionType || "TOP_UP"}</span>
                                </td>
                                <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                                    Rp {tx.amount.toLocaleString()}
                                </td>
                                <td style={{ padding: '8px', borderBottom: '1px solid #eee', color: '#888' }}>
                                    {formatDate(tx.createdAt || tx.date)}
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="3" style={{ padding: '20px', textAlign: 'center', color: '#888' }}>
                                No activity yet.
                                1</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default WalletPage;