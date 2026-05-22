import React, { useState, useEffect } from 'react';
import { tokenStore } from "../auth/tokenStore.js";

const WalletPage = () => {
    const [wallet, setWallet] = useState(null);
    const [history, setHistory] = useState([]);

    // Top Up State
    const [topUpAmount, setTopUpAmount] = useState('');

    // Withdraw State
    const [withdrawAmount, setWithdrawAmount] = useState('');
    const [bankAccount, setBankAccount] = useState('');

    // UX State
    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        const authData = tokenStore.get();
        if (!authData || !authData.accessToken) {
            setError("You must be logged in to view your wallet.");
            return;
        }

        const token = authData.accessToken;
        const headers = { 'Authorization': `Bearer ${token}` };

        try {
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
        const token = tokenStore.get()?.accessToken;
        setError(null);

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
                showSuccess("Top-up successful!");
                fetchAllData();
            } else {
                setError("Top-up failed.");
            }
        } catch (err) {
            console.error("Top-up failed:", err);
            setError("Connection error during top-up.");
        }
    };

    const handleWithdraw = async (e) => {
        e.preventDefault();
        const token = tokenStore.get()?.accessToken;
        setError(null);

        try {
            const response = await fetch('/api/wallet/withdraw', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    amount: parseInt(withdrawAmount),
                    bankAccount: bankAccount
                })
            });

            if (response.ok) {
                setWithdrawAmount('');
                setBankAccount('');
                showSuccess(`Successfully withdrew to ${bankAccount}`);
                fetchAllData();
            } else {
                const data = await response.json();
                setError(data.error || "Withdrawal failed due to insufficient balance.");
            }
        } catch (err) {
            console.error("Withdrawal failed:", err);
            setError("Connection error during withdrawal.");
        }
    };

    const showSuccess = (msg) => {
        setSuccessMsg(msg);
        setTimeout(() => setSuccessMsg(null), 3000); // Hide after 3 seconds
    };

    const formatDate = (dateInput) => {
        if (!dateInput) return "Just now";
        if (Array.isArray(dateInput)) {
            const [year, month, day, hour, minute] = dateInput;
            return new Date(year, month - 1, day, hour || 0, minute || 0).toLocaleString();
        }
        const date = new Date(dateInput);
        return date.toString() === "Invalid Date" ? "Just now" : date.toLocaleString();
    };

    if (error && !wallet) return <div className="panel" style={{ margin: '20px' }}><p style={{ color: '#e74c3c' }}>{error}</p></div>;
    if (!wallet) return <div className="panel" style={{ margin: '20px' }}>Loading your secure wallet...</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>

            {/* Floating Notifications (so they don't break the layout) */}
            {error && <div style={{ padding: '12px 20px', backgroundColor: '#fdecea', color: '#e74c3c', borderRadius: '8px', marginBottom: '20px', fontWeight: 'bold', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>{error}</div>}
            {successMsg && <div style={{ padding: '12px 20px', backgroundColor: '#eafaf1', color: '#2ecc71', borderRadius: '8px', marginBottom: '20px', fontWeight: 'bold', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>{successMsg}</div>}

            {/* Top Span: Balance Overview */}
            <div className="panel" style={{ marginBottom: '20px', padding: '30px', borderLeft: '5px solid #3498db', backgroundColor: '#fff', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                        <h2 style={{ margin: '0 0 5px 0', color: '#2c3e50' }}>Wallet Balance</h2>
                        <p className="muted" style={{ margin: '0 0 20px 0', fontSize: '0.85em', color: '#7f8c8d' }}>Account ID: <span style={{ fontFamily: 'monospace' }}>{wallet.userId}</span></p>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                        <span style={{ backgroundColor: '#e8f4f8', color: '#2980b9', padding: '6px 12px', borderRadius: '20px', fontSize: '0.85em', fontWeight: 'bold' }}>Active Session</span>
                    </div>
                </div>

                <div style={{ display: 'flex', gap: '60px', flexWrap: 'wrap' }}>
                    <div>
                        <p className="eyebrow" style={{ color: '#95a5a6', margin: '0 0 5px 0', textTransform: 'uppercase', letterSpacing: '1px', fontSize: '0.75em', fontWeight: 'bold' }}>Available</p>
                        <h1 style={{ color: '#2ecc71', margin: 0, fontSize: '2.5em' }}>Rp {wallet.availableBalance.toLocaleString()}</h1>
                    </div>
                    <div style={{ borderLeft: '1px solid #eee', paddingLeft: '60px' }}>
                        <p className="eyebrow" style={{ color: '#95a5a6', margin: '0 0 5px 0', textTransform: 'uppercase', letterSpacing: '1px', fontSize: '0.75em', fontWeight: 'bold' }}>Held (Escrow)</p>
                        <h2 style={{ color: '#f39c12', margin: 0, fontSize: '2em' }}>Rp {wallet.heldBalance.toLocaleString()}</h2>
                    </div>
                </div>
            </div>

            {/* Grid Layout: History (Left) and Actions (Right) */}
            <div style={{ display: 'grid', gridTemplateColumns: '3fr 2fr', gap: '20px', alignItems: 'start' }}>

                {/* Left Column: Transaction History (Scrollable inside) */}
                <div className="panel" style={{ padding: '0', backgroundColor: '#fff', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)', height: '600px', display: 'flex', flexDirection: 'column' }}>
                    <div style={{ padding: '20px 20px 15px 20px', borderBottom: '1px solid #eee', backgroundColor: '#fcfcfc', borderRadius: '8px 8px 0 0' }}>
                        <h3 style={{ margin: 0, color: '#2c3e50' }}>Transaction History</h3>
                    </div>

                    {/* This div handles the internal scroll */}
                    <div style={{ overflowY: 'auto', flex: 1, padding: '0 20px 20px 20px' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead style={{ position: 'sticky', top: 0, backgroundColor: '#fff', zIndex: 1 }}>
                            <tr style={{ textAlign: 'left' }}>
                                <th style={{ padding: '15px 8px', borderBottom: '2px solid #f1f2f6', color: '#7f8c8d', fontSize: '0.85em', textTransform: 'uppercase' }}>Date & Time</th>
                                <th style={{ padding: '15px 8px', borderBottom: '2px solid #f1f2f6', color: '#7f8c8d', fontSize: '0.85em', textTransform: 'uppercase' }}>Type</th>
                                <th style={{ padding: '15px 8px', borderBottom: '2px solid #f1f2f6', color: '#7f8c8d', fontSize: '0.85em', textTransform: 'uppercase', textAlign: 'right' }}>Amount</th>
                            </tr>
                            </thead>
                            <tbody>
                            {history.length > 0 ? (
                                history.map((tx, index) => (
                                    <tr key={tx.id || index} style={{ borderBottom: '1px solid #f8f9fa', transition: 'background-color 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f8f9fa'} onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}>
                                        <td style={{ padding: '15px 8px', color: '#555', fontSize: '0.9em', whiteSpace: 'nowrap' }}>
                                            {formatDate(tx.timestamp)}
                                        </td>
                                        <td style={{ padding: '15px 8px' }}>
                                            <span style={{
                                                display: 'inline-block',
                                                backgroundColor: tx.transactionType === 'HOLD' || tx.transactionType === 'WITHDRAW' ? '#fdecea' : tx.transactionType === 'PAYMENT' ? '#fef5e7' : '#eafaf1',
                                                color: tx.transactionType === 'HOLD' || tx.transactionType === 'WITHDRAW' ? '#e74c3c' : tx.transactionType === 'PAYMENT' ? '#f39c12' : '#2ecc71',
                                                padding: '4px 10px', borderRadius: '20px', fontSize: '0.75em', fontWeight: 'bold', letterSpacing: '0.5px'
                                            }}>
                                                {tx.transactionType}
                                            </span>
                                        </td>
                                        <td style={{ padding: '15px 8px', fontWeight: 'bold', textAlign: 'right', color: tx.transactionType === 'HOLD' || tx.transactionType === 'WITHDRAW' || tx.transactionType === 'PAYMENT' ? '#e74c3c' : '#2c3e50' }}>
                                            {tx.transactionType === 'TOP_UP' || tx.transactionType === 'RELEASE' ? '+' : '-'} Rp {tx.amount.toLocaleString()}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="3" style={{ padding: '40px', textAlign: 'center', color: '#bdc3c7' }}>
                                        <div style={{ fontSize: '2em', marginBottom: '10px' }}>📁</div>
                                        No transaction activity yet.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Right Column: Top Up & Withdraw Actions (Stacked) */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

                    {/* Top Up Panel */}
                    <div className="panel" style={{ padding: '25px', backgroundColor: '#fff', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                        <h3 style={{ marginTop: 0, color: '#2c3e50', borderBottom: '2px solid #f1f2f6', paddingBottom: '10px' }}>Quick Top Up</h3>
                        <p className="muted" style={{ fontSize: '0.85em', color: '#7f8c8d', marginBottom: '20px' }}>Add funds securely to your available balance.</p>
                        <form onSubmit={handleTopUp} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                            <div style={{ position: 'relative' }}>
                                <span style={{ position: 'absolute', left: '15px', top: '50%', transform: 'translateY(-50%)', color: '#95a5a6', fontWeight: 'bold' }}>Rp</span>
                                <input
                                    type="number"
                                    min="1000"
                                    value={topUpAmount}
                                    onChange={(e) => setTopUpAmount(e.target.value)}
                                    placeholder="Amount (e.g. 50000)"
                                    style={{ width: '100%', padding: '12px 12px 12px 45px', borderRadius: '6px', border: '1px solid #dfe6e9', boxSizing: 'border-box', fontSize: '1em', outline: 'none' }}
                                    required
                                />
                            </div>
                            <button type="submit" style={{ padding: '12px', backgroundColor: '#3498db', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', fontSize: '1em', transition: 'background-color 0.2s' }} onMouseOver={(e) => e.target.style.backgroundColor = '#2980b9'} onMouseOut={(e) => e.target.style.backgroundColor = '#3498db'}>
                                Add Funds
                            </button>
                        </form>
                    </div>

                    {/* Withdraw Panel */}
                    <div className="panel" style={{ padding: '25px', backgroundColor: '#fff', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                        <h3 style={{ marginTop: 0, color: '#2c3e50', borderBottom: '2px solid #f1f2f6', paddingBottom: '10px' }}>Withdraw Funds</h3>
                        <p className="muted" style={{ fontSize: '0.85em', color: '#7f8c8d', marginBottom: '20px' }}>Transfer your available balance to a bank account.</p>
                        <form onSubmit={handleWithdraw} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                            <div style={{ position: 'relative' }}>
                                <span style={{ position: 'absolute', left: '15px', top: '50%', transform: 'translateY(-50%)', color: '#95a5a6', fontWeight: 'bold' }}>Rp</span>
                                <input
                                    type="number"
                                    min="1000"
                                    value={withdrawAmount}
                                    onChange={(e) => setWithdrawAmount(e.target.value)}
                                    placeholder="Amount to withdraw"
                                    style={{ width: '100%', padding: '12px 12px 12px 45px', borderRadius: '6px', border: '1px solid #dfe6e9', boxSizing: 'border-box', fontSize: '1em', outline: 'none' }}
                                    required
                                />
                            </div>
                            <input
                                type="text"
                                value={bankAccount}
                                onChange={(e) => setBankAccount(e.target.value)}
                                placeholder="Bank Account No."
                                style={{ width: '100%', padding: '12px 15px', borderRadius: '6px', border: '1px solid #dfe6e9', boxSizing: 'border-box', fontSize: '1em', outline: 'none' }}
                                required
                            />
                            <button type="submit" style={{ padding: '12px', backgroundColor: '#e74c3c', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', fontSize: '1em', transition: 'background-color 0.2s' }} onMouseOver={(e) => e.target.style.backgroundColor = '#c0392b'} onMouseOut={(e) => e.target.style.backgroundColor = '#e74c3c'}>
                                Confirm Withdrawal
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default WalletPage;