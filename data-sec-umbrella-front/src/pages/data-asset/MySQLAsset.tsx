import React, { useState, useEffect } from 'react';
import { Layout, Tabs, Card, Table, Input, Button, Space, message, Modal, Descriptions, Tag, Form, Select, Tooltip } from 'antd';
import type { TableProps } from 'antd';
import { dataSourceApi, mysqlDatabaseApi, mysqlTableApi, mysqlAssetScanApi, databasePolicyApi } from '../../services/api';
import { useLocation, useNavigate } from 'react-router-dom';

const { Content } = Layout;
const { TabPane } = Tabs;
const { Option } = Select;

/** 与后端 ManualReviewLabelEnum 的 code 一致 */
type ManualReviewCode = 'IGNORE' | 'FALSE_POSITIVE' | 'SENSITIVE';

const MANUAL_REVIEW_OPTIONS: { value: ManualReviewCode; label: string }[] = [
  { value: 'IGNORE', label: '忽略' },
  { value: 'FALSE_POSITIVE', label: '误报' },
  { value: 'SENSITIVE', label: '敏感' },
];

// 实例数据类型
interface Instance {
  id: number;
  dataSourceType: string;
  instance: string;
  username: string;
  connectivity: string;
  createTime: string;
  modifyTime: string;
}

// 数据库信息类型
interface Database {
  id: number;
  instance: string;
  databaseName: string;
  description: string;
  sensitivityLevel: string;
  sensitivityTags: string;
  aiSensitivityLevel: string;
  aiSensitivityTags: string;
  /** 人工打标；空为未打标（默认，不参与人工结论） */
  manualReview: string | null;
  createTime: string;
  modifyTime: string;
}

// 列信息类型
interface Column {
  columnName: string;
  columnType: string;
  columnComment: string;
  isNullable: string;
  columnDefault: string;
}

interface ColumnScanInfo {
  columnName: string;
  sensitivityLevel?: string;
  sensitivityTags?: string[] | string;
  sensitiveSamples?: string[];
  samples?: string[];
}

interface ColumnRow extends Column {
  scanSensitivityLevel?: string;
  scanSensitivityTags?: string[] | string;
  scanSensitiveSamples?: string[];
  scanSamples?: string[];
  aiSensitivityLevel?: string;
  aiSensitivityTags?: string[] | string;
  aiSensitiveSamples?: string[];
  aiSamples?: string[];
}

/** 列详情表：按敏感等级 1–5 数值排序，无/非法值在升序时排在末尾 */
function compareColumnSensitivity(a?: string, b?: string): number {
  const rank = (v?: string) => {
    const n = Number(v);
    if (Number.isFinite(n) && n >= 1 && n <= 5) return n;
    return 999;
  };
  return rank(a) - rank(b);
}

// 表信息类型
interface Table {
  id: number;
  instance: string;
  databaseName: string;
  tableName: string;
  description: string;
  sensitivityLevel: string;
  sensitivityTags: string;
  aiSensitivityLevel: string;
  aiSensitivityTags: string;
  manualReview: string | null;
  columnInfo: string;
  columnScanInfo?: string;
  columnAiScanInfo?: string;
  createTime: string;
  modifyTime: string;
}

