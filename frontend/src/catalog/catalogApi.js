const API_BASE_URL = 'http://localhost:8080/api/listings';

export const fetchAllListings = async () => {
    const response = await fetch(API_BASE_URL);
    if (!response.ok) throw new Error("Failed to fetch listings");
    return response.json();
};