import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';

export default function ListingDetailPage({ currentUser }) {

    const { id } = useParams();

    const navigate = useNavigate();

    const [listing, setListing] = useState(null);

    const [loading, setLoading] = useState(true);

    const [message, setMessage] = useState('');

    useEffect(() => {

        const fetchListing = async () => {

            try {

                const response = await fetch(`/api/listings/${id}`);

                if (!response.ok) {
                    throw new Error('Listing not found');
                }

                const data = await response.json();

                setListing(data);

            } catch (err) {

                console.error(err);

            } finally {

                setLoading(false);

            }
        };

        fetchListing();

    }, [id]);

    const handleDelete = async () => {

        const confirmed = window.confirm(
            'Are you sure you want to delete this listing?'
        );

        if (!confirmed) return;

        try {

            const response = await fetch(`/api/listings/${id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {

                const errorText = await response.text();

                throw new Error(errorText);

            }

            navigate('/');

        } catch (err) {

            setMessage(err.message);

        }
    };

    if (loading) {

        return (
            <div className="panel mt-4 text-center">
                Loading Listing...
            </div>
        );

    }

    if (!listing) {

        return (
            <div className="panel mt-4 text-center text-red-500">
                Listing not found.
            </div>
        );

    }

    return (
        <div
            className="listing-detail-container"
            style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '2rem',
                marginTop: '1rem'
            }}
        >

            <section className="panel">

                <button
                    onClick={() => navigate('/')}
                    style={{
                        marginBottom: '1rem',
                        background: 'none',
                        border: 'none',
                        color: 'blue',
                        cursor: 'pointer'
                    }}
                >
                    &larr; Back to Catalog
                </button>

                <img
                    src={
                        listing.imageUrl ||
                        'https://via.placeholder.com/500'
                    }
                    alt={listing.title}
                    style={{
                        width: '100%',
                        borderRadius: '10px'
                    }}
                />

            </section>

            <section className="panel">

                <h1>{listing.title}</h1>

                <span
                    style={{
                        backgroundColor: '#e6fffa',
                        color: '#047481',
                        padding: '0.2rem 0.5rem',
                        borderRadius: '4px',
                        fontSize: '0.8rem'
                    }}
                >
                    {listing.active ? 'ACTIVE' : 'INACTIVE'}
                </span>

                <div
                    style={{
                        backgroundColor: '#f4f4f5',
                        padding: '1rem',
                        borderRadius: '8px',
                        margin: '1rem 0'
                    }}
                >

                    <p
                        style={{
                            margin: 0,
                            fontSize: '0.9rem',
                            color: '#52525b'
                        }}
                    >
                        Current Price
                    </p>

                    <p
                        style={{
                            margin: 0,
                            fontSize: '2rem',
                            fontWeight: 'bold'
                        }}
                    >
                        ${listing.currentPrice}
                    </p>

                </div>

                <p>
                    <strong>Seller:</strong> {listing.sellerId}
                </p>

                <p>
                    <strong>Category:</strong> {listing.category?.name}
                </p>

                <p>
                    <strong>Reserve Price:</strong> ${listing.reservePrice}
                </p>

                <p>
                    <strong>Bid Count:</strong> {listing.bidCount}
                </p>

                <p>
                    <strong>Auction Ends:</strong> {listing.endTime}
                </p>

                <div style={{ marginTop: '2rem' }}>

                    <h3>Description</h3>

                    <p>{listing.description}</p>

                </div>

                <div
                    style={{
                        display: 'flex',
                        gap: '1rem',
                        marginTop: '2rem'
                    }}
                >

                    <Link to={`/listings/edit/${listing.id}`}>

                        <button className="primary">
                            Edit Listing
                        </button>

                    </Link>

                    <button
                        onClick={handleDelete}
                        style={{
                            backgroundColor: '#ef4444',
                            color: 'white',
                            border: 'none',
                            padding: '0.5rem 1rem',
                            borderRadius: '5px',
                            cursor: 'pointer'
                        }}
                    >
                        Delete Listing
                    </button>

                </div>

                {message && (

                    <p
                        style={{
                            marginTop: '1rem',
                            color: 'red'
                        }}
                    >
                        {message}
                    </p>

                )}

            </section>

        </div>
    );
}