import React, { useState, useEffect, useCallback } from 'react';
import { Layout, Menu, Typography, Divider, Drawer, Button, Select } from 'antd';
import {
  DatabaseOutlined,
  MessageOutlined,
  SecurityScanOutlined,
  PlayCircleOutlined,
  FileDoneOutlined,
  SettingOutlined,
  ApiOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  RocketOutlined
} from '@ant-design/icons';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Header, Sider, Content } = Layout;
const { Title } = Typography;
const { Option } = Select;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // 环境选项
  const envOptions = [
    { value: 'dev', label: '开发环境' },
    { value: 'test', label: '测试环境' },
    { value: 'pre', label: '预发环境' },
    { value: 'prd', label: '生产环境' },
  ];
  
  // 从URL中获取当前环境，默认为prd
  const getCurrentEnvFromPath = useCallback(() => {
    const pathSegments = location.pathname.split('/');
    const envIndex = pathSegments.indexOf('env');
    if (envIndex !== -1 && envIndex + 1 < pathSegments.length) {
      return pathSegments[envIndex + 1];
    }
    return 'prd'; // 默认生产环境
  }, [location.pathname]);
  
  const [currentEnv, setCurrentEnv] = useState(getCurrentEnvFromPath());
  
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

  // 从路径获取当前产品类型
  const updateCurrentProductFromPath = useCallback(() => {
    const pathSegments = location.pathname.split('/');
    const productType = pathSegments[1];
    if (['database', 'api', 'message'].includes(productType)) {
      setCurrentProduct(productType);
    }
  }, [location.pathname]);
  
  // 监听路径变化，更新当前环境和产品类型
  useEffect(() => {
    setCurrentEnv(getCurrentEnvFromPath());
    updateCurrentProductFromPath();
  }, [location.pathname, getCurrentEnvFromPath, updateCurrentProductFromPath]);

  // 产品类型
  const [currentProduct, setCurrentProduct] = useState('database');
  const [drawerVisible, setDrawerVisible] = useState(false);
  
  const productOptions = [
    { value: 'database', label: '数据库安全', icon: <DatabaseOutlined />, title: '数据库安全保护伞' },
    { value: 'api', label: 'API安全', icon: <ApiOutlined />, title: 'API安全保护伞' },
    { value: 'message', label: 'MQ安全', icon: <RocketOutlined />, title: 'MQ安全保护伞' },
  ];

  // 处理产品切换
  const handleProductChange = (product: string) => {
    setCurrentProduct(product);
    setDrawerVisible(false);
    navigate(`/${product}/overview`);
  };

  // 根据当前产品生成菜单
  const getMenuItems = () => {
    return [
      {
        key: `${currentProduct}-overview`,
        icon: <FileTextOutlined />,
        label: <Link to={`/${currentProduct}/overview`}>概览</Link>,
      },
      {
        key: 'task-management',
        icon: <SecurityScanOutlined />,
        label: '任务管理',
        children: [
          {
            key: `${currentProduct}-policy-management`,
            icon: <SettingOutlined />,
            label: <Link to={`/${currentProduct}/policy`}>策略管理</Link>,
          },
          {
            key: `${currentProduct}-real-time-task`,
            icon: <PlayCircleOutlined />,
            label: <Link to={`/${currentProduct}/realtime`}>实时任务</Link>,
          },
          {
            key: `${currentProduct}-batch-task`,
            icon: <FileDoneOutlined />,
            label: <Link to={`/${currentProduct}/batch`}>批量任务</Link>,
          },
        ],
      },
      {
        key: 'data-assets',
        icon: <DatabaseOutlined />,
        label: '数据资产',
        children: [
          {
            key: `${currentProduct}-asset`,
            icon: currentProduct === 'database' ? <DatabaseOutlined /> : 
                  currentProduct === 'api' ? <ApiOutlined /> : <RocketOutlined />,
            label: <Link to={`/${currentProduct}/asset`}>资产管理</Link>,
          },
        ],
      },
    ];
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={250} theme="light">
        {/* 左上角图标按钮 */}
        <div style={{ padding: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Button
            type="text"
            icon={<AppstoreOutlined style={{ fontSize: '24px', color: '#1890ff' }} />}
            onClick={() => setDrawerVisible(true)}
            style={{ padding: 0 }}
          />
          <Title level={4} style={{ margin: '0 0 0 12px', color: '#1890ff' }}>
            {productOptions.find(p => p.value === currentProduct)?.title || '数据安全保护伞'}
          </Title>
        </div>
        
        <Divider style={{ margin: '8px 0' }} />
        
        <Menu
          mode="inline"
          defaultSelectedKeys={[`${currentProduct}-policy-management`]}
          defaultOpenKeys={['task-management']}
          style={{ borderRight: 0 }}
          items={getMenuItems()}
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

      {/* 产品选择抽屉 */}
      <Drawer
        title="选择产品"
        placement="left"
        onClose={() => setDrawerVisible(false)}
        open={drawerVisible}
        width={300}
      >
        <div style={{ padding: '20px 0' }}>
          {productOptions.map(option => (
            <Button
              key={option.value}
              type={currentProduct === option.value ? 'primary' : 'default'}
              icon={option.icon}
              onClick={() => handleProductChange(option.value)}
              style={{
                width: '100%',
                textAlign: 'left',
                marginBottom: '12px',
                padding: '12px 16px',
                justifyContent: 'flex-start',
                fontSize: '14px'
              }}
            >
              {option.label}
            </Button>
          ))}
        </div>
      </Drawer>
    </Layout>
  );
};

export default MainLayout;