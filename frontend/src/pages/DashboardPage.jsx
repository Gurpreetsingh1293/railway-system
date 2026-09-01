import { useEffect, useState } from 'react';
import { Row, Col, Statistic, Card, Button, Typography, Space, Tag, Spin, message } from 'antd';
import {
  AlertOutlined, CheckCircleOutlined, ClockCircleOutlined,
  ThunderboltOutlined, SyncOutlined, RocketOutlined
} from '@ant-design/icons';
import { defectApi } from '../api/defectApi';
import { scheduleApi } from '../api/scheduleApi';
import { blockRequestApi } from '../api/otherApis';

const { Title, Text } = Typography;

const SEVERITY_COLORS = { Critical: '#f85149', Major: '#f5a623', Minor: '#3dd68c' };

export default function DashboardPage() {
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [stats, setStats] = useState(null);
  const [scoringMode, setScoringMode] = useState('RULE_BASED');
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => { loadStats(); }, []);

  async function loadStats() {
    setLoading(true);
    try {
      const [defRes, blockRes, planRes] = await Promise.all([
        defectApi.getAll(),
        blockRequestApi.getAll(),
        scheduleApi.getPlan('WEEKLY'),
      ]);

      const defects = defRes.data.data || [];
      const requests = blockRes.data.data || [];
      const plan = planRes.data.data || [];

      setScoringMode(defRes.data.scoringMode || 'RULE_BASED');

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
        bundledBlocks: plan.filter(b => b.bundled).length,
        bySeverity,
      });
    } catch (e) {
      messageApi.warning('Could not load dashboard stats — is the backend running?');
    } finally {
      setLoading(false);
    }
  }

  async function handleGeneratePlan() {
    setGenerating(true);
    try {
      const res = await scheduleApi.generate('WEEKLY');
      const blocks = res.data.data || [];
      setScoringMode(res.data.scoringMode || 'RULE_BASED');
      messageApi.success(`Generated ${blocks.length} scheduled blocks for the weekly plan!`);
      loadStats();
    } catch (e) {
      messageApi.error('Failed to generate plan — check backend logs.');
    } finally {
      setGenerating(false);
    }
  }

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
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0, color: 'var(--text-primary)' }}>
              🚆 Block Planning Dashboard
            </Title>
            <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              AI-optimized maintenance scheduling · SIH PS 26027
            </Text>
          </Col>
          <Col>
            <Space>
              <Tag
                className={scoringMode === 'ML' ? 'scoring-badge-ml' : 'scoring-badge-rule'}
                style={{ padding: '4px 12px', fontSize: 12, borderRadius: 20 }}
              >
                {scoringMode === 'ML' ? '🟢 AI/ML Scoring' : '🔵 Rule-Based Scoring'}
              </Tag>
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

        {/* Severity breakdown */}
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
                title="Quick Actions"
                style={{ background: 'var(--bg-card)', border: '1px solid var(--border)' }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}
              >
                <Space direction="vertical" style={{ width: '100%' }}>
                  <Button block onClick={() => defectApi.rescoreAll().then(() => { messageApi.success('Defects re-scored!'); loadStats(); })}>
                    🔄 Re-score All Defects
                  </Button>
                  <Button block onClick={handleGeneratePlan} loading={generating}>
                    📅 Generate Weekly Plan
                  </Button>
                  <Button block onClick={() => scheduleApi.generate('MONTHLY').then(() => { messageApi.success('Monthly plan generated!'); loadStats(); })}>
                    📆 Generate Monthly Plan
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
