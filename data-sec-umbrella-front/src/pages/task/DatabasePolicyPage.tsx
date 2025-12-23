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
  Tabs
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  SearchOutlined,
  PlayCircleOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import API_CONFIG from '../../config/apiConfig.ts';

const { Title } = Typography;
const { Option } = Select;

const DatabasePolicyPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchText, setSearchText] = useState('');
  const [sensitivityLevelFilter, setSensitivityLevelFilter] = useState<number | null>(null);
  const [hideExampleFilter, setHideExampleFilter] = useState<boolean | null>(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<any>(null);
  const [form] = Form.useForm();
  const [policyData, setPolicyData] = useState<any[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [rules, setRules] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState(() => {
    if (location.pathname.includes('/task/policy/api')) return 'api';
    if (location.pathname.includes('/task/policy/message')) return 'message';
    if (location.pathname.includes('/task/policy/log')) return 'log';
    return 'database';
  });
  const [validationResult, setValidationResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [classificationRulesData, setClassificationRulesData] = useState<any[]>([]);

  // 获取策略数据
  const fetchPolicies = async (pageParams: { current?: number; pageSize?: number } = {}) => {
    try {
      setLoading(true);
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DATABASE_POLICY.LIST}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          policyCode: searchText || undefined,
          policyName: searchText || undefined,
          sensitivityLevel: sensitivityLevelFilter,
          hideExample: hideExampleFilter !== null ? (hideExampleFilter ? 1 : 0) : undefined,
          current: pageParams.current || pagination.current,
          size: pageParams.pageSize || pagination.pageSize
        })
      });
      
      if (response.ok) {
        const resultWrapper = await response.json();
        const result = resultWrapper.data;
        // 为前端表格添加key字段，并确保hideExample是数字类型
        const formattedData = result.records.map((item: any, index: number) => ({
          ...item,
          key: item.id || index,
          hideExample: typeof item.hideExample === 'boolean' ? (item.hideExample ? 1 : 0) : item.hideExample
        }));
        setPolicyData(formattedData);
        setPagination({
          ...pagination,
          current: result.current,
          pageSize: result.size,
          total: result.total,
        });
      } else {
        message.error('获取策略数据失败');
      }
    } catch (error) {
      console.error('获取策略数据出错:', error);
      message.error('获取策略数据出错');
    } finally {
      setLoading(false);
    }
  };

  // 组件挂载时获取数据
  useEffect(() => {
    fetchPolicies();
  }, [searchText, sensitivityLevelFilter, hideExampleFilter]);

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
      render: (_: any, record: any) => (
        <Select 
          defaultValue={record.conditionObject} 
          style={{ width: 120 }}
          onChange={(value) => handleRuleChange(record.id, 'conditionObject', value)}
        >
          <Option value="库名" title="ES集群">库名</Option>
          <Option value="库描述">库描述</Option>
          <Option value="表名" title="ES索引">表名</Option>
          <Option value="表描述">表描述</Option>
          <Option value="列名">列名</Option>
          <Option value="列描述">列描述</Option>
          <Option value="列值">列值</Option>
        </Select>
      ),
    },
    {
      title: '条件类型',
      dataIndex: 'conditionType',
      key: 'conditionType',
      render: (_: any, record: any) => (
        <Select 
          defaultValue={record.conditionType} 
          style={{ width: 120 }}
          onChange={(value) => handleRuleChange(record.id, 'conditionType', value)}
        >
          <Option value="包含">包含</Option>
          <Option value="不包含">不包含</Option>
          <Option value="等于">等于</Option>
          <Option value="不等于">不等于</Option>
          <Option value="以...开头">以...开头</Option>
          <Option value="不以...开头">不以...开头</Option>
          <Option value="以...结尾">以...结尾</Option>
          <Option value="不以...结尾">不以...结尾</Option>
          <Option value="在...之中">在...之中</Option>
          <Option value="不在...之中">不在...之中</Option>
          <Option value="大于">大于</Option>
          <Option value="小于">小于</Option>
          <Option value="大于等于">大于等于</Option>
          <Option value="小于等于">小于等于</Option>
          <Option value="正则匹配">正则匹配</Option>
          <Option value="内置算法">内置算法</Option>
        </Select>
      ),
    },
    {
      title: '表达式',
      dataIndex: 'expression',
      key: 'expression',
      render: (_: any, record: any) => (
        <Input 
          defaultValue={record.expression}
          onChange={(e) => handleRuleChange(record.id, 'expression', e.target.value)}
        />
      ),
    },
    {
      title: '比例',
      dataIndex: 'ratio',
      key: 'ratio',
      render: (_: any, record: any) => (
        <InputNumber 
          min={1} 
          max={100} 
          defaultValue={record.ratio || 100}
          onChange={(value) => handleRuleChange(record.id, 'ratio', value)}
          formatter={value => `${value}%`}
          parser={(value: any) => value!.replace('%', '') as unknown as number}
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

  // 处理选项卡切换
  const handleTabChange = (key: string) => {
    setActiveTab(key);
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

  // 添加规则
  const addRule = () => {
    const newId = rules.length > 0 ? Math.max(...rules.map(r => r.id)) + 1 : 0;
    setRules(prevRules => [
      ...prevRules,
      {
        id: newId,
        conditionObject: '库名',
        conditionType: '包含',
        expression: '',
        ratio: 100
      }
    ]);
  };

  // 删除规则
  const removeRule = (id: number) => {
    setRules(prevRules => prevRules.filter(rule => rule.id !== id));
  };

  // 显示模态框
  const showModal = (record: any = null) => {
    setEditingRecord(record);
    setIsModalVisible(true);
    
    if (record) {
      form.setFieldsValue({
        ...record,
        hideExample: record.hideExample === 1  // 将整数转换为布尔值
      });
      // 初始化规则数据
      if (record.classificationRules) {
        try {
          const parsedRules = JSON.parse(record.classificationRules);
          setRules(parsedRules);
        } catch (e) {
          console.error('解析分类规则失败:', e);
          setRules([]);
        }
      } else {
        setRules([]);
      }
      
      // 初始化验证数据
      if (record.classificationRulesData) {
        try {
          const parsedData = JSON.parse(record.classificationRulesData);
          setClassificationRulesData(parsedData);
        } catch (e) {
          console.error('解析验证数据失败:', e);
          setClassificationRulesData([]);
        }
      } else {
        setClassificationRulesData([]);
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
  };

  // 保存策略
  const handleSave = () => {
    form.validateFields().then(async values => {
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
      
      // 添加分类规则到表单数据，并转换布尔值为整数
      const formData = {
        ...values,
        hideExample: values.hideExample ? 1 : 0,  // 将布尔值转换为整数
        classificationRules: JSON.stringify(rules),
        classificationRulesData: JSON.stringify(classificationRulesData)
      };
      
      try {
        let response;
        
        if (editingRecord) {
          // 编辑
          response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DATABASE_POLICY.UPDATE}`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              id: editingRecord.id,
              ...formData
            })
          });
        } else {
          // 新增
          response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DATABASE_POLICY.CREATE}`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
          });
        }
        
        if (response.ok) {
          message.success(`${editingRecord ? '编辑' : '新增'}成功`);
          setClassificationRulesData([]); // 清空表格数据
          handleCancel();
          fetchPolicies(); // 重新获取数据
        } else {
          message.error(`${editingRecord ? '编辑' : '新增'}失败`);
        }
      } catch (error) {
        console.error('保存策略出错:', error);
        message.error('保存策略出错');
      }
    });
  };

  // 删除策略
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DATABASE_POLICY.DELETE}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ id })
      });
      
      if (response.ok) {
        message.success('删除成功');
        fetchPolicies(); // 重新获取数据
      } else {
        message.error('删除失败');
      }
    } catch (error) {
      console.error('删除策略出错:', error);
      message.error('删除策略出错');
    }
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

  // 策略表格列定义
  const policyColumns = [
    {
      title: '策略code',
      dataIndex: 'policyCode',
      key: 'policyCode',
    },
    {
      title: '策略名',
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
      render: (hide: number) => (
        <Tag color={hide === 1 ? 'red' : 'green'}>
          {hide === 1 ? '是' : '否'}
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
            onConfirm={() => handleDelete(record.id)}
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
        activeKey={activeTab} 
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
      
          <Select 
            placeholder="敏感等级" 
            style={{ width: 120, marginRight: 16 }}
            allowClear
            value={sensitivityLevelFilter}
            onChange={(value) => setSensitivityLevelFilter(value)}
          >
            {[1, 2, 3, 4, 5].map(level => (
              <Option key={level} value={level}>
                {level} {level > 3 ? '(高)' : level > 1 ? '(中)' : '(低)'}
              </Option>
            ))}
          </Select>
          <Select 
            placeholder="隐藏样例" 
            style={{ width: 120, marginRight: 16 }}
            allowClear
            value={hideExampleFilter}
            onChange={(value) => setHideExampleFilter(value)}
          >
            <Option value={true}>是</Option>
            <Option value={false}>否</Option>
          </Select>
          <Button 
            type="primary" 
            icon={<SearchOutlined />}
            onClick={() => fetchPolicies({ current: 1 })}
            style={{ marginRight: 8 }}
          >
            查询
          </Button>
          <Button 
            onClick={() => {
              setSearchText('');
              setSensitivityLevelFilter(null);
              setHideExampleFilter(null);
              fetchPolicies({ current: 1 });
            }}
          >
            清空条件
          </Button>
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
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          onChange: (page, pageSize) => {
            fetchPolicies({ current: page, pageSize });
          },
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
        }}
        rowKey="key"
        loading={loading}
      />

      {/* 新增/编辑策略模态框 */}
      <Modal
        title={`${editingRecord ? '编辑' : '新增'}数据库策略`}
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
            hideExample: false,
            classificationRules: '[]'
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="policyCode"
                label="策略code"
                rules={[{ required: true, message: '请输入策略code!' }]}
                extra="只能输入字母、数字、_、-"
              >
                <Input placeholder="请输入策略code" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="policyName"
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
            name="classificationRules"
            label="验证数据"
          >
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
              <Button
                type="dashed"
                onClick={() => {
                  setClassificationRulesData([
                    ...classificationRulesData,
                    { 
                      databaseName: '', 
                      databaseDescription: '', 
                      tableName: '', 
                      tableDescription: '', 
                      columnName: '', 
                      columnDescription: '', 
                      columnValue: '' 
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
                   title: '库名',
                   dataIndex: 'databaseName',
                   key: 'databaseName',
                   render: (_, record, index) => (
                     <Input
                       value={record.databaseName}
                       onChange={(e) => {
                         const newData = [...classificationRulesData];
                         newData[index].databaseName = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入库名"
                     />
                   )
                 },
                 {
                   title: '库描述',
                   dataIndex: 'databaseDescription',
                   key: 'databaseDescription',
                   render: (_, record, index) => (
                     <Input
                       value={record.databaseDescription}
                       onChange={(e) => {
                         const newData = [...classificationRulesData];
                         newData[index].databaseDescription = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入库描述"
                     />
                   )
                 },
                 {
                   title: '表名',
                   dataIndex: 'tableName',
                   key: 'tableName',
                   render: (_, record, index) => (
                     <Input
                       value={record.tableName}
                       onChange={(e) => {
                         const newData = [...classificationRulesData];
                         newData[index].tableName = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入表名"
                     />
                   )
                 },
                 {
                   title: '表描述',
                   dataIndex: 'tableDescription',
                   key: 'tableDescription',
                   render: (_, record, index) => (
                     <Input
                       value={record.tableDescription}
                       onChange={(e) => {
                         const newData = [...classificationRulesData];
                         newData[index].tableDescription = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入表描述"
                     />
                   )
                 },
                 {
                   title: '列名',
                   dataIndex: 'columnName',
                   key: 'columnName',
                   render: (_, record, index) => (
                     <Input
                       value={record.columnName}
                       onChange={(e) => {
                         const newData = [...classificationRulesData];
                         newData[index].columnName = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入列名"
                     />
                   )
                 },
                 {
                   title: '列描述',
                   dataIndex: 'columnDescription',
                   key: 'columnDescription',
                   render: (_, record, index) => (
                     <Input
                       value={record.columnDescription}
                       onChange={(e) => {
                         const newData = [...classificationRulesData];
                         newData[index].columnDescription = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入列描述"
                     />
                   )
                 },
                 {
                   title: '列值',
                   dataIndex: 'columnValue',
                   key: 'columnValue',
                   render: (_, record, index) => (
                     <Input.TextArea
                       value={record.columnValue}
                       onChange={(e) => {
                         const newData = [...classificationRulesData];
                         newData[index].columnValue = e.target.value;
                         setClassificationRulesData(newData);
                       }}
                       placeholder="请输入列值"
                       rows={2}
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
                         const newData = classificationRulesData.filter((_, i) => i !== index);
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
               rowKey={(record, index) => index}
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

export default DatabasePolicyPage;