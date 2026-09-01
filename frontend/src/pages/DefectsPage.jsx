import { useEffect, useState } from 'react';
import { Table, Tag, Select, Space, Button, Typography, message, Spin, Tooltip } from 'antd';
import { SearchOutlined, ReloadOutlined, RobotOutlined, CalculatorOutlined } from '@ant-design/icons';
import { defectApi } from '../api/defectApi';
import { useScoring } from '../context/ScoringContext';

const { Title, Text } = Typography;

const SEV_COLOR = { Critical: 'error', Major: 'warning', Minor: 'success' };
const SRC_COLOR = { TMS: 'blue', SMMS: 'purple', TDMS: 'green' };
const STATUS_COLOR = { Open: 'blue', Overdue: 'red', Scheduled: 'cyan', Closed: 'default' };

export default function DefectsPage() {
  const { scoringMode, switchMode, toggling, lastUpdated } = useScoring();
  const [defects, setDefects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({});
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    loadDefects();
  }, [filters, lastUpdated]);

  async function loadDefects() {
    setLoading(true);
    try {
      const res = await defectApi.getAll(filters);
      setDefects(res.data?.data || []);
    } catch {
      messageApi.warning('Backend not reachable');
    } finally {
      setLoading(false);
    }
  }

  async function handleRescore() {
    setLoading(true);
    try {
      await defectApi.rescoreAll();
      messageApi.success(`Priority scores recomputed using ${scoringMode === 'ML' ? 'AI/ML' : 'Rule-Based'} engine!`);
      loadDefects();
    } catch {
      messageApi.error('Scoring failed');
    } finally {
      setLoading(false);
    }
  }

  const isMlMode = scoringMode === 'ML';

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 60,
      sorter: (a, b) => a.id - b.id,
    },
    {
      title: 'Source',
      dataIndex: 'sourceSystem',
      width: 80,
      render: (v) => <Tag color={SRC_COLOR[v] || 'default'}>{v}</Tag>,
    },
    {
      title: 'Department',
      dataIndex: 'department',
      width: 160,
      ellipsis: true,
    },
    {
      title: 'Corridor',
      dataIndex: 'corridorName',
      ellipsis: true,
    },
    {
      title: 'Asset',
      dataIndex: 'assetType',
      width: 110,
    },
    {
      title: 'Severity',
      dataIndex: 'severity',
      width: 90,
      render: (v) => <Tag color={SEV_COLOR[v] || 'default'}>{v}</Tag>,
      sorter: (a, b) => ['Critical','Major','Minor'].indexOf(a.severity) - ['Critical','Major','Minor'].indexOf(b.severity),
    },
    {
      title: 'Due Date',
      dataIndex: 'dueDate',
      width: 110,
      render: (v) => {
        const overdue = new Date(v) < new Date();
        return <span style={{ color: overdue ? 'var(--accent-red)' : 'inherit' }}>{v}</span>;
      },
      sorter: (a, b) => new Date(a.dueDate) - new Date(b.dueDate),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      width: 100,
      render: (v) => <Tag color={STATUS_COLOR[v] || 'default'}>{v}</Tag>,
    },
    {
      title: 'Est. Hours',
      dataIndex: 'estimatedRepairHours',
      width: 90,
      render: (v) => `${v}h`,
      sorter: (a, b) => a.estimatedRepairHours - b.estimatedRepairHours,
    },
    {
      title: (
        <span>
          Priority Score{' '}
          <Tag color={isMlMode ? 'green' : 'blue'} style={{ fontSize: 10, marginLeft: 4 }}>
            {isMlMode ? 'ML' : 'RULE'}
          </Tag>
        </span>
      ),
      dataIndex: 'priorityScore',
      width: 130,
      render: (v) => (
        <span style={{
          color: v >= 20 ? 'var(--accent-red)' : v >= 10 ? 'var(--accent-amber)' : 'var(--accent-green)',
          fontWeight: 700,
        }}>
          {v ? Number(v).toFixed(2) : '—'}
        </span>
      ),
      sorter: (a, b) => (a.priorityScore || 0) - (b.priorityScore || 0),
      defaultSortOrder: 'descend',
    },
  ];

  return (
    <div className="animate-in">
      {contextHolder}

      {/* Header */}
      <div className="page-header" style={{ borderRadius: 12, marginBottom: 24 }}>
        <Space justify="space-between" style={{ width: '100%', display: 'flex', flexWrap: 'wrap' }}>
          <div>
            <Title level={3} style={{ margin: 0, color: 'var(--text-primary)' }}>
              🔧 Defects Backlog
            </Title>
            <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              Maintenance defects from TMS · SMMS · TDMS (Ranked by Priority Score)
            </Text>
          </div>
          <Space>
            {/* 1-Click Mode Switcher */}
            <Tooltip title={`Switch active model to ${isMlMode ? 'Rule-Based' : 'AI/ML'}`}>
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
                {isMlMode ? '🟢 AI/ML Model' : '🔵 Rule-Based'} (Click to Toggle)
              </Button>
            </Tooltip>

            <Button icon={<ReloadOutlined />} onClick={handleRescore} loading={loading}>
              Re-score
            </Button>
          </Space>
        </Space>
      </div>

      {/* Filters */}
      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          placeholder="Severity"
          allowClear
          style={{ width: 130 }}
          options={['Critical','Major','Minor'].map(s => ({ value: s, label: s }))}
          onChange={v => setFilters(f => ({ ...f, severity: v }))}
        />
        <Select
          placeholder="Source System"
          allowClear
          style={{ width: 140 }}
          options={['TMS','SMMS','TDMS'].map(s => ({ value: s, label: s }))}
          onChange={v => setFilters(f => ({ ...f, sourceSystem: v }))}
        />
        <Select
          placeholder="Status"
          allowClear
          style={{ width: 130 }}
          options={['Open','Overdue','Scheduled','Closed'].map(s => ({ value: s, label: s }))}
          onChange={v => setFilters(f => ({ ...f, status: v }))}
        />
      </Space>

      {/* Table */}
      <Spin spinning={loading}>
        <Table
          dataSource={defects}
          columns={columns}
          rowKey="id"
          size="small"
          pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `${t} defects` }}
          style={{ background: 'var(--bg-card)', borderRadius: 12 }}
          scroll={{ x: 900 }}
          rowClassName={(r) => r.status === 'Overdue' ? 'overdue-row' : ''}
        />
      </Spin>
    </div>
  );
}
