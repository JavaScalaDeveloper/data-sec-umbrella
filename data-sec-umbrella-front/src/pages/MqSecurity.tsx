import React, { useState } from 'react';
import { Layout, Menu, Typography } from 'antd';
import { HomeOutlined, LockOutlined, DatabaseOutlined, AppstoreOutlined, ThunderboltOutlined, SettingOutlined } from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';

const { Content, Sider } = Layout;
const { Title } = Typography;

const MqSecurity: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // 根据当前URL设置activeMenu
  const [activeMenu, setActiveMenu] = useState(() => {
    const path = location.pathname;
    if (path.includes('/policy-management')) {
      return '/policy-management';
    } else if (path.includes('/overview')) {
      return '/overview';
    } else if (path.includes('/data-source')) {
      return '/data-source';
    } else if (path.includes('/data-asset')) {
      return '/data-asset';
    } else if (path.includes('/task-management/realtime')) {
      return '/task-management/realtime';
    } else if (path.includes('/task-management/batch')) {
      return '/task-management/batch';
    } else if (path.includes('/configuration')) {
      return '/configuration';
    }
    return '/overview';
  });

  // 处理菜单点击
  const handleMenuClick = (key: string) => {
    setActiveMenu(key);
    // 使用navigate进行导航
    navigate(`/mq-security${key}`);
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={200} theme="light">
        <Menu
          mode="inline"
          selectedKeys={[activeMenu]}
          style={{ height: '100%', borderRight: 0 }}
          onClick={({ key }) => handleMenuClick(key)}
          items={[
            {
              key: '/overview',
              icon: <HomeOutlined />,
              label: '概览',
            },
            {
              key: '/policy-management',
              icon: <LockOutlined />,
              label: '策略管理',
            },
            {
              key: '/mq-monitoring',
              icon: <DatabaseOutlined />,
              label: 'MQ监控',
            },
            {
              key: '/mq-analysis',
              icon: <AppstoreOutlined />,
              label: 'MQ分析',
            },
            {
              key: '/task-management',
              icon: <ThunderboltOutlined />,
              label: '任务管理',
              children: [
                {
                  key: '/task-management/realtime',
                  label: '实时任务',
                },
                {
                  key: '/task-management/batch',
                  label: '批量任务',
                },
              ],
            },
            {
              key: '/configuration',
              icon: <SettingOutlined />,
              label: '配置中心',
            },
          ]}
        />
      </Sider>
      <Layout>
        <Content style={{ padding: '0 24px', marginTop: 64 }}>
          {activeMenu === '/overview' && <Title level={2}>MQ安全概览</Title>}
          {activeMenu === '/policy-management' && <Title level={2}>MQ策略管理</Title>}
          {activeMenu === '/mq-monitoring' && <Title level={2}>MQ监控</Title>}
          {activeMenu === '/mq-analysis' && <Title level={2}>MQ分析</Title>}
          {activeMenu === '/task-management/realtime' && <Title level={2}>实时任务</Title>}
          {activeMenu === '/task-management/batch' && <Title level={2}>批量任务</Title>}
          {activeMenu === '/configuration' && <Title level={2}>配置中心</Title>}
        </Content>
      </Layout>
    </Layout>
  );
};

export default MqSecurity;