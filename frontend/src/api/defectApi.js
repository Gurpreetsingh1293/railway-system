import apiClient from './apiClient';

/** All defect-related API calls */
export const defectApi = {
  /** Get all defects with optional filters */
  getAll: (params = {}) =>
    apiClient.get('/api/v1/defects', { params }),

  /** Get single defect */
  getById: (id) =>
    apiClient.get(`/api/v1/defects/${id}`),

  /** Trigger re-scoring of all active defects */
  rescoreAll: () =>
    apiClient.post('/api/v1/defects/score'),
};
