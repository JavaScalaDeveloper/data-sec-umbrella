import React, {useCallback, useEffect, useState} from 'react';
import {
    Button,
    Card,
    Col,
    Form,
    Input,
    InputNumber,
    message,
    Modal,
    Row,
    Select,
    Space,
    Switch,
    Table,
} from 'antd';
import type {ColumnsType} from 'antd/es/table';
import {EditOutlined, PlusOutlined, SearchOutlined, ThunderboltOutlined} from '@ant-design/icons';
import {databasePolicyApi, mysqlOfflineScanJobApi} from '../../services/api';

const {Option} = Select;
const {TextArea} = Input;

const SAMPLE_MODE_LABEL: Record<string, string> = {
    sequence: '顺序',
    reverse: '倒序',
    random: '随机',
};

const SCAN_PERIOD_LABEL: Record<string, string> = {
    manual: '手动',
    weekly: '每周一次',
    monthly: '每月一次',
};

const SCOPE_LABEL: Record<string, string> = {
    all: '全部',
    instance: '实例',
};

const TIME_RANGE_LABEL: Record<string, string> = {
    full: '全量',
    incremental: '增量',
};

function parseTags(raw: string | undefined): string[] {
    if (!raw || !raw.trim()) {
        return [];
    }
    try {
        const arr = JSON.parse(raw);
        return Array.isArray(arr) ? arr.map(String) : [];
    } catch {
        return [];
    }
}

