import React, { useState } from 'react';
import { Layout, Button } from 'antd';
import { MenuOutlined } from '@ant-design/icons';
import { Outlet } from 'react-router-dom';
import SideDrawer from './components/SideDrawer';
import './App.css';

const { Header } = Layout;

const App: React.FC = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);

  const handleDrawerOpen = () => {
    setDrawerOpen(true);
  };

  const handleDrawerClose = () => {
    setDrawerOpen(false);
  };

  return (<Layout style={{ minHeight: '100vh' }}><Header style={{ display: 'flex', alignItems: 'center', padding: '0 24px', backgroundColor: '#fff', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)' }}><Button
          type="text"
          icon={<MenuOutlined />}
          onClick={handleDrawerOpen}
          style={{ fontSize: '20px', marginRight: '20px' }}
        />
        <h1 style={{ margin: 0, fontSize: '20px', color: '#1890ff' }}>数据安全保护伞</h1></Header><Outlet /><SideDrawer open={drawerOpen} onClose={handleDrawerClose} /></Layout>);
};

export default App;
