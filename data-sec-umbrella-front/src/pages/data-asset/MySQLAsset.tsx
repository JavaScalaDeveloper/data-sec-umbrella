import React, { useState, useEffect } from 'react';
import { Layout, Tabs, Card, Table, Input, Button, Space, message, Modal, Descriptions } from 'antd';
import type { TableProps } from 'antd';
import { dataSourceApi, mysqlDatabaseApi, mysqlTableApi, mysqlAssetScanApi } from '../../services/api';

const { Content } = Layout;
const { TabPane } = Tabs;
const { Search } = Input;

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
  manualSensitive: boolean;
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
  manualSensitive: boolean;
  columnInfo: string;
  createTime: string;
  modifyTime: string;
}

const MySQLAsset: React.FC = () => {
  // 状态管理
  const [instanceData, setInstanceData] = useState<Instance[]>([]);
  const [databaseData, setDatabaseData] = useState<Database[]>([]);
  const [tableData, setTableData] = useState<Table[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  // 弹窗状态
  const [detailModalVisible, setDetailModalVisible] = useState<boolean>(false);
  const [currentTable, setCurrentTable] = useState<Table | null>(null);
  const [columns, setColumns] = useState<Column[]>([]);

  // 查询参数
  const [instanceSearch, setInstanceSearch] = useState<string>('');
  const [databaseSearch, setDatabaseSearch] = useState<string>('');
  const [tableSearch, setTableSearch] = useState<string>('');

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
    },
    {
      title: '敏感标签',
      dataIndex: 'sensitivityTags',
      key: 'sensitivityTags',
    },
    {
      title: 'AI敏感等级',
      dataIndex: 'aiSensitivityLevel',
      key: 'aiSensitivityLevel',
    },
    {
      title: 'AI敏感标签',
      dataIndex: 'aiSensitivityTags',
      key: 'aiSensitivityTags',
    },
    {
      title: '人审是否敏感',
      dataIndex: 'manualSensitive',
      key: 'manualSensitive',
      render: (_text) => (_text ? '是' : '否'),
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
    },
    {
      title: '敏感标签',
      dataIndex: 'sensitivityTags',
      key: 'sensitivityTags',
    },
    {
      title: 'AI敏感等级',
      dataIndex: 'aiSensitivityLevel',
      key: 'aiSensitivityLevel',
    },
    {
      title: 'AI敏感标签',
      dataIndex: 'aiSensitivityTags',
      key: 'aiSensitivityTags',
    },
    {
      title: '人审是否敏感',
      dataIndex: 'manualSensitive',
      key: 'manualSensitive',
      render: (_text) => (_text ? '是' : '否'),
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
  const fetchInstances = async () => {
    setLoading(true);
    try {
      const response = await dataSourceApi.getPage({
        dataSourceType: 'MySQL',
        instance: instanceSearch,
      });
      if (response.code === 200) {
        setInstanceData(response.data.records);
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
  const fetchDatabases = async () => {
    setLoading(true);
    try {
      const response = await mysqlDatabaseApi.getPage({
        instance: databaseSearch,
      });
      if (response.code === 200) {
        setDatabaseData(response.data.records);
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
  const fetchTables = async () => {
    setLoading(true);
    try {
      const response = await mysqlTableApi.getPage({
        databaseName: tableSearch,
      });
      if (response.code === 200) {
        setTableData(response.data.records);
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
    // 解析列信息
    try {
      const columnData = JSON.parse(record.columnInfo);
      setColumns(columnData);
    } catch (error) {
      console.error('解析列信息失败:', error);
      setColumns([]);
    }
    setDetailModalVisible(true);
  };

  // 初始加载实例数据
  useEffect(() => {
    fetchInstances();
  }, []);

  return (<Layout style={{ padding: '24px' }}><Content><Card title="MySQL数据资产"><Tabs defaultActiveKey="1" onChange={(key) =>{
              if (key === '1') {
                fetchInstances();
              } else if (key === '2') {
                fetchDatabases();
              } else if (key === '3') {
                fetchTables();
              }
            }}><TabPane tab="实例" key="1"><Space style={{ marginBottom: 16 }}><Search
                    placeholder="搜索实例"
                    allowClear
                    value={instanceSearch}
                    onChange={(e) =>setInstanceSearch(e.target.value)}
                    onSearch={fetchInstances}
                    style={{ width: 200 }}
                  /><Button type="primary" onClick={fetchInstances}>查询</Button><Button type="primary" onClick={handleScan}>扫描</Button></Space><Table
                  columns={instanceColumns}
                  dataSource={instanceData}
                  loading={loading}
                  rowKey="id"
                  pagination={{ pageSize: 10 }}
                /></TabPane><TabPane tab="数据库" key="2"><Space style={{ marginBottom: 16 }}><Search
                    placeholder="搜索数据库"
                    allowClear
                    value={databaseSearch}
                    onChange={(e) =>setDatabaseSearch(e.target.value)}
                    onSearch={fetchDatabases}
                    style={{ width: 200 }}
                  /><Button type="primary" onClick={fetchDatabases}>查询</Button></Space><Table
                  columns={databaseColumns}
                  dataSource={databaseData}
                  loading={loading}
                  rowKey="id"
                  pagination={{ pageSize: 10 }}
                /></TabPane><TabPane tab="表" key="3"><Space style={{ marginBottom: 16 }}><Search
                    placeholder="搜索表"
                    allowClear
                    value={tableSearch}
                    onChange={(e) =>setTableSearch(e.target.value)}
                    onSearch={fetchTables}
                    style={{ width: 200 }}
                  /><Button type="primary" onClick={fetchTables}>查询</Button></Space><Table
                  columns={tableColumns}
                  dataSource={tableData}
                  loading={loading}
                  rowKey="id"
                  pagination={{ pageSize: 10 }}
                /></TabPane></Tabs></Card>

        {/* 表详情弹窗 */}
        <Modal
          title="表详情"
          open={detailModalVisible}
          onCancel={() =>setDetailModalVisible(false)}
          footer={null}
          width="90%"
        >
          {currentTable && (<><Descriptions bordered column={2}><Descriptions.Item label="实例">{currentTable.instance}</Descriptions.Item><Descriptions.Item label="数据库名">{currentTable.databaseName}</Descriptions.Item><Descriptions.Item label="表名">{currentTable.tableName}</Descriptions.Item><Descriptions.Item label="表描述">{currentTable.description}</Descriptions.Item><Descriptions.Item label="敏感等级">{currentTable.sensitivityLevel}</Descriptions.Item><Descriptions.Item label="敏感标签">{currentTable.sensitivityTags}</Descriptions.Item><Descriptions.Item label="AI敏感等级">{currentTable.aiSensitivityLevel}</Descriptions.Item><Descriptions.Item label="AI敏感标签">{currentTable.aiSensitivityTags}</Descriptions.Item><Descriptions.Item label="人审是否敏感">{currentTable.manualSensitive ? '是' : '否'}</Descriptions.Item><Descriptions.Item label="创建时间">{currentTable.createTime}</Descriptions.Item><Descriptions.Item label="修改时间">{currentTable.modifyTime}</Descriptions.Item></Descriptions><Card title="列信息" style={{ marginTop: 20 }}><Table
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
                ]}
                dataSource={columns}
                rowKey="columnName"
                pagination={false}
              /></Card></>)}
        </Modal></Content></Layout>);
};

export default MySQLAsset;