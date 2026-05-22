import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchAuctionDetails, fetchBiddingHistory, placeBid } from './auctionApi';
import { getListingById } from '../catalog/catalogApi';
import { CountdownTimer } from './CountdownTimer';

export default function AuctionPage({ currentUser }) {
    const { listingId } = useParams();
    const navigate = useNavigate();

    const [auction, setAuction] = useState(null);
    const [listing, setListing] = useState(null);
    const [bids, setBids] = useState([]);
    const [bidAmount, setBidAmount] = useState('');
    const [message, setMessage] = useState({ text: '', type: '' });
    const [loading, setLoading] = useState(true);

    const bidderId = currentUser?.principal || "unknown-user";
    const auctionOpen = auction?.stage === 'ACTIVE' || auction?.stage === 'EXTENDED';
    const isWinner = auction?.stage === 'WON' && auction?.winnerId === bidderId;
    const isLoser = auction?.stage === 'WON' && auction?.winnerId && auction?.winnerId !== bidderId;
    const auctionStageLabel = isLoser ? 'LOST' : auction?.stage;
    const auctionStageStyle = isLoser
        ? { backgroundColor: '#fef2f2', color: '#b91c1c' }
        : { backgroundColor: '#e6fffa', color: '#047481' };

    useEffect(() => {
        let isMounted = true;

        const loadInitialData = async () => {
            try {
                const [auctionData, listingData] = await Promise.all([
                    fetchAuctionDetails(listingId),
                    getListingById(listingId)
                ]);
                if (isMounted) {
                    setAuction(auctionData);
                    setListing(listingData);
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
                <button type="button" className="secondary back-button" onClick={() => navigate('/')}>
                    Back to Catalog
                </button>

                <div className="auction-item-summary">
                    <div
                        className="auction-item-image"
                        style={{ backgroundImage: listing?.imageUrl ? `url(${listing.imageUrl})` : 'none' }}
                    >
                        {!listing?.imageUrl && <span>No Image</span>}
                    </div>
                    <div>
                        <p className="eyebrow">Bidding Room</p>
                        <h2>{listing?.title || 'Auction Item'}</h2>
                        <p className="muted" style={{ marginBottom: 0 }}>
                            Seller ID: {listing?.sellerId || auction.sellerId}
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                    <span style={{ ...auctionStageStyle, padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold' }}>
                        {auctionStageLabel}
                    </span>

                    {auction.endTime && <CountdownTimer endTime={auction.endTime} />}
                </div>

                <div style={{ backgroundColor: '#f4f4f5', padding: '1rem', borderRadius: '8px', margin: '1rem 0' }}>
                    <p style={{ margin: 0, fontSize: '0.9rem', color: '#52525b' }}>Current Highest Bid</p>
                    <p style={{ margin: 0, fontSize: '2rem', fontWeight: 'bold' }}>Rp {auction.currentHighestBid.toLocaleString()}</p>
                </div>

                {isWinner && (
                    <div style={{ backgroundColor: '#ecfdf5', color: '#047857', padding: '1rem', borderRadius: '8px', margin: '1rem 0', border: '1px solid #a7f3d0' }}>
                        <strong>You won this auction.</strong>
                    </div>
                )}

                {isLoser && (
                    <div style={{ backgroundColor: '#fef2f2', color: '#b91c1c', padding: '1rem', borderRadius: '8px', margin: '1rem 0', border: '1px solid #fecaca' }}>
                        <strong>You lost this auction.</strong> Winner: {auction.winnerId}
                    </div>
                )}

                {auction.stage === 'UNSOLD' && (
                    <div style={{ backgroundColor: '#fef2f2', color: '#b91c1c', padding: '1rem', borderRadius: '8px', margin: '1rem 0', border: '1px solid #fecaca' }}>
                        Reserve price was not met. This auction ended unsold.
                    </div>
                )}

                <form onSubmit={handleBidSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div>
                        <label>Bidding as: <strong>{bidderId}</strong></label>
                    </div>
                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem' }}>Bid Amount (IDR)</label>
                        <input
                            type="number" step="0.01" required
                            disabled={!auctionOpen}
                            value={bidAmount} onChange={e => setBidAmount(e.target.value)}
                            style={{ width: '100%', padding: '0.5rem' }}
                        />
                    </div>
                    <button type="submit" className="primary" disabled={!auctionOpen}>Place Bid</button>
                    {message.text && (
                        <p style={{ textAlign: 'center', color: message.type === 'error' ? 'red' : 'green' }}>
                            {message.text}
                        </p>
                    )}
                </form>
            </section>

            <section className="panel">
                <div className="section-heading compact-heading">
                    <h3>Bidding History</h3>
                    <span className="live-indicator">
                        Live
                    </span>
                </div>

                <ol className="bid-history-list">
                    {bids.length === 0 ? (
                        <li className="empty-state">No bids placed yet.</li>
                    ) : (
                        bids.map((bid, index) => (
                            <li key={index} className="bid-history-row">
                                <span className="bid-rank">#{index + 1}</span>
                                <span className="bidder-id">{bid.bidderId}</span>
                                <strong>Rp {bid.amount.toLocaleString()}</strong>
                            </li>
                        ))
                    )}
                </ol>
            </section>
        </div>
    );
}
