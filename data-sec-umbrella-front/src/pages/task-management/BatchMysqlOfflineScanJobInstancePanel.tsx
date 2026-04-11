import React, {useCallback, useEffect, useRef, useState} from 'react';
import {Alert, Button, Card, Form, Input, message, Modal, Select, Space, Table, Tabs, Tag, Tooltip} from 'antd';
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

/** 多个样例换行展示（与 Tooltip 全文一致） */
const renderMultilineSamples = (arr?: string[]) => {
    const list = arr || [];
    if (!list.length) {
        return '-';
    }
    const text = list.join('\n');
    return (
        <Tooltip title={<span style={{whiteSpace: 'pre-line'}}>{text}</span>}>
            <span style={{whiteSpace: 'pre-line', wordBreak: 'break-word', cursor: 'default'}}>{text}</span>
        </Tooltip>
    );
};

const formatProgress = (done: number, total: number) => {
    if (total <= 0) {
        return `100% (${done}/${total})`;
    }
    const percent = Math.min(100, Math.max(0, (done / total) * 100));
    return `${percent.toFixed(2)}% (${done}/${total})`;
};

/** 与后端 buildColumnDetailsJson 一致：snake_case；兼容 camelCase */
type SnapshotColumnDetailRow = {
    key: string;
    columnName: string;
    sensitivityLevel: string;
    sensitivityTags: string[];
    samples: string[];
    sensitiveSamples: string[];
};

const asStringArray = (v: unknown): string[] => {
    if (!Array.isArray(v)) {
        return [];
    }
    return v.map((x) => (x == null ? '' : String(x)));
};

const parseColumnDetailsPayload = (raw: string): { rows: SnapshotColumnDetailRow[]; error: string | null } => {
    try {
        const parsed: unknown = JSON.parse(raw);
        if (!Array.isArray(parsed)) {
            return {rows: [], error: '列详情格式异常：应为 JSON 数组'};
        }
        const rows: SnapshotColumnDetailRow[] = parsed.map((item: any, idx: number) => {
            const name = item?.column_name ?? item?.columnName ?? '';
            const levelRaw = item?.sensitivity_level ?? item?.sensitivityLevel;
            const level = levelRaw == null || levelRaw === '' ? '-' : String(levelRaw);
            return {
                key: `${idx}-${String(name)}`,
                columnName: String(name),
                sensitivityLevel: level,
                sensitivityTags: asStringArray(item?.sensitivity_tags ?? item?.sensitivityTags),
                samples: asStringArray(item?.samples),
                sensitiveSamples: asStringArray(item?.sensitive_samples ?? item?.sensitiveSamples),
            };
        });
        return {rows, error: null};
    } catch {
        return {rows: [], error: '列详情 JSON 解析失败'};
    }
};

