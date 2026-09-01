import apiClient from './apiClient';

/** All schedule/plan-related API calls */
export const scheduleApi = {
  /** Generate a new plan for the given horizon */
  generate: (horizon = 'WEEKLY') =>
    apiClient.post('/api/v1/plans/generate', null, { params: { horizon } }),

  /** Get current plan data (for Gantt rendering) */
  getPlan: (horizon = 'WEEKLY', corridorId = null) =>
    apiClient.get('/api/v1/plans', { params: { horizon, ...(corridorId && { corridorId }) } }),

  /** Get before/after comparison stats */
  getComparison: (horizon = 'WEEKLY') =>
    apiClient.get('/api/v1/plans/comparison', { params: { horizon } }),
};
