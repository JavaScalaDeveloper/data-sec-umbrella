import React from 'react';
import { Layout, Menu, Typography } from 'antd';
import {
  DatabaseOutlined,
  MessageOutlined,
  FileTextOutlined,
  SecurityScanOutlined,
  PlayCircleOutlined,
  FileDoneOutlined,
  SettingOutlined
} from '@ant-design/icons';
import { Link, Outlet, useNavigate } from 'react-router-dom';

const { Header, Sider, Content } = Layout;
const { Title } = Typography;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();

  const menuItems = [
    {
      key: 'task-management',
      icon: <SecurityScanOutlined />,
      label: '任务管理',
      children: [
        {
          key: 'policy-management',
          icon: <SettingOutlined />,
          label: <Link to="/task/policy">策略管理</Link>,
        },
        {
          key: 'real-time-task',
          icon: <PlayCircleOutlined />,
          label: <Link to="/task/realtime">实时任务</Link>,
        },
        {
          key: 'batch-task',
          icon: <FileDoneOutlined />,
          label: <Link to="/task/batch">批量任务</Link>,
        },
      ],
    },
    {
      key: 'data-assets',
      icon: <DatabaseOutlined />,
      label: '数据资产',
      children: [
        {
          key: 'database',
          icon: <DatabaseOutlined />,
          label: <Link to="/asset/database">数据库</Link>,
        },
        {
          key: 'message-queue',
          icon: <MessageOutlined />,
          label: <Link to="/asset/mq">消息队列</Link>,
        },
        {
          key: 'log',
          icon: <FileTextOutlined />,
          label: <Link to="/asset/log">日志</Link>,
        },
      ],
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={250} theme="light">
        <div style={{ padding: '16px', textAlign: 'center' }}>
          <Title level={4} style={{ margin: 0, color: '#1890ff' }}>数据安全保护伞</Title>
        </div>
        <Menu
          mode="inline"
          defaultSelectedKeys={['policy-management']}
          defaultOpenKeys={['task-management']}
          style={{ borderRight: 0 }}
          items={menuItems}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: 0, boxShadow: '0 1px 4px rgba(0,21,41,.08)', cursor: 'pointer' }} onClick={() => navigate('/')}>
          <div style={{ padding: '0 24px' }}>
            <Title level={3} style={{ lineHeight: '64px', margin: 0 }}>数据安全保护伞管理系统</Title>
          </div>
        </Header>
        <Content style={{ margin: '24px 16px 0' }}>
          <div style={{ padding: 24, background: '#fff', minHeight: 360 }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;