const MySQLAsset: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // 状态管理
  const [instanceData, setInstanceData] = useState<Instance[]>([]);
  const [databaseData, setDatabaseData] = useState<Database[]>([]);
  const [tableData, setTableData] = useState<Table[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  // 弹窗状态
  const [detailModalVisible, setDetailModalVisible] = useState<boolean>(false);
  const [currentTable, setCurrentTable] = useState<Table | null>(null);
  const [columns, setColumns] = useState<ColumnRow[]>([]);

  // 查询参数
  const [instanceSearch, setInstanceSearch] = useState<string>('');
  const [instanceUsernameSearch, setInstanceUsernameSearch] = useState<string>('');
  const [databaseInstanceSearch, setDatabaseInstanceSearch] = useState<string>('');
  const [databaseNameSearch, setDatabaseNameSearch] = useState<string>('');
  const [databaseSensitivityLevelSearch, setDatabaseSensitivityLevelSearch] = useState<string[]>([]);
  const [databaseSensitivityTagsSearch, setDatabaseSensitivityTagsSearch] = useState<string>('');
  const [tableInstanceSearch, setTableInstanceSearch] = useState<string>('');
  const [tableDatabaseSearch, setTableDatabaseSearch] = useState<string>('');
  const [tableNameSearch, setTableNameSearch] = useState<string>('');
  const [tableSensitivityLevelSearch, setTableSensitivityLevelSearch] = useState<string[]>([]);
  const [tableSensitivityTagsSearch, setTableSensitivityTagsSearch] = useState<string>('');

  const [instancePagination, setInstancePagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [databasePagination, setDatabasePagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [tablePagination, setTablePagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [policyCodeNameMap, setPolicyCodeNameMap] = useState<Record<string, string>>({});
  const [activeTab, setActiveTab] = useState<'instance' | 'database' | 'table'>(() => {
    const path = location.pathname;
    if (path.endsWith('/database-security/data-asset/mysql/database')) {
      return 'database';
    }
    if (path.endsWith('/database-security/data-asset/mysql/table')) {
      return 'table';
    }
    return 'instance';
  });

  const levelColorMap: Record<number, string> = {
    1: '#ffccc7',
    2: '#ffa39e',
    3: '#ff7875',
    4: '#ff4d4f',
    5: '#cf1322',
  };

  const renderSensitivityLevel = (value?: string | number) => {
    const level = Number(value);
    if (!level) {
      return '-';
    }
    return <Tag color={levelColorMap[level] || '#52c41a'}>{level}</Tag>;
  };

  const renderTags = (value?: string) => {
    if (!value) {
      return '-';
    }
    const tags = value.split(',').map((item) => item.trim()).filter(Boolean);
    if (tags.length === 0) {
      return '-';
    }
    return (
      <Space size={[4, 4]} wrap>
        {tags.map((item) => (
          <Tag key={item} color="blue">{policyCodeNameMap[item] || item}</Tag>
        ))}
      </Space>
    );
  };

  const renderTagList = (value?: string[] | string) => {
    if (!value) {
      return '-';
    }
    const tags = Array.isArray(value) ? value : String(value).split(',');
    const normalized = tags.map((item) => String(item).trim()).filter(Boolean);
    if (!normalized.length) {
      return '-';
    }
    return (
      <Space size={[4, 4]} wrap>
        {normalized.map((item) => (
          <Tag key={item} color="blue">{policyCodeNameMap[item] || item}</Tag>
        ))}
      </Space>
    );
  };

  const renderSampleList = (samples?: string[]) => {
    if (!samples || samples.length === 0) {
      return '-';
    }
    const copySample = async (text: string) => {
      try {
        await navigator.clipboard.writeText(text);
        message.success('已复制样例');
      } catch (e) {
        message.error('复制失败');
        console.error(e);
      }
    };
    const maxLength = 24;
    return (
      <Space size={[4, 4]} wrap>
        {samples.map((raw, idx) => {
          const full = String(raw ?? '');
          const short = full.length > maxLength ? `${full.slice(0, maxLength)}...` : full;
          return (
            <Tooltip key={`${full}-${idx}`} title={full}>
              <Tag
                style={{ cursor: 'pointer', maxWidth: 240, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                onClick={() => copySample(full)}
              >
                {short}
              </Tag>
            </Tooltip>
          );
        })}
      </Space>
    );
  };

  // 实例表格列定义
  const instanceColumns: TableProps<Instance>['columns'] = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
    },
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
  ];

  // 数据库表格列定义
  const databaseColumns: TableProps<Database>['columns'] = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '实例',
      dataIndex: 'instance',
      key: 'instance',
    },
    {
      title: '数据库名',
      dataIndex: 'databaseName',
      key: 'databaseName',
    },
    {
      title: '数据库描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '敏感等级',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      render: (value: string) => renderSensitivityLevel(value),
    },
    {
      title: '敏感标签',
      dataIndex: 'sensitivityTags',
      key: 'sensitivityTags',
      render: (value: string) => renderTags(value),
    },
    {
      title: 'AI敏感等级',
      dataIndex: 'aiSensitivityLevel',
      key: 'aiSensitivityLevel',
      render: (value: string) => renderSensitivityLevel(value),
    },
    {
      title: 'AI敏感标签',
      dataIndex: 'aiSensitivityTags',
      key: 'aiSensitivityTags',
      render: (value: string) => renderTags(value),
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
  ];

  // 表表格列定义
  const tableColumns: TableProps<Table>['columns'] = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '实例',
      dataIndex: 'instance',
      key: 'instance',
    },
    {
      title: '数据库名',
      dataIndex: 'databaseName',
      key: 'databaseName',
    },
    {
      title: '表名',
      dataIndex: 'tableName',
      key: 'tableName',
    },
    {
      title: '表描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '敏感等级',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      render: (value: string) => renderSensitivityLevel(value),
    },
    {
      title: '敏感标签',
      dataIndex: 'sensitivityTags',
      key: 'sensitivityTags',
      render: (value: string) => renderTags(value),
    },
    {
      title: 'AI敏感等级',
      dataIndex: 'aiSensitivityLevel',
      key: 'aiSensitivityLevel',
      render: (value: string) => renderSensitivityLevel(value),
    },
    {
      title: 'AI敏感标签',
      dataIndex: 'aiSensitivityTags',
      key: 'aiSensitivityTags',
      render: (value: string) => renderTags(value),
    },
    {
      title: '人工打标',
      dataIndex: 'manualReview',
      key: 'manualReview',
      width: 150,
      render: (_text, record) => (
        <Select
          style={{ minWidth: 120 }}
          allowClear
          placeholder="默认"
          value={record.manualReview || undefined}
          onChange={async (v) => {
            try {
              const res = await mysqlTableApi.updateManualReview({
                id: record.id,
                manualReview: v,
              });
              if (res.code === 200) {
                message.success('已更新人工打标');
                setTableData((rows) =>
                  rows.map((r) => (r.id === record.id ? { ...r, manualReview: v ?? null } : r)),
                );
              } else {
                message.error(res.message || '更新失败');
              }
            } catch (e) {
              message.error('网络错误');
              console.error(e);
            }
          }}
        >
          {MANUAL_REVIEW_OPTIONS.map((o) => (
            <Option key={o.value} value={o.value}>
              {o.label}
            </Option>
          ))}
        </Select>
      ),
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
      title: '操作',
      key: 'action',
      render: (_text, record) => (
        <Button type="link" size="small" onClick={() =>handleViewDetail(record)}>查看</Button>
      ),
    },
  ];

  // 获取实例列表
  const fetchInstances = async (page = instancePagination.current, pageSize = instancePagination.pageSize) => {
    setLoading(true);
    try {
      const response = await dataSourceApi.getPage({
        current: page,
        size: pageSize,
        dataSourceType: 'MySQL',
        instance: instanceSearch,
        username: instanceUsernameSearch,
      });
      if (response.code === 200) {
        setInstanceData(response.data.records || []);
        setInstancePagination({
          current: Number(response.data.current) || page,
          pageSize: Number(response.data.size) || pageSize,
          total: Number(response.data.total) || 0,
        });
      } else {
        message.error('获取实例列表失败');
      }
    } catch (error) {
      message.error('网络请求失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 手动触发MySQL资产扫描
  const handleScan = async () => {
    try {
      message.info('开始扫描MySQL资产...');
      const response = await mysqlAssetScanApi.scan();
      if (response.code === 200) {
        message.success('MySQL资产扫描完成');
        // 重新加载数据
        fetchInstances();
        fetchDatabases();
        fetchTables();
      } else {
        message.error('扫描失败：' + response.message);
      }
    } catch (error) {
      message.error('扫描失败：网络请求错误');
      console.error(error);
    }
  };

  // 获取数据库列表
  const fetchDatabases = async (page = databasePagination.current, pageSize = databasePagination.pageSize) => {
    setLoading(true);
    try {
      const response = await mysqlDatabaseApi.getPage({
        current: page,
        size: pageSize,
        instance: databaseInstanceSearch,
        databaseName: databaseNameSearch,
        sensitivityLevelList: databaseSensitivityLevelSearch,
        sensitivityTags: databaseSensitivityTagsSearch,
      });
      if (response.code === 200) {
        setDatabaseData(response.data.records || []);
        setDatabasePagination({
          current: Number(response.data.current) || page,
          pageSize: Number(response.data.size) || pageSize,
          total: Number(response.data.total) || 0,
        });
      } else {
        message.error('获取数据库列表失败');
      }
    } catch (error) {
      message.error('网络请求失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 获取表列表
  const fetchTables = async (page = tablePagination.current, pageSize = tablePagination.pageSize) => {
    setLoading(true);
    try {
      const response = await mysqlTableApi.getPage({
        current: page,
        size: pageSize,
        instance: tableInstanceSearch,
        databaseName: tableDatabaseSearch,
        tableName: tableNameSearch,
        sensitivityLevelList: tableSensitivityLevelSearch,
        sensitivityTags: tableSensitivityTagsSearch,
      });
      if (response.code === 200) {
        setTableData(response.data.records || []);
        setTablePagination({
          current: Number(response.data.current) || page,
          pageSize: Number(response.data.size) || pageSize,
          total: Number(response.data.total) || 0,
        });
      } else {
        message.error('获取表列表失败');
      }
    } catch (error) {
      message.error('网络请求失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 查看表详情
  const handleViewDetail = (record: Table) => {
    setCurrentTable(record);
    try {
      const columnData: Column[] = record.columnInfo ? JSON.parse(record.columnInfo) : [];
      const scanData: ColumnScanInfo[] = record.columnScanInfo ? JSON.parse(record.columnScanInfo) : [];
      const aiScanData: ColumnScanInfo[] = record.columnAiScanInfo ? JSON.parse(record.columnAiScanInfo) : [];
      const scanMap = new Map<string, ColumnScanInfo>();
      const aiMap = new Map<string, ColumnScanInfo>();
      scanData.forEach((item) => scanMap.set(item.columnName, item));
      aiScanData.forEach((item) => aiMap.set(item.columnName, item));
      const merged: ColumnRow[] = columnData.map((col) => {
        const scan = scanMap.get(col.columnName);
        const ai = aiMap.get(col.columnName);
        return {
          ...col,
          scanSensitivityLevel: scan?.sensitivityLevel,
          scanSensitivityTags: scan?.sensitivityTags,
          scanSensitiveSamples: scan?.sensitiveSamples,
          scanSamples: scan?.samples,
          aiSensitivityLevel: ai?.sensitivityLevel,
          aiSensitivityTags: ai?.sensitivityTags,
          aiSensitiveSamples: ai?.sensitiveSamples,
          aiSamples: ai?.samples,
        };
      });
      setColumns(merged);
    } catch (error) {
      console.error('解析列信息失败:', error);
      setColumns([]);
    }
    setDetailModalVisible(true);
  };

  // 获取策略 code -> 中文名 映射，用于标签翻译展示
  const fetchPolicyMap = async () => {
    try {
      const response = await databasePolicyApi.getPage({ current: 1, size: 500 });
      if (response.code === 200 && response.data?.records) {
        const map: Record<string, string> = {};
        response.data.records.forEach((item: any) => {
          if (item?.policyCode) {
            map[item.policyCode] = item.policyName || item.policyCode;
          }
        });
        setPolicyCodeNameMap(map);
      }
    } catch (error) {
      console.error('获取策略映射失败:', error);
    }
  };

  // 初始加载实例数据
  useEffect(() => {
    fetchPolicyMap();
  }, []);

  useEffect(() => {
    const path = location.pathname;
    // 兼容旧入口：/database-security/data-asset/mysql 默认跳到 instance
    if (path.endsWith('/database-security/data-asset/mysql')) {
      navigate('/database-security/data-asset/mysql/instance', { replace: true });
      return;
    }
    if (path.endsWith('/database-security/data-asset/mysql/database')) {
      setActiveTab('database');
      fetchDatabases(1, databasePagination.pageSize);
      return;
    }
    if (path.endsWith('/database-security/data-asset/mysql/table')) {
      setActiveTab('table');
      fetchTables(1, tablePagination.pageSize);
      return;
    }
    // instance
    setActiveTab('instance');
    fetchInstances(1, instancePagination.pageSize);
  }, [location.pathname]);

  const handleTabChange = (key: string) => {
    const next = key === 'database' ? 'database' : key === 'table' ? 'table' : 'instance';
    setActiveTab(next);
    navigate(`/database-security/data-asset/mysql/${next}`);
  };

  return (<Layout style={{ padding: '24px' }}><Content><Card title="MySQL数据资产"><Tabs activeKey={activeTab} onChange={handleTabChange}><TabPane tab="实例" key="instance"><Form layout="inline" style={{ marginBottom: 16 }}><Form.Item label="实例"><Input
                          placeholder="请输入实例"
                          allowClear
                          value={instanceSearch}
                          onChange={(e) =>setInstanceSearch(e.target.value)}
                          onPressEnter={() => fetchInstances(1, instancePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item label="用户名"><Input
                          placeholder="请输入用户名"
                          allowClear
                          value={instanceUsernameSearch}
                          onChange={(e) =>setInstanceUsernameSearch(e.target.value)}
                          onPressEnter={() => fetchInstances(1, instancePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item><Space><Button type="primary" onClick={() => fetchInstances(1, instancePagination.pageSize)}>查询</Button><Button type="primary" onClick={handleScan}>扫描</Button></Space></Form.Item></Form><Table
                  columns={instanceColumns}
                  dataSource={instanceData}
                  loading={loading}
                  rowKey="id"
                  pagination={{
                    current: instancePagination.current,
                    pageSize: instancePagination.pageSize,
                    total: instancePagination.total,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (p, ps) => fetchInstances(p, ps),
                  }}
                /></TabPane><TabPane tab="数据库" key="database"><Form layout="inline" style={{ marginBottom: 16 }}><Form.Item label="实例"><Input
                          placeholder="请输入实例"
                          allowClear
                          value={databaseInstanceSearch}
                          onChange={(e) =>setDatabaseInstanceSearch(e.target.value)}
                          onPressEnter={() => fetchDatabases(1, databasePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item label="数据库名"><Input
                          placeholder="请输入数据库名"
                          allowClear
                          value={databaseNameSearch}
                          onChange={(e) =>setDatabaseNameSearch(e.target.value)}
                          onPressEnter={() => fetchDatabases(1, databasePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item label="敏感等级"><Select
                          mode="multiple"
                          allowClear
                          placeholder="请选择敏感等级"
                          value={databaseSensitivityLevelSearch}
                          onChange={(v) => setDatabaseSensitivityLevelSearch((v || []).map(String))}
                          style={{ width: 220 }}
                        ><Option value="1">1</Option><Option value="2">2</Option><Option value="3">3</Option><Option value="4">4</Option><Option value="5">5</Option></Select></Form.Item><Form.Item label="敏感标签"><Input
                          placeholder="请输入敏感标签"
                          allowClear
                          value={databaseSensitivityTagsSearch}
                          onChange={(e) =>setDatabaseSensitivityTagsSearch(e.target.value)}
                          onPressEnter={() => fetchDatabases(1, databasePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item><Space><Button type="primary" onClick={() => fetchDatabases(1, databasePagination.pageSize)}>查询</Button></Space></Form.Item></Form><Table
                  columns={databaseColumns}
                  dataSource={databaseData}
                  loading={loading}
                  rowKey="id"
                  pagination={{
                    current: databasePagination.current,
                    pageSize: databasePagination.pageSize,
                    total: databasePagination.total,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (p, ps) => fetchDatabases(p, ps),
                  }}
                /></TabPane><TabPane tab="表" key="table"><Form layout="inline" style={{ marginBottom: 16 }}><Form.Item label="实例"><Input
                          placeholder="请输入实例"
                          allowClear
                          value={tableInstanceSearch}
                          onChange={(e) =>setTableInstanceSearch(e.target.value)}
                          onPressEnter={() => fetchTables(1, tablePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item label="数据库"><Input
                          placeholder="请输入数据库名"
                          allowClear
                          value={tableDatabaseSearch}
                          onChange={(e) =>setTableDatabaseSearch(e.target.value)}
                          onPressEnter={() => fetchTables(1, tablePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item label="表名"><Input
                          placeholder="请输入表名"
                          allowClear
                          value={tableNameSearch}
                          onChange={(e) =>setTableNameSearch(e.target.value)}
                          onPressEnter={() => fetchTables(1, tablePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item label="敏感等级"><Select
                          mode="multiple"
                          allowClear
                          placeholder="请选择敏感等级"
                          value={tableSensitivityLevelSearch}
                          onChange={(v) => setTableSensitivityLevelSearch((v || []).map(String))}
                          style={{ width: 220 }}
                        ><Option value="1">1</Option><Option value="2">2</Option><Option value="3">3</Option><Option value="4">4</Option><Option value="5">5</Option></Select></Form.Item><Form.Item label="敏感标签"><Input
                          placeholder="请输入敏感标签"
                          allowClear
                          value={tableSensitivityTagsSearch}
                          onChange={(e) =>setTableSensitivityTagsSearch(e.target.value)}
                          onPressEnter={() => fetchTables(1, tablePagination.pageSize)}
                          style={{ width: 220 }}
                        /></Form.Item><Form.Item><Space><Button type="primary" onClick={() => fetchTables(1, tablePagination.pageSize)}>查询</Button></Space></Form.Item></Form><Table
                  columns={tableColumns}
                  dataSource={tableData}
                  loading={loading}
                  rowKey="id"
                  pagination={{
                    current: tablePagination.current,
                    pageSize: tablePagination.pageSize,
                    total: tablePagination.total,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (p, ps) => fetchTables(p, ps),
                  }}
                /></TabPane></Tabs></Card>

        {/* 表详情弹窗 */}
        <Modal
          title="表详情"
          open={detailModalVisible}
          onCancel={() =>setDetailModalVisible(false)}
          footer={null}
          width="90%"
        >
          {currentTable && (<><Descriptions bordered column={2}><Descriptions.Item label="实例">{currentTable.instance}</Descriptions.Item><Descriptions.Item label="数据库名">{currentTable.databaseName}</Descriptions.Item><Descriptions.Item label="表名">{currentTable.tableName}</Descriptions.Item><Descriptions.Item label="表描述">{currentTable.description}</Descriptions.Item><Descriptions.Item label="敏感等级">{currentTable.sensitivityLevel}</Descriptions.Item><Descriptions.Item label="敏感标签">{currentTable.sensitivityTags}</Descriptions.Item><Descriptions.Item label="AI敏感等级">{currentTable.aiSensitivityLevel}</Descriptions.Item><Descriptions.Item label="AI敏感标签">{currentTable.aiSensitivityTags}</Descriptions.Item><Descriptions.Item label="人工打标" span={2}><Select
                    style={{ minWidth: 200 }}
                    allowClear
                    placeholder="默认（未人工打标）"
                    value={currentTable.manualReview || undefined}
                    onChange={async (v) => {
                      try {
                        const res = await mysqlTableApi.updateManualReview({
                          id: currentTable.id,
                          manualReview: v,
                        });
                        if (res.code === 200) {
                          message.success('已更新人工打标');
                          const next = v ?? null;
                          setCurrentTable({ ...currentTable, manualReview: next });
                          setTableData((rows) =>
                            rows.map((r) => (r.id === currentTable.id ? { ...r, manualReview: next } : r)),
                          );
                        } else {
                          message.error(res.message || '更新失败');
                        }
                      } catch (e) {
                        message.error('网络错误');
                        console.error(e);
                      }
                    }}
                  >
                    {MANUAL_REVIEW_OPTIONS.map((o) => (
                      <Option key={o.value} value={o.value}>
                        {o.label}
                      </Option>
                    ))}
                  </Select></Descriptions.Item><Descriptions.Item label="创建时间">{currentTable.createTime}</Descriptions.Item><Descriptions.Item label="修改时间">{currentTable.modifyTime}</Descriptions.Item></Descriptions><Card title="列信息" style={{ marginTop: 20 }}><Table
                columns={[
                  {
                    title: '列名',
                    dataIndex: 'columnName',
                    key: 'columnName',
                  },
                  {
                    title: '数据类型',
                    dataIndex: 'columnType',
                    key: 'columnType',
                  },
                  {
                    title: '列描述',
                    dataIndex: 'columnComment',
                    key: 'columnComment',
                  },
                  {
                    title: '规则敏感等级',
                    dataIndex: 'scanSensitivityLevel',
                    key: 'scanSensitivityLevel',
                    sorter: (a: ColumnRow, b: ColumnRow) =>
                      compareColumnSensitivity(a.scanSensitivityLevel, b.scanSensitivityLevel),
                    sortDirections: ['descend', 'ascend'],
                    showSorterTooltip: true,
                    render: (value: string) => renderSensitivityLevel(value),
                  },
                  {
                    title: '规则敏感标签',
                    dataIndex: 'scanSensitivityTags',
                    key: 'scanSensitivityTags',
                    render: (value: string[] | string) => renderTagList(value),
                  },
                  {
                    title: '规则敏感样例',
                    dataIndex: 'scanSensitiveSamples',
                    key: 'scanSensitiveSamples',
                    render: (value: string[]) => renderSampleList(value),
                  },
                  {
                    title: '规则样例列表',
                    dataIndex: 'scanSamples',
                    key: 'scanSamples',
                    render: (value: string[]) => renderSampleList(value),
                  },
                  {
                    title: 'AI敏感等级',
                    dataIndex: 'aiSensitivityLevel',
                    key: 'aiSensitivityLevel',
                    sorter: (a: ColumnRow, b: ColumnRow) =>
                      compareColumnSensitivity(a.aiSensitivityLevel, b.aiSensitivityLevel),
                    sortDirections: ['descend', 'ascend'],
                    showSorterTooltip: true,
                    render: (value: string) => renderSensitivityLevel(value),
                  },
                  {
                    title: 'AI敏感标签',
                    dataIndex: 'aiSensitivityTags',
                    key: 'aiSensitivityTags',
                    render: (value: string[] | string) => renderTagList(value),
                  },
                  {
                    title: 'AI敏感样例',
                    dataIndex: 'aiSensitiveSamples',
                    key: 'aiSensitiveSamples',
                    render: (value: string[]) => renderSampleList(value),
                  },
                  {
                    title: 'AI样例列表',
                    dataIndex: 'aiSamples',
                    key: 'aiSamples',
                    render: (value: string[]) => renderSampleList(value),
                  },
                ]}
                dataSource={columns}
                rowKey="columnName"
                pagination={false}
              /></Card></>)}
        </Modal></Content></Layout>);
};

export default MySQLAsset;