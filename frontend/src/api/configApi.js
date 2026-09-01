import apiClient from './apiClient';

/** Scoring configuration API calls */
export const configApi = {
  /** Get current scoring weights and mode */
  getScoringConfig: () =>
    apiClient.get('/api/v1/config/scoring'),

  /** Update scoring weights and/or mode */
  updateScoringConfig: (dto) =>
    apiClient.put('/api/v1/config/scoring', dto),
};
