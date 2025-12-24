import React, { useState } from 'react';
import { Typography, Table, Tag, Space, Button, Input, Row, Col, Tabs } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;
const { TabPane } = Tabs;

// 模拟数据
const instanceData = [
  {
    key: '1',
    name: 'MySQL实例1',
    type: 'MySQL',
    host: '192.168.1.100',
    port: 3306,
    status: 'connected',
    version: '8.0.25',
  },
  {
    key: '2',
    name: 'PostgreSQL实例1',
    type: 'PostgreSQL',
    host: '192.168.1.101',
    port: 5432,
    status: 'connected',
    version: '13.3',
  },
  {
    key: '3',
    name: 'MongoDB实例1',
    type: 'MongoDB',
    host: '192.168.1.102',
    port: 27017,
    status: 'disconnected',
    version: '5.0.3',
  },
];

const databaseData = [
  {
    key: '1',
    name: '用户信息数据库',
    instance: 'MySQL实例1',
    charset: 'utf8mb4',
    collation: 'utf8mb4_general_ci',
    size: '2.5GB',
    tables: 15,
  },
  {
    key: '2',
    name: '订单系统数据库',
    instance: 'PostgreSQL实例1',
    charset: 'UTF8',
    collation: 'en_US.UTF-8',
    size: '5.8GB',
    tables: 32,
  },
  {
    key: '3',
    name: '日志存储数据库',
    instance: 'MongoDB实例1',
    charset: 'N/A',
    collation: 'N/A',
    size: '12.3GB',
    tables: 8,
  },
];

const tableData = [
  {
    key: '1',
    name: '用户表',
    database: '用户信息数据库',
    type: 'InnoDB',
    rows: 125000,
    size: '45.2MB',
    comment: '存储用户基本信息',
  },
  {
    key: '2',
    name: '订单表',
    database: '订单系统数据库',
    type: 'heap',
    rows: 580000,
    size: '128.7MB',
    comment: '存储订单信息',
  },
  {
    key: '3',
    name: '日志表',
    database: '日志存储数据库',
    type: 'collection',
    rows: 2500000,
    size: '3.2GB',
    comment: '存储系统日志',
  },
];

interface DatabaseTabContentProps {
  activeTab?: string;
}

const DatabaseTabContentNew: React.FC<DatabaseTabContentProps> = ({ activeTab = 'instance' }) => {
  const [searchText, setSearchText] = useState('');
  
  // 实例表格列定义
  const instanceColumns = [
    {
      title: '实例名称',
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
      title: '版本',
      dataIndex: 'version',
      key: 'version',
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

  // 数据库表格列定义
  const databaseColumns = [
    {
      title: '数据库名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '所属实例',
      dataIndex: 'instance',
      key: 'instance',
    },
    {
      title: '字符集',
      dataIndex: 'charset',
      key: 'charset',
    },
    {
      title: '排序规则',
      dataIndex: 'collation',
      key: 'collation',
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
    },
    {
      title: '表数量',
      dataIndex: 'tables',
      key: 'tables',
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

  // 表格列定义
  const tableColumns = [
    {
      title: '表名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '所属数据库',
      dataIndex: 'database',
      key: 'database',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '行数',
      dataIndex: 'rows',
      key: 'rows',
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
    },
    {
      title: '注释',
      dataIndex: 'comment',
      key: 'comment',
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
      case 'instance':
        return {
          columns: instanceColumns,
          dataSource: instanceData.filter(item => 
            searchText === '' || item.name.includes(searchText) || item.type.includes(searchText)
          ),
          title: '实例管理',
          addButtonLabel: '添加实例',
          placeholder: '请输入实例名称'
        };
      case 'database':
        return {
          columns: databaseColumns,
          dataSource: databaseData.filter(item => 
            searchText === '' || item.name.includes(searchText) || item.instance.includes(searchText)
          ),
          title: '数据库管理',
          addButtonLabel: '添加数据库',
          placeholder: '请输入数据库名称'
        };
      case 'table':
        return {
          columns: tableColumns,
          dataSource: tableData.filter(item => 
            searchText === '' || item.name.includes(searchText) || item.database.includes(searchText)
          ),
          title: '表管理',
          addButtonLabel: '添加表',
          placeholder: '请输入表名称'
        };
      default:
        return {
          columns: instanceColumns,
          dataSource: instanceData,
          title: '实例管理',
          addButtonLabel: '添加实例',
          placeholder: '请输入实例名称'
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

export default DatabaseTabContentNew;