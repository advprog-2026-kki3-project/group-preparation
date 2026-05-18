import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

export default function BrowseListingsPage() {

    const [listings, setListings] = useState([]);
    const [categories, setCategories] = useState([]);

    const [loading, setLoading] = useState(true);

    const [keyword, setKeyword] = useState('');
    const [category, setCategory] = useState('');
    const [minPrice, setMinPrice] = useState('');
    const [maxPrice, setMaxPrice] = useState('');

    useEffect(() => {

        const fetchCategories = async () => {

            try {

                const response = await fetch('/api/categories');

                const data = await response.json();

                setCategories(data);

            } catch (err) {

                console.error(err);

            }
        };

        fetchCategories();

    }, []);

    useEffect(() => {

        const fetchListings = async () => {

            try {

                let url = '/api/listings';

                const params = new URLSearchParams();

                if (keyword) {
                    params.append('keyword', keyword);
                }

                if (category) {
                    params.append('category', category);
                }

                if (minPrice) {
                    params.append('minPrice', minPrice);
                }

                if (maxPrice) {
                    params.append('maxPrice', maxPrice);
                }

                if (params.toString()) {
                    url += `?${params.toString()}`;
                }

                const response = await fetch(url);

                if (!response.ok) {
                    throw new Error('Failed to fetch listings');
                }

                const data = await response.json();

                setListings(data);

            } catch (err) {

                console.error(err);

            } finally {

                setLoading(false);

            }
        };

        fetchListings();

    }, [keyword, category, minPrice, maxPrice]);

    if (loading) {

        return (
            <div className="panel mt-4 text-center">
                Loading Listings...
            </div>
        );

    }

    return (
        <div
            className="catalogue-container"
            style={{
                display: 'grid',
                gridTemplateColumns: '250px 1fr',
                gap: '2rem',
                marginTop: '1rem'
            }}
        >

            {/* Sidebar */}
            <section className="panel">

                <h3>Filters</h3>

                <div style={{ marginBottom: '1rem' }}>

                    <label>Search</label>

                    <input
                        type="text"
                        value={keyword}
                        onChange={e => setKeyword(e.target.value)}
                        placeholder="Search listings..."
                        style={{
                            width: '100%',
                            padding: '0.5rem',
                            marginTop: '0.5rem'
                        }}
                    />

                </div>

                <div style={{ marginBottom: '1rem' }}>

                    <label>Category</label>

                    <select
                        value={category}
                        onChange={e => setCategory(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '0.5rem',
                            marginTop: '0.5rem'
                        }}
                    >

                        <option value="">
                            All Categories
                        </option>

                        {categories.map(category => (

                            <option
                                key={category.id}
                                value={category.name}
                            >
                                {category.name}
                            </option>

                        ))}

                    </select>

                </div>

                <div style={{ marginBottom: '1rem' }}>

                    <label>Min Price</label>

                    <input
                        type="number"
                        value={minPrice}
                        onChange={e => setMinPrice(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '0.5rem',
                            marginTop: '0.5rem'
                        }}
                    />

                </div>

                <div>

                    <label>Max Price</label>

                    <input
                        type="number"
                        value={maxPrice}
                        onChange={e => setMaxPrice(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '0.5rem',
                            marginTop: '0.5rem'
                        }}
                    />

                </div>

            </section>

            {/* Listings */}
            <section className="panel">

                <h2>Browse Listings</h2>

                <div
                    style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
                        gap: '1rem',
                        marginTop: '1rem'
                    }}
                >

                    {listings.map((listing) => (

                        <Link
                            key={listing.id}
                            to={`/listings/${listing.id}`}
                            style={{
                                textDecoration: 'none',
                                color: 'inherit'
                            }}
                        >

                            <div
                                style={{
                                    border: '1px solid #e4e4e7',
                                    borderRadius: '10px',
                                    overflow: 'hidden'
                                }}
                            >

                                <img
                                    src={
                                        listing.imageUrl ||
                                        'https://via.placeholder.com/300'
                                    }
                                    alt={listing.title}
                                    style={{
                                        width: '100%',
                                        height: '200px',
                                        objectFit: 'cover'
                                    }}
                                />

                                <div style={{ padding: '1rem' }}>

                                    <h3>{listing.title}</h3>

                                    <p>
                                        {listing.category?.name}
                                    </p>

                                    <p
                                        style={{
                                            fontWeight: 'bold',
                                            fontSize: '1.2rem'
                                        }}
                                    >
                                        ${listing.currentPrice}
                                    </p>

                                </div>

                            </div>

                        </Link>

                    ))}

                </div>

            </section>

        </div>
    );
}