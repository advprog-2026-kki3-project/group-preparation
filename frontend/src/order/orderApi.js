import { tokenStore } from '../auth/tokenStore';

const getAuthHeaders = () => {
    const token = tokenStore.get()?.accessToken;
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };
};

const request = async (path, options = {}) => {
    const response = await fetch(path, {
        ...options,
        headers: {
            ...getAuthHeaders(),
            ...(options.headers || {})
        }
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw new Error(data?.message || text || `Request failed with status ${response.status}`);
    }

    return data;
};

export const fetchBuyerOrders = (username) => request(`/api/orders/buyer/${username}`);

export const fetchSellerOrders = (username) => request(`/api/orders/seller/${username}`);

export const updateOrderStatus = (orderId, status) => request(`/api/orders/${orderId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
});

export const updateShipping = (orderId, trackingNumber) => request(`/api/orders/${orderId}/shipping`, {
    method: 'PATCH',
    body: JSON.stringify({ trackingNumber })
});
