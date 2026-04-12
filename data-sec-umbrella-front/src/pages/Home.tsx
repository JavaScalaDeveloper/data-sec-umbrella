import React from 'react';
import {Button, Card, Col, Row, Typography} from 'antd';
import {ApiOutlined, DatabaseOutlined, RightOutlined, RocketOutlined, SettingOutlined} from '@ant-design/icons';
import {useNavigate} from 'react-router-dom';

const {Title, Paragraph, Text, Link} = Typography;

const MODULES = [
    {
        key: 'database',
        title: '数据库安全保护伞',
        description: '数据源与资产、策略治理、批量离线扫描等数据库侧能力。',
        icon: <DatabaseOutlined style={{fontSize: 40, color: '#1890ff'}}/>,
        path: '/database-security',
        color: '#e6f4ff',
    },
    {
        key: 'api',
        title: 'API安全保护伞',
        description: 'API 策略、监控与分析，覆盖接口暴露面的风险治理。',
        icon: <ApiOutlined style={{fontSize: 40, color: '#52c41a'}}/>,
        path: '/api-security',
        color: '#f6ffed',
    },
    {
        key: 'mq',
        title: 'MQ安全保护伞',
        description: '消息队列策略、监控与分析，保障消息链路安全。',
        icon: <RocketOutlined style={{fontSize: 40, color: '#faad14'}}/>,
        path: '/mq-security',
        color: '#fffbe6',
    },
    {
        key: 'admin',
        title: '管理中心',
        description: '管理员账号与权限等后台配置（进入后需登录）。',
        icon: <SettingOutlined style={{fontSize: 40, color: '#f5222d'}}/>,
        path: '/admin-center/account',
        color: '#fff1f0',
    },
] as const;

const Home: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div
            style={{
                display: 'flex',
                flexDirection: 'column',
                minHeight: 'calc(100vh - 64px)',
                background: 'linear-gradient(180deg, #f0f5ff 0%, #f5f5f5 35%, #fafafa 100%)',
            }}
        >
            <div style={{flex: 1, padding: '32px 24px 24px', maxWidth: 1100, margin: '0 auto', width: '100%'}}>
                <div style={{textAlign: 'center', marginBottom: 36}}>
                    <img
                        src="/images/数据安全保护伞.jpg"
                        alt="数据安全保护伞"
                        style={{width: 96, height: 96, marginBottom: 16, borderRadius: 12, boxShadow: '0 4px 14px rgba(0,0,0,0.08)'}}
                    />
                    <Title level={2} style={{marginBottom: 8, color: '#1677ff'}}>
                        数据安全保护伞
                    </Title>
                    <Paragraph type="secondary" style={{fontSize: 15, marginBottom: 0, maxWidth: 560, margin: '0 auto'}}>
                        请选择要进入的产品模块。除本页外，各业务功能需在登录后使用；未登录时可点击右上角「登录」。
                    </Paragraph>
                </div>

                <Row gutter={[20, 20]}>
                    {MODULES.map((m) => (
                        <Col xs={24} sm={12} key={m.key}>
                            <Card
                                hoverable
                                styles={{body: {padding: 24}}}
                                style={{
                                    height: '100%',
                                    borderRadius: 12,
                                    border: '1px solid rgba(0,0,0,0.06)',
                                    background: m.color,
                                }}
                            >
                                <div style={{display: 'flex', gap: 16, alignItems: 'flex-start'}}>
                                    <div style={{flexShrink: 0}}>{m.icon}</div>
                                    <div style={{flex: 1, minWidth: 0}}>
                                        <Title level={4} style={{marginTop: 0, marginBottom: 8}}>
                                            {m.title}
                                        </Title>
                                        <Paragraph type="secondary" style={{marginBottom: 16, fontSize: 14}}>
                                            {m.description}
                                        </Paragraph>
                                        <Button
                                            type="primary"
                                            danger={m.key === 'admin'}
                                            icon={<RightOutlined/>}
                                            onClick={() => navigate(m.path)}
                                        >
                                            进入模块
                                        </Button>
                                    </div>
                                </div>
                            </Card>
                        </Col>
                    ))}
                </Row>
            </div>

            <footer
                style={{
                    padding: '20px 16px 28px',
                    textAlign: 'center',
                    borderTop: '1px solid rgba(0,0,0,0.06)',
                    background: '#fff',
                }}
            >
                <div style={{marginBottom: 8}}>
                    <Link href="https://arelore.com" target="_blank" rel="noopener noreferrer">
                        arelore.com 官网
                    </Link>
                </div>
                <Text type="secondary" style={{fontSize: 13}}>
                    备案号：鄂ICP备2026013952号-1
                </Text>
            </footer>
        </div>
    );
};

export default Home;
