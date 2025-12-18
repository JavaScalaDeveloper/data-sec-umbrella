import React, { useState } from 'react';
import { Typography, Table, Tag, Space, Button, DatePicker, Input, Row, Col } from 'antd';
import { UploadOutlined, PlayCircleOutlined, StopOutlined, HistoryOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;
const { RangePicker } = DatePicker;

// 模拟数据
const batchTaskData = [
  {
    key: '1',
    name: '月度数据安全报告生成',
    status: 'completed',
    schedule: '每月1号 02:00',
    lastRun: '2023-06-01 02:35:18',
    nextRun: '2023-07-01 02:00:00',
  },
  {
    key: '2',
    name: '季度数据库备份验证',
    status: 'running',
    schedule: '每季度首月15号',
    lastRun: '2023-06-15 03:20:45',
    nextRun: '2023-09-15 02:00:00',
  },
  {
    key: '3',
    name: '年度合规性检查',
    status: 'scheduled',
    schedule: '每年12月31号',
    lastRun: '2022-12-31 23:59:59',
    nextRun: '2023-12-31 02:00:00',
  },
];

const BatchTaskPage: React.FC = () => {
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
        let color = 'default';
        let text = '';
        switch (status) {
          case 'completed':
            color = 'success';
            text = '已完成';
            break;
          case 'running':
            color = 'processing';
            text = '运行中';
            break;
          case 'scheduled':
            color = 'warning';
            text = '已计划';
            break;
          default:
            text = status;
        }
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: '调度计划',
      dataIndex: 'schedule',
      key: 'schedule',
    },
    {
      title: '上次运行',
      dataIndex: 'lastRun',
      key: 'lastRun',
    },
    {
      title: '下次运行',
      dataIndex: 'nextRun',
      key: 'nextRun',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Space size="middle">
          <Button type="primary" icon={<PlayCircleOutlined />} size="small">
            执行
          </Button>
          <Button icon={<StopOutlined />} size="small">
            停止
          </Button>
          <Button icon={<HistoryOutlined />} size="small">
            历史
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>批量任务</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col span={12}>
          <Input
            placeholder="请输入任务名称"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300, marginRight: 16 }}
          />
          <RangePicker />
        </Col>
        <Col>
          <Space>
            <Button>查询</Button>
            <Button type="primary" icon={<UploadOutlined />}>
              导入任务
            </Button>
          </Space>
        </Col>
      </Row>
      <Table columns={columns} dataSource={batchTaskData} pagination={{ pageSize: 10 }} />
    </div>
  );
};

export default BatchTaskPage;