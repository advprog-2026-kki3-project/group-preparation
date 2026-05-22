import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchAuctionDetails, fetchBiddingHistory, placeBid } from './auctionApi';
import { CountdownTimer } from './CountdownTimer';

export default function AuctionPage({ currentUser }) {
    const { listingId } = useParams();
    const navigate = useNavigate();

    const [auction, setAuction] = useState(null);
    const [bids, setBids] = useState([]);
    const [bidAmount, setBidAmount] = useState('');
    const [message, setMessage] = useState({ text: '', type: '' });
    const [loading, setLoading] = useState(true);

    const bidderId = currentUser?.principal || "unknown-user";

    useEffect(() => {
        let isMounted = true;

        const loadInitialData = async () => {
            try {
                const auctionData = await fetchAuctionDetails(listingId);
                if (isMounted) {
                    setAuction(auctionData);
                    const bidHistory = await fetchBiddingHistory(auctionData.id);
                    setBids(bidHistory);
                }
            } catch (err) {
                if (isMounted) setMessage({ text: "Auction not found for this listing.", type: "error" });
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        const pollForNewBids = async () => {
            try {
                const updatedAuction = await fetchAuctionDetails(listingId);
                if (isMounted) {
                    setAuction(updatedAuction);
                    const updatedBids = await fetchBiddingHistory(updatedAuction.id);
                    setBids(updatedBids);
                }
            } catch (err) {
                console.error("Polling error. Connection might be lost.");
            }
        };

        loadInitialData();

        const intervalId = setInterval(pollForNewBids, 3000);

        return () => {
            isMounted = false;
            clearInterval(intervalId);
        };
    }, [listingId]);

    const handleBidSubmit = async (e) => {
        e.preventDefault();
        setMessage({ text: "Placing bid...", type: "info" });
        try {
            await placeBid(auction.id, bidderId, parseFloat(bidAmount));
            setMessage({ text: "Bid placed successfully!", type: "success" });
            setBidAmount('');
            const updatedBids = await fetchBiddingHistory(auction.id);
            setBids(updatedBids);
            setAuction(prev => ({ ...prev, currentHighestBid: parseFloat(bidAmount) }));
        } catch (err) {
            setMessage({ text: `Failed: ${err.message}`, type: "error" });
        }
    };

    if (loading) return <div className="panel mt-4 text-center">Loading Auction...</div>;
    if (!auction) return <div className="panel mt-4 text-center text-red-500">{message.text}</div>;

    return (
        <div className="auction-container" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '1rem' }}>
            <section className="panel">
                <button onClick={() => navigate('/')} style={{ marginBottom: '1rem', background: 'none', border: 'none', color: 'blue', cursor: 'pointer' }}>
                    &larr; Back to Catalog
                </button>

                <h2>Catalogue Item: {listingId}</h2>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                    <span style={{ backgroundColor: '#e6fffa', color: '#047481', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold' }}>
                        {auction.stage}
                    </span>

                    {auction.endTime && <CountdownTimer endTime={auction.endTime} />}
                </div>

                <div style={{ backgroundColor: '#f4f4f5', padding: '1rem', borderRadius: '8px', margin: '1rem 0' }}>
                    <p style={{ margin: 0, fontSize: '0.9rem', color: '#52525b' }}>Current Highest Bid</p>
                    <p style={{ margin: 0, fontSize: '2rem', fontWeight: 'bold' }}>Rp {auction.currentHighestBid.toLocaleString()}</p>
                </div>

                <form onSubmit={handleBidSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div>
                        <label>Bidding as: <strong>{bidderId}</strong></label>
                    </div>
                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem' }}>Bid Amount (IDR)</label>
                        <input
                            type="number" step="0.01" required
                            value={bidAmount} onChange={e => setBidAmount(e.target.value)}
                            style={{ width: '100%', padding: '0.5rem' }}
                        />
                    </div>
                    <button type="submit" className="primary">Place Bid</button>
                    {message.text && (
                        <p style={{ textAlign: 'center', color: message.type === 'error' ? 'red' : 'green' }}>
                            {message.text}
                        </p>
                    )}
                </form>
            </section>

            <section className="panel">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3>Bidding History</h3>
                    <span style={{ fontSize: '0.75rem', color: '#10b981', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <span style={{ width: '8px', height: '8px', backgroundColor: '#10b981', borderRadius: '50%', display: 'inline-block' }}></span>
                        Live
                    </span>
                </div>

                <ul style={{ listStyle: 'none', padding: 0, maxHeight: '400px', overflowY: 'auto' }}>
                    {bids.length === 0 ? (
                        <li style={{ textAlign: 'center', padding: '1rem', color: '#71717a' }}>No bids placed yet.</li>
                    ) : (
                        bids.map((bid, index) => (
                            <li key={index} style={{ padding: '0.75rem 0', borderBottom: '1px solid #e4e4e7', display: 'flex', justifyContent: 'space-between' }}>
                                <span>{bid.bidderId}</span>
                                <strong>Rp {bid.amount.toLocaleString()}</strong>
                            </li>
                        ))
                    )}
                </ul>
            </section>
        </div>
    );
}