import { tokenStore } from '../auth/tokenStore';

const API_BASE_URL = 'http://localhost:8080/api/listings';

export const fetchAllListings = async () => {
    // 1. Grab the VIP Wristband
    const token = tokenStore.get()?.accessToken;

    // 2. Show the wristband to the Spring Security Bouncer
    const response = await fetch(API_BASE_URL, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error("Failed to fetch listings");
    }

    return response.json();
};