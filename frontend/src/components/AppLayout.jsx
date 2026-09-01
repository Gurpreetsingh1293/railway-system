import { useState } from 'react';
import { Layout, Menu, Tag, Typography, Space, Segmented } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  AlertOutlined,
  CalendarOutlined,
  BarChartOutlined,
  SettingOutlined,
  TrademarkOutlined,
  RobotOutlined,
  CalculatorOutlined,
} from '@ant-design/icons';
import { useScoring } from '../context/ScoringContext';

const { Sider, Content, Header } = Layout;
const { Text } = Typography;

const NAV_ITEMS = [
  { key: '/',          icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/defects',   icon: <AlertOutlined />,     label: 'Defects' },
  { key: '/schedule',  icon: <CalendarOutlined />,  label: 'Block Schedule' },
  { key: '/comparison',icon: <BarChartOutlined />,  label: 'Comparison' },
  { key: '/config',    icon: <SettingOutlined />,   label: 'Config' },
];

export default function AppLayout({ children }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const { scoringMode, switchMode, toggling } = useScoring();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={220}
        style={{
          background: 'var(--bg-secondary)',
          borderRight: '1px solid var(--border)',
          position: 'fixed',
          height: '100vh',
          left: 0,
          top: 0,
          zIndex: 100,
        }}
      >
        {/* Logo / Brand */}
        <div style={{
          padding: collapsed ? '20px 16px' : '20px 24px',
          borderBottom: '1px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          gap: 10,
        }}>
          <TrademarkOutlined style={{ fontSize: 22, color: 'var(--accent-blue)' }} />
          {!collapsed && (
            <div>
              <div style={{ fontWeight: 800, fontSize: 15, color: 'var(--text-primary)', lineHeight: 1.2 }}>
                RailBlock AI
              </div>
              <div style={{ fontSize: 10, color: 'var(--text-muted)', letterSpacing: '0.06em' }}>
                SIH PS 26027
              </div>
            </div>
          )}
        </div>

        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={NAV_ITEMS}
          onClick={({ key }) => navigate(key)}
          style={{ background: 'transparent', border: 'none', marginTop: 8 }}
        />

        {/* Bottom notice */}
        {!collapsed && (
          <div style={{
            position: 'absolute', bottom: 60, left: 0, right: 0,
            padding: '0 16px',
          }}>
            <Tag color="warning" style={{ fontSize: 10, width: '100%', textAlign: 'center' }}>
              SYNTHETIC DATA
            </Tag>
          </div>
        )}
      </Sider>

      <Layout style={{
        marginLeft: collapsed ? 80 : 220,
        transition: 'margin-left 0.2s',
        background: 'var(--bg-primary)',
      }}>
        {/* Top header bar */}
        <Header style={{
          background: 'var(--bg-secondary)',
          borderBottom: '1px solid var(--border)',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          height: 60,
          position: 'sticky',
          top: 0,
          zIndex: 99,
        }}>
          <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
            Ministry of Railways · AI-Powered Block Planning
          </Text>
          <Space size="middle" align="middle">
            {/* Interactive AI / Rule-Based Toggle Switch in Header */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              background: 'rgba(255, 255, 255, 0.05)',
              padding: '3px 6px',
              borderRadius: '8px',
              border: '1px solid var(--border)',
            }}>
              <Text style={{ fontSize: 12, marginRight: 8, color: 'var(--text-muted)' }}>
                Scoring Engine:
              </Text>
              <Segmented
                disabled={toggling}
                value={scoringMode}
                onChange={(value) => switchMode(value)}
                options={[
                  {
                    label: (
                      <span style={{ padding: '0 4px', display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                        <CalculatorOutlined style={{ color: scoringMode === 'RULE_BASED' ? '#4f8ef7' : 'inherit' }} />
                        <span>Rule-Based</span>
                      </span>
                    ),
                    value: 'RULE_BASED',
                  },
                  {
                    label: (
                      <span style={{ padding: '0 4px', display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                        <RobotOutlined style={{ color: scoringMode === 'ML' ? '#3dd68c' : 'inherit' }} />
                        <span>AI / ML Model</span>
                      </span>
                    ),
                    value: 'ML',
                  },
                ]}
              />
            </div>

            <Tag color="blue" style={{ fontSize: 11 }}>
              Adapter: Mock
            </Tag>
          </Space>
        </Header>

        <Content style={{ padding: 24, minHeight: 'calc(100vh - 60px)' }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
