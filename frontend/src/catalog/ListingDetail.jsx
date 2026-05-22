import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getListingById, cancelListing } from './catalogApi';

export function ListingDetail({ currentUser }) {
    const { id } = useParams();
    const navigate = useNavigate();
    const [listing, setListing] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        getListingById(id)
            .then(data => {
                setListing(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, [id]);

    const handleCancel = async () => {
        if (!window.confirm("Are you sure you want to cancel this listing? This cannot be undone.")) return;

        try {
            await cancelListing(id);
            navigate('/');
        } catch (err) {
            alert(err.message);
        }
    };

    if (loading) return <div className="panel mt-4 text-center">Loading details...</div>;
    if (error) return <div className="panel mt-4 text-center text-red-500">Error: {error}</div>;
    if (!listing) return null;

    const isSeller = currentUser?.principal === listing.sellerId;
    const canEditOrCancel = isSeller && listing.bidCount === 0;

    return (
        <div className="panel mt-4" style={{ maxWidth: '800px', margin: '2rem auto' }}>
            <Link to="/" style={{ display: 'inline-block', marginBottom: '1rem', color: '#3f3f46', textDecoration: 'none' }}>
                &larr; Back to Catalog
            </Link>

            <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
                {/* Left Side: Image */}
                <div style={{ flex: '1 1 300px' }}>
                    <div style={{ width: '100%', height: '300px', backgroundColor: '#f4f4f5', borderRadius: '8px', backgroundImage: `url(${listing.imageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }}>
                        {!listing.imageUrl && <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#a1a1aa' }}>No Image Provided</div>}
                    </div>
                </div>

                {/* Right Side: Info */}
                <div style={{ flex: '2 1 400px', display: 'flex', flexDirection: 'column' }}>
                    <h2 style={{ marginTop: 0, marginBottom: '0.5rem' }}>{listing.title}</h2>
                    <p style={{ color: '#71717a', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                        Seller ID: {listing.sellerId}
                    </p>

                    <div style={{ backgroundColor: '#f8fafc', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', border: '1px solid #e2e8f0' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                            <span style={{ color: '#64748b' }}>Current Price:</span>
                            <strong style={{ fontSize: '1.2rem', color: '#0f172a' }}>Rp {listing.currentPrice.toLocaleString()}</strong>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem' }}>
                            <span style={{ color: '#64748b' }}>Total Bids:</span>
                            <strong style={{ color: '#0f172a' }}>{listing.bidCount}</strong>
                        </div>
                    </div>

                    <h3 style={{ fontSize: '1.1rem', marginBottom: '0.5rem' }}>Description</h3>
                    <p style={{ lineHeight: '1.6', color: '#334155', flex: 1, whiteSpace: 'pre-wrap' }}>
                        {listing.description}
                    </p>

                    {/* Action Buttons */}
                    <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem' }}>
                        <Link to={`/auctions/${listing.id}`} className="button primary" style={{ flex: 1, textAlign: 'center', fontSize: '1.1rem', padding: '0.75rem' }}>
                            Enter Bidding Room
                        </Link>

                        {canEditOrCancel && (
                            <button onClick={handleCancel} className="button" style={{ backgroundColor: '#ef4444', color: 'white', border: 'none', cursor: 'pointer', padding: '0.75rem 1.5rem', borderRadius: '4px' }}>
                                Cancel Listing
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
