import { useState } from 'react';
import { Layout, Menu, Tag, Typography, Space } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  AlertOutlined,
  CalendarOutlined,
  BarChartOutlined,
  SettingOutlined,
  TrademarkOutlined,
} from '@ant-design/icons';

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
          height: 56,
          position: 'sticky',
          top: 0,
          zIndex: 99,
        }}>
          <Text style={{ color: 'var(--text-muted)', fontSize: 13 }}>
            Ministry of Railways · AI-Powered Block Planning
          </Text>
          <Space>
            <Tag color="blue" style={{ fontSize: 11 }}>
              Adapter: Mock (Synthetic)
            </Tag>
            <Tag color="geekblue" style={{ fontSize: 11 }}>
              v1.0.0
            </Tag>
          </Space>
        </Header>

        <Content style={{ padding: 24, minHeight: 'calc(100vh - 56px)' }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
