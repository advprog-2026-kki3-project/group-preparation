import { tokenStore } from '../auth/tokenStore';

const API_BASE_URL = 'http://localhost:8080/api/auctions';

const getAuthHeaders = () => {
    const token = tokenStore.get()?.accessToken;
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const fetchAuctionDetails = async (listingId) => {
    const response = await fetch(`${API_BASE_URL}/listing/${listingId}`, {
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Auction not found");
    return response.json();
};

export const fetchBiddingHistory = async (auctionId) => {
    const response = await fetch(`${API_BASE_URL}/${auctionId}/bids`, {
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Failed to fetch bids");
    return response.json();
};

export const placeBid = async (auctionId, bidderId, amount) => {
    const response = await fetch(`${API_BASE_URL}/${auctionId}/bids`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ amount: amount })
    });

    if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText);
    }
    return response.json();
};