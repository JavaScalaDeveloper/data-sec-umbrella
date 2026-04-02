import React, {useState, useEffect} from 'react';
import {
    Layout,
    Menu,
    Typography,
    Tabs,
    Table,
    Button,
    Space,
    Form,
    Input,
    Select,
    Card,
    Row,
    Col,
    message,
    Modal,
    InputNumber,
    Popconfirm,
    Tag
} from 'antd';
import {
    HomeOutlined,
    LockOutlined,
    DatabaseOutlined,
    AppstoreOutlined,
    ThunderboltOutlined,
    SettingOutlined,
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    SearchOutlined,
    PlayCircleOutlined
} from '@ant-design/icons';
import type {ColumnsType} from 'antd/es/table';
import {databasePolicyApi} from '../services/api';

const {Content, Sider} = Layout;
const {Title} = Typography;
const {TabPane} = Tabs;
const {Option} = Select;
const {TextArea} = Input;

interface Policy {
    id: number;
    createTime: string;
    modifyTime: string;
    creator: string;
    modifier: string;
    policyCode: string;
    policyName: string;
    description: string;
    sensitivityLevel: number;
    hideExample: number;
}

const DatabaseSecurity: React.FC = () => {
    const [activeMenu, setActiveMenu] = useState('2');
    const [activeTab, setActiveTab] = useState('mysql');
    const [form] = Form.useForm();
    const [editForm] = Form.useForm();
    const [policies, setPolicies] = useState<Policy[]>([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({
        current: 1,
        pageSize: 10,
        total: 0,
    });
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [currentPolicy, setCurrentPolicy] = useState<Policy | null>(null);
    const [classificationRules, setClassificationRules] = useState<any[]>([]);
    const [ruleExpression, setRuleExpression] = useState('');
    const [aiRule, setAiRule] = useState('');
    const [classificationRulesData, setClassificationRulesData] = useState<any[]>([{
        id: Date.now(),
        databaseName: '',
        databaseDescription: '',
        tableName: '',
        tableDescription: '',
        columnName: '',
        columnDescription: '',
        columnValues: ['']
    }]);
    const [validationResult, setValidationResult] = useState<any>(null);

    // 获取数据
    const fetchData = async (params: any = {}) => {
        setLoading(true);
        try {
            console.log('请求参数:', params);
            const response = await databasePolicyApi.getPage({
                current: pagination.current,
                size: pagination.pageSize,
                ...params,
            });
            console.log('响应数据:', response);
            if (response.code === 200) {
                console.log('数据记录:', response.data.records);
                setPolicies(response.data.records);
                setPagination({
                    ...pagination,
                    total: response.data.total,
                });
            } else {
                message.error(response.message || '获取数据失败');
            }
        } catch (error) {
            message.error('网络请求失败');
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    // 初始化数据
    useEffect(() => {
        console.log('组件挂载，开始获取数据');
        fetchData();
    }, [pagination.current, pagination.pageSize]);

    // 监听policies变化
    useEffect(() => {
        console.log('policies状态变化:', policies);
        console.log('policies长度:', policies.length);
    }, [policies]);

    // 表格列配置
    const columns: ColumnsType<Policy> = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
        },
        {
            title: '修改时间',
            dataIndex: 'modifyTime',
            key: 'modifyTime',
        },
        {
            title: '创建人',
            dataIndex: 'creator',
            key: 'creator',
        },
        {
            title: '修改人',
            dataIndex: 'modifier',
            key: 'modifier',
        },
        {
            title: '策略代码',
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
            render: (level) => (
                <span style={{color: level >= 4 ? '#f5222d' : level >= 2 ? '#faad14' : '#52c41a'}}>{level}</span>),
        },
        {
            title: '隐藏样例',
            dataIndex: 'hideExample',
            key: 'hideExample',
            render: (hide) => (hide === 1 ? '是' : '否'),
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (<Space size="middle"><Button icon={<EditOutlined/>}
                                                                 onClick={() => handleEdit(record.id)}>编辑</Button>
                <Button
                    icon={<DeleteOutlined/>} danger onClick={() => handleDelete(record.id)}>删除</Button>
            </Space>),
        },
    ];

    // 处理菜单点击
    const handleMenuClick = (key: string) => {
        setActiveMenu(key);
    };

    // 处理Tab切换
    const handleTabChange = (key: string) => {
        setActiveTab(key);
    };

    // 处理查询
    const handleSearch = () => {
        const values = form.getFieldsValue();
        const params = {
            policyCode: values.policy_code,
            policyName: values.policy_name,
            creator: values.creator,
            sensitivityLevel: values.sensitivity_level,
            hideExample: values.hide_example,
        };
        fetchData(params);
    };

    // 处理重置
    const handleReset = () => {
        form.resetFields();
        fetchData();
    };

    // 处理分页
    const handlePaginationChange = (page: number, pageSize: number) => {
        setPagination({
            current: page,
            pageSize,
            total: pagination.total,
        });
    };

    // 处理删除
    const handleDelete = async (id: number) => {
        try {
            const response = await databasePolicyApi.delete(id);
            if (response.code === 200) {
                message.success('删除成功');
                fetchData();
            } else {
                message.error(response.message || '删除失败');
            }
        } catch (error) {
            message.error('网络请求失败');
            console.error(error);
        }
    };

    // 处理编辑按钮点击
    const handleEdit = async (id: number) => {
        try {
            const response = await databasePolicyApi.getById(id);
            if (response.code === 200) {
                setCurrentPolicy(response.data);
                editForm.setFieldsValue(response.data);

                // 解析分类规则
                if (response.data.classificationRules) {
                    try {
                        setClassificationRules(JSON.parse(response.data.classificationRules));
                    } catch (e) {
                        setClassificationRules([]);
                    }
                } else {
                    setClassificationRules([]);
                }

                // 设置规则表达式和AI规则
                setRuleExpression(response.data.ruleExpression || '');
                setAiRule(response.data.aiRule || '');

                setEditModalVisible(true);
            } else {
                message.error(response.message || '获取策略详情失败');
            }
        } catch (error) {
            message.error('网络请求失败');
            console.error(error);
        }
    };

    // 添加分类规则
    const addRule = () => {
        const newRule = {
            id: Date.now(),
            conditionObject: '',
            conditionType: '',
            expression: '',
            ratio: 100,
        };
        setClassificationRules([...classificationRules, newRule]);
    };

    // 修改分类规则
    const handleRuleChange = (id: number, field: string, value: any) => {
        setClassificationRules(classificationRules.map(rule =>
            rule.id === id ? {...rule, [field]: value} : rule
        ));
    };

    // 删除分类规则
    const removeRule = (id: number) => {
        setClassificationRules(classificationRules.filter(rule => rule.id !== id));
    };

    // 测试规则
    const testRules = async () => {
        try {
            const formValues = editForm.getFieldsValue();
            const response = await databasePolicyApi.testRules({
                classificationRules: classificationRules,
                ruleExpression,
                aiRule,
                testData: classificationRulesData,
                databaseType: formValues.databaseType,
            });
            if (response.code === 200) {
                setValidationResult(response.data);
            } else {
                message.error(response.message || '测试规则失败');
            }
        } catch (error) {
            message.error('网络请求失败');
            console.error(error);
        }
    };

    // 处理编辑提交
    const handleEditSubmit = async () => {
        try {
            const values = await editForm.validateFields();
            const response = await databasePolicyApi.update({
                id: currentPolicy?.id,
                policyCode: values.policyCode,
                policyName: values.policyName,
                description: values.description,
                sensitivityLevel: values.sensitivityLevel,
                hideExample: values.hideExample,
                classification_rules: JSON.stringify(classificationRules),
                rule_expression: ruleExpression,
                ai_rule: aiRule,
            });
            if (response.code === 200) {
                message.success('编辑成功');
                setEditModalVisible(false);
                fetchData();
            } else {
                message.error(response.message || '编辑失败');
            }
        } catch (error) {
            message.error('表单验证失败或网络请求失败');
            console.error(error);
        }
    };

    return (
        <>
            <Layout style={{minHeight: '100vh', width: '100%', margin: 0, padding: 0}}>
                <Sider width={200} theme="light">
                    <Menu
                        mode="inline"
                        selectedKeys={[activeMenu]}
                        style={{height: '100%', borderRight: 0}}
                        onClick={({key}) => handleMenuClick(key)}
                        items={[
                            {
                                key: '1',
                                icon: <HomeOutlined/>,
                                label: '概览',
                            },
                            {
                                key: '2',
                                icon: <LockOutlined/>,
                                label: '策略管理',
                            },
                            {
                                key: '3',
                                icon: <DatabaseOutlined/>,
                                label: '数据源',
                            },
                            {
                                key: '4',
                                icon: <AppstoreOutlined/>,
                                label: '数据资产',
                            },
                            {
                                key: '5',
                                icon: <ThunderboltOutlined/>,
                                label: '任务管理',
                                children: [
                                    {
                                        key: '5-1',
                                        label: '实时任务',
                                    },
                                    {
                                        key: '5-2',
                                        label: '批量任务',
                                    },
                                ],
                            },
                            {
                                key: '6',
                                icon: <SettingOutlined/>,
                                label: '配置中心',
                            },
                        ]}
                    />
                </Sider>
                <Layout style={{width: 'calc(100% - 200px)', margin: 0, padding: 0}}>
                    <Content style={{
                        padding: '24px',
                        marginTop: 0,
                        minHeight: 'calc(100vh - 64px)',
                        margin: 0,
                        width: '100%'
                    }}>
                        {activeMenu === '2' ? (
                            <>
                                <div style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    marginBottom: '24px'
                                }}>
                                    <Title level={3}>策略管理</Title>
                                    <Button type="primary" icon={<PlusOutlined/>}>新增策略</Button>
                                </div>
                                <Tabs activeKey={activeTab} onChange={handleTabChange}>
                                    <TabPane tab="MySQL" key="mysql">
                                        <Card style={{marginBottom: '24px'}}>
                                            <Form form={form} layout="inline">
                                                <Row gutter={16}>
                                                    <Col span={6}>
                                                        <Form.Item name="policy_code" label="策略代码">
                                                            <Input placeholder="请输入策略代码"/>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Form.Item name="policy_name" label="策略名称">
                                                            <Input placeholder="请输入策略名称"/>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Form.Item name="creator" label="创建人">
                                                            <Input placeholder="请输入创建人"/>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Form.Item name="sensitivity_level" label="敏感等级">
                                                            <Select placeholder="请选择敏感等级"
                                                                    style={{width: '100%'}}>
                                                                <Option value={1}>1-低</Option>
                                                                <Option value={2}>2-中低</Option>
                                                                <Option value={3}>3-中</Option>
                                                                <Option value={4}>4-中高</Option>
                                                                <Option value={5}>5-高</Option>
                                                            </Select>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Form.Item name="hide_example" label="隐藏样例">
                                                            <Select placeholder="请选择是否隐藏样例"
                                                                    style={{width: '100%'}}>
                                                                <Option value={0}>否</Option>
                                                                <Option value={1}>是</Option>
                                                            </Select>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Space>
                                                            <Button type="primary" icon={<SearchOutlined/>}
                                                                    onClick={handleSearch}>查询</Button>
                                                            <Button onClick={handleReset}>重置</Button>
                                                        </Space>
                                                    </Col>
                                                </Row>
                                            </Form>
                                        </Card>
                                        <Table
                                            columns={columns}
                                            dataSource={policies}
                                            rowKey="id"
                                            loading={loading}
                                            pagination={{
                                                current: pagination.current,
                                                pageSize: pagination.pageSize,
                                                total: pagination.total,
                                                onChange: handlePaginationChange,
                                            }}
                                            style={{width: '100%'}}
                                        />
                                    </TabPane>
                                    <TabPane tab="Clickhouse" key="clickhouse">
                                        <div
                                            style={{textAlign: 'center', padding: '50px'}}>Clickhouse策略管理功能开发中
                                        </div>
                                    </TabPane>
                                </Tabs>
                            </>
                        ) : activeMenu === '1' ? (
                            <Title level={3}>概览</Title>
                        ) : activeMenu === '3' ? (
                            <Title level={3}>数据源</Title>
                        ) : activeMenu === '4' ? (
                            <Title level={3}>数据资产</Title>
                        ) : activeMenu === '5-1' ? (
                            <Title level={3}>实时任务</Title>
                        ) : activeMenu === '5-2' ? (
                            <Title level={3}>批量任务</Title>
                        ) : activeMenu === '6' ? (
                            <Title level={3}>配置中心</Title>
                        ) : null}
                    </Content>
                </Layout>
            </Layout>
            <Modal
                title="编辑策略"
                open={editModalVisible}
                onCancel={() => {
                    setEditModalVisible(false);
                    setValidationResult(null);
                }}
                onOk={handleEditSubmit}
                width={800}
                footer={[
                    <Button key="cancel" onClick={() => {
                        setEditModalVisible(false);
                        setValidationResult(null);
                    }}>取消</Button>,
                    <Button key="submit" type="primary" onClick={handleEditSubmit}>提交</Button>,
                ]}
            >
                <Form form={editForm} layout="vertical">
                    <Form.Item
                        name="policyCode"
                        label="策略代码"
                        rules={[{required: true, message: '请输入策略代码'}]}
                    >
                        <Input placeholder="请输入策略代码"/>
                    </Form.Item>
                    <Form.Item
                        name="policyName"
                        label="策略名称"
                        rules={[{required: true, message: '请输入策略名称'}]}
                    >
                        <Input placeholder="请输入策略名称"/>
                    </Form.Item>
                    <Form.Item name="description" label="描述">
                        <TextArea placeholder="请输入描述" rows={4}/>
                    </Form.Item>
                    <Form.Item
                        name="sensitivityLevel"
                        label="敏感等级"
                        rules={[{required: true, message: '请选择敏感等级'}]}
                    >
                        <Select placeholder="请选择敏感等级">
                            <Option value={1}>1-低</Option>
                            <Option value={2}>2-中低</Option>
                            <Option value={3}>3-中</Option>
                            <Option value={4}>4-中高</Option>
                            <Option value={5}>5-高</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item
                        name="hideExample"
                        label="隐藏样例"
                        rules={[{required: true, message: '请选择是否隐藏样例'}]}
                    >
                        <Select placeholder="请选择是否隐藏样例">
                            <Option value={0}>否</Option>
                            <Option value={1}>是</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item
                        name="databaseType"
                        label="数据库类型"
                        rules={[{required: true, message: '请选择数据库类型'}]}
                    >
                        <Select placeholder="请选择数据库类型">
                            <Option value="MySQL">MySQL</Option>
                            <Option value="Clickhouse">Clickhouse</Option>
                            <Option value="PostgreSQL">PostgreSQL</Option>
                            <Option value="Oracle">Oracle</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="分类规则">
                        <div style={{marginBottom: 16}}>
                            <Button type="primary" onClick={addRule} icon={<PlusOutlined/>}>添加规则</Button>
                        </div>
                        <Table
                            columns={[
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
                                            value={record.conditionObject}
                                            style={{width: 120}}
                                            onChange={(value) => handleRuleChange(record.id, 'conditionObject', value)}
                                        >
                                            <Option value="库名">库名</Option>
                                            <Option value="库描述">库描述</Option>
                                            <Option value="表名">表名</Option>
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
                                            value={record.conditionType}
                                            style={{width: 120}}
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
                                            value={record.expression}
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
                                            value={record.ratio || 100}
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
                                            <Button type="link" danger icon={<DeleteOutlined/>}/>
                                        </Popconfirm>
                                    ),
                                },
                            ]}
                            dataSource={classificationRules}
                            rowKey="id"
                            pagination={false}
                            size="small"
                        />
                    </Form.Item>
                    <Form.Item
                        label="规则表达式"
                        rules={[{required: true, message: '请输入规则表达式'}]}
                    >
                        <TextArea
                            placeholder="请输入规则表达式"
                            rows={4}
                            value={ruleExpression}
                            onChange={(e) => setRuleExpression(e.target.value)}
                        />
                    </Form.Item>
                    <Form.Item label="AI规则">
                        <TextArea placeholder="请输入AI规则" rows={4} value={aiRule}
                                  onChange={(e) => setAiRule(e.target.value)}/>
                    </Form.Item>
                    <Form.Item label="验证数据">
                        <div style={{marginBottom: '16px'}}>
                            {/* 基本信息输入区域 */}
                            <Row gutter={16} style={{marginBottom: '16px'}}>
                                <Col span={8}>
                                    <Form.Item label="库名">
                                        <Input
                                            value={classificationRulesData[0]?.databaseName}
                                            onChange={(e) => {
                                                const newData = [...classificationRulesData];
                                                newData[0].databaseName = e.target.value;
                                                setClassificationRulesData(newData);
                                            }}
                                            placeholder="请输入库名"
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={8}>
                                    <Form.Item label="库描述">
                                        <Input
                                            value={classificationRulesData[0]?.databaseDescription}
                                            onChange={(e) => {
                                                const newData = [...classificationRulesData];
                                                newData[0].databaseDescription = e.target.value;
                                                setClassificationRulesData(newData);
                                            }}
                                            placeholder="请输入库描述"
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={8}>
                                    <Form.Item label="表名">
                                        <Input
                                            value={classificationRulesData[0]?.tableName}
                                            onChange={(e) => {
                                                const newData = [...classificationRulesData];
                                                newData[0].tableName = e.target.value;
                                                setClassificationRulesData(newData);
                                            }}
                                            placeholder="请输入表名"
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={16} style={{marginBottom: '16px'}}>
                                <Col span={8}>
                                    <Form.Item label="表描述">
                                        <Input
                                            value={classificationRulesData[0]?.tableDescription}
                                            onChange={(e) => {
                                                const newData = [...classificationRulesData];
                                                newData[0].tableDescription = e.target.value;
                                                setClassificationRulesData(newData);
                                            }}
                                            placeholder="请输入表描述"
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={8}>
                                    <Form.Item label="列名">
                                        <Input
                                            value={classificationRulesData[0]?.columnName}
                                            onChange={(e) => {
                                                const newData = [...classificationRulesData];
                                                newData[0].columnName = e.target.value;
                                                setClassificationRulesData(newData);
                                            }}
                                            placeholder="请输入列名"
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={8}>
                                    <Form.Item label="列描述">
                                        <Input
                                            value={classificationRulesData[0]?.columnDescription}
                                            onChange={(e) => {
                                                const newData = [...classificationRulesData];
                                                newData[0].columnDescription = e.target.value;
                                                setClassificationRulesData(newData);
                                            }}
                                            placeholder="请输入列描述"
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>

                            {/* 列值表格 */}
                            <Table
                                columns={[
                                    {
                                        title: '列值',
                                        dataIndex: 'columnValues',
                                        key: 'columnValues',
                                        render: (columnValues: string[], _, index: number) => (
                                            <div>
                                                {columnValues.map((value, valueIndex) => (
                                                    <div key={valueIndex} style={{
                                                        marginBottom: '8px',
                                                        display: 'flex',
                                                        alignItems: 'flex-start'
                                                    }}>
                                                        <Input.TextArea
                                                            value={value}
                                                            onChange={(e) => {
                                                                const newData = [...classificationRulesData];
                                                                newData[index].columnValues[valueIndex] = e.target.value;
                                                                setClassificationRulesData(newData);
                                                            }}
                                                            placeholder="请输入列值"
                                                            rows={2}
                                                            style={{marginRight: '8px', flex: 1}}
                                                        />
                                                        {columnValues.length > 1 && (
                                                            <Button
                                                                type="link"
                                                                danger
                                                                onClick={() => {
                                                                    const newData = [...classificationRulesData];
                                                                    newData[index].columnValues.splice(valueIndex, 1);
                                                                    setClassificationRulesData(newData);
                                                                }}
                                                                style={{marginTop: '4px'}}
                                                            >
                                                                删除
                                                            </Button>
                                                        )}
                                                    </div>
                                                ))}
                                                <Button
                                                    type="dashed"
                                                    onClick={() => {
                                                        const newData = [...classificationRulesData];
                                                        newData[index].columnValues.push('');
                                                        setClassificationRulesData(newData);
                                                    }}
                                                >
                                                    + 添加列值
                                                </Button>
                                            </div>
                                        )
                                    }
                                ]}
                                dataSource={classificationRulesData}
                                rowKey="id"
                                pagination={false}
                                size="small"
                            />
                        </div>
                    </Form.Item>
                    <Form.Item>
                        <Button
                            type="primary"
                            icon={<PlayCircleOutlined/>}
                            onClick={async () => {
                                // 验证分类规则是否为空
                                if (classificationRules.length === 0) {
                                    message.warning('请添加至少一条分类规则');
                                    return;
                                }

                                // 验证规则表达式是否为空
                                if (!ruleExpression.trim()) {
                                    message.warning('请输入规则表达式');
                                    return;
                                }

                                // 验证每条分类规则的必填字段
                                const invalidRule = classificationRules.find(rule =>
                                    !rule.conditionObject || !rule.conditionType || !rule.expression
                                );

                                if (invalidRule) {
                                    message.warning('请完善所有分类规则的必填字段');
                                    return;
                                }

                                // 调用测试规则函数，传递所有数据
                                await testRules();
                            }}
                        >
                            测试规则
                        </Button>
                    </Form.Item>
                    {validationResult && (
                        <Card title="验证结果" size="small">
                            <p>
                                规则验证:<Tag
                                color={validationResult.rulePassed ? 'green' : 'red'}>{validationResult.rulePassed ? '通过' : '未通过'}</Tag>
                            </p>
                            <p>
                                AI规则验证:<Tag
                                color={validationResult.aiPassed ? 'green' : 'red'}>{validationResult.aiPassed ? '通过' : '未通过'}</Tag>
                            </p>
                            {validationResult.ruleDetails && (
                                <div>
                                    <p>规则命中详情:</p>
                                    <ul>
                                        {validationResult.ruleDetails.map((detail: any, index: number) => (
                                            <li key={index}>{detail.rule}:<Tag
                                                color={detail.matched ? 'green' : 'red'}>{detail.matched ? '命中' : '未命中'}
                                            </Tag>({detail.detail})
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                            {validationResult.aiDetail && (<p>AI分析详情: {validationResult.aiDetail}</p>)}
                        </Card>
                    )}
                </Form>
            </Modal>
        </>
    );
}

export default DatabaseSecurity;