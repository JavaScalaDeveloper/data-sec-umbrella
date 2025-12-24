import React, { useState } from 'react';
import { Typography, Table, Tag, Space, Button, Input, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

// 模拟域名数据
const domainData = [
  {
    key: '1',
    name: 'api.example.com',
    type: '生产环境',
    ssl: '是',
    status: 'active',
    certificateExpiry: '2024-12-31',
  },
  {
    key: '2',
    name: 'test-api.example.com',
    type: '测试环境',
    ssl: '是',
    status: 'active',
    certificateExpiry: '2024-10-15',
  },
  {
    key: '3',
    name: 'dev-api.example.com',
    type: '开发环境',
    ssl: '否',
    status: 'inactive',
    certificateExpiry: 'N/A',
  },
];

// 模拟API数据
const apiData = [
  {
    key: '1',
    name: '用户信息API',
    method: 'REST',
    endpoint: '/api/users',
    host: 'https://api.example.com',
    status: 'active',
  },
  {
    key: '2',
    name: '订单系统API',
    method: 'GraphQL',
    endpoint: '/graphql',
    host: 'https://orders.example.com',
    status: 'active',
  },
  {
    key: '3',
    name: '支付网关API',
    method: 'REST',
    endpoint: '/api/payment',
    host: 'https://payment.example.com',
    status: 'inactive',
  },
];

interface ApiTabContentProps {
  activeTab?: string;
}

const ApiTabContent: React.FC<ApiTabContentProps> = ({ activeTab = 'domain' }) => {
  const [searchText, setSearchText] = useState('');
  
  // 域名表格列定义
  const domainColumns = [
    {
      title: '域名',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '环境类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: 'SSL证书',
      dataIndex: 'ssl',
      key: 'ssl',
    },
    {
      title: '证书过期时间',
      dataIndex: 'certificateExpiry',
      key: 'certificateExpiry',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const isActive = status === 'active';
        return (
          <Tag color={isActive ? 'success' : 'error'}>
            {isActive ? '活跃' : '非活跃'}
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

  // API表格列定义
  const apiColumns = [
    {
      title: 'API名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'method',
      key: 'method',
    },
    {
      title: '端点',
      dataIndex: 'endpoint',
      key: 'endpoint',
    },
    {
      title: '主机地址',
      dataIndex: 'host',
      key: 'host',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const isActive = status === 'active';
        return (
          <Tag color={isActive ? 'success' : 'error'}>
            {isActive ? '活跃' : '非活跃'}
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

  // 根据当前tab获取对应的表格数据和列定义
  const getCurrentTableData = () => {
    switch (activeTab) {
      case 'domain':
        return {
          columns: domainColumns,
          dataSource: domainData.filter(item => 
            searchText === '' || item.name.includes(searchText) || item.type.includes(searchText)
          ),
          title: '域名管理',
          addButtonLabel: '添加域名',
          placeholder: '请输入域名'
        };
      case 'api':
        return {
          columns: apiColumns,
          dataSource: apiData.filter(item => 
            searchText === '' || item.name.includes(searchText) || item.method.includes(searchText)
          ),
          title: 'API管理',
          addButtonLabel: '添加API',
          placeholder: '请输入API名称'
        };
      default:
        return {
          columns: domainColumns,
          dataSource: domainData,
          title: '域名管理',
          addButtonLabel: '添加域名',
          placeholder: '请输入域名'
        };
    }
  };

  const { columns, dataSource, title, addButtonLabel, placeholder } = getCurrentTableData();

  return (
    <div>
      <Title level={3}>{title}</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Input
            placeholder={placeholder}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300 }}
          />
        </Col>
        <Col>
          <Button type="primary" icon={<PlusOutlined />}>
            {addButtonLabel}
          </Button>
        </Col>
      </Row>
      <Table columns={columns} dataSource={dataSource} pagination={{ pageSize: 10 }} />
    </div>
  );
};

export default ApiTabContent;