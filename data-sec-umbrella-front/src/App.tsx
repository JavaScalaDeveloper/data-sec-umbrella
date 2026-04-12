import React, { useEffect, useState } from 'react';
import { Layout, Button, Avatar, Dropdown, Space, message, Typography } from 'antd';
import { MenuOutlined, UserOutlined, DatabaseOutlined, ApiOutlined, RocketOutlined, SettingOutlined, HomeOutlined } from '@ant-design/icons';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import SideDrawer from './components/SideDrawer';
import SiteFooter from './components/SiteFooter';
import { adminCenterApi, getAdminAuth, setAdminAuth } from './services/api';
import './App.css';

const { Header } = Layout;
const { Text } = Typography;

const App: React.FC = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [auth, setAuth] = useState<any>(null);
  const location = useLocation();
  const navigate = useNavigate();

  const checkLoginState = async () => {
    if (location.pathname === '/login') {
      return;
    }
    // 首页公开访问：不强制跳转登录；若本地有 token 则静默校验用于展示头像
    if (location.pathname === '/') {
      const localAuth = getAdminAuth();
      if (!localAuth) {
        setAuth(null);
        return;
      }
      setAuth(localAuth);
      try {
        const res = await adminCenterApi.current();
        if (res.code === 200 && res.data) {
          setAuth(res.data);
          setAdminAuth(res.data);
        } else {
          setAdminAuth(null);
          setAuth(null);
        }
      } catch {
        setAdminAuth(null);
        setAuth(null);
      }
      return;
    }
    const localAuth = getAdminAuth();
    if (!localAuth) {
      setAuth(null);
      navigate('/login', { replace: true });
      return;
    }
    // 先采用本地登录态放行页面，避免登录后瞬时被误判未登录
    setAuth(localAuth);
    try {
      const res = await adminCenterApi.current();
      if (res.code !== 200 || !res.data) {
        throw new Error(res.message || '登录态失效');
      }
      setAuth(res.data);
      setAdminAuth(res.data);
    } catch {
      setAdminAuth(null);
      setAuth(null);
      navigate('/login', { replace: true });
    }
  };

  useEffect(() => {
    checkLoginState();
  }, [location.pathname]);

  const handleDrawerOpen = () => {
    setDrawerOpen(true);
  };

  const handleDrawerClose = () => {
    setDrawerOpen(false);
  };

  const handleLogout = () => {
    setAdminAuth(null);
    setAuth(null);
    message.success('已退出登录');
    navigate('/login', { replace: true });
  };

  const moduleInfo = (() => {
    if (location.pathname.startsWith('/database-security')) {
      return {
        title: '数据库安全保护伞',
        icon: <DatabaseOutlined style={{ color: '#1890ff', fontSize: 20 }} />,
      };
    }
    if (location.pathname.startsWith('/api-security')) {
      return {
        title: 'API安全保护伞',
        icon: <ApiOutlined style={{ color: '#52c41a', fontSize: 20 }} />,
      };
    }
    if (location.pathname.startsWith('/mq-security')) {
      return {
        title: 'MQ安全保护伞',
        icon: <RocketOutlined style={{ color: '#faad14', fontSize: 20 }} />,
      };
    }
    if (location.pathname.startsWith('/admin-center')) {
      return {
        title: '管理中心',
        icon: <SettingOutlined style={{ color: '#f5222d', fontSize: 20 }} />,
      };
    }
    return {
      title: '首页',
      icon: <HomeOutlined style={{ color: '#1677ff', fontSize: 20 }} />,
    };
  })();

  const showSiteFooter = location.pathname !== '/';

  return (
    <Layout style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Header style={{ display: 'flex', alignItems: 'center', padding: '0 24px', backgroundColor: '#fff', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)', flexShrink: 0 }}>
        <Button
          type="text"
          icon={<MenuOutlined />}
          onClick={handleDrawerOpen}
          style={{ fontSize: '20px', marginRight: '20px' }}
        />
        <Space style={{ flex: 1 }}>
          {moduleInfo.icon}
          <h1 style={{ margin: 0, fontSize: '20px', color: '#1890ff' }}>{moduleInfo.title}</h1>
        </Space>
        {auth ? (
          <Dropdown
            menu={{
              items: [
                { key: 'user', label: <Text>用户：{auth.username}</Text>, disabled: true },
                { key: 'role', label: <Text>角色：{auth.roleCode}</Text>, disabled: true },
                { type: 'divider' as const },
                { key: 'logout', label: '退出登录', danger: true, onClick: handleLogout },
              ],
            }}
            trigger={['click']}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} />
              <Text>{auth.username}</Text>
            </Space>
          </Dropdown>
        ) : (
          <Button type="primary" onClick={() => navigate('/login')}>登录</Button>
        )}
      </Header>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
        <div style={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
          <Outlet />
        </div>
        {showSiteFooter ? <SiteFooter /> : null}
      </div>
      <SideDrawer open={drawerOpen} onClose={handleDrawerClose} />
    </Layout>
  );
};

export default App;
