import React, { useState } from 'react';
import { 
  Typography, 
  Table, 
  Button, 
  Space, 
  Tag, 
  Input, 
  Row, 
  Col, 
  Modal, 
  Form, 
  Select, 
  InputNumber, 
  Switch, 
  Popconfirm,
  message,
  Card
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined
} from '@ant-design/icons';

const { Title } = Typography;
const { Option } = Select;

// 模拟数据
const initialPolicyData = [
  {
    key: '1',
    code: 'ERROR_LOG',
    chineseName: '错误日志',
    description: '系统错误日志监控',
    sensitivityLevel: 4,
    hideExample: true,
  },
  {
    key: '2',
    code: 'ACCESS_LOG',
    chineseName: '访问日志',
    description: '系统访问日志监控',
    sensitivityLevel: 2,
    hideExample: false,
  },
];

const LogPolicyPage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<any>(null);
  const [form] = Form.useForm();
  const [policyData, setPolicyData] = useState(initialPolicyData);

  // 显示模态框
  const showModal = (record: any = null) => {
    setEditingRecord(record);
    setIsModalVisible(true);
    
    if (record) {
      form.setFieldsValue({
        ...record,
        hideExample: record.hideExample || false
      });
    } else {
      form.resetFields();
    }
  };

  // 关闭模态框
  const handleCancel = () => {
    setIsModalVisible(false);
    setEditingRecord(null);
    form.resetFields();
  };

  // 保存策略
  const handleSave = () => {
    form.validateFields().then(values => {
      if (editingRecord) {
        // 编辑
        setPolicyData(prevData => 
          prevData.map(item => 
            item.key === editingRecord.key ? { ...values, key: editingRecord.key } : item
          )
        );
      } else {
        // 新增
        const newRecord = {
          ...values,
          key: `${policyData.length + 1}`
        };
        setPolicyData(prevData => [...prevData, newRecord]);
      }
      message.success(`${editingRecord ? '编辑' : '新增'}成功`);
      handleCancel();
    });
  };

  // 删除策略
  const handleDelete = (key: string) => {
    setPolicyData(prevData => prevData.filter(item => item.key !== key));
    message.success('删除成功');
  };

  // 策略表格列定义
  const policyColumns = [
    {
      title: '策略code',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: '策略名',
      dataIndex: 'chineseName',
      key: 'chineseName',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '敏感等级',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      render: (level: number) => (
        <Tag color={level > 3 ? 'red' : level > 1 ? 'orange' : 'green'}>
          {level}
        </Tag>
      ),
    },
    {
      title: '隐藏样例',
      dataIndex: 'hideExample',
      key: 'hideExample',
      render: (hide: boolean) => (
        <Tag color={hide ? 'red' : 'green'}>
          {hide ? '是' : '否'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Space size="middle">
          <Button 
            type="primary" 
            icon={<EditOutlined />} 
            size="small"
            onClick={() => showModal(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这条策略吗？"
            onConfirm={() => handleDelete(record.key)}
            okText="确定"
            cancelText="取消"
          >
            <Button icon={<DeleteOutlined />} size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
        <Col>
          <Input
            placeholder="请输入策略code或策略名"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            suffix={<SearchOutlined />}
            style={{ width: 300 }}
          />
        </Col>
        <Col>
          <Button 
            type="primary" 
            icon={<PlusOutlined />}
            onClick={() => showModal()}
          >
            新增策略
          </Button>
        </Col>
      </Row>
      
      <Table 
        columns={policyColumns} 
        dataSource={policyData.filter(item => 
          searchText === '' || item.code.includes(searchText) || item.chineseName.includes(searchText)
        )} 
        pagination={{ pageSize: 10 }} 
        rowKey="key"
      />

      {/* 新增/编辑策略模态框 */}
      <Modal
        title={`${editingRecord ? '编辑' : '新增'}日志策略`}
        open={isModalVisible}
        onOk={handleSave}
        onCancel={handleCancel}
        width={600}
        maskClosable={false}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            sensitivityLevel: 1,
            hideExample: false
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="code"
                label="策略code"
                rules={[{ required: true, message: '请输入策略code!' }]}
              >
                <Input placeholder="请输入策略code" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="chineseName"
                label="策略名"
                rules={[{ required: true, message: '请输入策略名!' }]}
              >
                <Input placeholder="请输入策略名" />
              </Form.Item>
            </Col>
          </Row>
          
          <Form.Item
            name="description"
            label="描述"
            rules={[{ required: true, message: '请输入描述!' }]}
          >
            <Input placeholder="请输入描述" />
          </Form.Item>
          
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="sensitivityLevel"
                label="敏感等级"
                rules={[{ required: true, message: '请选择敏感等级!' }]}
              >
                <Select placeholder="请选择敏感等级">
                  {[1, 2, 3, 4, 5].map(level => (
                    <Option key={level} value={level}>
                      {level} {level > 3 ? '(高)' : level > 1 ? '(中)' : '(低)'}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="hideExample"
                label="隐藏样例"
                tooltip="适用于薪资、期权等高敏场景，在命中该策略时直接从内存里隐藏样例。"
                valuePropName="checked"
              >
                <Switch checkedChildren="是" unCheckedChildren="否" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
};

export default LogPolicyPage;