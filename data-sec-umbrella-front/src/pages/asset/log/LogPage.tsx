import React, { useState } from 'react';
import { Typography, Table, Tag, Space, Button, DatePicker, Input, Row, Col } from 'antd';
import { SearchOutlined, DownloadOutlined, EyeOutlined } from '@ant-design/icons';

const { Title } = Typography;
const { RangePicker } = DatePicker;

// 模拟数据
const logData = [
  {
    key: '1',
    fileName: 'security-audit-2023-06-20.log',
    size: '25.6 MB',
    createTime: '2023-06-20 08:30:25',
    level: 'INFO',
    status: 'normal',
  },
  {
    key: '2',
    fileName: 'access-log-2023-06-20.log',
    size: '120.3 MB',
    createTime: '2023-06-20 09:15:42',
    level: 'WARN',
    status: 'warning',
  },
  {
    key: '3',
    fileName: 'error-log-2023-06-20.log',
    size: '2.1 MB',
    createTime: '2023-06-20 10:20:18',
    level: 'ERROR',
    status: 'critical',
  },
];

const LogPage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  
  const columns = [
    {
      title: '日志文件名',
      dataIndex: 'fileName',
      key: 'fileName',
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
    {
      title: '级别',
      dataIndex: 'level',
      key: 'level',
      render: (level: string) => {
        let color = 'default';
        switch (level) {
          case 'INFO':
            color = 'success';
            break;
          case 'WARN':
            color = 'warning';
            break;
          case 'ERROR':
            color = 'error';
            break;
          default:
            color = 'default';
        }
        return <Tag color={color}>{level}</Tag>;
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        let color = 'default';
        let text = '';
        switch (status) {
          case 'normal':
            color = 'success';
            text = '正常';
            break;
          case 'warning':
            color = 'warning';
            text = '警告';
            break;
          case 'critical':
            color = 'error';
            text = '严重';
            break;
          default:
            text = status;
        }
        return <Tag color={color}>{text}</Tag>;
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
          <Button icon={<DownloadOutlined />} size="small">
            下载
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>日志管理</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col span={12}>
          <Input
            placeholder="请输入日志文件名"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300, marginRight: 16 }}
          />
          <RangePicker />
        </Col>
        <Col>
          <Space>
            <Button type="primary" icon={<SearchOutlined />}>
              查询
            </Button>
          </Space>
        </Col>
      </Row>
      <Table columns={columns} dataSource={logData} pagination={{ pageSize: 10 }} />
    </div>
  );
};

export default LogPage;