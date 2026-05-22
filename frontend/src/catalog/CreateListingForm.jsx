import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createListing, fetchCategories } from './catalogApi';
import { CategoryCascade } from './CategoryCascade';

const toLocalDateTimePayload = (value) => {
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
};

export function CreateListingForm() {
    const navigate = useNavigate();
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        initialPrice: '',
        reservePrice: '',
        endTime: '',
        imageUrl: '',
        category: { id: '' }
    });

    useEffect(() => {
        fetchCategories()
            .then(data => setCategories(data))
            .catch(err => console.error("Failed to load categories", err));
    }, []);

    const handleCategoryChange = (categoryId) => {
        setFormData({ ...formData, category: { id: categoryId } });
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const payload = {
                ...formData,
                initialPrice: parseFloat(formData.initialPrice),
                reservePrice: parseFloat(formData.reservePrice),
                endTime: toLocalDateTimePayload(formData.endTime)
            };

            await createListing(payload);

            navigate('/catalog');
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="panel" style={{ maxWidth: '600px', margin: '2rem auto', padding: '2rem' }}>
            <h2 style={{ marginBottom: '1.5rem', textAlign: 'center' }}>Create a New Auction</h2>

            {error && (
                <div style={{ backgroundColor: '#fee2e2', color: '#b91c1c', padding: '1rem', borderRadius: '6px', marginBottom: '1.5rem' }}>
                    <strong>Error:</strong> {error}
                </div>
            )}

            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>

                <div>
                    <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '0.5rem' }}>Title</label>
                    <input
                        type="text" name="title" className="input" required
                        placeholder="e.g. Vintage 1980s Mechanical Keyboard"
                        value={formData.title} onChange={handleChange}
                        style={{ width: '100%' }}
                    />
                </div>

                <CategoryCascade
                    categories={categories}
                    value={formData.category.id}
                    onChange={handleCategoryChange}
                    required
                    allLabel="Select a category..."
                />

                <div>
                    <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '0.5rem' }}>Description</label>
                    <textarea
                        name="description" className="input" required rows="4"
                        placeholder="Describe the item's condition, history, and specs..."
                        value={formData.description} onChange={handleChange}
                        style={{ width: '100%', resize: 'vertical' }}
                    />
                </div>

                <div>
                    <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '0.5rem' }}>Image URL</label>
                    <input
                        type="url" name="imageUrl" className="input"
                        placeholder="https://example.com/image.jpg"
                        value={formData.imageUrl} onChange={handleChange}
                        style={{ width: '100%' }}
                    />
                </div>

                <div style={{ display: 'flex', gap: '1rem' }}>
                    <div style={{ flex: 1 }}>
                        <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '0.5rem' }}>Initial Price (Rp)</label>
                        <input
                            type="number" name="initialPrice" className="input" required min="0" step="1000"
                            value={formData.initialPrice} onChange={handleChange}
                            style={{ width: '100%' }}
                        />
                    </div>
                    <div style={{ flex: 1 }}>
                        <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '0.5rem' }}>Reserve Price (Rp)</label>
                        <input
                            type="number" name="reservePrice" className="input" required min="0" step="1000"
                            value={formData.reservePrice} onChange={handleChange}
                            style={{ width: '100%' }}
                        />
                    </div>
                </div>

                <div>
                    <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '0.5rem' }}>Auction End Time</label>
                    <input
                        type="datetime-local" name="endTime" className="input" required
                        value={formData.endTime} onChange={handleChange}
                        style={{ width: '100%' }}
                    />
                </div>

                <button
                    type="submit"
                    className="button primary"
                    disabled={loading}
                    style={{ marginTop: '1rem', padding: '0.75rem', fontSize: '1.1rem', backgroundColor: '#10b981', color: 'white' }}
                >
                    {loading ? 'Publishing...' : 'Publish Auction'}
                </button>
            </form>
        </div>
    );
}
