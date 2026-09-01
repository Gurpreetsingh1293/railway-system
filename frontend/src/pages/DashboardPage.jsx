import { useEffect, useState } from 'react';
import { Row, Col, Card, Button, Typography, Space, Tag, Spin, message, Switch, Tooltip } from 'antd';
import {
  AlertOutlined, CheckCircleOutlined, ClockCircleOutlined,
  ThunderboltOutlined, SyncOutlined, RocketOutlined,
  RobotOutlined, CalculatorOutlined
} from '@ant-design/icons';
import { defectApi } from '../api/defectApi';
import { scheduleApi } from '../api/scheduleApi';
import { blockRequestApi } from '../api/otherApis';
import { useScoring } from '../context/ScoringContext';

const { Title, Text } = Typography;

const SEVERITY_COLORS = { Critical: '#f85149', Major: '#f5a623', Minor: '#3dd68c' };

export default function DashboardPage() {
  const { scoringMode, switchMode, toggling, lastUpdated } = useScoring();
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [stats, setStats] = useState(null);
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    loadStats();
  }, [lastUpdated]);

  async function loadStats() {
    setLoading(true);
    try {
      const [defRes, blockRes, planRes] = await Promise.all([
        defectApi.getAll(),
        blockRequestApi.getAll(),
        scheduleApi.getPlan('WEEKLY'),
      ]);

      const defects = defRes.data?.data || [];
      const requests = blockRes.data?.data || [];
      const plan = planRes.data?.data || [];

      const bySeverity = defects.reduce((acc, d) => {
        acc[d.severity] = (acc[d.severity] || 0) + 1;
        return acc;
      }, {});

      setStats({
        totalDefects: defects.length,
        openDefects: defects.filter(d => d.status === 'Open' || d.status === 'Overdue').length,
        overdueDefects: defects.filter(d => d.status === 'Overdue').length,
        criticalDefects: bySeverity.Critical || 0,
        pendingRequests: requests.filter(r => r.approvalStatus === 'Pending').length,
        scheduledBlocks: plan.length,
        bundledBlocks: plan.filter(b => b.isBundled).length,
        bySeverity,
      });
    } catch (e) {
      console.error(e);
      messageApi.warning('Could not load dashboard stats — check backend connection.');
    } finally {
      setLoading(false);
    }
  }

  async function handleGeneratePlan() {
    setGenerating(true);
    try {
      const res = await scheduleApi.generate('WEEKLY');
      const blocks = res.data?.data || [];
      messageApi.success(`Generated ${blocks.length} scheduled blocks for the weekly plan!`);
      loadStats();
    } catch (e) {
      messageApi.error('Failed to generate plan — check backend logs.');
    } finally {
      setGenerating(false);
    }
  }

  const isMlMode = scoringMode === 'ML';

  const kpiData = stats ? [
    { label: 'Total Defects', value: stats.totalDefects, icon: <AlertOutlined />, color: '#4f8ef7' },
    { label: 'Open / Overdue', value: stats.openDefects, icon: <ClockCircleOutlined />, color: '#f85149' },
    { label: 'Critical Defects', value: stats.criticalDefects, icon: <ThunderboltOutlined />, color: '#f5a623' },
    { label: 'Scheduled Blocks', value: stats.scheduledBlocks, icon: <CheckCircleOutlined />, color: '#3dd68c' },
  ] : [];

  return (
    <div className="animate-in">
      {contextHolder}

      {/* Page header */}
      <div className="page-header" style={{ marginBottom: 24, borderRadius: 12 }}>
        <Row justify="space-between" align="middle" gutter={[16, 16]}>
          <Col xs={24} lg={12}>
            <Title level={3} style={{ margin: 0, color: 'var(--text-primary)' }}>
              🚆 Block Planning Dashboard
            </Title>
            <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              AI-optimized maintenance scheduling · SIH PS 26027
            </Text>
          </Col>
          <Col xs={24} lg={12} style={{ textAlign: 'right' }}>
            <Space wrap size="middle">
              {/* Interactive Mode Switcher with Visual Indicators */}
              <Tooltip title={`Click to switch to ${isMlMode ? 'Rule-Based' : 'AI/ML'} scoring mode`}>
                <Button
                  onClick={() => switchMode(isMlMode ? 'RULE_BASED' : 'ML')}
                  loading={toggling}
                  icon={isMlMode ? <RobotOutlined style={{ color: '#3dd68c' }} /> : <CalculatorOutlined style={{ color: '#4f8ef7' }} />}
                  style={{
                    borderRadius: 20,
                    borderColor: isMlMode ? '#3dd68c' : '#4f8ef7',
                    background: isMlMode ? 'rgba(61,214,140,0.1)' : 'rgba(79,142,247,0.1)',
                    color: isMlMode ? '#3dd68c' : '#4f8ef7',
                    fontWeight: 600,
                  }}
                >
                  Mode: {isMlMode ? '🟢 AI/ML Scorer (Active)' : '🔵 Rule-Based (Active)'}
                </Button>
              </Tooltip>

              <Button
                type="primary"
                icon={<RocketOutlined />}
                loading={generating}
                onClick={handleGeneratePlan}
                style={{ background: 'var(--accent-blue)', borderColor: 'var(--accent-blue)' }}
              >
                Generate Weekly Plan
              </Button>
              <Button icon={<SyncOutlined spin={loading} />} onClick={loadStats}>
                Refresh
              </Button>
            </Space>
          </Col>
        </Row>
      </div>

      {/* Mode Explanation Banner */}
      <div style={{
        background: isMlMode ? 'rgba(61, 214, 140, 0.08)' : 'rgba(79, 142, 247, 0.08)',
        border: `1px solid ${isMlMode ? 'rgba(61, 214, 140, 0.3)' : 'rgba(79, 142, 247, 0.3)'}`,
        borderRadius: 10,
        padding: '12px 18px',
        marginBottom: 20,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: 10,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          {isMlMode ? <RobotOutlined style={{ fontSize: 20, color: '#3dd68c' }} /> : <CalculatorOutlined style={{ fontSize: 20, color: '#4f8ef7' }} />}
          <div>
            <span style={{ fontWeight: 600, color: isMlMode ? '#3dd68c' : '#4f8ef7', marginRight: 8 }}>
              {isMlMode ? 'Gradient Boosting ML Microservice Enabled' : 'Transparent Rule-Based Formula Engine Enabled'}
            </span>
            <Text style={{ color: 'var(--text-muted)', fontSize: 12 }}>
              {isMlMode
                ? 'Predictions served by FastAPI endpoint (POST /score) running scikit-learn model.'
                : 'Scoring: Severity × (1 + OverdueFactor) × SafetyRiskWeight (Tuned via Config).'}
            </Text>
          </div>
        </div>
        <Button
          size="small"
          type="link"
          onClick={() => switchMode(isMlMode ? 'RULE_BASED' : 'ML')}
          style={{ color: isMlMode ? '#3dd68c' : '#4f8ef7', fontWeight: 600, padding: 0 }}
        >
          Switch to {isMlMode ? 'Rule-Based Formula' : 'AI/ML Model'} ➔
        </Button>
      </div>

      {/* KPI Cards */}
      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          {kpiData.map((kpi, i) => (
            <Col xs={24} sm={12} xl={6} key={i}>
              <div className="kpi-card animate-in" style={{ animationDelay: `${i * 0.07}s` }}>
                <Row justify="space-between" align="middle">
                  <Col>
                    <div className="kpi-value" style={{
                      background: `linear-gradient(135deg, ${kpi.color}, #4f8ef7)`,
                      WebkitBackgroundClip: 'text',
                      WebkitTextFillColor: 'transparent',
                    }}>
                      {kpi.value ?? '—'}
                    </div>
                    <div className="kpi-label">{kpi.label}</div>
                  </Col>
                  <Col>
                    <div style={{ fontSize: 28, color: kpi.color, opacity: 0.6 }}>
                      {kpi.icon}
                    </div>
                  </Col>
                </Row>
              </div>
            </Col>
          ))}
        </Row>

        {/* Severity breakdown & Quick actions */}
        {stats && (
          <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
            <Col xs={24} md={12}>
              <Card
                title="Defects by Severity"
                style={{ background: 'var(--bg-card)', border: '1px solid var(--border)' }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}
              >
                <Space direction="vertical" style={{ width: '100%' }}>
                  {Object.entries(stats.bySeverity).map(([sev, count]) => (
                    <Row key={sev} justify="space-between">
                      <Col>
                        <span className={`severity-${sev.toLowerCase()}`}>● {sev}</span>
                      </Col>
                      <Col>
                        <strong style={{ color: SEVERITY_COLORS[sev] }}>{count}</strong>
                      </Col>
                    </Row>
                  ))}
                </Space>
              </Card>
            </Col>
            <Col xs={24} md={12}>
              <Card
                title="Optimization Controls"
                style={{ background: 'var(--bg-card)', border: '1px solid var(--border)' }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}
              >
                <Space direction="vertical" style={{ width: '100%' }}>
                  <Button
                    block
                    icon={<SyncOutlined />}
                    onClick={() => defectApi.rescoreAll().then(() => { messageApi.success('Defects re-scored with current engine!'); loadStats(); })}
                  >
                    Re-score All Active Defects
                  </Button>
                  <Button
                    block
                    type="primary"
                    icon={<RocketOutlined />}
                    onClick={handleGeneratePlan}
                    loading={generating}
                  >
                    Generate Weekly Bundled Plan
                  </Button>
                  <Button
                    block
                    onClick={() => scheduleApi.generate('MONTHLY').then(() => { messageApi.success('Monthly plan generated!'); loadStats(); })}
                  >
                    Generate Monthly Horizon Plan
                  </Button>
                </Space>
              </Card>
            </Col>
          </Row>
        )}
      </Spin>
    </div>
  );
}
