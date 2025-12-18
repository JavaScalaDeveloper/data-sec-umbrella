import React, { useState } from 'react';
import { Typography, Table, Button, Space, Tag, Input, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

// 模拟数据
const policyData = [
  {
    key: '1',
    name: '数据库访问控制策略',
    type: '访问控制',
    status: 'active',
    createTime: '2023-05-15',
    updateTime: '2023-06-20',
  },
  {
    key: '2',
    name: '敏感数据加密策略',
    type: '数据加密',
    status: 'active',
    createTime: '2023-04-10',
    updateTime: '2023-06-18',
  },
  {
    key: '3',
    name: '日志审计策略',
    type: '审计监控',
    status: 'inactive',
    createTime: '2023-03-22',
    updateTime: '2023-05-10',
  },
];

const PolicyManagementPage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  
  const columns = [
    {
      title: '策略名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '策略类型',
      dataIndex: 'type',
      key: 'type',
      render: (text: string) => {
        let color = 'geekblue';
        if (text === '数据加密') {
          color = 'green';
        } else if (text === '审计监控') {
          color = 'volcano';
        }
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'active' ? 'success' : 'default'}>
          {status === 'active' ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Space size="middle">
          <Button type="primary" icon={<EditOutlined />} size="small">
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
      <Title level={3}>策略管理</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Input
            placeholder="请输入策略名称"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300 }}
          />
        </Col>
        <Col>
          <Button type="primary" icon={<PlusOutlined />}>
            新增策略
          </Button>
        </Col>
      </Row>
      <Table columns={columns} dataSource={policyData} pagination={{ pageSize: 10 }} />
    </div>
  );
};

export default PolicyManagementPage;