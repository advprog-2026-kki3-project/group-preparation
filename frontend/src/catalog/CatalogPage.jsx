import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { searchListings, fetchCategories } from './catalogApi';

export function CatalogPage() {
    // Data States
    const [listings, setListings] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Filter States
    const [keyword, setKeyword] = useState('');
    const [categoryId, setCategoryId] = useState('');
    const [minPrice, setMinPrice] = useState('');
    const [maxPrice, setMaxPrice] = useState('');

    useEffect(() => {
        fetchCategories()
            .then(data => setCategories(data))
            .catch(err => console.error("Could not load categories", err));
    }, []);

    const fetchCatalog = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await searchListings({
                keyword: keyword || undefined,
                categoryId: categoryId || undefined,
                minPrice: minPrice || undefined,
                maxPrice: maxPrice || undefined
            });
            setListings(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [keyword, categoryId, minPrice, maxPrice]);

    useEffect(() => {
        fetchCatalog();
    }, [fetchCatalog]);

    const handleSearch = (e) => {
        e.preventDefault();
        fetchCatalog();
    };

    return (
        <div style={{ display: 'flex', gap: '2rem', marginTop: '1.5rem' }}>

            {/* LEFT SIDEBAR: Filters */}
            <div style={{ width: '280px', flexShrink: 0 }} className="panel">
                <h3 style={{ borderBottom: '1px solid #e4e4e7', paddingBottom: '0.5rem', marginBottom: '1rem' }}>Filters</h3>

                <form onSubmit={handleSearch} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div>
                        <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>Keyword</label>
                        <input
                            type="text"
                            className="input"
                            placeholder="e.g. Laptop"
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                        />
                    </div>

                    {/* NEW: Category Dropdown */}
                    <div>
                        <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>Category</label>
                        <select
                            className="input"
                            value={categoryId}
                            onChange={(e) => setCategoryId(e.target.value)}
                        >
                            <option value="">All Categories</option>
                            {categories.map(cat => (
                                <option key={cat.id} value={cat.id}>
                                    {cat.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <div style={{ flex: 1 }}>
                            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>Min Price</label>
                            <input
                                type="number"
                                className="input"
                                placeholder="Rp 0"
                                value={minPrice}
                                onChange={(e) => setMinPrice(e.target.value)}
                            />
                        </div>
                        <div style={{ flex: 1 }}>
                            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>Max Price</label>
                            <input
                                type="number"
                                className="input"
                                placeholder="Rp 50000"
                                value={maxPrice}
                                onChange={(e) => setMaxPrice(e.target.value)}
                            />
                        </div>
                    </div>

                    <button type="submit" className="button primary" style={{ width: '100%', marginTop: '0.5rem' }}>
                        Apply Filters
                    </button>

                    <button
                        type="button"
                        onClick={() => { setKeyword(''); setCategoryId(''); setMinPrice(''); setMaxPrice(''); }}
                        style={{ width: '100%', padding: '0.5rem', background: 'none', border: 'none', color: '#71717a', cursor: 'pointer', fontSize: '0.85rem' }}
                    >
                        Clear All
                    </button>
                </form>
            </div>

            {/* MAIN CONTENT: Grid View */}
            <div style={{ flex: 1 }} className="panel">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <h2 style={{ margin: 0 }}>Current Auctions</h2>
                    <Link to="/catalog/create" className="button primary" style={{ backgroundColor: '#10b981', color: 'white' }}>
                        + Create Listing
                    </Link>
                </div>

                {loading ? (
                    <div style={{ textAlign: 'center', padding: '2rem' }}>Loading Catalog...</div>
                ) : error ? (
                    <div style={{ textAlign: 'center', padding: '2rem', color: '#ef4444' }}>Error: {error}</div>
                ) : listings.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '3rem', border: '1px dashed #d4d4d8', borderRadius: '8px', color: '#71717a' }}>
                        No items match your search criteria.
                    </div>
                ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1.5rem' }}>
                        {listings.map(listing => (
                            <div key={listing.id} style={{ border: '1px solid #e4e4e7', borderRadius: '8px', overflow: 'hidden', display: 'flex', flexDirection: 'column', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>

                                <div style={{ height: '160px', backgroundColor: '#f4f4f5', backgroundImage: `url(${listing.imageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center', borderBottom: '1px solid #e4e4e7' }}>
                                    {!listing.imageUrl && <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#a1a1aa' }}>No Image</div>}
                                </div>

                                <div style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                    <h3 style={{ margin: '0 0 0.5rem 0', color: '#18181b', fontSize: '1.1rem' }}>{listing.title || 'Untitled Item'}</h3>

                                    <p style={{ fontSize: '0.85rem', color: '#52525b', marginBottom: '1rem', flex: 1, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                                        {listing.description || 'No description available.'}
                                    </p>

                                    <div style={{ borderTop: '1px solid #f4f4f5', paddingTop: '0.75rem', marginBottom: '1rem' }}>
                                        <span style={{ display: 'block', fontSize: '0.75rem', textTransform: 'uppercase', color: '#a1a1aa', fontWeight: 'bold' }}>Current Price</span>
                                        <strong style={{ fontSize: '1.25rem', color: '#09090b' }}>Rp {listing.currentPrice?.toLocaleString() || '0'}</strong>
                                    </div>

                                    <Link to={`/catalog/${listing.id}`} className="button primary" style={{ textAlign: 'center', width: '100%', display: 'block' }}>
                                        View Details
                                    </Link>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}