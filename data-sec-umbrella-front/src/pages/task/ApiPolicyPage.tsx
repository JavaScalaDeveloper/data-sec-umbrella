import React, { useState, useEffect, useCallback } from 'react';
import { 
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
  Switch, 
  Popconfirm,
  message,
  Card,
  Tabs,
  Tooltip
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined,
  InfoCircleOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import API_CONFIG from '../../config/apiConfig.ts';

const { Option } = Select;

// API调用函数
const fetchApiPolicies = async (params: any) => {
  try {
    const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.API_POLICY.LIST}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(params),
    });
    const result = await response.json();
    if (result.code === 200) {
      return result.data;
    } else {
      throw new Error(result.message || '获取策略列表失败');
    }
  } catch (error) {
    console.error('获取API策略列表失败:', error);
    throw error;
  }
};

const getApiPolicyById = async (id: number) => {
  try {
    const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.API_POLICY.GET}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ id }),
    });
    const result = await response.json();
    if (result.code === 200) {
      return result.data;
    } else {
      throw new Error(result.message || '获取策略详情失败');
    }
  } catch (error) {
    console.error('获取API策略详情失败:', error);
    throw error;
  }
};

const createApiPolicy = async (data: any) => {
  try {
    const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.API_POLICY.CREATE}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    const result = await response.json();
    if (result.code === 200) {
      return result.data;
    } else {
      throw new Error(result.message || '创建策略失败');
    }
  } catch (error) {
    console.error('创建API策略失败:', error);
    throw error;
  }
};

const updateApiPolicy = async (data: any) => {
  try {
    const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.API_POLICY.UPDATE}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    const result = await response.json();
    if (result.code === 200) {
      return result.data;
    } else {
      throw new Error(result.message || '更新策略失败');
    }
  } catch (error) {
    console.error('更新API策略失败:', error);
    throw error;
  }
};

const deleteApiPolicy = async (id: number) => {
  try {
    const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.API_POLICY.DELETE}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ id }),
    });
    const result = await response.json();
    if (result.code === 200) {
      return result.data;
    } else {
      throw new Error(result.message || '删除策略失败');
    }
  } catch (error) {
    console.error('删除API策略失败:', error);
    throw error;
  }
};

