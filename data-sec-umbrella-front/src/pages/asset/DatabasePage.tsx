import React, { useState } from 'react';
import { Typography, Table, Tag, Space, Button, Input, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

// 模拟数据
const databaseData = [
  {
    key: '1',
    name: '用户信息数据库',
    type: 'MySQL',
    host: '192.168.1.100',
    port: 3306,
    status: 'connected',
  },
  {
    key: '2',
    name: '订单系统数据库',
    type: 'PostgreSQL',
    host: '192.168.1.101',
    port: 5432,
    status: 'connected',
  },
  {
    key: '3',
    name: '日志存储数据库',
    type: 'MongoDB',
    host: '192.168.1.102',
    port: 27017,
    status: 'disconnected',
  },
];

const DatabasePage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  
  const columns = [
    {
      title: '数据库名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '主机地址',
      dataIndex: 'host',
      key: 'host',
    },
    {
      title: '端口',
      dataIndex: 'port',
      key: 'port',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const isConnected = status === 'connected';
        return (
          <Tag color={isConnected ? 'success' : 'error'}>
            {isConnected ? '已连接' : '未连接'}
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
      <Title level={3}>数据库管理</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Input
            placeholder="请输入数据库名称"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300 }}
          />
        </Col>
        <Col>
          <Button type="primary" icon={<PlusOutlined />}>
            添加数据库
          </Button>
        </Col>
      </Row>
      <Table columns={columns} dataSource={databaseData} pagination={{ pageSize: 10 }} />
    </div>
  );
};

export default DatabasePage;