import { useEffect, useState } from 'react';
import { Button, Row, Col, Select, Space, Tag, Typography, Spin, message, Empty, Card, Badge } from 'antd';
import { CalendarOutlined, RocketOutlined } from '@ant-design/icons';
import { scheduleApi } from '../api/scheduleApi';
import { corridorApi } from '../api/otherApis';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

// Colors for departments in Gantt bars
const DEPT_COLORS = {
  'Engineering': '#4f8ef7',
  'S&T': '#a371f7',
  'Traction Distribution': '#3dd68c',
};

function GanttBar({ block }) {
  const depts = (block.departmentsInvolved || '').split(',').map(d => d.trim());
  const isBundled = block.bundled || block.isBundled;

  return (
    <div
      style={{
        background: isBundled
          ? 'linear-gradient(90deg, #a371f7, #4f8ef7)'
          : DEPT_COLORS[depts[0]] || '#4f8ef7',
        borderRadius: 6,
        padding: '8px 12px',
        marginBottom: 8,
        cursor: 'pointer',
        transition: 'transform 0.15s, box-shadow 0.15s',
        boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
        position: 'relative',
        overflow: 'hidden',
      }}
      onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-1px)'; e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.4)'; }}
      onMouseLeave={e => { e.currentTarget.style.transform = 'none'; e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.3)'; }}
    >
      {/* Shimmer overlay for bundled */}
      {isBundled && (
        <div style={{
          position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
          background: 'linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.05) 50%, transparent 100%)',
          backgroundSize: '200% 100%',
          animation: 'shimmer 2s infinite',
        }} />
      )}

      <Row justify="space-between" align="middle">
        <Col>
          <Text strong style={{ color: '#fff', fontSize: 13 }}>
            {block.corridorName}
          </Text>
          <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.8)', marginTop: 2 }}>
            {block.blockDate} · {block.blockStartHour}:00 · {block.blockDurationHours}h
          </div>
          <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.7)', marginTop: 2 }}>
            {block.departmentsInvolved}
          </div>
        </Col>
        <Col>
          <Space direction="vertical" align="end" size={2}>
            {isBundled && <Tag color="gold" style={{ fontSize: 10 }}>BUNDLED</Tag>}
            <Text style={{ color: 'rgba(255,255,255,0.9)', fontSize: 11, fontWeight: 600 }}>
              Score: {block.totalPriorityScore ? Number(block.totalPriorityScore).toFixed(1) : '—'}
            </Text>
          </Space>
        </Col>
      </Row>
    </div>
  );
}

export default function SchedulePage() {
  const [plan, setPlan] = useState([]);
  const [corridors, setCorridors] = useState([]);
  const [horizon, setHorizon] = useState('WEEKLY');
  const [corridorFilter, setCorridorFilter] = useState(null);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [scoringMode, setScoringMode] = useState('RULE_BASED');
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    corridorApi.getAll().then(r => setCorridors(r.data.data || []));
    loadPlan();
  }, []);

  useEffect(() => { loadPlan(); }, [horizon, corridorFilter]);

  async function loadPlan() {
    setLoading(true);
    try {
      const res = await scheduleApi.getPlan(horizon, corridorFilter);
      setPlan(res.data.data || []);
      setScoringMode(res.data.scoringMode || 'RULE_BASED');
    } catch {
      messageApi.warning('Could not load plan');
    } finally {
      setLoading(false);
    }
  }

  async function generate() {
    setGenerating(true);
    try {
      const res = await scheduleApi.generate(horizon);
      setPlan(res.data.data || []);
      setScoringMode(res.data.scoringMode || 'RULE_BASED');
      messageApi.success(`${horizon} plan generated — ${res.data.data?.length || 0} blocks scheduled`);
    } catch {
      messageApi.error('Plan generation failed');
    } finally {
      setGenerating(false);
    }
  }

  // Group plan by date for timeline view
  const byDate = plan.reduce((acc, block) => {
    const d = block.blockDate;
    if (!acc[d]) acc[d] = [];
    acc[d].push(block);
    return acc;
  }, {});

  return (
    <div className="animate-in">
      {contextHolder}
      <style>{`
        @keyframes shimmer {
          0% { background-position: 200% 0; }
          100% { background-position: -200% 0; }
        }
      `}</style>

      {/* Header */}
      <div className="page-header" style={{ borderRadius: 12, marginBottom: 24 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0, color: 'var(--text-primary)' }}>
              📅 Block Schedule
            </Title>
            <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              Optimized maintenance windows · Gantt Timeline
            </Text>
          </Col>
          <Col>
            <Space>
              <Tag className={scoringMode === 'ML' ? 'scoring-badge-ml' : 'scoring-badge-rule'}
                style={{ padding: '4px 12px', fontSize: 12, borderRadius: 20 }}>
                {scoringMode === 'ML' ? '🟢 AI/ML' : '🔵 Rule-Based'}
              </Tag>
              <Button type="primary" icon={<RocketOutlined />} loading={generating} onClick={generate}>
                Generate {horizon} Plan
              </Button>
            </Space>
          </Col>
        </Row>
      </div>

      {/* Controls */}
      <Space wrap style={{ marginBottom: 20 }}>
        <Select
          value={horizon}
          onChange={setHorizon}
          style={{ width: 130 }}
          options={[{ value: 'WEEKLY', label: '7-Day Plan' }, { value: 'MONTHLY', label: '30-Day Plan' }]}
        />
        <Select
          placeholder="Filter by corridor"
          allowClear
          style={{ width: 220 }}
          options={corridors.map(c => ({ value: c.id, label: c.corridorName }))}
          onChange={v => setCorridorFilter(v || null)}
        />
        <Text style={{ color: 'var(--text-muted)' }}>
          {plan.length} blocks · {plan.filter(b => b.bundled || b.isBundled).length} bundled
        </Text>
      </Space>

      {/* Legend */}
      <Space wrap style={{ marginBottom: 16 }}>
        <Tag color="gold">BUNDLED — multiple depts share one window</Tag>
        {Object.entries(DEPT_COLORS).map(([d, c]) => (
          <Tag key={d} style={{ background: c + '22', color: c, borderColor: c }}>● {d}</Tag>
        ))}
      </Space>

      <Spin spinning={loading || generating}>
        {Object.keys(byDate).length === 0 ? (
          <Empty
            description="No scheduled blocks. Click 'Generate' to run the optimizer."
            style={{ padding: 60 }}
          />
        ) : (
          <Row gutter={[16, 16]}>
            {Object.entries(byDate).sort(([a],[b]) => a.localeCompare(b)).map(([date, blocks]) => (
              <Col xs={24} md={12} xl={8} key={date}>
                <Card
                  title={
                    <Space>
                      <CalendarOutlined style={{ color: 'var(--accent-blue)' }} />
                      <span>{dayjs(date).format('ddd, MMM D')}</span>
                      <Badge count={blocks.length} style={{ backgroundColor: 'var(--accent-blue)' }} />
                    </Space>
                  }
                  style={{ background: 'var(--bg-card)', border: '1px solid var(--border)' }}
                  headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}
                  bodyStyle={{ padding: 12 }}
                >
                  {blocks
                    .sort((a, b) => a.blockStartHour - b.blockStartHour)
                    .map(b => <GanttBar key={b.id} block={b} />)
                  }
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Spin>
    </div>
  );
}
