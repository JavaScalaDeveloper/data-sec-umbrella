import React from 'react';
import { Typography, Table, Tag, Space, Button, Input, Row, Col, Card } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

// 模拟集群数据
const clusterData = [
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
    name: '订单处理消息队列',
    type: 'RabbitMQ',
    broker: 'rabbitmq-1:5672,rabbitmq-2:5672',
    topicCount: 8,
    status: 'active',
  },
  {
    key: '3',
    name: '日志收集消息队列',
    type: 'Kafka',
    broker: 'kafka-3:9092,kafka-4:9092',
    topicCount: 15,
    status: 'inactive',
  },
];

// 模拟Topic数据
const topicData = [
  {
    key: '1',
    name: 'user-behavior-events',
    cluster: '用户行为消息队列',
    partitions: 6,
    replicationFactor: 2,
    messageRetention: '7 days',
    status: 'active',
  },
  {
    key: '2',
    name: 'order-processing-events',
    cluster: '订单处理消息队列',
    partitions: 4,
    replicationFactor: 2,
    messageRetention: '3 days',
    status: 'active',
  },
  {
    key: '3',
    name: 'log-collection-events',
    cluster: '日志收集消息队列',
    partitions: 8,
    replicationFactor: 3,
    messageRetention: '14 days',
    status: 'inactive',
  },
];

interface MessageQueueTabContentProps {
  activeTab: string;
}

const MessageQueueTabContent: React.FC<MessageQueueTabContentProps> = ({ activeTab }) => {
  // 集群表格列定义
  const clusterColumns = [
    {
      title: '队列名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => (
        <Tag color={type === 'Kafka' ? 'blue' : 'green'}>{type}</Tag>
      ),
    },
    {
      title: 'Broker地址',
      dataIndex: 'broker',
      key: 'broker',
    },
    {
      title: 'Topic数量',
      dataIndex: 'topicCount',
      key: 'topicCount',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'active' ? 'green' : 'red'}>
          {status === 'active' ? '运行中' : '已停止'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: () => (
        <Space size="middle">
          <Button type="link" icon={<EyeOutlined />}>
            查看
          </Button>
          <Button type="link" icon={<EditOutlined />}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  // Topic表格列定义
  const topicColumns = [
    {
      title: 'Topic名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '所属集群',
      dataIndex: 'cluster',
      key: 'cluster',
    },
    {
      title: '分区数',
      dataIndex: 'partitions',
      key: 'partitions',
    },
    {
      title: '副本因子',
      dataIndex: 'replicationFactor',
      key: 'replicationFactor',
    },
    {
      title: '消息保留时间',
      dataIndex: 'messageRetention',
      key: 'messageRetention',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'active' ? 'green' : 'red'}>
          {status === 'active' ? '运行中' : '已停止'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: () => (
        <Space size="middle">
          <Button type="link" icon={<EyeOutlined />}>
            查看
          </Button>
          <Button type="link" icon={<EditOutlined />}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ marginTop: 16 }}>
      {activeTab === 'cluster' && (
        <Card>
          <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
            <Col>
              <Input
                placeholder="请输入队列名称"
                style={{ width: 300 }}
                suffix={<SearchOutlined />}
              />
            </Col>
            <Col>
              <Button type="primary" icon={<PlusOutlined />}>
                添加消息队列
              </Button>
            </Col>
          </Row>
          <Table columns={clusterColumns} dataSource={clusterData} pagination={{ pageSize: 10 }} />
        </Card>
      )}
      
      {activeTab === 'topic' && (
        <Card>
          <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
            <Col>
              <Input
                placeholder="请输入Topic名称"
                style={{ width: 300 }}
                suffix={<SearchOutlined />}
              />
            </Col>
            <Col>
              <Button type="primary" icon={<PlusOutlined />}>
                添加Topic
              </Button>
            </Col>
          </Row>
          <Table columns={topicColumns} dataSource={topicData} pagination={{ pageSize: 10 }} />
        </Card>
      )}
    </div>
  );
};

export default MessageQueueTabContent;