const BatchMysqlOfflineScanJobInstancePanel: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [list, setList] = useState<any[]>([]);
    const [pagination, setPagination] = useState({current: 1, pageSize: 10, total: 0});
    const [snapshotOpen, setSnapshotOpen] = useState(false);
    const [snapshotLoading, setSnapshotLoading] = useState(false);
    const [snapshotInstanceId, setSnapshotInstanceId] = useState<number | null>(null);
    const [snapshotScanKind, setSnapshotScanKind] = useState<'RULE' | 'AI'>('RULE');
    const [snapshotUniqueKey, setSnapshotUniqueKey] = useState('');
    const [snapshotLevels, setSnapshotLevels] = useState<string[]>([]);
    const [snapshotTags, setSnapshotTags] = useState('');
    const [tableSnapshots, setTableSnapshots] = useState<any[]>([]);
    const [columnSnapshots, setColumnSnapshots] = useState<any[]>([]);
    const [snapshotTablePage, setSnapshotTablePage] = useState(1);
    const [snapshotTablePageSize, setSnapshotTablePageSize] = useState(10);
    const [snapshotColumnPage, setSnapshotColumnPage] = useState(1);
    const [snapshotColumnPageSize, setSnapshotColumnPageSize] = useState(10);
    const [snapshotTableTotal, setSnapshotTableTotal] = useState(0);
    const [snapshotColumnTotal, setSnapshotColumnTotal] = useState(0);
    const [snapshotRequestKey, setSnapshotRequestKey] = useState(0);
    const [columnDetailModalOpen, setColumnDetailModalOpen] = useState(false);
    const [columnDetailModalUniqueKey, setColumnDetailModalUniqueKey] = useState('');
    const [columnDetailRows, setColumnDetailRows] = useState<SnapshotColumnDetailRow[]>([]);
    const [columnDetailParseError, setColumnDetailParseError] = useState<string | null>(null);

    const snapshotFilterRef = useRef({uniqueKey: '', levels: [] as string[], tags: ''});
    snapshotFilterRef.current = {
        uniqueKey: snapshotUniqueKey,
        levels: snapshotLevels,
        tags: snapshotTags,
    };

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

    const fetchSnapshots = useCallback(async () => {
        if (snapshotInstanceId == null) {
            return;
        }
        const f = snapshotFilterRef.current;
        setSnapshotLoading(true);
        try {
            const res = await mysqlOfflineScanJobInstanceApi.getSnapshotDetail({
                id: snapshotInstanceId,
                scanKind: snapshotScanKind,
                uniqueKeyContains: f.uniqueKey.trim() || undefined,
                sensitivityLevels: f.levels.length ? f.levels : undefined,
                sensitivityTagsContains: f.tags.trim() || undefined,
                tableCurrent: snapshotTablePage,
                tableSize: snapshotTablePageSize,
                columnCurrent: snapshotColumnPage,
                columnSize: snapshotColumnPageSize,
            });
            if (res.code === 200 && res.data) {
                setTableSnapshots(res.data.tableSnapshots || []);
                setColumnSnapshots(res.data.columnSnapshots || []);
                setSnapshotTableTotal(Number(res.data.tableTotal) || 0);
                setSnapshotColumnTotal(Number(res.data.columnTotal) || 0);
            } else {
                message.error(res.message || '加载快照失败');
            }
        } catch (e) {
            message.error('加载快照失败');
            console.error(e);
        } finally {
            setSnapshotLoading(false);
        }
    }, [
        snapshotInstanceId,
        snapshotScanKind,
        snapshotTablePage,
        snapshotTablePageSize,
        snapshotColumnPage,
        snapshotColumnPageSize,
        snapshotRequestKey,
    ]);

    const openSnapshotDetail = (instanceId: number) => {
        setSnapshotInstanceId(instanceId);
        setSnapshotScanKind('RULE');
        setSnapshotUniqueKey('');
        setSnapshotLevels([]);
        setSnapshotTags('');
        setSnapshotTablePage(1);
        setSnapshotColumnPage(1);
        setSnapshotTablePageSize(10);
        setSnapshotColumnPageSize(10);
        setSnapshotOpen(true);
    };

    useEffect(() => {
        if (!snapshotOpen || snapshotInstanceId == null) {
            return;
        }
        void fetchSnapshots();
    }, [snapshotOpen, snapshotInstanceId, fetchSnapshots]);

    useEffect(() => {
        if (!snapshotOpen) {
            setColumnDetailModalOpen(false);
        }
    }, [snapshotOpen]);

    const openColumnDetailsModal = (raw: string | undefined, uniqueKey?: string) => {
        setColumnDetailModalUniqueKey(uniqueKey?.trim() || '');
        if (!raw || raw === '[]') {
            setColumnDetailRows([]);
            setColumnDetailParseError(null);
            setColumnDetailModalOpen(true);
            return;
        }
        const {rows, error} = parseColumnDetailsPayload(raw);
        setColumnDetailRows(rows);
        setColumnDetailParseError(error);
        setColumnDetailModalOpen(true);
    };

    const renderTagList = (tags?: string[]) =>
        (tags || []).length ? (
            <Space size={[0, 4]} wrap>
                {(tags || []).map((t) => (
                    <Tag key={t}>{t}</Tag>
                ))}
            </Space>
        ) : (
            '-'
        );

    const columnDetailModalColumns: ColumnsType<SnapshotColumnDetailRow> = [
        {title: '列名', dataIndex: 'columnName', key: 'columnName', width: 160, ellipsis: true},
        {title: '敏感等级', dataIndex: 'sensitivityLevel', key: 'sensitivityLevel', width: 100},
        {
            title: '敏感标签',
            dataIndex: 'sensitivityTags',
            key: 'sensitivityTags',
            width: 220,
            render: (_: unknown, r) => renderTagList(r.sensitivityTags),
        },
        {
            title: '样例',
            dataIndex: 'samples',
            key: 'samples',
            width: 220,
            render: (_: unknown, r) => renderMultilineSamples(r.samples),
        },
        {
            title: '敏感样例',
            dataIndex: 'sensitiveSamples',
            key: 'sensitiveSamples',
            width: 220,
            render: (_: unknown, r) => renderMultilineSamples(r.sensitiveSamples),
        },
    ];

    const uniqueKeyColumn: ColumnsType<any>[0] = {
        title: '唯一键',
        dataIndex: 'uniqueKey',
        key: 'uniqueKey',
        ellipsis: true,
        width: 260,
        render: (v: string) => (
            <Tooltip title={v || '-'} placement="topLeft">
                <span style={{cursor: 'default'}}>{v || '-'}</span>
            </Tooltip>
        ),
    };

    const snapshotTableColumns: ColumnsType<any> = [
        {title: '事件时间', dataIndex: 'eventTime', width: 100},
        uniqueKeyColumn,
        {title: '敏感等级', dataIndex: 'sensitivityLevel', width: 100},
        {
            title: '敏感标签',
            dataIndex: 'sensitivityTags',
            width: 220,
            render: (_: unknown, r: any) => renderTagList(r.sensitivityTags),
        },
        {
            title: '列详情',
            dataIndex: 'columnDetails',
            width: 80,
            ellipsis: true,
            render: (v: string, r: any) =>
                !v || v === '[]' ? (
                    '-'
                ) : (
                    <Button type="link" size="small" style={{padding: 0, height: 'auto'}} onClick={() => openColumnDetailsModal(v, r.uniqueKey)}>
                        查看详情
                    </Button>
                ),
        },
        // {title: '扫描类型', dataIndex: 'scanKind', width: 90},
        // {title: '任务名', dataIndex: 'taskName', width: 160, ellipsis: true},
        // {title: 'Job ID', dataIndex: 'jobId', width: 90},
    ];

    const snapshotColumnColumns: ColumnsType<any> = [
        {title: '事件时间', dataIndex: 'eventTime', width: 170},
        uniqueKeyColumn,
        {title: '敏感等级', dataIndex: 'sensitivityLevel', width: 100},
        {
            title: '敏感标签',
            dataIndex: 'sensitivityTags',
            width: 200,
            render: (_: unknown, r: any) => renderTagList(r.sensitivityTags),
        },
        {
            title: '样例',
            dataIndex: 'samples',
            width: 220,
            render: (_: unknown, r: any) => renderMultilineSamples(r.samples as string[] | undefined),
        },
        {
            title: '敏感样例',
            dataIndex: 'sensitiveSamples',
            width: 220,
            render: (_: unknown, r: any) => renderMultilineSamples(r.sensitiveSamples as string[] | undefined),
        },
        {title: '扫描类型', dataIndex: 'scanKind', width: 90},
        {title: '任务名', dataIndex: 'taskName', width: 160, ellipsis: true},
        {title: 'Job ID', dataIndex: 'jobId', width: 90},
    ];

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
                    <Button size="small" onClick={() => openSnapshotDetail(Number(record.id))}>
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
            <Modal
                title={snapshotInstanceId != null ? `扫描快照（实例 ID ${snapshotInstanceId}）` : '扫描快照'}
                open={snapshotOpen}
                onCancel={() => setSnapshotOpen(false)}
                footer={null}
                width={1400}
                destroyOnClose
            >
                <div style={{marginBottom: 12}}>
                    <Space wrap align="start" style={{width: '100%'}}>
                        <span>扫描类型：</span>
                        <Select
                            style={{width: 140}}
                            value={snapshotScanKind}
                            onChange={(v) => {
                                setSnapshotScanKind(v as 'RULE' | 'AI');
                                setSnapshotTablePage(1);
                                setSnapshotColumnPage(1);
                            }}
                            options={[
                                {value: 'RULE', label: 'RULE（规则）'},
                                {value: 'AI', label: 'AI'},
                            ]}
                        />
                        <span>唯一键（模糊）：</span>
                        <Input
                            allowClear
                            placeholder="实例,库,表…"
                            style={{width: 200}}
                            value={snapshotUniqueKey}
                            onChange={(e) => setSnapshotUniqueKey(e.target.value)}
                        />
                        <span>敏感等级：</span>
                        <Select
                            mode="multiple"
                            allowClear
                            placeholder="多选"
                            style={{minWidth: 200}}
                            value={snapshotLevels}
                            onChange={(v) => setSnapshotLevels(v)}
                            options={['1', '2', '3', '4', '5'].map((n) => ({
                                value: n,
                                label: n,
                            }))}
                        />
                        <span>敏感标签（模糊）：</span>
                        <Input
                            allowClear
                            placeholder="标签子串"
                            style={{width: 180}}
                            value={snapshotTags}
                            onChange={(e) => setSnapshotTags(e.target.value)}
                        />
                        <Button
                            type="primary"
                            loading={snapshotLoading}
                            onClick={() => {
                                setSnapshotTablePage(1);
                                setSnapshotColumnPage(1);
                                setSnapshotRequestKey((k) => k + 1);
                            }}
                        >
                            刷新
                        </Button>
                    </Space>
                    <div style={{marginTop: 8, color: '#888', fontSize: 12}}>
                        数据源 engine 由服务端根据任务实例解析（与本次扫描资产类型一致），无需选择。
                    </div>
                </div>
                <Tabs
                    items={[
                        {
                            key: 'table',
                            label: '表级快照',
                            children: (
                                <Table
                                    rowKey={(r) => `tbl-${r.eventTime}-${r.uniqueKey}-${r.jobId}-${r.dispatchVersion}`}
                                    loading={snapshotLoading}
                                    columns={snapshotTableColumns}
                                    dataSource={tableSnapshots}
                                    scroll={{x: 1500}}
                                    pagination={{
                                        current: snapshotTablePage,
                                        pageSize: snapshotTablePageSize,
                                        total: snapshotTableTotal,
                                        showSizeChanger: true,
                                        showTotal: (t) => `共 ${t} 条`,
                                        onChange: (p, ps) => {
                                            setSnapshotTablePage(p);
                                            setSnapshotTablePageSize(ps || 10);
                                        },
                                    }}
                                />
                            ),
                        },
                        {
                            key: 'column',
                            label: '字段级快照',
                            children: (
                                <Table
                                    rowKey={(r) => `col-${r.eventTime}-${r.uniqueKey}-${r.jobId}-${r.dispatchVersion}`}
                                    loading={snapshotLoading}
                                    columns={snapshotColumnColumns}
                                    dataSource={columnSnapshots}
                                    scroll={{x: 1500}}
                                    pagination={{
                                        current: snapshotColumnPage,
                                        pageSize: snapshotColumnPageSize,
                                        total: snapshotColumnTotal,
                                        showSizeChanger: true,
                                        showTotal: (t) => `共 ${t} 条`,
                                        onChange: (p, ps) => {
                                            setSnapshotColumnPage(p);
                                            setSnapshotColumnPageSize(ps || 10);
                                        },
                                    }}
                                />
                            ),
                        },
                    ]}
                />
                <Modal
                    title={
                        columnDetailModalUniqueKey
                            ? `列详情 — ${columnDetailModalUniqueKey}`
                            : '列详情'
                    }
                    open={columnDetailModalOpen}
                    onCancel={() => setColumnDetailModalOpen(false)}
                    footer={
                        <Button type="primary" onClick={() => setColumnDetailModalOpen(false)}>
                            关闭
                        </Button>
                    }
                    width={1100}
                    zIndex={1100}
                    styles={{body: {maxHeight: '72vh', overflow: 'auto'}}}
                >
                    {columnDetailParseError ? (
                        <Alert type="warning" showIcon message={columnDetailParseError} style={{marginBottom: 12}}/>
                    ) : null}
                    <Table<SnapshotColumnDetailRow>
                        size="small"
                        rowKey="key"
                        columns={columnDetailModalColumns}
                        dataSource={columnDetailRows}
                        pagination={columnDetailRows.length > 12 ? {pageSize: 12, showSizeChanger: false, showTotal: (t) => `共 ${t} 列`} : false}
                        scroll={{x: 960}}
                        locale={{
                            emptyText: columnDetailParseError ? '无法解析为表格，请见上方提示' : '暂无列详情',
                        }}
                    />
                </Modal>
            </Modal>
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
