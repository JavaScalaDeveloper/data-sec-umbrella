import React, { useState } from 'react';
import { Typography, Table, Tag, Space, Button, Input, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

// 模拟数据
const mqData = [
  {
    key: '1',
    name: '用户行为消息队列',
    type: 'Kafka',
    broker: 'kafka-1:9092,kafka-2:9092',
    topicCount: 12,
    status: 'active',
  },
  {
    key: '2',
    name: '订单处理队列',
    type: 'RabbitMQ',
    broker: 'rabbitmq-server:5672',
    topicCount: 5,
    status: 'active',
  },
  {
    key: '3',
    name: '日志收集队列',
    type: 'RocketMQ',
    broker: 'rocketmq-namesrv:9876',
    topicCount: 8,
    status: 'inactive',
  },
];

const MessageQueuePage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  
  const columns = [
    {
      title: '队列名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: 'Broker地址',
      dataIndex: 'broker',
      key: 'broker',
    },
    {
      title: '主题数量',
      dataIndex: 'topicCount',
      key: 'topicCount',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        return (
          <Tag color={status === 'active' ? 'success' : 'default'}>
            {status === 'active' ? '活跃' : '非活跃'}
          </Tag>
        );
      },
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Space size="middle">
          <Button type="primary" icon={<EyeOutlined />} size="small">
            查看
          </Button>
          <Button icon={<EditOutlined />} size="small">
            编辑
          </Button>
          <Button icon={<DeleteOutlined />} size="small" danger>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>消息队列管理</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Input
            placeholder="请输入队列名称"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300 }}
          />
        </Col>
        <Col>
          <Button type="primary" icon={<PlusOutlined />}>
            添加消息队列
          </Button>
        </Col>
      </Row>
      <Table columns={columns} dataSource={mqData} pagination={{ pageSize: 10 }} />
    </div>
  );
};

export default MessageQueuePage;