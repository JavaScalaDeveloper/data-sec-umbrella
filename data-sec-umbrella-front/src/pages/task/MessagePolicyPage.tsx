import React, { useState, useEffect } from 'react';
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
  Card,
  Tabs,
  Spin
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined,
  PlayCircleOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import API_CONFIG from '../../config/apiConfig.ts';

const { Option } = Select;
const { Title } = Typography;

// 定义消息策略接口
interface MessagePolicy {
  id?: number;
  policyCode: string;
  policyName: string;
  description: string;
  sensitivityLevel: number;
  hideExample: number;
  classificationRules?: string;
  classificationRulesData?: string;
  ruleExpression?: string;
  aiRule?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

// 分页响应DTO
interface PageResponseDTO<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 查询请求DTO
interface MessagePolicyQueryDTO {
  current: number;
  size: number;
  policyName?: string;
}

const MessagePolicyPage: React.FC = () => {
  const [searchText, setSearchText] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<MessagePolicy | null>(null);
  const [form] = Form.useForm();
  const [policyData, setPolicyData] = useState<MessagePolicy[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const navigate = useNavigate();
  
  // 分类规则状态
  const [rules, setRules] = useState<any[]>([]);
  const [validationResult, setValidationResult] = useState<any>(null);
  const [classificationRulesData, setClassificationRulesData] = useState<any[]>([]);

  // API调用函数
  const fetchMessagePolicies = async (params: MessagePolicyQueryDTO) => {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MESSAGE_POLICY.LIST}`, {
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
      console.error('获取消息策略列表失败:', error);
      throw error;
    }
  };

  const getMessagePolicyById = async (id: number) => {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MESSAGE_POLICY.GET}`, {
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
      console.error('获取消息策略详情失败:', error);
      throw error;
    }
  };

  const createMessagePolicy = async (policy: MessagePolicy) => {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MESSAGE_POLICY.CREATE}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(policy),
      });
      const result = await response.json();
      if (result.code === 200) {
        return result.data;
      } else {
        throw new Error(result.message || '创建策略失败');
      }
    } catch (error) {
      console.error('创建消息策略失败:', error);
      throw error;
    }
  };

  const updateMessagePolicy = async (policy: MessagePolicy) => {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MESSAGE_POLICY.UPDATE}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(policy),
      });
      const result = await response.json();
      if (result.code === 200) {
        return result.data;
      } else {
        throw new Error(result.message || '更新策略失败');
      }
    } catch (error) {
      console.error('更新消息策略失败:', error);
      throw error;
    }
  };

  const deleteMessagePolicy = async (id: number) => {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MESSAGE_POLICY.DELETE}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ id }),
      });
      const result = await response.json();
      if (result.code === 200) {
        return true;
      } else {
        throw new Error(result.message || '删除策略失败');
      }
    } catch (error) {
      console.error('删除消息策略失败:', error);
      throw error;
    }
  };

  // 加载数据
  const loadPolicyData = async () => {
    setLoading(true);
    try {
      const params: MessagePolicyQueryDTO = {
        current: currentPage,
        size: pageSize,
        policyName: searchText,
      };
      const data = await fetchMessagePolicies(params);
      setPolicyData(data.records || []);
      setTotal(data.total || 0);
    } catch (error) {
      message.error('获取策略列表失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 搜索函数
  const handleSearch = () => {
    setCurrentPage(1); // 搜索时重置到第一页
    loadPolicyData();
  };

  // 初始化加载数据
  useEffect(() => {
    loadPolicyData();
  }, [currentPage, pageSize]);

  // 分类规则表格列定义
  const ruleColumns = [
    {
      title: '序号',
      key: 'index',
      render: (_: any, __: any, index: number) => index + 1,
    },
    {
      title: '条件对象',
      dataIndex: 'conditionObject',
      key: 'conditionObject',
      render: (value: string, record: any) => (
        <Select
          value={value}
          style={{ width: '100%' }}
          onChange={(val: any) => handleRuleChange(record.id, 'conditionObject', val)}
        >
          <Option value="集群名">集群名</Option>
          <Option value="集群描述">集群描述</Option>
          <Option value="Topic名">Topic名</Option>
          <Option value="Topic描述">Topic描述</Option>
          <Option value="消息内容">消息内容</Option>
        </Select>
      ),
    },
    {
      title: '条件类型',
      dataIndex: 'conditionType',
      key: 'conditionType',
      render: (value: string, record: any) => (
        <Select
          value={value}
          style={{ width: '100%' }}
          onChange={(val: any) => handleRuleChange(record.id, 'conditionType', val)}
        >
          <Option value="包含">包含</Option>
          <Option value="不包含">不包含</Option>
          <Option value="等于">等于</Option>
          <Option value="不等于">不等于</Option>
          <Option value="正则匹配">正则匹配</Option>
        </Select>
      ),
    },
    {
      title: '表达式',
      dataIndex: 'expression',
      key: 'expression',
      render: (value: string, record: any) => (
        <Input
          value={value}
          onChange={(e: any) => handleRuleChange(record.id, 'expression', e.target.value)}
          placeholder="请输入表达式"
        />
      ),
    },
    {
      title: '比例(%)',
      dataIndex: 'ratio',
      key: 'ratio',
      render: (value: number, record: any) => (
        <InputNumber
          value={value}
          min={0}
          max={100}
          onChange={(val: any) => handleRuleChange(record.id, 'ratio', val)}
          style={{ width: '100%' }}
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
    setRules((prevRules: any[]) => 
      prevRules.map((rule: any) => 
        rule.id === id ? { ...rule, [field]: value } : rule
      )
    );
  };

  // 添加规则
  const addRule = () => {
    const newId = rules.length > 0 ? Math.max(...rules.map((r: any) => r.id)) + 1 : 0;
    setRules((prevRules: any[]) => [
      ...prevRules,
      {
        id: newId,
        conditionObject: '集群名',
        conditionType: '包含',
        expression: '',
        ratio: 100
      }
    ]);
  };

  // 删除规则
  const removeRule = (id: number) => {
    setRules((prevRules: any[]) => prevRules.filter((rule: any) => rule.id !== id));
  };

  // 测试规则
  const testRules = (validationData: string) => {
    // 模拟测试结果
    const result = {
      rulePassed: true,
      ruleDetails: [
        { rule: '规则1', matched: true, detail: '匹配到用户信息表' },
        { rule: '规则2', matched: false, detail: '未匹配到敏感字段' }
      ],
      aiPassed: true,
      aiDetail: 'AI分析通过，未发现异常模式'
    };
    setValidationResult(result);
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

  // 显示模态框
  const showModal = async (record: MessagePolicy | null = null) => {
    setEditingRecord(record);
    setIsModalVisible(true);
    
    if (record && record.id) {
      try {
        // 从后端获取完整的策略数据
        const policyData = await getMessagePolicyById(record.id);
        
        form.setFieldsValue({
          policyCode: policyData.policyCode,
          policyName: policyData.policyName,
          description: policyData.description,
          sensitivityLevel: policyData.sensitivityLevel,
          hideExample: policyData.hideExample === 1,
          ruleExpression: policyData.ruleExpression,
          aiRule: policyData.aiRule
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
        if (policyData.classificationRulesData) {
          try {
            const parsedData = JSON.parse(policyData.classificationRulesData);
            setClassificationRulesData(parsedData);
          } catch (e) {
            console.error('解析验证数据失败:', e);
            setClassificationRulesData([]);
          }
        } else {
          setClassificationRulesData([]);
        }
      } catch (error) {
        message.error('获取策略详情失败');
        console.error(error);
        handleCancel();
      }
    } else {
      form.resetFields();
      setRules([]);
      setClassificationRulesData([]);
    }
  };

  // 关闭模态框
  const handleCancel = () => {
    setIsModalVisible(false);
    setEditingRecord(null);
    form.resetFields();
    setRules([]);
    setClassificationRulesData([]);
    setValidationResult(null);
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
        if (!rule.conditionObject || !rule.conditionType || !rule.expression || !rule.ratio) {
          message.error('请完善所有分类规则的必填字段');
          return;
        }
      }
      
      const submitData: MessagePolicy = {
        policyCode: values.policyCode,
        policyName: values.policyName,
        description: values.description,
        sensitivityLevel: values.sensitivityLevel || 1,
        hideExample: values.hideExample ? 1 : 0,  // 将布尔值转换为整数
        ruleExpression: values.ruleExpression,
        aiRule: values.aiRule,
        classificationRules: JSON.stringify(rules),
        classificationRulesData: JSON.stringify(classificationRulesData),
        status: 1  // 默认启用状态
      };
      
      if (editingRecord && editingRecord.id) {
        // 编辑
        submitData.id = editingRecord.id;
        await updateMessagePolicy(submitData);
        message.success('编辑成功');
      } else {
        // 新增
        await createMessagePolicy(submitData);
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
  const handleDelete = async (record: MessagePolicy) => {
    if (!record.id) {
      message.error('无效的策略ID');
      return;
    }
    
    try {
      await deleteMessagePolicy(record.id);
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
        activeKey="message" 
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
              placeholder="请输入策略名称"
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onPressEnter={handleSearch}
              style={{ width: 200 }}
            />
            <Button 
              type="primary" 
              icon={<SearchOutlined />}
              onClick={handleSearch}
            >
              搜索
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
          showTotal: (total: number) => `共 ${total} 条`,
          onChange: (page: number, size: number) => {
            setCurrentPage(page);
            setPageSize(size || 10);
          }
        }} 
        rowKey="id"
      />

      {/* 新增/编辑策略模态框 */}
      <Modal
        title={`${editingRecord ? '编辑' : '新增'}消息策略`}
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
            name="aiRule"
            label="AI规则"
            rules={[{ required: true, message: '请输入AI规则!' }]}
          >
            <Input placeholder="请输入AI规则" />
          </Form.Item>
          
          <Form.Item
            name="classificationRulesData"
            label="验证数据"
          >
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
              <Button
                type="dashed"
                onClick={() => {
                  setClassificationRulesData([
                    ...classificationRulesData,
                    { 
                      clusterName: '', 
                      clusterDescription: '', 
                      topicName: '', 
                      topicDescription: '', 
                      messageContent: ''
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
                   title: '集群名',
                   dataIndex: 'clusterName',
                   key: 'clusterName',
                   render: (_: any, record: any, index: number) => (
                     <Input
                       value={record.clusterName}
                       onChange={(e: any) => {
                         const newData = [...classificationRulesData];
                         newData[index].clusterName = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入集群名"
                     />
                   )
                 },
                 {
                   title: '集群描述',
                   dataIndex: 'clusterDescription',
                   key: 'clusterDescription',
                   render: (_: any, record: any, index: number) => (
                     <Input
                       value={record.clusterDescription}
                       onChange={(e: any) => {
                         const newData = [...classificationRulesData];
                         newData[index].clusterDescription = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入集群描述"
                     />
                   )
                 },
                 {
                   title: 'Topic名',
                   dataIndex: 'topicName',
                   key: 'topicName',
                   render: (_: any, record: any, index: number) => (
                     <Input
                       value={record.topicName}
                       onChange={(e: any) => {
                         const newData = [...classificationRulesData];
                         newData[index].topicName = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入Topic名"
                     />
                   )
                 },
                 {
                   title: 'Topic描述',
                   dataIndex: 'topicDescription',
                   key: 'topicDescription',
                   render: (_: any, record: any, index: number) => (
                     <Input
                       value={record.topicDescription}
                       onChange={(e: any) => {
                         const newData = [...classificationRulesData];
                         newData[index].topicDescription = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入Topic描述"
                     />
                   )
                 },
                 {
                   title: '消息内容',
                   dataIndex: 'messageContent',
                   key: 'messageContent',
                   render: (_: any, record: any, index: number) => (
                     <Input.TextArea
                       value={record.messageContent}
                       onChange={(e: any) => {
                         const newData = [...classificationRulesData];
                         newData[index].messageContent = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入消息内容"
                       rows={2}
                     />
                   )
                 },
                 {
                   title: '操作',
                   key: 'action',
                   render: (_: any, record: any, index: number) => (
                     <Button
                       type="link"
                       danger
                       onClick={() => {
                         const newData = classificationRulesData.filter((_: any, i: number) => i !== index);
                         setClassificationRulesData(newData);
                       }}
                     >
                       删除
                     </Button>
                   )
                 }
               ]}
               dataSource={classificationRulesData}
               pagination={false}
               rowKey={(record, index) => index?.toString() || ''}
             />
          </Form.Item>
          
          <Form.Item>
            <Button 
              type="primary" 
              icon={<PlayCircleOutlined />}
              onClick={() => {
                if (rules.length > 0) {
                  testRules(JSON.stringify(rules));
                } else {
                  message.warning('请添加至少一条分类规则');
                }
              }}
            >
              测试规则
            </Button>
          </Form.Item>
          
          {validationResult && (
            <Card title="验证结果" size="small">
              <p>
                规则验证:
                <Tag color={validationResult.rulePassed ? 'green' : 'red'}>
                  {validationResult.rulePassed ? '通过' : '未通过'}
                </Tag>
              </p>
              <p>
                AI规则验证:
                <Tag color={validationResult.aiPassed ? 'green' : 'red'}>
                  {validationResult.aiPassed ? '通过' : '未通过'}
                </Tag>
              </p>
              {validationResult.ruleDetails && (
                <div>
                  <p>规则命中详情:</p>
                  <ul>
                    {validationResult.ruleDetails.map((detail: any, index: number) => (
                      <li key={index}>
                        {detail.rule}: 
                        <Tag color={detail.matched ? 'green' : 'red'}>
                          {detail.matched ? '命中' : '未命中'}
                        </Tag>
                        ({detail.detail})
                      </li>
                    ))}
                  </ul>
                </div>
              )}
              {validationResult.aiDetail && (
                <p>AI分析详情: {validationResult.aiDetail}</p>
              )}
            </Card>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default MessagePolicyPage;