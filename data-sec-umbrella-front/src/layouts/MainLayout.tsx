import React, { useState, useEffect } from 'react';
import { Layout, Menu, Typography, Select } from 'antd';
import {
  DatabaseOutlined,
  MessageOutlined,
  FileTextOutlined,
  SecurityScanOutlined,
  PlayCircleOutlined,
  FileDoneOutlined,
  SettingOutlined,
  ApiOutlined
} from '@ant-design/icons';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Header, Sider, Content } = Layout;
const { Title } = Typography;
const { Option } = Select;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // 从URL中获取当前环境，默认为prd
  const getCurrentEnvFromPath = () => {
    const pathSegments = location.pathname.split('/');
    const envIndex = pathSegments.indexOf('env');
    if (envIndex !== -1 && envIndex + 1 < pathSegments.length) {
      return pathSegments[envIndex + 1];
    }
    return 'prd'; // 默认生产环境
  };
  
  const [currentEnv, setCurrentEnv] = useState(getCurrentEnvFromPath());
  
  // 环境选项
  const envOptions = [
    { value: 'dev', label: '开发环境' },
    { value: 'test', label: '测试环境' },
    { value: 'pre', label: '预发环境' },
    { value: 'prd', label: '生产环境' },
  ];
  
  // 处理环境切换
  const handleEnvChange = (env: string) => {
    setCurrentEnv(env);
    
    // 构建新的URL路径
    const pathSegments = location.pathname.split('/');
    
    // 移除现有的环境部分（如果有）
    const envIndex = pathSegments.indexOf('env');
    if (envIndex !== -1) {
      pathSegments.splice(envIndex, 2); // 移除 'env' 和环境值
    }
    
    // 如果不是生产环境，添加环境路径
    if (env !== 'prd') {
      pathSegments.splice(1, 0, 'env', env); // 在根路径后插入环境路径
    }
    
    const newPath = pathSegments.join('/') || '/';
    navigate(newPath);
  };
  
  // 监听路径变化，更新当前环境
  useEffect(() => {
    setCurrentEnv(getCurrentEnvFromPath());
  }, [location.pathname]);

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
          key: 'api',
          icon: <ApiOutlined />,
          label: <Link to="/asset/api">API</Link>,
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
        <Header style={{ background: '#fff', padding: 0, boxShadow: '0 1px 4px rgba(0,21,41,.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ padding: '0 24px', cursor: 'pointer' }} onClick={() => navigate('/')}>
            <Title level={3} style={{ lineHeight: '64px', margin: 0 }}>数据安全保护伞管理系统</Title>
          </div>
          <div style={{ padding: '0 24px', display: 'flex', alignItems: 'center' }}>
            <span style={{ marginRight: 8 }}>环境：</span>
            <Select
              value={currentEnv}
              onChange={handleEnvChange}
              style={{ width: 120 }}
            >
              {envOptions.map(option => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
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