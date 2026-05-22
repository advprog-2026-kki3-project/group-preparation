import React, { useEffect, useState } from 'react';
import { fetchBuyerOrders, fetchSellerOrders, updateOrderStatus, updateShipping } from './orderApi';

const currency = (amount) => `Rp ${(amount || 0).toLocaleString()}`;

const statusStyle = (status) => {
    if (status === 'COMPLETED') return { backgroundColor: '#ecfdf5', color: '#047857' };
    if (status === 'DISPUTED' || status === 'CANCELLED') return { backgroundColor: '#fef2f2', color: '#b91c1c' };
    if (status === 'SHIPPED' || status === 'PACKED') return { backgroundColor: '#eff6ff', color: '#1d4ed8' };
    return { backgroundColor: '#f4f4f5', color: '#3f3f46' };
};

function OrderCard({ order, currentUser, onChange }) {
    const [trackingNumber, setTrackingNumber] = useState(order.trackingNumber || '');
    const [message, setMessage] = useState('');
    const isBuyer = order.buyerUsername === currentUser.principal;
    const isSeller = order.sellerUsername === currentUser.principal;

    const runAction = async (action) => {
        setMessage('');
        try {
            await action();
            await onChange();
        } catch (error) {
            setMessage(error.message);
        }
    };

    return (
        <article className="list-item" style={{ alignItems: 'stretch' }}>
            <div style={{ display: 'grid', gap: '6px' }}>
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
                    <strong>Order #{order.id}</strong>
                    <span style={{ ...statusStyle(order.status), padding: '2px 8px', borderRadius: '4px', fontSize: '0.78rem', fontWeight: 700 }}>
                        {order.status}
                    </span>
                </div>
                <small>Auction: {order.auctionId}</small>
                <small>Buyer: {order.buyerUsername}</small>
                <small>Seller: {order.sellerUsername}</small>
                <small>Amount: {currency(order.amount)}</small>
                <small>Ship to: {order.shippingAddress}</small>
                <small>Tracking: {order.trackingNumber || 'Not available yet'}</small>
                {message && <small style={{ color: '#b91c1c' }}>{message}</small>}
            </div>

            <div style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                {isSeller && order.status === 'PAID' && (
                    <button className="secondary" onClick={() => runAction(() => updateOrderStatus(order.id, 'PACKED'))}>
                        Mark Packed
                    </button>
                )}

                {isSeller && (order.status === 'PAID' || order.status === 'PACKED') && (
                    <>
                        <input
                            value={trackingNumber}
                            onChange={(event) => setTrackingNumber(event.target.value)}
                            placeholder="Tracking number"
                            style={{ width: '180px' }}
                        />
                        <button onClick={() => runAction(() => updateShipping(order.id, trackingNumber))}>
                            Mark Shipped
                        </button>
                    </>
                )}

                {isBuyer && order.status === 'SHIPPED' && (
                    <>
                        <button onClick={() => runAction(() => updateOrderStatus(order.id, 'COMPLETED'))}>
                            Confirm Receipt
                        </button>
                        <button className="secondary" onClick={() => runAction(() => updateOrderStatus(order.id, 'DISPUTED'))}>
                            Raise Dispute
                        </button>
                    </>
                )}
            </div>
        </article>
    );
}

export function OrdersPage({ currentUser }) {
    const [buyerOrders, setBuyerOrders] = useState([]);
    const [sellerOrders, setSellerOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const loadOrders = async () => {
        setError('');
        try {
            const [buyer, seller] = await Promise.all([
                fetchBuyerOrders(currentUser.principal),
                fetchSellerOrders(currentUser.principal)
            ]);
            setBuyerOrders(buyer);
            setSellerOrders(seller);
        } catch (loadError) {
            setError(loadError.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadOrders();
    }, [currentUser.principal]);

    if (loading) return <div className="panel">Loading orders...</div>;

    return (
        <div className="grid two orders-layout">
            <section className="panel">
                <div className="section-heading">
                    <h2>Buying</h2>
                    <button className="secondary" onClick={loadOrders}>Refresh</button>
                </div>
                {error && <div className="message error">{error}</div>}
                <div className="list">
                    {buyerOrders.length === 0 ? (
                        <div className="empty-state">No buyer orders yet.</div>
                    ) : (
                        buyerOrders.map((order) => (
                            <OrderCard key={order.id} order={order} currentUser={currentUser} onChange={loadOrders} />
                        ))
                    )}
                </div>
            </section>

            <section className="panel">
                <div className="section-heading">
                    <h2>Selling</h2>
                </div>
                <div className="list">
                    {sellerOrders.length === 0 ? (
                        <div className="empty-state">No seller orders yet.</div>
                    ) : (
                        sellerOrders.map((order) => (
                            <OrderCard key={order.id} order={order} currentUser={currentUser} onChange={loadOrders} />
                        ))
                    )}
                </div>
            </section>
        </div>
    );
}
