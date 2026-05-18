import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

export default function CreateEditListingPage({ currentUser }) {

    const { id } = useParams();

    const navigate = useNavigate();

    const [loading, setLoading] = useState(false);

    const [categories, setCategories] = useState([]);

    const [message, setMessage] = useState({
        text: '',
        type: ''
    });

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        imageUrl: '',
        initialPrice: '',
        reservePrice: '',
        duration: '',
        categoryId: ''
    });

    // Load categories
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

    // Edit mode
    useEffect(() => {

        if (!id) return;

        const fetchListing = async () => {

            try {

                setLoading(true);

                const response = await fetch(`/api/listings/${id}`);

                if (!response.ok) {
                    throw new Error('Failed to fetch listing');
                }

                const data = await response.json();

                setFormData({
                    title: data.title || '',
                    description: data.description || '',
                    imageUrl: data.imageUrl || '',
                    initialPrice: data.initialPrice || '',
                    reservePrice: data.reservePrice || '',
                    duration: '',
                    categoryId: data.category?.id || ''
                });

            } catch (err) {

                setMessage({
                    text: err.message,
                    type: 'error'
                });

            } finally {

                setLoading(false);

            }
        };

        fetchListing();

    }, [id]);

    const handleChange = (e) => {

        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });

    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            setMessage({
                text: id
                    ? 'Updating listing...'
                    : 'Creating listing...',
                type: 'info'
            });

            const response = await fetch(

                id
                    ? `/api/listings/${id}`
                    : '/api/listings',

                {
                    method: id ? 'PUT' : 'POST',

                    headers: {
                        'Content-Type': 'application/json'
                    },

                    body: JSON.stringify({
                        sellerId: currentUser?.principal || 'unknown-user',
                        title: formData.title,
                        description: formData.description,
                        imageUrl: formData.imageUrl,
                        initialPrice: parseFloat(formData.initialPrice),
                        reservePrice: parseFloat(formData.reservePrice),
                        category: {
                            id: formData.categoryId
                        }
                    })
                }
            );

            if (!response.ok) {

                const errorText = await response.text();

                throw new Error(errorText);

            }

            const savedListing = await response.json();

            setMessage({
                text: id
                    ? 'Listing updated successfully!'
                    : 'Listing created successfully!',
                type: 'success'
            });

            setTimeout(() => {
                navigate(`/listings/${savedListing.id}`);
            }, 1000);

        } catch (err) {

            setMessage({
                text: err.message,
                type: 'error'
            });

        }
    };

    if (loading) {

        return (
            <div className="panel mt-4 text-center">
                Loading Listing...
            </div>
        );

    }

    return (
        <div
            className="panel"
            style={{
                maxWidth: '800px',
                margin: '2rem auto'
            }}
        >

            <h2>
                {id
                    ? 'Edit Listing'
                    : 'Create Listing'}
            </h2>

            <form
                onSubmit={handleSubmit}
                style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '1rem',
                    marginTop: '1rem'
                }}
            >

                <div>

                    <label>Seller</label>

                    <input
                        type="text"
                        value={currentUser?.principal || 'unknown-user'}
                        disabled
                        style={{
                            width: '100%',
                            padding: '0.5rem'
                        }}
                    />

                </div>

                <div>

                    <label>Title</label>

                    <input
                        type="text"
                        name="title"
                        required
                        value={formData.title}
                        onChange={handleChange}
                        style={{
                            width: '100%',
                            padding: '0.5rem'
                        }}
                    />

                </div>

                <div>

                    <label>Description</label>

                    <textarea
                        name="description"
                        rows="5"
                        required
                        value={formData.description}
                        onChange={handleChange}
                        style={{
                            width: '100%',
                            padding: '0.5rem'
                        }}
                    />

                </div>

                <div>

                    <label>Image URL</label>

                    <input
                        type="text"
                        name="imageUrl"
                        value={formData.imageUrl}
                        onChange={handleChange}
                        style={{
                            width: '100%',
                            padding: '0.5rem'
                        }}
                    />

                </div>

                <div>

                    <label>Category</label>

                    <select
                        name="categoryId"
                        value={formData.categoryId}
                        onChange={handleChange}
                        required
                        style={{
                            width: '100%',
                            padding: '0.5rem'
                        }}
                    >

                        <option value="">
                            Select Category
                        </option>

                        {categories.map(category => (

                            <option
                                key={category.id}
                                value={category.id}
                            >
                                {category.name}
                            </option>

                        ))}

                    </select>

                </div>

                <div>

                    <label>Initial Price ($)</label>

                    <input
                        type="number"
                        step="0.01"
                        name="initialPrice"
                        required
                        value={formData.initialPrice}
                        onChange={handleChange}
                        style={{
                            width: '100%',
                            padding: '0.5rem'
                        }}
                    />

                </div>

                <div>

                    <label>Reserve Price ($)</label>

                    <input
                        type="number"
                        step="0.01"
                        name="reservePrice"
                        required
                        value={formData.reservePrice}
                        onChange={handleChange}
                        style={{
                            width: '100%',
                            padding: '0.5rem'
                        }}
                    />

                </div>

                <button
                    type="submit"
                    className="primary"
                >
                    {id
                        ? 'Update Listing'
                        : 'Create Listing'}
                </button>

                {message.text && (

                    <p
                        style={{
                            textAlign: 'center',
                            color:
                                message.type === 'error'
                                    ? 'red'
                                    : '#10b981'
                        }}
                    >
                        {message.text}
                    </p>

                )}

            </form>

        </div>
    );
}