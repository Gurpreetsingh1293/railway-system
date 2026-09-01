import { Routes, Route, Navigate } from 'react-router-dom';
import { ScoringProvider } from './context/ScoringContext';
import AppLayout from './components/AppLayout';
import DashboardPage from './pages/DashboardPage';
import DefectsPage from './pages/DefectsPage';
import SchedulePage from './pages/SchedulePage';
import ComparisonPage from './pages/ComparisonPage';
import ConfigPage from './pages/ConfigPage';

export default function App() {
  return (
    <ScoringProvider>
      <AppLayout>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/defects" element={<DefectsPage />} />
          <Route path="/schedule" element={<SchedulePage />} />
          <Route path="/comparison" element={<ComparisonPage />} />
          <Route path="/config" element={<ConfigPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AppLayout>
    </ScoringProvider>
  );
}
