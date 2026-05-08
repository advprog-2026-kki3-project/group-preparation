const API_BASE_URL = 'http://localhost:8080/api/auctions';

export const fetchAuctionDetails = async (listingId) => {
    const response = await fetch(`${API_BASE_URL}/listing/${listingId}`);
    if (!response.ok) throw new Error("Auction not found");
    return response.json();
};

export const fetchBiddingHistory = async (auctionId) => {
    const response = await fetch(`${API_BASE_URL}/${auctionId}/bids`);
    if (!response.ok) throw new Error("Failed to fetch bids");
    return response.json();
};

export const placeBid = async (auctionId, bidderId, amount) => {
    const response = await fetch(`${API_BASE_URL}/${auctionId}/bids`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ bidderId, amount })
    });
    if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText);
    }
    return response.json();
};