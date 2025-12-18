import React, { useState } from 'react';
import { Typography, Table, Tag, Space, Button, Input, Row, Col } from 'antd';
import { ReloadOutlined, PauseOutlined, PlayCircleOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

// 模拟数据
const taskData = [
  {
    key: '1',
    name: '数据库实时监控任务',
    status: 'running',
    startTime: '2023-06-20 08:30:25',
    duration: '2小时35分钟',
    progress: 85,
  },
  {
    key: '2',
    name: 'API访问日志分析任务',
    status: 'paused',
    startTime: '2023-06-19 14:15:42',
    duration: '1天5小时',
    progress: 40,
  },
  {
    key: '3',
    name: '敏感数据扫描任务',
    status: 'running',
    startTime: '2023-06-20 10:20:18',
    duration: '45分钟',
    progress: 65,
  },
];

const RealTimeTaskPage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  
  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        let color = status === 'running' ? 'success' : 'warning';
        let text = status === 'running' ? '运行中' : '已暂停';
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
    },
    {
      title: '持续时间',
      dataIndex: 'duration',
      key: 'duration',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Space size="middle">
          <Button type="primary" icon={<ReloadOutlined />} size="small">
            重启
          </Button>
          {record.status === 'running' ? (
            <Button icon={<PauseOutlined />} size="small">
              暂停
            </Button>
          ) : (
            <Button icon={<PlayCircleOutlined />} size="small">
              启动
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>实时任务</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Input
            placeholder="请输入任务名称"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300 }}
          />
        </Col>
        <Col>
          <Space>
            <Button type="primary">刷新</Button>
          </Space>
        </Col>
      </Row>
      <Table columns={columns} dataSource={taskData} pagination={false} />
    </div>
  );
};

export default RealTimeTaskPage;