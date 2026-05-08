import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { fetchAllListings } from './catalogApi';

export function CatalogPage() {
    const [listings, setListings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchAllListings()
            .then(data => {
                setListings(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    if (loading) return <div className="panel mt-4 text-center">Loading Catalog...</div>;
    if (error) return <div className="panel mt-4 text-center text-red-500">Error: {error}</div>;

    return (
        <div className="panel mt-4">
            <h2 style={{ marginBottom: '1.5rem' }}>Current Auctions</h2>

            {listings.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2rem', color: '#71717a' }}>
                    No catalog items to show yet.
                </div>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.5rem' }}>
                    {listings.map(listing => (
                        <div key={listing.id} style={{ border: '1px solid #e4e4e7', borderRadius: '8px', padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', backgroundColor: '#fff', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
                            <div>
                                <h3 style={{ margin: '0 0 0.5rem 0', color: '#18181b' }}>{listing.title || 'Untitled Item'}</h3>
                                <p style={{ fontSize: '0.9rem', color: '#52525b', marginBottom: '1.5rem', lineHeight: '1.5' }}>
                                    {listing.description || 'No description available.'}
                                </p>

                                <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', borderTop: '1px solid #f4f4f5', paddingTop: '1rem' }}>
                                    <div>
                                        <span style={{ display: 'block', fontSize: '0.75rem', textTransform: 'uppercase', color: '#a1a1aa', fontWeight: 'bold' }}>Starting Price</span>
                                        <strong style={{ fontSize: '1.25rem', color: '#09090b' }}>${listing.initialPrice?.toFixed(2) || '0.00'}</strong>
                                    </div>
                                </div>
                            </div>

                            <Link
                                to={`/auction/${listing.id}`}
                                className="button primary"
                                style={{ textAlign: 'center', textDecoration: 'none', display: 'block', width: '100%', padding: '0.75rem', fontWeight: 'bold' }}
                            >
                                Enter Bidding Room
                            </Link>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}