import { useEffect, useState } from 'react';
import { Row, Col, Statistic, Card, Select, Button, Typography, Space, Tag, Spin, message, Progress } from 'antd';
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';
import { TrophyOutlined } from '@ant-design/icons';
import { scheduleApi } from '../api/scheduleApi';

const { Title, Text } = Typography;

export default function ComparisonPage() {
  const [data, setData] = useState(null);
  const [horizon, setHorizon] = useState('WEEKLY');
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => { load(); }, [horizon]);

  async function load() {
    setLoading(true);
    try {
      const res = await scheduleApi.getComparison(horizon);
      setData(res.data.data);
    } catch {
      messageApi.warning('No plan data — generate a plan first.');
    } finally {
      setLoading(false);
    }
  }

  const chartData = data ? [
    { name: 'Without AI\n(Departments independent)', hours: data.naiveDowntimeHours, fill: '#f85149' },
    { name: 'With RailBlock AI\n(Bundled & optimized)', hours: data.optimizedDowntimeHours, fill: '#3dd68c' },
  ] : [];

  return (
    <div className="animate-in">
      {contextHolder}

      <div className="page-header" style={{ borderRadius: 12, marginBottom: 24 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0, color: 'var(--text-primary)' }}>
              📊 Before vs After Comparison
            </Title>
            <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              How much downtime does AI-based bundling save?
            </Text>
          </Col>
          <Col>
            <Space>
              <Select
                value={horizon}
                onChange={setHorizon}
                style={{ width: 130 }}
                options={[{ value: 'WEEKLY', label: '7-Day Plan' }, { value: 'MONTHLY', label: '30-Day Plan' }]}
              />
              <Button onClick={load} loading={loading}>Refresh</Button>
            </Space>
          </Col>
        </Row>
      </div>

      <Spin spinning={loading}>
        {!data ? (
          <Card style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', textAlign: 'center', padding: 60 }}>
            <Text style={{ color: 'var(--text-muted)' }}>
              No plan data yet. Go to Schedule page and generate a plan first.
            </Text>
          </Card>
        ) : (
          <Row gutter={[16, 16]}>
            {/* Savings hero card */}
            <Col xs={24}>
              <div style={{
                background: 'linear-gradient(135deg, #0d1b2a, #1c3a5e)',
                border: '1px solid rgba(79,142,247,0.3)',
                borderRadius: 16,
                padding: '32px 40px',
                textAlign: 'center',
                boxShadow: '0 8px 32px rgba(79,142,247,0.15)',
              }}>
                <TrophyOutlined style={{ fontSize: 40, color: '#f5a623', marginBottom: 8 }} />
                <Title level={1} style={{ margin: '8px 0',
                  background: 'linear-gradient(135deg, #4f8ef7, #3dd68c)',
                  WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
                }}>
                  {data.savedHours.toFixed(1)}h saved
                </Title>
                <Text style={{ color: 'var(--text-muted)', fontSize: 16 }}>
                  {data.savingsPercent}% reduction in track downtime by bundling maintenance across departments
                </Text>
                <div style={{ marginTop: 16 }}>
                  <Progress
                    percent={data.savingsPercent}
                    strokeColor={{ '0%': '#4f8ef7', '100%': '#3dd68c' }}
                    trailColor='rgba(255,255,255,0.1)'
                    style={{ maxWidth: 500, margin: '0 auto' }}
                    format={() => `${data.savingsPercent}% saved`}
                  />
                </div>
              </div>
            </Col>

            {/* Stats grid */}
            {[
              { label: 'Naive Downtime (no bundling)', value: `${data.naiveDowntimeHours.toFixed(1)}h`, color: '#f85149' },
              { label: 'Optimized Downtime (with AI)', value: `${data.optimizedDowntimeHours.toFixed(1)}h`, color: '#3dd68c' },
              { label: 'Hours Saved', value: `${data.savedHours.toFixed(1)}h`, color: '#f5a623' },
              { label: 'Blocks in Plan', value: data.totalScheduledBlocks, color: '#4f8ef7' },
              { label: 'Bundled Blocks', value: data.bundledBlocks, color: '#a371f7' },
              { label: 'Requests Scheduled', value: data.scheduledRequests, color: '#3dd68c' },
            ].map((s, i) => (
              <Col xs={12} md={8} xl={4} key={i}>
                <div className="kpi-card">
                  <div className="kpi-value" style={{
                    fontSize: '1.8rem',
                    background: `linear-gradient(135deg, ${s.color}, #4f8ef7)`,
                    WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
                  }}>
                    {s.value}
                  </div>
                  <div className="kpi-label">{s.label}</div>
                </div>
              </Col>
            ))}

            {/* Bar chart */}
            <Col xs={24}>
              <Card
                title="Downtime Comparison (Hours)"
                style={{ background: 'var(--bg-card)', border: '1px solid var(--border)' }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}
              >
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={chartData} barCategoryGap="40%">
                    <XAxis dataKey="name" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} />
                    <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 12 }} unit="h" />
                    <Tooltip
                      contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 8 }}
                      formatter={(v) => [`${v.toFixed(1)}h`, 'Track Downtime']}
                    />
                    <Bar dataKey="hours" radius={[6, 6, 0, 0]}>
                      {chartData.map((entry, index) => (
                        <Cell key={index} fill={entry.fill} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </Card>
            </Col>
          </Row>
        )}
      </Spin>
    </div>
  );
}
