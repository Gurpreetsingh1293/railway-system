import apiClient from './apiClient';

/** Corridor API calls */
export const corridorApi = {
  getAll: () => apiClient.get('/api/v1/corridors'),
  getById: (id) => apiClient.get(`/api/v1/corridors/${id}`),
};

/** Block request API calls */
export const blockRequestApi = {
  getAll: (params = {}) => apiClient.get('/api/v1/block-requests', { params }),
  approve: (id) => apiClient.patch(`/api/v1/block-requests/${id}/approve`),
  reject: (id) => apiClient.patch(`/api/v1/block-requests/${id}/reject`),
};

/** Availability windows */
export const availabilityApi = {
  getAll: (params = {}) => apiClient.get('/api/v1/availability', { params }),
};
