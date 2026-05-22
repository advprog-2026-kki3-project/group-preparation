import React, { useEffect, useState } from 'react';
import { tokenStore } from "../auth/tokenStore.js";

const ledgerShell = {
    display: 'grid',
    gap: '24px',
    color: '#182a2a'
};

const framedPanel = {
    border: '1px solid #c8b57d',
    borderRadius: '4px',
    background: '#fffdf8',
    boxShadow: '0 18px 40px -28px rgba(24, 42, 42, 0.45)'
};

const panelHeader = {
    padding: '18px 22px',
    borderBottom: '1px solid #dfd2ab',
    background: 'linear-gradient(90deg, #fffaf0, #f8f0db)'
};

const fieldWrap = {
    position: 'relative'
};

const currencyMark = {
    position: 'absolute',
    left: '14px',
    top: '50%',
    transform: 'translateY(-50%)',
    color: '#8a6f2f',
    fontWeight: 700
};

const moneyInput = {
    paddingLeft: '46px',
    borderColor: '#d8c48a',
    background: '#fffdf8'
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

const transactionStyle = (type) => {
    if (type === 'TOP_UP' || type === 'RELEASE') {
        return { backgroundColor: '#edf7f0', color: '#275c3f', borderColor: '#b9d4bf' };
    }
    if (type === 'PAYMENT') {
        return { backgroundColor: '#fff4d6', color: '#7a5b13', borderColor: '#dfc77d' };
    }
    return { backgroundColor: '#f9eaea', color: '#893f3f', borderColor: '#deb8b8' };
};

const WalletPage = () => {
    const [wallet, setWallet] = useState(null);
    const [history, setHistory] = useState([]);
    const [topUpAmount, setTopUpAmount] = useState('');
    const [withdrawAmount, setWithdrawAmount] = useState('');
    const [bankAccount, setBankAccount] = useState('');
    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        const authData = tokenStore.get();
        if (!authData?.accessToken) {
            setError("You must be logged in.");
            return;
        }

        const headers = { Authorization: `Bearer ${authData.accessToken}` };

        try {
            const [walletRes, historyRes] = await Promise.all([
                fetch('/api/wallet', { headers }),
                fetch('/api/wallet/history', { headers })
            ]);

            if (walletRes.status === 403) {
                const errorText = await walletRes.text();
                if (errorText.includes("2FA_REQUIRED")) {
                    setError("2FA is required to access your wallet. Please complete 2FA verification.");
                    return;
                }
            }

            if (walletRes.ok && historyRes.ok) {
                setWallet(await walletRes.json());
                setHistory(await historyRes.json());
            } else {
                setError("Failed to fetch wallet data.");
            }
        } catch (err) {
            setError("Connection to backend lost.");
        }
    };

    const showSuccess = (msg) => {
        setSuccessMsg(msg);
        setTimeout(() => setSuccessMsg(null), 3000);
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
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({ amount: parseInt(topUpAmount) })
            });

            if (response.ok) {
                setTopUpAmount('');
                showSuccess("Top-up successful.");
                fetchAllData();
            } else {
                setError("Top-up failed.");
            }
        } catch (err) {
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
                    Authorization: `Bearer ${token}`
                },
                body: JSON.stringify({
                    amount: parseInt(withdrawAmount),
                    bankAccount
                })
            });

            if (response.ok) {
                setWithdrawAmount('');
                setBankAccount('');
                showSuccess(`Withdrawal request sent to ${bankAccount}.`);
                fetchAllData();
            } else {
                const data = await response.json();
                setError(data.error || "Withdrawal failed due to insufficient balance.");
            }
        } catch (err) {
            setError("Connection error during withdrawal.");
        }
    };

    if (error && !wallet) {
        return <div className="panel"><p style={{ color: '#893f3f', margin: 0 }}>{error}</p></div>;
    }

    if (!wallet) {
        return <div className="panel">Opening your account ledger...</div>;
    }

    return (
        <div style={ledgerShell}>
            {(error || successMsg) && (
                <div className={`message ${error ? 'error' : 'success'}`}>
                    {error || successMsg}
                </div>
            )}

            <section style={{ ...framedPanel, overflow: 'hidden' }}>
                <div className="wallet-hero-grid" style={{
                    display: 'grid',
                    gridTemplateColumns: 'minmax(0, 1.2fr) minmax(260px, 0.8fr)',
                    gap: '0',
                    background: 'linear-gradient(135deg, #12383b 0%, #1e4b47 52%, #6c1f2f 100%)',
                    color: '#fffaf0'
                }}>
                    <div style={{ padding: '34px' }}>
                        <p className="eyebrow" style={{ color: '#d9bd67' }}>Private Wallet</p>
                        <h2 style={{ color: '#fffaf0', marginBottom: '10px', fontSize: '34px' }}>Account Ledger</h2>
                        <p style={{ maxWidth: '620px', marginBottom: 0, color: '#efe6ce' }}>
                            Account ID <span style={{ fontFamily: 'monospace' }}>{wallet.userId}</span>
                        </p>
                    </div>
                    <div style={{
                        display: 'grid',
                        alignContent: 'center',
                        gap: '18px',
                        padding: '30px',
                        borderLeft: '1px solid rgba(217, 189, 103, 0.55)',
                        background: 'rgba(255, 250, 240, 0.08)'
                    }}>
                        <div>
                            <p style={{ margin: '0 0 6px', color: '#d9bd67', textTransform: 'uppercase', fontSize: '12px', letterSpacing: '0.12em', fontWeight: 700 }}>Available</p>
                            <strong style={{ display: 'block', fontFamily: 'Georgia, serif', fontSize: '34px' }}>
                                Rp {wallet.availableBalance.toLocaleString()}
                            </strong>
                        </div>
                        <div>
                            <p style={{ margin: '0 0 6px', color: '#d9bd67', textTransform: 'uppercase', fontSize: '12px', letterSpacing: '0.12em', fontWeight: 700 }}>Held in Escrow</p>
                            <strong style={{ display: 'block', fontFamily: 'Georgia, serif', fontSize: '24px' }}>
                                Rp {wallet.heldBalance.toLocaleString()}
                            </strong>
                        </div>
                    </div>
                </div>
            </section>

            <div className="wallet-ledger-grid" style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.45fr) minmax(300px, 0.75fr)', gap: '24px', alignItems: 'start' }}>
                <section style={{ ...framedPanel, overflow: 'hidden' }}>
                    <div style={panelHeader}>
                        <p className="eyebrow" style={{ marginBottom: '6px' }}>Transactions</p>
                        <h3 style={{ margin: 0 }}>House Ledger</h3>
                    </div>

                    <div style={{ maxHeight: '560px', overflowY: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead style={{ position: 'sticky', top: 0, backgroundColor: '#fffdf8', zIndex: 1 }}>
                                <tr style={{ textAlign: 'left' }}>
                                    <th style={{ padding: '16px 22px', borderBottom: '1px solid #dfd2ab', color: '#8a6f2f', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Date</th>
                                    <th style={{ padding: '16px 22px', borderBottom: '1px solid #dfd2ab', color: '#8a6f2f', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.08em' }}>Entry</th>
                                    <th style={{ padding: '16px 22px', borderBottom: '1px solid #dfd2ab', color: '#8a6f2f', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.08em', textAlign: 'right' }}>Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                {history.length > 0 ? (
                                    history.map((tx, index) => {
                                        const badgeStyle = transactionStyle(tx.transactionType);
                                        const positive = tx.transactionType === 'TOP_UP' || tx.transactionType === 'RELEASE';
                                        return (
                                            <tr key={tx.id || index} style={{ borderBottom: '1px solid #eee5ca' }}>
                                                <td style={{ padding: '16px 22px', color: '#4f625f', fontSize: '14px', whiteSpace: 'nowrap' }}>
                                                    {formatDate(tx.timestamp)}
                                                </td>
                                                <td style={{ padding: '16px 22px' }}>
                                                    <span style={{
                                                        display: 'inline-block',
                                                        border: `1px solid ${badgeStyle.borderColor}`,
                                                        backgroundColor: badgeStyle.backgroundColor,
                                                        color: badgeStyle.color,
                                                        padding: '4px 10px',
                                                        borderRadius: '4px',
                                                        fontSize: '11px',
                                                        fontWeight: 800,
                                                        letterSpacing: '0.08em',
                                                        textTransform: 'uppercase'
                                                    }}>
                                                        {tx.transactionType}
                                                    </span>
                                                </td>
                                                <td style={{ padding: '16px 22px', textAlign: 'right', fontFamily: 'Georgia, serif', fontWeight: 700, color: positive ? '#275c3f' : '#893f3f' }}>
                                                    {positive ? '+' : '-'} Rp {tx.amount.toLocaleString()}
                                                </td>
                                            </tr>
                                        );
                                    })
                                ) : (
                                    <tr>
                                        <td colSpan="3" style={{ padding: '54px 22px', textAlign: 'center', color: '#8a6f2f', fontFamily: 'Georgia, serif', fontStyle: 'italic' }}>
                                            No ledger entries yet.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </section>

                <aside style={{ display: 'grid', gap: '20px' }}>
                    <section style={{ ...framedPanel, padding: '24px' }}>
                        <p className="eyebrow">Deposit</p>
                        <h3 style={{ marginBottom: '16px' }}>Add Funds</h3>
                        <form onSubmit={handleTopUp} style={{ display: 'grid', gap: '14px' }}>
                            <div style={fieldWrap}>
                                <span style={currencyMark}>Rp</span>
                                <input
                                    type="number"
                                    min="1000"
                                    value={topUpAmount}
                                    onChange={(e) => setTopUpAmount(e.target.value)}
                                    placeholder="50000"
                                    style={moneyInput}
                                    required
                                />
                            </div>
                            <button type="submit">Add Funds</button>
                        </form>
                    </section>

                    <section style={{ ...framedPanel, padding: '24px' }}>
                        <p className="eyebrow">Withdrawal</p>
                        <h3 style={{ marginBottom: '16px' }}>Transfer Out</h3>
                        <form onSubmit={handleWithdraw} style={{ display: 'grid', gap: '14px' }}>
                            <div style={fieldWrap}>
                                <span style={currencyMark}>Rp</span>
                                <input
                                    type="number"
                                    min="1000"
                                    value={withdrawAmount}
                                    onChange={(e) => setWithdrawAmount(e.target.value)}
                                    placeholder="Amount"
                                    style={moneyInput}
                                    required
                                />
                            </div>
                            <input
                                type="text"
                                value={bankAccount}
                                onChange={(e) => setBankAccount(e.target.value)}
                                placeholder="Bank account number"
                                style={{ borderColor: '#d8c48a', background: '#fffdf8' }}
                                required
                            />
                            <button type="submit" className="secondary">Confirm Transfer</button>
                        </form>
                    </section>
                </aside>
            </div>
        </div>
    );
};

export default WalletPage;