const ApiPolicyPage: React.FC = () => {
  // 状态管理
  const [policyData, setPolicyData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchText, setSearchText] = useState('');
  const [policyCode, setPolicyCode] = useState('');
  const [sensitivityLevel, setSensitivityLevel] = useState<number | undefined>();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<any>(null);
  const [rules, setRules] = useState<any[]>([]);
  const [apiRulesData, setApiRulesData] = useState<any[]>([]);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  // 加载数据
  const loadPolicyData = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        current: currentPage,
        size: pageSize,
        policyCode: policyCode,
        policyName: searchText,
        sensitivityLevel: sensitivityLevel,
      };
      const data = await fetchApiPolicies(params);
      setPolicyData(data.records || []);
      setTotal(data.total || 0);
    } catch (error) {
      message.error('获取策略列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, policyCode, searchText, sensitivityLevel]);

  // 组件挂载时加载数据
  useEffect(() => {
    loadPolicyData();
  }, [loadPolicyData]);

  // 搜索处理
  const handleSearch = () => {
    setCurrentPage(1);
    loadPolicyData();
  };

  // 重置搜索条件
  const handleReset = () => {
    setPolicyCode('');
    setSearchText('');
    setSensitivityLevel(undefined);
    setCurrentPage(1);
    loadPolicyData();
  };

  // 处理选项卡切换
  const handleTabChange = (key: string) => {
    switch (key) {
      case 'database':
        navigate('/task/policy/database');
        break;
      case 'api':
        navigate('/task/policy/api');
        break;
      case 'message':
        navigate('/task/policy/message');
        break;
      case 'log':
        navigate('/task/policy/log');
        break;
      default:
        navigate('/task/policy/database');
    }
  };

  // 分类规则表格列定义
  const ruleColumns = [
    {
      title: '编号',
      dataIndex: 'id',
      key: 'id',
      width: 60,
      render: (_: any, __: any, index: number) => index + 1,
    },
    {
      title: '条件对象',
      dataIndex: 'conditionObject',
      key: 'conditionObject',
      render: (value: any, record: any) => (
        <Select
          value={value}
          style={{ width: '100%' }}
          onChange={(val) => handleRuleChange(record.id, 'conditionObject', val)}
        >
          <Option value="API路径">API路径</Option>
          <Option value="请求头">请求头</Option>
          <Option value="请求体">请求体</Option>
          <Option value="响应头">响应头</Option>
          <Option value="响应体">响应体</Option>
          <Option value="完整报文">完整报文</Option>
        </Select>
      ),
    },
    {
      title: '条件操作符',
      dataIndex: 'conditionOperator',
      key: 'conditionOperator',
      render: (value: any, record: any) => (
        <Select
          value={value}
          style={{ width: '100%' }}
          onChange={(val) => handleRuleChange(record.id, 'conditionOperator', val)}
        >
          <Option value="等于">等于</Option>
          <Option value="不等于">不等于</Option>
          <Option value="包含">包含</Option>
          <Option value="不包含">不包含</Option>
          <Option value="正则匹配">正则匹配</Option>
        </Select>
      ),
    },
    {
      title: '条件值',
      dataIndex: 'conditionValue',
      key: 'conditionValue',
      render: (value: any, record: any) => (
        <Input
          value={value}
          onChange={(e) => handleRuleChange(record.id, 'conditionValue', e.target.value)}
          placeholder="请输入条件值"
        />
      ),
    },
    {
      title: '风险等级',
      dataIndex: 'riskLevel',
      key: 'riskLevel',
      render: (value: any, record: any) => (
        <Select
          value={value}
          style={{ width: '100%' }}
          onChange={(val) => handleRuleChange(record.id, 'riskLevel', val)}
        >
          <Option value="高">高</Option>
          <Option value="中">中</Option>
          <Option value="低">低</Option>
        </Select>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      render: (value: any, record: any) => (
        <Input
          value={value}
          onChange={(e) => handleRuleChange(record.id, 'description', e.target.value)}
          placeholder="请输入规则描述"
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: any) => (
        <Popconfirm
          title="确定要删除这条规则吗？"
          onConfirm={() => removeRule(record.id)}
          okText="确定"
          cancelText="取消"
        >
          <Button type="link" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  // 处理规则变更
  const handleRuleChange = (id: number, field: string, value: any) => {
    setRules(prevRules => 
      prevRules.map(rule => 
        rule.id === id ? { ...rule, [field]: value } : rule
      )
    );
  };

  // 添加规则
  const addRule = () => {
    const newId = rules.length > 0 ? Math.max(...rules.map(r => r.id)) + 1 : 0;
    setRules(prevRules => [
      ...prevRules,
      {
        id: newId,
        conditionObject: 'API路径',
        conditionOperator: '包含',
        conditionValue: '',
        riskLevel: '中',
        description: ''
      }
    ]);
  };

  // 删除规则
  const removeRule = (id: number) => {
    setRules(prevRules => prevRules.filter(rule => rule.id !== id));
  };

  // 显示模态框
  const showModal = async (record: any = null) => {
    setEditingRecord(record);
    setIsModalVisible(true);
    
    if (record) {
      try {
        // 从后端获取完整的策略数据
        const policyData = await getApiPolicyById(record.id);
        form.setFieldsValue({
          policyCode: policyData.policyCode,
          policyName: policyData.policyName,
          description: policyData.description,
          sensitivityLevel: policyData.sensitivityLevel,
          hideExample: !!policyData.hideExample,
          ruleExpression: policyData.ruleExpression,
          aiRules: policyData.aiRule  // 注意：后端是aiRule，前端表单是aiRules
        });
        
        // 初始化规则数据
        if (policyData.classificationRules) {
          try {
            const parsedRules = JSON.parse(policyData.classificationRules);
            setRules(parsedRules);
          } catch (e) {
            console.error('解析分类规则失败:', e);
            setRules([]);
          }
        } else {
          setRules([]);
        }
        
        // 初始化验证数据
        if (policyData.validationData) {
          try {
            const parsedData = JSON.parse(policyData.validationData);
            setApiRulesData(parsedData);
          } catch (e) {
            console.error('解析验证数据失败:', e);
            setApiRulesData([]);
          }
        } else {
          setApiRulesData([]);
        }
      } catch (error) {
        message.error('获取策略详情失败');
        console.error(error);
      }
    } else {
      form.resetFields();
      setRules([]);
      setApiRulesData([]);
    }
  };

  // 关闭模态框
  const handleCancel = () => {
    setIsModalVisible(false);
    setEditingRecord(null);
    form.resetFields();
    setRules([]);
    setApiRulesData([]);
  };

  // 保存策略
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      
      // 验证分类规则是否为空
      if (rules.length === 0) {
        message.error('请至少添加一条分类规则');
        return;
      }
      
      // 验证每条规则的必填字段
      for (const rule of rules) {
        if (!rule.conditionObject || !rule.conditionOperator || !rule.conditionValue || !rule.riskLevel) {
          message.error('请完善所有分类规则的必填字段');
          return;
        }
      }
      
      const submitData = {
        policyCode: values.policyCode,
        policyName: values.policyName,
        description: values.description,
        sensitivityLevel: values.sensitivityLevel || 1,
        hideExample: values.hideExample ? 1 : 0,  // 将布尔值转换为整数
        ruleExpression: values.ruleExpression,
        aiRule: values.aiRules,  // 注意：前端表单字段是aiRules，后端是aiRule
        classificationRules: JSON.stringify(rules),
        status: 1  // 默认启用状态
      };
      
      if (editingRecord && editingRecord.id) {
        // 编辑
        let updateData = {
        ...submitData,
        id: editingRecord.id
      };
        await updateApiPolicy(updateData);
        message.success('编辑成功');
      } else {
        // 新增
        await createApiPolicy(submitData);
        message.success('新增成功');
      }
      
      handleCancel();
      loadPolicyData();  // 重新加载数据
    } catch (error) {
      console.error('保存策略失败:', error);
      message.error('保存策略失败');
    }
  };

  // 删除策略
  const handleDelete = async (record: any) => {
    try {
      await deleteApiPolicy(record.id);
      message.success('删除成功');
      loadPolicyData();  // 重新加载数据
    } catch (error) {
      console.error('删除策略失败:', error);
      message.error('删除策略失败');
    }
  };

  // 策略表格列定义
  const policyColumns = [
    {
      title: '策略ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '策略编码',
      dataIndex: 'policyCode',
      key: 'policyCode',
    },
    {
      title: '策略名称',
      dataIndex: 'policyName',
      key: 'policyName',
    },
    {
      title: '策略描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '敏感等级',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      render: (level: number) => (
        <Tag color={level <= 2 ? 'green' : level <= 4 ? 'orange' : 'red'}>
          {level}
        </Tag>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '隐藏样例',
      dataIndex: 'hideExample',
      key: 'hideExample',
      render: (hide: number) => (
        <Tag color={hide === 1 ? 'red' : 'green'}>
          {hide === 1 ? '是' : '否'}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
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
            onConfirm={() => handleDelete(record)}
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
      <Tabs 
        activeKey="api" 
        onChange={handleTabChange}
        items={[
          { key: 'database', label: '数据库' },
          { key: 'api', label: 'API' },
          { key: 'message', label: '消息' },
          { key: 'log', label: '日志' }
        ]}
      />
      
      <Row justify="space-between" align="middle" style={{ marginBottom: 16, marginTop: 16 }}>
        <Col>
          <Space>
            <Input
              placeholder="策略编码"
              value={policyCode}
              onChange={(e) => setPolicyCode(e.target.value)}
              onPressEnter={handleSearch}
              style={{ width: 150 }}
            />
            <Input
              placeholder="策略名称"
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onPressEnter={handleSearch}
              style={{ width: 200 }}
            />
            <Select
              placeholder="敏感等级"
              value={sensitivityLevel}
              onChange={(value) => setSensitivityLevel(value)}
              style={{ width: 120 }}
              allowClear
            >
              <Option value={1}>1 - 低敏感</Option>
              <Option value={2}>2 - 中低敏感</Option>
              <Option value={3}>3 - 中敏感</Option>
              <Option value={4}>4 - 中高敏感</Option>
              <Option value={5}>5 - 高敏感</Option>
            </Select>
            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
              搜索
            </Button>
            <Button onClick={handleReset}>
              重置
            </Button>
          </Space>
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
        dataSource={policyData}
        loading={loading}
        pagination={{ 
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (page, size) => {
            setCurrentPage(page);
            setPageSize(size);
          }
        }} 
        rowKey="id"
      />

      {/* 新增/编辑策略模态框 */}
      <Modal
        title={`${editingRecord ? '编辑' : '新增'}API策略`}
        open={isModalVisible}
        onOk={handleSave}
        onCancel={handleCancel}
        width="80%"
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
                name="policyCode"
                label="策略编码"
                rules={[{ required: true, message: '请输入策略编码!' }]}
              >
                <Input placeholder="请输入策略编码" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="policyName"
                label="策略名称"
                rules={[{ required: true, message: '请输入策略名称!' }]}
              >
                <Input placeholder="请输入策略名称" />
              </Form.Item>
            </Col>
          </Row>
          
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="description"
                label="策略描述"
                rules={[{ required: true, message: '请输入策略描述!' }]}
              >
                <Input placeholder="请输入策略描述" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="sensitivityLevel"
                label="敏感等级"
                rules={[{ required: true, message: '请选择敏感等级!' }]}
              >
                <Select placeholder="请选择敏感等级">
                  <Option value={1}>1 - 低敏感</Option>
                  <Option value={2}>2 - 中低敏感</Option>
                  <Option value={3}>3 - 中敏感</Option>
                  <Option value={4}>4 - 中高敏感</Option>
                  <Option value={5}>5 - 高敏感</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          
          <Row gutter={16}>
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
          
          <Card 
            title={<span>分类规则 <span style={{ color: 'red' }}>*</span></span>}
            extra={
              <Button type="primary" onClick={addRule}>
                添加规则
              </Button>
            }
            size="small"
          >
            <Table
              columns={ruleColumns}
              dataSource={rules}
              pagination={false}
              rowKey="id"
              size="small"
            />
          </Card>
          
          <Form.Item
            name="ruleExpression"
            label="规则表达式"
            rules={[{ required: true, message: '请输入规则表达式!' }]}
            extra="例如: !&(2|3)"
          >
            <Input placeholder="请输入规则表达式" />
          </Form.Item>
          
          <Form.Item
            name="aiRules"
            label="AI规则"
            rules={[{ required: true, message: '请输入AI规则!' }]}
          >
            <Input placeholder="请输入AI规则" />
          </Form.Item>
          
          <Form.Item
            name="validationData"
            label="验证数据"
          >
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
              <Button
                type="dashed"
                onClick={() => {
                  setApiRulesData([
                    ...apiRulesData,
                    { 
                      apiPath: '', 
                      requestHeader: '', 
                      requestBody: '', 
                      responseHeader: '', 
                      responseBody: '', 
                      fullMessage: '' 
                    }
                  ]);
                }}
              >
                + 添加样例
              </Button>
            </div>
            <Table
              columns={[
                {
                  title: 'API路径',
                  dataIndex: 'apiPath',
                  key: 'apiPath',
                  render: (_, record, index) => (
                    <Input
                      value={record.apiPath}
                      onChange={(e) => {
                        const newData = [...apiRulesData];
                        newData[index].apiPath = e.target.value;
                        setApiRulesData(newData);
                      }}
                      placeholder="请输入API路径"
                    />
                  )
                },
                {
                  title: '请求头',
                  dataIndex: 'requestHeader',
                  key: 'requestHeader',
                  render: (_, record, index) => (
                    <Input
                      value={record.requestHeader}
                      onChange={(e) => {
                        const newData = [...apiRulesData];
                        newData[index].requestHeader = e.target.value;
                        setApiRulesData(newData);
                      }}
                      placeholder="请输入请求头"
                    />
                  )
                },
                {
                  title: '请求体',
                  dataIndex: 'requestBody',
                  key: 'requestBody',
                  render: (_, record, index) => (
                    <Input.TextArea
                      value={record.requestBody}
                      onChange={(e) => {
                        const newData = [...apiRulesData];
                        newData[index].requestBody = e.target.value;
                        setApiRulesData(newData);
                      }}
                      placeholder="请输入请求体"
                      rows={2}
                    />
                  )
                },
                {
                  title: '响应头',
                  dataIndex: 'responseHeader',
                  key: 'responseHeader',
                  render: (_, record, index) => (
                    <Input
                      value={record.responseHeader}
                      onChange={(e) => {
                        const newData = [...apiRulesData];
                        newData[index].responseHeader = e.target.value;
                        setApiRulesData(newData);
                      }}
                      placeholder="请输入响应头"
                    />
                  )
                },
                {
                  title: '响应体',
                  dataIndex: 'responseBody',
                  key: 'responseBody',
                  render: (_, record, index) => (
                    <Input.TextArea
                      value={record.responseBody}
                      onChange={(e) => {
                        const newData = [...apiRulesData];
                        newData[index].responseBody = e.target.value;
                        setApiRulesData(newData);
                      }}
                      placeholder="请输入响应体"
                      rows={2}
                    />
                  )
                },
                {
                  title: (
                    <Tooltip title="如果选择完整报文，则不对请求响应的头体进行检测">
                      <span>完整报文 <InfoCircleOutlined /></span>
                    </Tooltip>
                  ),
                  dataIndex: 'fullMessage',
                  key: 'fullMessage',
                  render: (_, record, index) => (
                    <Input.TextArea
                      value={record.fullMessage}
                      onChange={(e) => {
                        const newData = [...apiRulesData];
                        newData[index].fullMessage = e.target.value;
                        setApiRulesData(newData);
                      }}
                      placeholder="请输入完整报文"
                      rows={3}
                    />
                  )
                },
                {
                  title: '操作',
                  key: 'action',
                  render: (_, record, index) => (
                    <Button
                      type="link"
                      danger
                      onClick={() => {
                        const newData = apiRulesData.filter((_, i) => i !== index);
                        setApiRulesData(newData);
                      }}
                    >
                      删除
                    </Button>
                  )
                }
              ]}
              dataSource={apiRulesData}
              pagination={false}
              rowKey={(record, index) => index || 0}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ApiPolicyPage;