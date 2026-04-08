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

const {TextArea} = Input;
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
import {useNavigate, useLocation} from 'react-router-dom';
import {
    databasePolicyApi,
    dataSourceApi,
    DATA_SOURCE_PASSWORD_UNCHANGED_SENTINEL,
} from '../services/api';
import BatchMysqlOfflineScanJobPanel from './task-management/BatchMysqlOfflineScanJobPanel';
import BatchMysqlOfflineScanJobInstancePanel from './task-management/BatchMysqlOfflineScanJobInstancePanel';
import MySQLAsset from './data-asset/MySQLAsset';
import ClickhouseAsset from './data-asset/ClickhouseAsset';
import DatabaseOverview from './overview/DatabaseOverview';

const {Content, Sider} = Layout;
const {Title} = Typography;
const {TabPane} = Tabs;
const {Option} = Select;

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
    const navigate = useNavigate();
    const location = useLocation();

    // 根据当前URL设置activeMenu和activeTab
    const [activeMenu, setActiveMenu] = useState(() => {
        const path = location.pathname;
        if (path.includes('/policy-management')) {
            return '/policy-management';
        } else if (path.includes('/overview')) {
            return '/overview';
        } else if (path.includes('/data-source')) {
            return '/data-source';
        } else if (path.includes('/data-asset')) {
            return '/data-asset';
        } else if (path.includes('/task-management/realtime')) {
            return '/task-management/realtime';
        } else if (path.includes('/task-management/batch')) {
            return '/task-management/batch';
        } else if (path.includes('/configuration')) {
            return '/configuration';
        }
        return '/policy-management';
    });

    const [activeTab, setActiveTab] = useState(() => {
        const path = location.pathname;
        if (path.includes('/policy-management/')) {
            if (path.includes('/policy-management/clickhouse')) {
                return 'clickhouse';
            }
            return 'mysql';
        }
        if (path.includes('/data-source/')) {
            if (path.includes('/data-source/clickhouse')) {
                return 'clickhouse';
            }
            return 'mysql';
        }
        if (path.includes('/clickhouse')) {
            return 'clickhouse';
        }
        return 'mysql';
    });

    const [batchTaskTab, setBatchTaskTab] = useState<'mysql' | 'clickhouse'>(() => {
        const path = location.pathname;
        if (path.includes('/task-management/batch/clickhouse')) {
            return 'clickhouse';
        }
        return 'mysql';
    });
    const [batchMysqlTab, setBatchMysqlTab] = useState<'config' | 'instances'>(() => {
        const path = location.pathname;
        if (path.includes('/task-management/batch/mysql/instances')) {
            return 'instances';
        }
        return 'config';
    });
    const [form] = Form.useForm();
    const [editForm] = Form.useForm();
    const [dataSourceForm] = Form.useForm();
    const [policies, setPolicies] = useState<Policy[]>([]);
    const [dataSources, setDataSources] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({
        current: 1,
        pageSize: 10,
        total: 0,
    });
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [dataSourceModalVisible, setDataSourceModalVisible] = useState(false);
    const [currentPolicy, setCurrentPolicy] = useState<Policy | null>(null);
    // const [currentDataSource, setCurrentDataSource] = useState<any>(null);
    const [connectivityStatus, setConnectivityStatus] = useState<{ success: boolean; message: string } | null>(null);
    const [classificationRules, setClassificationRules] = useState<any[]>([]);
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
    const currentDbType = activeTab === 'clickhouse' ? 'Clickhouse' : 'MySQL';

    useEffect(() => {
        if (classificationRules.length > 0) {
            editForm.setFields([{name: 'classificationRulesCount', errors: []}]);
        }
    }, [classificationRules.length, editForm]);

    // 获取数据
    const fetchData = async (params: any = {}) => {
        setLoading(true);
        try {
            console.log('请求参数:', params);
            if (activeMenu === '/data-source') {
                // 获取数据源数据
                const response = await dataSourceApi.getPage({
                    current: pagination.current,
                    size: pagination.pageSize,
                    ...params,
                });
                console.log('响应数据:', response);
                if (response.code === 200) {
                    console.log('数据记录:', response.data.records);
                    setDataSources(response.data.records);
                    setPagination({
                        ...pagination,
                        total: response.data.total,
                    });
                } else {
                    message.error(response.message || '获取数据失败');
                }
            } else {
                // 获取策略数据
                const response = await databasePolicyApi.getPage({
                    current: pagination.current,
                    size: pagination.pageSize,
                    databaseType: currentDbType,
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
            }
        } catch (error) {
            message.error('网络请求失败');
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    // 初始化及分页/菜单变更时拉取列表数据（策略管理、数据源）
    useEffect(() => {
        if (activeMenu === '/policy-management' || activeMenu === '/data-source') {
            fetchData();
        }
    }, [pagination.current, pagination.pageSize, activeMenu, activeTab]);

    // 监听policies变化
    useEffect(() => {
        console.log('policies状态变化:', policies);
        console.log('policies长度:', policies.length);
    }, [policies]);

    useEffect(() => {
        const path = location.pathname;
        if (path.includes('/policy-management')) {
            setActiveMenu('/policy-management');
            setActiveTab(path.includes('/policy-management/clickhouse') ? 'clickhouse' : 'mysql');
        } else if (path.includes('/overview')) {
            setActiveMenu('/overview');
        } else if (path.includes('/data-source')) {
            setActiveMenu('/data-source');
            setActiveTab(path.includes('/data-source/clickhouse') ? 'clickhouse' : 'mysql');
        } else if (path.includes('/data-asset')) {
            setActiveMenu('/data-asset');
        } else if (path.includes('/task-management/realtime')) {
            setActiveMenu('/task-management/realtime');
        } else if (path.includes('/task-management/batch')) {
            setActiveMenu('/task-management/batch');
            if (path.includes('/task-management/batch/clickhouse')) {
                setBatchTaskTab('clickhouse');
            } else {
                setBatchTaskTab('mysql');
                setBatchMysqlTab(path.includes('/task-management/batch/mysql/instances') ? 'instances' : 'config');
            }
        } else if (path.includes('/configuration')) {
            setActiveMenu('/configuration');
        }
    }, [location.pathname]);

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
            title: '策略Code',
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

    // 数据源表格列配置
    const dataSourceColumns: ColumnsType<any> = [
        {
            title: '数据源类型',
            dataIndex: 'dataSourceType',
            key: 'dataSourceType',
        },
        {
            title: '实例',
            dataIndex: 'instance',
            key: 'instance',
        },
        {
            title: '用户名',
            dataIndex: 'username',
            key: 'username',
        },
        {
            title: '连通性',
            dataIndex: 'connectivity',
            key: 'connectivity',
            render: (connectivity) => (
                <Tag color={connectivity === '可连接' ? 'success' : 'error'}>{connectivity}</Tag>
            ),
        },
        {
            title: '创建人',
            dataIndex: 'creator',
            key: 'creator',
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space size="middle">
                    <Button icon={<EditOutlined/>} onClick={() => handleDataSourceEdit(record.id)}>编辑</Button>
                    <Button icon={<DeleteOutlined/>} danger
                            onClick={() => handleDataSourceDelete(record.id)}>删除</Button>
                </Space>
            ),
        },
    ];

    // 处理菜单点击
    const handleMenuClick = (key: string) => {
        // 数据资产二级菜单：/data-asset/mysql|clickhouse
        if (key.startsWith('/data-asset/')) {
            setActiveMenu('/data-asset');
            navigate(`/database-security${key}`);
            return;
        }

        // 数据源一级菜单默认进入 mysql tab
        if (key === '/data-source') {
            setActiveMenu('/data-source');
            setActiveTab('mysql');
            navigate('/database-security/data-source/mysql');
            return;
        }

        // 批量任务下的三级菜单：/task-management/batch/mysql|clickhouse
        if (key.startsWith('/task-management/batch')) {
            const tabKey = key.endsWith('/clickhouse') ? 'clickhouse' : 'mysql';
            setActiveMenu('/task-management/batch');
            setBatchTaskTab(tabKey as 'mysql' | 'clickhouse');
            if (tabKey === 'mysql') {
                setBatchMysqlTab('config');
                navigate('/database-security/task-management/batch/mysql/config');
            } else {
                navigate('/database-security/task-management/batch/clickhouse');
            }
            return;
        }

        setActiveMenu(key);
        navigate(`/database-security${key}`);
    };

    // 处理Tab切换（策略管理走子路由；数据源仅本地切换）
    const handleTabChange = (key: string) => {
        setActiveTab(key);
        if (activeMenu === '/policy-management') {
            navigate(`/database-security/policy-management/${key}`);
        } else if (activeMenu === '/data-source') {
            navigate(`/database-security/data-source/${key}`);
        }
    };

    const handleBatchMysqlTabChange = (key: string) => {
        const next = key === 'instances' ? 'instances' : 'config';
        setBatchMysqlTab(next);
        navigate(`/database-security/task-management/batch/mysql/${next}`);
    };

    // 处理查询
    const handleSearch = () => {
        const values = form.getFieldsValue();
        if (activeMenu === '/data-source') {
            // 搜索数据源
            const params = {
                dataSourceType: values.data_source_type,
                instance: values.instance,
                username: values.username,
            };
            fetchData(params);
        } else {
            // 搜索策略
            const params = {
                policyCode: values.policy_code,
                policyName: values.policy_name,
                creator: values.creator,
                sensitivityLevel: values.sensitivity_level,
                hideExample: values.hide_example,
                databaseType: currentDbType,
            };
            fetchData(params);
        }
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

    // 处理数据源编辑
    const handleDataSourceEdit = async (id: number) => {
        try {
            const response = await dataSourceApi.getById(id);
            if (response.code === 200) {
                // 编辑时不回传真实密码，用占位 *** 表示沿用库中密码（明文展示三个星号）
                dataSourceForm.setFieldsValue({
                    ...response.data,
                    password: DATA_SOURCE_PASSWORD_UNCHANGED_SENTINEL,
                });
                setDataSourceModalVisible(true);
            } else {
                message.error(response.message || '获取数据源失败');
            }
        } catch (error) {
            message.error('网络请求失败');
            console.error(error);
        }
    };

    // 处理数据源删除
    const handleDataSourceDelete = async (id: number) => {
        try {
            const response = await dataSourceApi.delete(id);
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

    // 处理数据源提交
    const handleDataSourceSubmit = async () => {
        try {
            const values = await dataSourceForm.validateFields();
            console.log('提交的数据:', values);
            if (values.id) {
                // 更新数据源
                const response = await dataSourceApi.update(values);
                if (response.code === 200) {
                    message.success('更新成功');
                    setDataSourceModalVisible(false);
                    dataSourceForm.resetFields();
                    fetchData();
                } else {
                    message.error(response.message || '更新失败');
                }
            } else {
                // 创建数据源
                const response = await dataSourceApi.create(values);
                if (response.code === 200) {
                    message.success('新增成功');
                    setDataSourceModalVisible(false);
                    dataSourceForm.resetFields();
                    fetchData();
                } else {
                    message.error(response.message || '新增失败');
                }
            }
        } catch (error) {
            message.error('表单验证失败或网络请求失败');
            console.error(error);
        }
    };

    // 处理测试连接
    const handleTestConnection = async () => {
        try {
            const baseFields = ['dataSourceType', 'instance', 'username'] as const;
            const fields = dataSourceForm.getFieldValue('id')
                ? [...baseFields]
                : [...baseFields, 'password'];
            await dataSourceForm.validateFields(fields);
            const values = dataSourceForm.getFieldsValue();
            // 调用后端测试连接接口
            const response = await dataSourceApi.testConnection(values);
            if (response.code === 200) {
                if (response.data) {
                    message.success('测试连接成功');
                    setConnectivityStatus({success: true, message: '连接成功'});
                    dataSourceForm.setFieldsValue({connectivity: '可连接'});
                    console.log('设置connectivity为: 可连接');
                } else {
                    message.error('测试连接失败');
                    setConnectivityStatus({success: false, message: response.message || '连接失败'});
                    dataSourceForm.setFieldsValue({connectivity: '无法连接'});
                    console.log('设置connectivity为: 无法连接');
                }
            } else {
                message.error(response.message || '测试连接失败');
                setConnectivityStatus({success: false, message: response.message || '连接失败'});
                dataSourceForm.setFieldsValue({connectivity: '无法连接'});
                console.log('设置connectivity为: 无法连接');
            }
        } catch (error) {
            message.error('网络请求失败');
            setConnectivityStatus({success: false, message: '网络请求失败'});
            dataSourceForm.setFieldsValue({connectivity: '无法连接'});
            console.log('设置connectivity为: 无法连接');
            console.error(error);
        }
    };

    // 处理编辑按钮点击
    const handleEdit = async (id: number) => {
        try {
            const response = await databasePolicyApi.getById(id);
            if (response.code === 200) {
                const data = response.data;
                setCurrentPolicy(data);

                // 解析分类规则
                let parsedRules: any[] = [];
                if (data.classificationRules) {
                    try {
                        parsedRules = JSON.parse(data.classificationRules);
                    } catch (e) {
                        parsedRules = [];
                    }
                }
                setClassificationRules(parsedRules);

                // 设置表单字段
                editForm.setFieldsValue({
                    policyCode: data.policyCode,
                    policyName: data.policyName,
                    description: data.description,
                    sensitivityLevel: data.sensitivityLevel,
                    hideExample: data.hideExample,
                    ruleExpression: data.ruleExpression || '',
                    aiRule: data.aiRule || '',
                });

                // 回显分类规则到表单
                const rulesFormValues: Record<string, any> = {};
                parsedRules.forEach((rule: any) => {
                    rulesFormValues[`rules[${rule.id}].conditionObject`] = rule.conditionObject;
                    rulesFormValues[`rules[${rule.id}].conditionType`] = rule.conditionType;
                    rulesFormValues[`rules[${rule.id}].expression`] = rule.expression;
                });
                editForm.setFieldsValue(rulesFormValues);

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
                ruleExpression: formValues.ruleExpression,
                testData: classificationRulesData,
                databaseType: currentDbType,
            });
            if (response.code === 200) {
                setValidationResult(response.data);
                setValidationResult((prev: any) => ({
                    ...(prev || response.data),
                    aiPassed: false,
                    aiDetail: 'AI规则流式测试中...',
                }));
                await databasePolicyApi.testAiRulesStream(
                    {
                        aiRule: formValues.aiRule,
                        testData: classificationRulesData,
                        databaseType: currentDbType,
                    },
                    (chunk: string) => {
                        setValidationResult((prev: any) => ({
                            ...(prev || {}),
                            aiDetail: `${prev?.aiDetail && prev.aiDetail !== 'AI规则流式测试中...' ? prev.aiDetail : ''}${chunk}`,
                        }));
                    },
                    (done: { aiPassed: boolean; aiDetail: string }) => {
                        setValidationResult((prev: any) => ({
                            ...(prev || {}),
                            aiPassed: done.aiPassed,
                            aiDetail: done.aiDetail || prev?.aiDetail,
                        }));
                    },
                    (err: string) => {
                        setValidationResult((prev: any) => ({
                            ...(prev || {}),
                            aiPassed: false,
                            aiDetail: `AI规则测试失败: ${err}`,
                        }));
                    }
                );
            } else {
                message.error(response.message || '测试规则失败');
            }
        } catch (error) {
            message.error('网络请求失败');
            console.error(error);
        }
    };

    // 处理新增策略
    const handleAdd = () => {
        // 重置表单和状态
        editForm.resetFields();
        setCurrentPolicy(null);
        setClassificationRules([]);
        setValidationResult(null);
        // 打开编辑模态框
        setEditModalVisible(true);
    };

    // 处理编辑提交
    const handleEditSubmit = async () => {
        try {
            const values = await editForm.validateFields();

            const invalidRule = classificationRules.find(rule =>
                !rule.conditionObject || !rule.conditionType || !rule.expression
            );

            if (invalidRule) {
                message.warning('请完善所有分类规则的必填字段');
                return;
            }
            
            // 将分类规则的id改为连续编号（从1开始）
            const rulesWithCorrectIds = classificationRules.map((rule, index) => ({
                ...rule,
                id: index + 1
            }));
            
            let response;
            if (currentPolicy?.id) {
                // 更新策略
                response = await databasePolicyApi.update({
                    id: currentPolicy.id,
                    policyCode: values.policyCode,
                    policyName: values.policyName,
                    description: values.description,
                    sensitivityLevel: values.sensitivityLevel,
                    hideExample: values.hideExample,
                    classificationRules: JSON.stringify(rulesWithCorrectIds),
                    ruleExpression: values.ruleExpression,
                    aiRule: values.aiRule,
                    databaseType: currentDbType,
                });
                if (response.code === 200) {
                    message.success('编辑成功');
                } else {
                    message.error(response.message || '编辑失败');
                }
            } else {
                // 创建新策略
                response = await databasePolicyApi.create({
                    policyCode: values.policyCode,
                    policyName: values.policyName,
                    description: values.description,
                    sensitivityLevel: values.sensitivityLevel,
                    hideExample: values.hideExample,
                    classificationRules: JSON.stringify(rulesWithCorrectIds),
                    ruleExpression: values.ruleExpression,
                    aiRule: values.aiRule,
                    databaseType: currentDbType,
                });
                if (response.code === 200) {
                    message.success('新增成功');
                } else {
                    message.error(response.message || '新增失败');
                }
            }
            
            if (response.code === 200) {
                setEditModalVisible(false);
                fetchData();
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
                                key: '/overview',
                                icon: <HomeOutlined/>,
                                label: '概览',
                            },
                            {
                                key: '/policy-management',
                                icon: <LockOutlined/>,
                                label: '策略管理',
                            },
                            {
                                key: '/data-source',
                                icon: <DatabaseOutlined/>,
                                label: '数据源',
                            },
                            {
                                key: '/data-asset',
                                icon: <AppstoreOutlined/>,
                                label: '数据资产',
                                children: [
                                    {
                                        key: '/data-asset/mysql',
                                        label: 'MySQL',
                                    },
                                    {
                                        key: '/data-asset/clickhouse',
                                        label: 'Clickhouse',
                                    },
                                ],
                            },
                            {
                                key: '/task-management',
                                icon: <ThunderboltOutlined/>,
                                label: '任务管理',
                                children: [
                                    {
                                        key: '/task-management/realtime',
                                        label: '实时任务',
                                    },
                                    {
                                        key: '/task-management/batch',
                                        label: '批量任务',
                                        children: [
                                            {
                                                key: '/task-management/batch/mysql',
                                                label: 'MySQL',
                                            },
                                            {
                                                key: '/task-management/batch/clickhouse',
                                                label: 'Clickhouse',
                                            },
                                        ],
                                    },
                                ],
                            },
                            {
                                key: '/configuration',
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
                        {activeMenu === '/policy-management' ? (
                            <>
                                <div style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    marginBottom: '24px'
                                }}>
                                    <Title level={3}>策略管理</Title>

                                </div>
                                <Tabs activeKey={activeTab} onChange={handleTabChange}>
                                    <TabPane tab="MySQL" key="mysql">
                                        <Card style={{marginBottom: '24px'}}>
                                            <Form form={form} layout="inline">
                                                <Row gutter={16}>
                                                    <Col span={6}>
                                                        <Form.Item name="policy_code" label="策略Code">
                                                            <Input placeholder="请输入策略Code"/>
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
                                                            <Button type="primary" icon={<PlusOutlined/>} onClick={handleAdd}>新增策略</Button>
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
                                                showSizeChanger: true,
                                                showTotal: (total) => `共 ${total} 条`,
                                                onChange: handlePaginationChange,
                                            }}
                                            style={{width: '100%'}}
                                        />
                                    </TabPane>
                                    <TabPane tab="Clickhouse" key="clickhouse">
                                        <Card style={{marginBottom: '24px'}}>
                                            <Form form={form} layout="inline">
                                                <Row gutter={16}>
                                                    <Col span={6}>
                                                        <Form.Item name="policy_code" label="策略Code">
                                                            <Input placeholder="请输入策略Code"/>
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
                                                            <Button type="primary" icon={<PlusOutlined/>} onClick={handleAdd}>新增策略</Button>
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
                                                showSizeChanger: true,
                                                showTotal: (total) => `共 ${total} 条`,
                                                onChange: handlePaginationChange,
                                            }}
                                            style={{width: '100%'}}
                                        />
                                    </TabPane>
                                </Tabs>
                            </>
                        ) : activeMenu === '/overview' ? (
                            <DatabaseOverview />
                        ) : activeMenu === '/data-source' ? (
                            <>
                                <div style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    marginBottom: '24px'
                                }}>
                                    <Title level={3}>数据源管理</Title>
                                </div>
                                <Tabs activeKey={activeTab} onChange={handleTabChange}>
                                    <TabPane tab="MySQL" key="mysql">

                                        <Card style={{marginBottom: '24px'}}>
                                            <div
                                                style={{
                                                    textAlign: 'center',
                                                    padding: '20px',
                                                    marginBottom: '24px'
                                                }}>不仅仅是MySQL，Oracle、SQL Server都支持配置及扫描
                                            </div>
                                            <div style={{
                                                display: 'flex',
                                                justifyContent: 'flex-end',
                                                marginBottom: '24px'
                                            }}>

                                            </div>
                                            <Form form={form} layout="inline">
                                                <Row gutter={16}>
                                                    <Col span={6}>
                                                        <Form.Item name="data_source_type" label="数据源类型">
                                                            <Select placeholder="请选择数据源类型"
                                                                    style={{width: '100%'}}>
                                                                <Option value="MySQL">MySQL</Option>
                                                                <Option value="Oracle">Oracle</Option>
                                                                <Option value="SQL Server">SQL Server</Option>
                                                            </Select>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Form.Item name="instance" label="实例">
                                                            <Input placeholder="请输入实例（域名:端口）"/>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Form.Item name="username" label="用户名">
                                                            <Input placeholder="请输入用户名"/>
                                                        </Form.Item>
                                                    </Col>
                                                    <Col span={6}>
                                                        <Space>
                                                            <Button type="primary" icon={<SearchOutlined/>}
                                                                    onClick={handleSearch}>查询</Button>
                                                            <Button onClick={handleReset}>重置</Button>
                                                            <Button type="primary" icon={<PlusOutlined/>}
                                                                    onClick={() => {
                                                                        dataSourceForm.resetFields();
                                                                        setConnectivityStatus(null);
                                                                        setDataSourceModalVisible(true);
                                                                    }}>新增数据源</Button>
                                                        </Space>
                                                    </Col>
                                                </Row>
                                            </Form>
                                        </Card>
                                        <Table
                                            columns={dataSourceColumns}
                                            dataSource={dataSources}
                                            rowKey="id"
                                            loading={loading}
                                            pagination={{
                                                current: pagination.current,
                                                pageSize: pagination.pageSize,
                                                total: pagination.total,
                                                showSizeChanger: true,
                                                showTotal: (total) => `共 ${total} 条`,
                                                onChange: handlePaginationChange,
                                            }}
                                            style={{width: '100%'}}
                                        />
                                    </TabPane>
                                    <TabPane tab="Clickhouse" key="clickhouse">
                                        <div
                                            style={{textAlign: 'center', padding: '50px'}}>Clickhouse数据源管理功能开发中
                                        </div>
                                    </TabPane>
                                </Tabs>
                            </>
                        ) : activeMenu === '/data-asset' ? (
                            location.pathname.includes('/data-asset/mysql') ? (
                                <MySQLAsset/>
                            ) : location.pathname.includes('/data-asset/clickhouse') ? (
                                <ClickhouseAsset/>
                            ) : (
                                <Title level={3}>数据资产</Title>
                            )
                        ) : activeMenu === '/task-management/realtime' ? (
                            <Title level={3}>实时任务</Title>
                        ) : activeMenu === '/task-management/batch' ? (
                            <>
                                <Title level={3}>
                                    批量任务 - {batchTaskTab === 'mysql' ? 'MySQL' : 'Clickhouse'}
                                </Title>
                                {batchTaskTab === 'mysql' ? (
                                    <Tabs activeKey={batchMysqlTab} onChange={handleBatchMysqlTabChange}>
                                        <TabPane tab="任务配置" key="config">
                                            <BatchMysqlOfflineScanJobPanel/>
                                        </TabPane>
                                        <TabPane tab="任务实例" key="instances">
                                            <BatchMysqlOfflineScanJobInstancePanel/>
                                        </TabPane>
                                    </Tabs>
                                ) : (
                                    <div style={{textAlign: 'center', padding: '48px'}}>
                                        Clickhouse 批量任务开发中
                                    </div>
                                )}
                            </>
                        ) : activeMenu === '/configuration' ? (
                            <Title level={3}>配置中心</Title>
                        ) : (
                            null
                        )}
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
                width="100%"
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
                        label="策略Code"
                        rules={[{required: true, message: '请输入策略Code'}]}
                    >
                        <Input placeholder="请输入策略Code" disabled={!!currentPolicy?.id}/>
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
                    <Form.Item label="分类规则" required>
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
                                        <Form.Item
                                            name={`rules[${record.id}].conditionObject`}
                                            rules={[{required: true, message: '请选择条件对象'}]}
                                            style={{margin: 0}}
                                        >
                                            <Select
                                                value={record.conditionObject}
                                                style={{width: 120}}
                                                onChange={(value) => handleRuleChange(record.id, 'conditionObject', value)}
                                                placeholder="请选择条件对象"
                                            >
                                                <Option value="库名">库名</Option>
                                                <Option value="库描述">库描述</Option>
                                                <Option value="表名">表名</Option>
                                                <Option value="表描述">表描述</Option>
                                                <Option value="列名">列名</Option>
                                                <Option value="列描述">列描述</Option>
                                                <Option value="列值">列值</Option>
                                            </Select>
                                        </Form.Item>
                                    ),
                                },
                                {
                                    title: '条件类型',
                                    dataIndex: 'conditionType',
                                    key: 'conditionType',
                                    render: (_: any, record: any) => (
                                        <Form.Item
                                            name={`rules[${record.id}].conditionType`}
                                            rules={[{required: true, message: '请选择条件类型'}]}
                                            style={{margin: 0}}
                                        >
                                            <Select
                                                value={record.conditionType}
                                                style={{width: 120}}
                                                onChange={(value) => handleRuleChange(record.id, 'conditionType', value)}
                                                placeholder="请选择条件类型"
                                            >
                                                <Option value="正则匹配">正则匹配</Option>
                                                <Option value="非正则匹配">非正则匹配</Option>
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
                                                <Option value="内置算法">内置算法</Option>
                                            </Select>
                                        </Form.Item>
                                    ),
                                },
                                {
                                    title: '表达式',
                                    dataIndex: 'expression',
                                    key: 'expression',
                                    render: (_: any, record: any) => (
                                        <Form.Item
                                            name={`rules[${record.id}].expression`}
                                            rules={[{required: true, message: '请输入表达式'}]}
                                            style={{margin: 0}}
                                        >
                                            <Input
                                                value={record.expression}
                                                onChange={(e) => handleRuleChange(record.id, 'expression', e.target.value)}
                                                placeholder="请输入表达式"
                                            />
                                        </Form.Item>
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
                        <Form.Item
                            name="classificationRulesCount"
                            noStyle
                            rules={[
                                {
                                    validator: () =>
                                        classificationRules.length > 0
                                            ? Promise.resolve()
                                            : Promise.reject(new Error('请至少添加一条分类规则')),
                                },
                            ]}
                        >
                            <Input type="hidden"/>
                        </Form.Item>
                    </Form.Item>
                    <Form.Item
                        name="ruleExpression"
                        label="规则表达式"
                        rules={[{required: true, whitespace: true, message: '请输入规则表达式'}]}
                    >
                        <TextArea placeholder="请输入规则表达式" rows={4}/>
                    </Form.Item>
                    <Form.Item
                        name="aiRule"
                        label="AI规则"
                        rules={[{required: true, whitespace: true, message: '请输入AI规则'}]}
                    >
                        <TextArea placeholder="请输入AI规则" rows={4}/>
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
                                try {
                                    await editForm.validateFields([
                                        'classificationRulesCount',
                                        'ruleExpression',
                                        'aiRule',
                                    ]);
                                } catch {
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

            {/* 新增/编辑数据源模态框 */}
            <Modal
                title={dataSourceForm.getFieldValue('id') ? "编辑数据源" : "新增数据源"}
                open={dataSourceModalVisible}
                onCancel={() => {
                    setDataSourceModalVisible(false);
                    dataSourceForm.resetFields();
                }}
                onOk={handleDataSourceSubmit}
                width={600}
                footer={[
                    <Button key="cancel" onClick={() => {
                        setDataSourceModalVisible(false);
                        dataSourceForm.resetFields();
                    }}>取消</Button>,
                    <Button key="submit" type="primary" onClick={handleDataSourceSubmit}>提交</Button>,
                ]}
            >
                <Form form={dataSourceForm} layout="vertical">
                    <Form.Item name="id" hidden></Form.Item>
                    <Form.Item name="connectivity" hidden></Form.Item>
                    <Form.Item
                        name="dataSourceType"
                        label="数据源类型"
                        rules={[{required: true, message: '请选择数据源类型'}]}
                    >
                        <Select
                            placeholder="请选择数据源类型"
                            disabled={!!dataSourceForm.getFieldValue('id')}
                        >
                            <Option value="MySQL">MySQL</Option>
                            <Option value="Oracle">Oracle</Option>
                            <Option value="SQL Server">SQL Server</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item
                        name="instance"
                        label="实例"
                        rules={[{required: true, message: '请输入实例（域名:端口）'}]}
                    >
                        <Input
                            placeholder="请输入实例（域名:端口）"
                            disabled={!!dataSourceForm.getFieldValue('id')}
                        />
                    </Form.Item>
                    <Form.Item
                        name="username"
                        label="用户名"
                        rules={[{required: true, message: '请输入用户名'}]}
                    >
                        <Input placeholder="请输入用户名"/>
                    </Form.Item>
                    <Form.Item noStyle shouldUpdate>
                        {() => {
                            const editingId = dataSourceForm.getFieldValue('id');
                            const isEditMode = !!editingId;
                            const pwdVal = dataSourceForm.getFieldValue('password');
                            const showUnchangedHint =
                                isEditMode && pwdVal === DATA_SOURCE_PASSWORD_UNCHANGED_SENTINEL;
                            return (
                                <Form.Item
                                    name="password"
                                    label="密码"
                                    rules={[
                                        {
                                            validator: (_: unknown, value: string) => {
                                                if (!isEditMode) {
                                                    if (!value || !String(value).trim()) {
                                                        return Promise.reject(new Error('请输入密码'));
                                                    }
                                                }
                                                return Promise.resolve();
                                            },
                                        },
                                    ]}
                                >
                                    {showUnchangedHint ? (
                                        <Input
                                            key="ds-pwd-unchanged"
                                            readOnly
                                            onClick={() =>
                                                dataSourceForm.setFieldsValue({password: ''})
                                            }
                                            style={{cursor: 'pointer'}}
                                            title="点击后输入新密码"
                                        />
                                    ) : (
                                        <Input.Password
                                            key="ds-pwd-editable"
                                            placeholder={
                                                isEditMode
                                                    ? '输入新密码；留空则沿用原密码'
                                                    : '请输入密码'
                                            }
                                            visibilityToggle={!isEditMode}
                                        />
                                    )}
                                </Form.Item>
                            );
                        }}
                    </Form.Item>
                    <Form.Item label="连通性">
                        <div style={{display: 'flex', alignItems: 'center'}}>
                            <Button type="primary" onClick={handleTestConnection}
                                    style={{marginRight: '16px'}}>测试连接</Button>
                            {connectivityStatus && (
                                <span style={{color: connectivityStatus.success ? 'green' : 'red'}}>
                                    {connectivityStatus.success ? '可连接' : `无法连接: ${connectivityStatus.message}`}
                                </span>
                            )}
                        </div>
                    </Form.Item>
                    <Form.Item
                        name="extendInfo"
                        label="拓展信息"
                    >
                        <TextArea placeholder="请输入拓展信息（JSON字符串）" rows={4}/>
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
}

export default DatabaseSecurity;