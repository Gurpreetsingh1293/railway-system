import React, { createContext, useContext, useState, useEffect } from 'react';
import { message } from 'antd';
import { configApi } from '../api/configApi';
import { defectApi } from '../api/defectApi';

const ScoringContext = createContext();

export function ScoringProvider({ children }) {
  const [scoringMode, setScoringMode] = useState('RULE_BASED');
  const [toggling, setToggling] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(Date.now());

  useEffect(() => {
    fetchCurrentMode();
  }, []);

  async function fetchCurrentMode() {
    try {
      const res = await configApi.getScoringConfig();
      if (res.data?.data?.mode) {
        setScoringMode(res.data.data.mode);
      }
    } catch (e) {
      console.warn('Could not fetch current scoring mode', e);
    }
  }

  async function switchMode(newMode) {
    if (toggling || newMode === scoringMode) return;
    setToggling(true);
    try {
      // 1. Update backend scoring configuration
      await configApi.updateScoringConfig({ mode: newMode });
      setScoringMode(newMode);

      // 2. Automatically trigger defect re-scoring with the newly selected engine
      await defectApi.rescoreAll();

      setLastUpdated(Date.now());

      if (newMode === 'ML') {
        message.success('Switched to AI/ML Mode (Gradient Boosting Model active)');
      } else {
        message.info('Switched to Rule-Based Mode (Deterministic Formula active)');
      }
    } catch (err) {
      console.error('Failed to switch scoring mode:', err);
      message.error('Failed to switch scoring mode. Please check if backend/ML service is running.');
    } finally {
      setToggling(false);
    }
  }

  return (
    <ScoringContext.Provider value={{ scoringMode, switchMode, toggling, lastUpdated }}>
      {children}
    </ScoringContext.Provider>
  );
}

export function useScoring() {
  return useContext(ScoringContext);
}
