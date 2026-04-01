import React from 'react';
import { Layout, Menu, Typography } from 'antd';
import { HomeOutlined, LockOutlined, DatabaseOutlined, AppstoreOutlined, ThunderboltOutlined, InboxOutlined, SettingOutlined } from '@ant-design/icons';

const { Content, Sider } = Layout;
const { Title } = Typography;

const AdminCenter: React.FC = () => {
  return (<Layout style={{ minHeight: '100vh' }}><Sider width={200} theme="light"><Menu
          mode="inline"
          defaultSelectedKeys={['1']}
          style={{ height: '100%', borderRight: 0 }}
          items={[
            {
              key: '1',
              icon: <HomeOutlined />,
              label: '概览',
            },
            {
              key: '2',
              icon: <LockOutlined />,
              label: '策略管理',
            },
            {
              key: '3',
              icon: <DatabaseOutlined />,
              label: '数据源',
            },
            {
              key: '4',
              icon: <AppstoreOutlined />,
              label: '数据资产',
            },
            {
              key: '5',
              icon: <ThunderboltOutlined />,
              label: '任务管理',
              children: [
                {
                  key: '5-1',
                  label: '实时任务',
                },
                {
                  key: '5-2',
                  label: '批量任务',
                },
              ],
            },
            {
              key: '6',
              icon: <SettingOutlined />,
              label: '配置中心',
            },
          ]}
        /></Sider><Layout><Content style={{ padding: '0 24px', marginTop: 64 }}><Title level={2}>管理中心</Title></Content></Layout></Layout>);
};

export default AdminCenter;