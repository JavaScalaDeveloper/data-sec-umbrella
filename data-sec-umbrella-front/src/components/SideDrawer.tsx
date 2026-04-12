import React from 'react';
import {Drawer, Card, Typography} from 'antd';
import {DatabaseOutlined, ApiOutlined, RocketOutlined, SettingOutlined} from '@ant-design/icons';
import {useLocation, useNavigate} from 'react-router-dom';

const {Title} = Typography;

interface SideDrawerProps {
    open: boolean;
    onClose: () => void;
}

const SideDrawer: React.FC<SideDrawerProps> = ({open, onClose}) => {
    const navigate = useNavigate();
    const location = useLocation();

    const menuItems: {
        title: string;
        icon: React.ReactNode;
        path: string;
        /** 用于高亮「当前模块」，例如 /admin-center/role 仍属管理中心 */
        activePathPrefix?: string;
    }[] = [
        {
            title: '数据库安全保护伞',
            icon: <DatabaseOutlined style={{fontSize: '32px', color: '#1890ff'}}/>,
            path: '/database-security',
        },
        {
            title: 'API安全保护伞',
            icon: <ApiOutlined style={{fontSize: '32px', color: '#52c41a'}}/>,
            path: '/api-security',
        },
        {
            title: 'MQ安全保护伞',
            icon: <RocketOutlined style={{fontSize: '32px', color: '#faad14'}}/>,
            path: '/mq-security',
        },
        {
            title: '管理中心',
            icon: <SettingOutlined style={{fontSize: '32px', color: '#f5222d'}}/>,
            path: '/admin-center/account',
            activePathPrefix: '/admin-center',
        },
    ];

    const handleItemClick = (path: string) => {
        navigate(path);
        onClose();
    };

    const currentModule =
        menuItems.find((item) => location.pathname.startsWith(item.activePathPrefix ?? item.path)) || menuItems[0];

    return (<Drawer
        title="模块选择"
        placement="left"
        onClose={onClose}
        open={open}
        size="large"
    >
        <div style={{display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px'}}>
            {currentModule.icon}
            <Title level={5} style={{margin: 0}}>当前模块：{currentModule.title}</Title>
        </div>
        <div style={{display: 'flex', flexDirection: 'column', gap: '20px'}}>{menuItems.map((item) => (<Card
            key={item.path}
            hoverable
            onClick={() => handleItemClick(item.path)}
            style={{cursor: 'pointer'}}
        >
            <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px'}}>{item.icon}<Title
                level={5}>{item.title}</Title></div>
        </Card>))}</div>
    </Drawer>);
};

export default SideDrawer;