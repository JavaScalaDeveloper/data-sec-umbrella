import React from 'react';
import { Typography, Card, Row, Col, Statistic, Divider } from 'antd';
import {
  SecurityScanOutlined,
  DatabaseOutlined,
  PlayCircleOutlined,
  FileDoneOutlined
} from '@ant-design/icons';

const { Title, Paragraph } = Typography;

const HomePage: React.FC = () => {
  return (
    <div>
      <Title level={2}>欢迎使用数据安全保护伞管理系统</Title>
      <Paragraph style={{ fontSize: '16px' }}>
        这是一个统一的数据安全管理平台，帮助您监控和管理各种数据资产的安全策略。
      </Paragraph>
      
      <Divider orientation="left">系统概览</Divider>
      
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic
              title="活跃策略"
              value={28}
              prefix={<SecurityScanOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="数据源"
              value={12}
              prefix={<DatabaseOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="实时任务"
              value={5}
              prefix={<PlayCircleOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="批量任务"
              value={3}
              prefix={<FileDoneOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
      </Row>
      
      <Divider orientation="left">功能介绍</Divider>
      
      <Row gutter={[16, 16]}>
        <Col span={12}>
          <Card title="任务管理" bordered={false}>
            <ul>
              <li><strong>策略管理：</strong>创建、编辑和部署数据安全策略</li>
              <li><strong>实时任务：</strong>监控正在运行的数据安全任务</li>
              <li><strong>批量任务：</strong>管理和调度批量数据处理任务</li>
            </ul>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="数据资产" bordered={false}>
            <ul>
              <li><strong>数据库：</strong>管理和监控数据库连接及访问权限</li>
              <li><strong>消息队列：</strong>跟踪消息队列中的敏感数据流</li>
              <li><strong>日志：</strong>分析系统日志以识别潜在安全风险</li>
            </ul>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default HomePage;