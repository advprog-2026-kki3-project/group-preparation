import { tokenStore } from '../auth/tokenStore';

const API_BASE_URL = '/api/listings';
const CATEGORY_API_URL = '/api/categories';

const getAuthHeaders = () => {
    const token = tokenStore.get()?.accessToken;
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const searchListings = async (filters = {}) => {
    const queryParams = new URLSearchParams();
    if (filters.keyword) queryParams.append('keyword', filters.keyword);
    if (filters.categoryId) queryParams.append('categoryId', filters.categoryId);
    if (filters.minPrice) queryParams.append('minPrice', filters.minPrice);
    if (filters.maxPrice) queryParams.append('maxPrice', filters.maxPrice);
    if (filters.endDate) queryParams.append('endDate', filters.endDate);

    const url = `${API_BASE_URL}?${queryParams.toString()}`;

    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error("Failed to fetch listings");
    return response.json();
};

export const getListingById = async (id) => {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error("Failed to fetch listing details");
    return response.json();
};

export const createListing = async (listingData) => {
    const response = await fetch(API_BASE_URL, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(listingData),
    });

    if (!response.ok) throw new Error("Failed to create listing");
    return response.json();
};

export const updateListing = async (id, updateData) => {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(updateData),
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Failed to update listing (it may already have bids)");
    }
    return response.json();
};

export const cancelListing = async (id) => {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Failed to cancel listing (it may already have bids)");
    }

    return true;
};

export const fetchCategories = async () => {
    const response = await fetch(CATEGORY_API_URL, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error("Failed to fetch categories");
    return response.json();
};