const BatchMysqlOfflineScanJobPanel: React.FC = () => {
    const [form] = Form.useForm();
    const [modalForm] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [list, setList] = useState<any[]>([]);
    const [pagination, setPagination] = useState({current: 1, pageSize: 10, total: 0});
    const [modalOpen, setModalOpen] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [policyOptions, setPolicyOptions] = useState<{label: string; value: string}[]>([]);

    const fetchPolicies = useCallback(async () => {
        try {
            const res = await databasePolicyApi.getPage({current: 1, size: 500});
            if (res.code === 200 && res.data?.records) {
                setPolicyOptions(
                    res.data.records.map((p: any) => ({
                        label: `${p.policyCode}（${p.policyName || ''}）`,
                        value: p.policyCode,
                    }))
                );
            }
        } catch {
            /* ignore */
        }
    }, []);

    const fetchList = useCallback(async (page: number, pageSize: number) => {
        setLoading(true);
        try {
            const q = form.getFieldsValue();
            const res = await mysqlOfflineScanJobApi.getPage({
                current: page,
                size: pageSize,
                taskName: q.taskName || undefined,
            });
            if (res.code === 200 && res.data) {
                setList(res.data.records || []);
                setPagination({
                    current: Number(res.data.current) || page,
                    pageSize: Number(res.data.size) || pageSize,
                    total: Number(res.data.total) || 0,
                });
            } else {
                message.error(res.message || '加载失败');
            }
        } catch (e) {
            message.error('网络请求失败');
            console.error(e);
        } finally {
            setLoading(false);
        }
    }, [form]);

    useEffect(() => {
        fetchPolicies();
    }, [fetchPolicies]);

    useEffect(() => {
        fetchList(1, 10);
    }, [fetchList]);

    const openCreate = () => {
        setEditingId(null);
        modalForm.resetFields();
        modalForm.setFieldsValue({
            sampleCount: 10,
            sampleMode: 'sequence',
            enableSampling: true,
            enableAiScan: false,
            scanPeriod: 'manual',
            scanScope: 'all',
            timeRangeType: 'full',
            enabledStatus: true,
            policyCodes: [],
        });
        setModalOpen(true);
    };

    const openEdit = async (id: number) => {
        setEditingId(id);
        try {
            const res = await mysqlOfflineScanJobApi.getById(id);
            if (res.code !== 200 || !res.data) {
                message.error(res.message || '获取任务失败');
                return;
            }
            const d = res.data;
            modalForm.setFieldsValue({
                taskName: d.taskName,
                taskDescription: d.taskDescription,
                sampleCount: d.sampleCount,
                sampleMode: d.sampleMode || 'sequence',
                enableSampling: d.enableSampling === 1,
                enableAiScan: d.enableAiScan === 1,
                scanPeriod: d.scanPeriod || 'manual',
                scanScope: d.scanScope || 'all',
                timeRangeType: d.timeRangeType || 'full',
                enabledStatus: d.enabledStatus === 1,
                policyCodes: parseTags(d.supportedTags),
            });
            setModalOpen(true);
        } catch (e) {
            message.error('网络请求失败');
            console.error(e);
        }
    };

    const buildPayload = (values: any) => ({
        taskName: values.taskName,
        taskDescription: values.taskDescription || '',
        sampleCount: values.sampleCount,
        sampleMode: values.sampleMode,
        enableSampling: values.enableSampling ? 1 : 0,
        enableAiScan: values.enableAiScan ? 1 : 0,
        scanPeriod: values.scanPeriod,
        supportedTags: JSON.stringify(values.policyCodes || []),
        scanScope: values.scanScope,
        timeRangeType: values.timeRangeType,
        enabledStatus: values.enabledStatus ? 1 : 0,
    });

    const handleModalOk = async () => {
        try {
            const values = await modalForm.validateFields();
            const payload = buildPayload(values);
            if (editingId != null) {
                const res = await mysqlOfflineScanJobApi.update({...payload, id: editingId});
                if (res.code === 200) {
                    message.success('更新成功');
                    setModalOpen(false);
                    fetchList(pagination.current, pagination.pageSize);
                } else {
                    message.error(res.message || '更新失败');
                }
            } else {
                const res = await mysqlOfflineScanJobApi.create(payload);
                if (res.code === 200) {
                    message.success('创建成功');
                    setModalOpen(false);
                    fetchList(1, pagination.pageSize);
                } else {
                    message.error(res.message || '创建失败');
                }
            }
        } catch (e) {
            if (e instanceof Error && e.message) {
                /* validation */
            }
        }
    };

    const handleExecute = async (id: number) => {
        try {
            const res = await mysqlOfflineScanJobApi.execute(id);
            if (res.code === 200) {
                message.success(`已创建执行实例 #${res.data}，任务已加入分发队列`);
            } else {
                message.error(res.message || '执行失败');
            }
        } catch (e) {
            message.error('网络请求失败');
            console.error(e);
        }
    };

    const handleEnabledChange = async (record: any, checked: boolean) => {
        const next = {...record, enabledStatus: checked ? 1 : 0};
        try {
            const res = await mysqlOfflineScanJobApi.update({
                id: next.id,
                taskName: next.taskName,
                taskDescription: next.taskDescription || '',
                sampleCount: next.sampleCount,
                sampleMode: next.sampleMode,
                enableSampling: next.enableSampling,
                enableAiScan: next.enableAiScan,
                scanPeriod: next.scanPeriod,
                supportedTags: next.supportedTags,
                scanScope: next.scanScope,
                scanInstanceIds: next.scanInstanceIds,
                timeRangeType: next.timeRangeType,
                enabledStatus: next.enabledStatus,
            });
            if (res.code === 200) {
                message.success(checked ? '已启用' : '已停用');
                fetchList(pagination.current, pagination.pageSize);
            } else {
                message.error(res.message || '更新失败');
            }
        } catch (e) {
            message.error('网络请求失败');
            console.error(e);
        }
    };

    const columns: ColumnsType<any> = [
        {title: 'ID', dataIndex: 'id', width: 70},
        {title: '任务名', dataIndex: 'taskName', ellipsis: true},
        {title: '任务描述', dataIndex: 'taskDescription', ellipsis: true, width: 200},
        {title: '样例数', dataIndex: 'sampleCount', width: 80},
        {
            title: '取样方式',
            dataIndex: 'sampleMode',
            width: 90,
            render: (v: string) => SAMPLE_MODE_LABEL[v] || v,
        },
        {
            title: '是否取样',
            dataIndex: 'enableSampling',
            width: 90,
            render: (v: number) => (v === 1 ? '是' : '否'),
        },
        {
            title: 'AI扫描',
            dataIndex: 'enableAiScan',
            width: 90,
            render: (v: number) => (v === 1 ? '是' : '否'),
        },
        {
            title: '扫描周期',
            dataIndex: 'scanPeriod',
            width: 100,
            render: (v: string) => SCAN_PERIOD_LABEL[v] || v,
        },
        {
            title: '扫描范围',
            dataIndex: 'scanScope',
            width: 80,
            render: (v: string) => SCOPE_LABEL[v] || v,
        },
        {
            title: '时间范围',
            dataIndex: 'timeRangeType',
            width: 80,
            render: (v: string) => TIME_RANGE_LABEL[v] || v,
        },
        {
            title: '启用',
            dataIndex: 'enabledStatus',
            width: 100,
            render: (_: any, record: any) => (
                <Switch
                    checked={record.enabledStatus === 1}
                    onChange={(c) => handleEnabledChange(record, c)}
                />
            ),
        },
        {
            title: '操作',
            key: 'action',
            width: 200,
            fixed: 'right',
            render: (_: any, record: any) => (
                <Space>
                    <Button type="link" size="small" icon={<EditOutlined/>} onClick={() => openEdit(record.id)}>
                        编辑
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        icon={<ThunderboltOutlined/>}
                        onClick={() => handleExecute(record.id)}
                    >
                        执行
                    </Button>
                </Space>
            ),
        },
    ];

    return (
        <>
            <Card style={{marginBottom: 16}}>
                <Form form={form} layout="inline">
                    <Form.Item name="taskName" label="任务名">
                        <Input placeholder="模糊查询" allowClear style={{width: 200}}/>
                    </Form.Item>
                    <Form.Item>
                        <Space>
                            <Button
                                type="primary"
                                icon={<SearchOutlined/>}
                                onClick={() => fetchList(1, pagination.pageSize)}
                            >
                                查询
                            </Button>
                            <Button
                                type="primary"
                                icon={<PlusOutlined/>}
                                onClick={openCreate}
                            >
                                新建任务
                            </Button>
                        </Space>
                    </Form.Item>
                </Form>
            </Card>
            <Table
                rowKey="id"
                loading={loading}
                columns={columns}
                dataSource={list}
                scroll={{x: 1400}}
                pagination={{
                    current: pagination.current,
                    pageSize: pagination.pageSize,
                    total: pagination.total,
                    showSizeChanger: true,
                    onChange: (p, ps) => fetchList(p, ps || 10),
                }}
            />
            <Modal
                title={editingId != null ? '编辑离线扫描任务' : '新建离线扫描任务'}
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={handleModalOk}
                width={720}
                destroyOnClose
            >
                <Form form={modalForm} layout="vertical">
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item name="taskName" label="任务名" rules={[{required: true, message: '请输入任务名'}]}>
                                <Input placeholder="唯一任务名" disabled={editingId !== null}/>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="sampleCount"
                                label="样例数"
                                rules={[{required: true, message: '请输入样例数'}]}
                            >
                                <InputNumber min={1} max={200} style={{width: '100%'}}/>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="taskDescription" label="任务描述">
                        <TextArea rows={2} placeholder="任务描述"/>
                    </Form.Item>
                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item name="sampleMode" label="取样方式" rules={[{required: true}]}>
                                <Select>
                                    <Option value="sequence">顺序</Option>
                                    <Option value="reverse">倒序</Option>
                                    <Option value="random">随机</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="enableSampling" label="是否取样" valuePropName="checked">
                                <Switch/>
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="enableAiScan" label="启用AI扫描" valuePropName="checked">
                                <Switch/>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item name="scanPeriod" label="扫描周期" rules={[{required: true}]}>
                                <Select>
                                    <Option value="manual">手动</Option>
                                    <Option value="weekly">每周一次</Option>
                                    <Option value="monthly">每月一次</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="scanScope" label="扫描范围" rules={[{required: true}]}>
                                <Select>
                                    <Option value="all">全部</Option>
                                    <Option value="instance">实例</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="timeRangeType" label="时间范围" rules={[{required: true}]}>
                                <Select>
                                    <Option value="full">全量</Option>
                                    <Option value="incremental">增量</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="policyCodes" label="支持的标签（策略 policy_code）">
                        <Select
                            mode="multiple"
                            allowClear
                            placeholder="选择 database_policy 中的策略"
                            options={policyOptions}
                            optionFilterProp="label"
                        />
                    </Form.Item>
                    <Form.Item name="enabledStatus" label="启用状态" valuePropName="checked">
                        <Switch checkedChildren="启用" unCheckedChildren="停用"/>
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
};

export default BatchMysqlOfflineScanJobPanel;
