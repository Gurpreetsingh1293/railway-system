import { useEffect, useState } from 'react';
import { Row, Col, Card, Form, Slider, Select, Button, Typography, Space, Tag, message, Spin, Divider } from 'antd';
import { SettingOutlined, SaveOutlined } from '@ant-design/icons';
import { configApi } from '../api/configApi';

const { Title, Text } = Typography;

export default function ConfigPage() {
  const [config, setConfig] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => { loadConfig(); }, []);

  async function loadConfig() {
    setLoading(true);
    try {
      const res = await configApi.getScoringConfig();
      const d = res.data.data;
      setConfig(d);
      form.setFieldsValue({
        mode: d.mode,
        severityCritical: d.severityCritical,
        severityMajor: d.severityMajor,
        severityMinor: d.severityMinor,
        overdueFactor: d.overdueFactor,
        maxOverdueDays: d.maxOverdueDays,
        safetyHigh: d.safetyHigh,
        safetyMedium: d.safetyMedium,
        safetyLow: d.safetyLow,
      });
    } catch {
      messageApi.warning('Backend not reachable');
    } finally {
      setLoading(false);
    }
  }

  async function handleSave(values) {
    setSaving(true);
    try {
      const res = await configApi.updateScoringConfig(values);
      setConfig(res.data.data);
      messageApi.success('Scoring configuration updated! Re-score defects to apply changes.');
    } catch {
      messageApi.error('Failed to save config');
    } finally {
      setSaving(false);
    }
  }

  const currentMode = form.getFieldValue('mode') || config?.mode || 'RULE_BASED';

  return (
    <div className="animate-in">
      {contextHolder}

      <div className="page-header" style={{ borderRadius: 12, marginBottom: 24 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0, color: 'var(--text-primary)' }}>
              ⚙️ Scoring Configuration
            </Title>
            <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              Tune priority weights live — no restart needed
            </Text>
          </Col>
          <Col>
            <Tag className={currentMode === 'ML' ? 'scoring-badge-ml' : 'scoring-badge-rule'}
              style={{ padding: '4px 12px', fontSize: 12, borderRadius: 20 }}>
              Active: {currentMode === 'ML' ? '🟢 AI/ML Mode' : '🔵 Rule-Based Mode'}
            </Tag>
          </Col>
        </Row>
      </div>

      {/* Formula explanation */}
      <Card style={{ background: 'rgba(79,142,247,0.05)', border: '1px solid rgba(79,142,247,0.2)', marginBottom: 24, borderRadius: 12 }}>
        <Title level={5} style={{ color: 'var(--accent-blue)', marginTop: 0 }}>
          📐 Priority Score Formula (Rule-Based)
        </Title>
        <code style={{ color: 'var(--accent-green)', fontSize: 14 }}>
          score = severityWeight × (1 + min(daysOverdue, maxDays)/maxDays × overdueFactor) × safetyRiskWeight
        </code>
        <div style={{ marginTop: 12, color: 'var(--text-muted)', fontSize: 12 }}>
          Higher score = higher priority. Critical bridge with 10 days overdue gets the highest score.
        </div>
      </Card>

      <Spin spinning={loading}>
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Row gutter={[24, 0]}>
            {/* Scoring Mode */}
            <Col xs={24} md={12}>
              <Card title="Scoring Mode" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 12 }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                <Form.Item name="mode" label="Active Mode">
                  <Select
                    options={[
                      { value: 'RULE_BASED', label: '🔵 Rule-Based (always available)' },
                      { value: 'ML', label: '🟢 AI/ML (requires ML microservice)' },
                    ]}
                  />
                </Form.Item>
                <Text style={{ color: 'var(--text-muted)', fontSize: 12 }}>
                  ML mode calls the FastAPI microservice. Falls back to Rule-Based if unreachable.
                </Text>
              </Card>
            </Col>

            {/* Severity Weights */}
            <Col xs={24} md={12}>
              <Card title="Severity Weights" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 12 }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                <Form.Item name="severityCritical" label={<span style={{ color: '#f85149' }}>Critical weight</span>}>
                  <Slider min={1} max={20} step={0.5} marks={{ 1: '1', 10: '10', 20: '20' }} />
                </Form.Item>
                <Form.Item name="severityMajor" label={<span style={{ color: '#f5a623' }}>Major weight</span>}>
                  <Slider min={1} max={15} step={0.5} marks={{ 1: '1', 5: '5', 15: '15' }} />
                </Form.Item>
                <Form.Item name="severityMinor" label={<span style={{ color: '#3dd68c' }}>Minor weight</span>}>
                  <Slider min={0.5} max={5} step={0.5} marks={{ 0.5: '0.5', 1: '1', 5: '5' }} />
                </Form.Item>
              </Card>
            </Col>

            {/* Overdue Factor */}
            <Col xs={24} md={12}>
              <Card title="Overdue Factor" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 12, marginTop: 16 }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                <Form.Item name="overdueFactor" label="Overdue multiplier (0–1)">
                  <Slider min={0} max={1} step={0.05} marks={{ 0: '0', 0.5: '0.5', 1: '1' }} />
                </Form.Item>
                <Form.Item name="maxOverdueDays" label="Max overdue days (cap)">
                  <Slider min={7} max={90} step={1} marks={{ 7: '7d', 30: '30d', 90: '90d' }} />
                </Form.Item>
              </Card>
            </Col>

            {/* Safety Risk Weights */}
            <Col xs={24} md={12}>
              <Card title="Safety Risk Weights" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 12, marginTop: 16 }}
                headStyle={{ borderBottom: '1px solid var(--border)', color: 'var(--text-primary)' }}>
                <Form.Item name="safetyHigh" label="High risk (Bridge/Crossing/Sub-station)">
                  <Slider min={1} max={5} step={0.5} marks={{ 1: '1', 3: '3', 5: '5' }} />
                </Form.Item>
                <Form.Item name="safetyMedium" label="Medium risk (Rail Track/OHE Wire/Signal)">
                  <Slider min={1} max={4} step={0.5} marks={{ 1: '1', 2: '2', 4: '4' }} />
                </Form.Item>
                <Form.Item name="safetyLow" label="Low risk (other assets)">
                  <Slider min={0.5} max={2} step={0.5} marks={{ 0.5: '0.5', 1: '1', 2: '2' }} />
                </Form.Item>
              </Card>
            </Col>
          </Row>

          <div style={{ marginTop: 24, textAlign: 'right' }}>
            <Space>
              <Button onClick={loadConfig}>Reset to Saved</Button>
              <Button type="primary" icon={<SaveOutlined />} htmlType="submit" loading={saving}>
                Save Configuration
              </Button>
            </Space>
          </div>
        </Form>
      </Spin>
    </div>
  );
}
