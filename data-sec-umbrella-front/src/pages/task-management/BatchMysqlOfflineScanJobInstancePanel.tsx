import React, {useCallback, useEffect, useState} from 'react';
import {Button, Card, Form, Input, message, Select, Space, Table, Tag} from 'antd';
import type {ColumnsType} from 'antd/es/table';
import {SearchOutlined} from '@ant-design/icons';
import {mysqlOfflineScanJobInstanceApi} from '../../services/api';

const {Option} = Select;

const STATUS_LABEL: Record<string, string> = {
    waiting: '等待运行',
    running: '运行中',
    stopped: '已停止',
    completed: '已完成',
    failed: '已失败',
};

const STATUS_COLOR: Record<string, string> = {
    waiting: 'default',
    running: 'processing',
    stopped: 'warning',
    completed: 'success',
    failed: 'error',
};

const formatProgress = (done: number, total: number) => {
    if (total <= 0) {
        return `100% (${done}/${total})`;
    }
    const percent = Math.min(100, Math.max(0, (done / total) * 100));
    return `${percent.toFixed(2)}% (${done}/${total})`;
};

const BatchMysqlOfflineScanJobInstancePanel: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [list, setList] = useState<any[]>([]);
    const [pagination, setPagination] = useState({current: 1, pageSize: 10, total: 0});

    const fetchList = useCallback(async (page: number, pageSize: number) => {
        setLoading(true);
        try {
            const q = form.getFieldsValue();
            const res = await mysqlOfflineScanJobInstanceApi.getPage({
                current: page,
                size: pageSize,
                taskName: q.taskName || undefined,
                runStatus: q.runStatus || undefined,
            });
            if (res.code === 200 && res.data) {
                setList(res.data.records || []);
                setPagination({
                    current: Number(res.data.current) || page,
                    pageSize: Number(res.data.size) || pageSize,
                    total: Number(res.data.total) || 0,
                });
            } else {
                message.error(res.message || '加载任务实例失败');
            }
        } catch (e) {
            message.error('网络请求失败');
            console.error(e);
        } finally {
            setLoading(false);
        }
    }, [form]);

    useEffect(() => {
        fetchList(1, 10);
    }, [fetchList]);

    const columns: ColumnsType<any> = [
        {title: 'ID', dataIndex: 'id', width: 80},
        {title: '任务名', dataIndex: 'taskName', width: 180},
        {
            title: '运行状态',
            dataIndex: 'runStatus',
            width: 120,
            render: (v: string) => <Tag color={STATUS_COLOR[v] || 'default'}>{STATUS_LABEL[v] || v}</Tag>,
        },
        {title: '成功', dataIndex: 'successCount', width: 80},
        {title: '失败', dataIndex: 'failCount', width: 80},
        {title: '敏感', dataIndex: 'sensitiveCount', width: 80},
        {
            title: '扫描进度',
            width: 280,
            render: (_, record) => {
                const expected = Number(record.expectedTotal || 0);
                const submitted = Number(record.submittedTotal || 0);
                const consumed = Number(record.successCount || 0) + Number(record.failCount || 0);
                return (
                    <div>
                        <div>生产进度：{formatProgress(submitted, expected)}</div>
                        <div>消费进度：{formatProgress(consumed, expected)}</div>
                    </div>
                );
            },
        },
        {title: 'AI成功', dataIndex: 'aiSuccessCount', width: 90},
        {title: 'AI失败', dataIndex: 'aiFailCount', width: 90},
        {title: 'AI敏感', dataIndex: 'aiSensitiveCount', width: 90},
        {
            title: 'AI扫描进度',
            width: 280,
            render: (_, record) => {
                const aiExpected = Number(record.aiExpectedTotal || 0);
                const aiSubmitted = Number(record.aiSubmittedTotal || 0);
                const aiConsumed = Number(record.aiSuccessCount || 0) + Number(record.aiFailCount || 0);
                return (
                    <div>
                        <div>生产进度：{formatProgress(aiSubmitted, aiExpected)}</div>
                        <div>消费进度：{formatProgress(aiConsumed, aiExpected)}</div>
                    </div>
                );
            },
        },
        {
            title: '创建/修改时间',
            width: 220,
            render: (_, record) => (
                <div>
                    <div>创建：{record.createTime || '-'}</div>
                    <div>修改：{record.modifyTime || '-'}</div>
                </div>
            ),
        },
        {
            title: '操作',
            width: 150,
            fixed: 'right',
            render: (_, record) => (
                <Space size="small">
                    <Button
                        size="small"
                        onClick={() => message.info(`实例详情：${record.id}`)}
                    >
                        详情
                    </Button>
                    <Button
                        size="small"
                        danger
                        disabled={record.runStatus !== 'running' && record.runStatus !== 'waiting'}
                        onClick={() => message.info(`停止实例：${record.id}`)}
                    >
                        停止
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
                        <Input placeholder="模糊查询" allowClear style={{width: 220}}/>
                    </Form.Item>
                    <Form.Item name="runStatus" label="运行状态">
                        <Select placeholder="全部" allowClear style={{width: 180}}>
                            <Option value="waiting">等待运行</Option>
                            <Option value="running">运行中</Option>
                            <Option value="stopped">已停止</Option>
                            <Option value="completed">已完成</Option>
                            <Option value="failed">已失败</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item>
                        <Space>
                            <Button type="primary" icon={<SearchOutlined/>} onClick={() => fetchList(1, pagination.pageSize)}>
                                查询
                            </Button>
                        </Space>
                    </Form.Item>
                </Form>
            </Card>

            <Table
                columns={columns}
                dataSource={list}
                rowKey="id"
                loading={loading}
                scroll={{x: 1700}}
                pagination={{
                    current: pagination.current,
                    pageSize: pagination.pageSize,
                    total: pagination.total,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (p, ps) => fetchList(p, ps || 10),
                }}
            />
        </>
    );
};

export default BatchMysqlOfflineScanJobInstancePanel;
