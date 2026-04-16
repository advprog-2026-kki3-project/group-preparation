const auctionDataEl = document.getElementById('auction-data');
const AUCTION_ID = auctionDataEl.getAttribute('data-auction-id');

const currentHighestBidEl = document.getElementById('currentHighestBid');
const bidHistoryListEl = document.getElementById('bidHistoryList');
const bidForm = document.getElementById('bidForm');
const feedbackMessage = document.getElementById('feedbackMessage');

document.addEventListener('DOMContentLoaded', () => {
    ensureMockAuctionExists().then(() => fetchBiddingHistory());
});

async function ensureMockAuctionExists() {
    const requestBody = {
        sellerId: "seller-admin",
        catalogueListingId: "item-watch-01",
        initialPrice: 50.00,
        reservePrice: 200.00,
        startTime: new Date().toISOString(),
        endTime: new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000).toISOString()
    };

    try {
        await fetch('/api/auctions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });
    } catch (error) {
        console.error("Error setting up mock auction:", error);
    }
}

async function fetchBiddingHistory() {
    try {
        const response = await fetch(`/api/auctions/${AUCTION_ID}/bids`);
        if (!response.ok) return;

        const bids = await response.json();
        bidHistoryListEl.innerHTML = '';

        if (bids.length === 0) {
            bidHistoryListEl.innerHTML = '<li class="py-4 text-center text-gray-500 text-sm">No bids placed yet.</li>';
            currentHighestBidEl.innerText = "50.00";
            return;
        }

        currentHighestBidEl.innerText = bids[0].amount.toFixed(2);

        bids.forEach(bid => {
            const li = document.createElement('li');
            li.className = 'py-3 flex justify-between items-center';
            li.innerHTML = `
                <div class="flex items-center">
                    <span class="text-sm font-medium text-gray-900">${bid.bidderId}</span>
                </div>
                <div class="text-sm text-gray-500 text-right">
                    <p class="font-bold text-gray-900">$${bid.amount.toFixed(2)}</p>
                </div>
            `;
            bidHistoryListEl.appendChild(li);
        });
    } catch (error) {
        bidHistoryListEl.innerHTML = '<li class="py-4 text-center text-red-500 text-sm">Failed to load history.</li>';
    }
}

bidForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const bidderId = document.getElementById('bidderId').value;
    const amount = parseFloat(document.getElementById('bidAmount').value);
    const submitBtn = bidForm.querySelector('button');

    submitBtn.disabled = true;
    submitBtn.innerText = "Placing...";
    feedbackMessage.classList.add('hidden');

    try {
        const response = await fetch(`/api/auctions/${AUCTION_ID}/bids`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bidderId, amount })
        });

        if (response.ok) {
            feedbackMessage.innerText = "Bid placed successfully!";
            feedbackMessage.className = "text-sm text-center font-medium mt-2 text-green-600 block";
            document.getElementById('bidAmount').value = '';
            fetchBiddingHistory();
        } else {
            const errorText = await response.text();
            feedbackMessage.innerText = "Failed: " + errorText;
            feedbackMessage.className = "text-sm text-center font-medium mt-2 text-red-600 block";
        }
    } catch (error) {
        feedbackMessage.innerText = "Network error. Please try again.";
        feedbackMessage.className = "text-sm text-center font-medium mt-2 text-red-600 block";
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerText = "Place Bid";
    }
});