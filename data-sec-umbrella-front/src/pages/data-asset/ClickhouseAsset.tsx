import React, { useState, useEffect } from 'react';
import { Layout, Tabs, Card, Table, Input, Button, Space, message } from 'antd';
import type { TableProps } from 'antd';
import { dataSourceApi } from '../../services/api';

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

const ClickhouseAsset: React.FC = () => {
  // 状态管理
  const [instanceData, setInstanceData] = useState<Instance[]>([]);
  const [databaseData, setDatabaseData] = useState<Database[]>([]);
  const [tableData, setTableData] = useState<Table[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  // 查询参数
  const [instanceSearch, setInstanceSearch] = useState<string>('');
  const [databaseSearch, setDatabaseSearch] = useState<string>('');
  const [tableSearch, setTableSearch] = useState<string>('');
  const [instancePagination, setInstancePagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [databasePagination, setDatabasePagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [tablePagination, setTablePagination] = useState({ current: 1, pageSize: 10, total: 0 });

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
      title: '列信息',
      dataIndex: 'columnInfo',
      key: 'columnInfo',
      render: (_text) => (
        <Button type="link" size="small">查看</Button>
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
  ];

  // 获取实例列表
  const fetchInstances = async (page = instancePagination.current, pageSize = instancePagination.pageSize) => {
    setLoading(true);
    try {
      const response = await dataSourceApi.getPage({
        current: page,
        size: pageSize,
        dataSourceType: 'Clickhouse',
        instance: instanceSearch,
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

  // 获取数据库列表
  const fetchDatabases = async (page = databasePagination.current, pageSize = databasePagination.pageSize) => {
    setLoading(true);
    try {
      // TODO: 调用Clickhouse数据库列表接口
      // const response = await clickhouseDatabaseApi.getPage({
      //   instance: databaseSearch,
      // });
      // if (response.code === 200) {
      //   setDatabaseData(response.data.records);
      // }
      // 模拟数据
      const mock = [
        {
          id: 1,
          instance: 'localhost:9000',
          databaseName: 'default',
          description: '默认数据库',
          sensitivityLevel: '低',
          sensitivityTags: '默认',
          aiSensitivityLevel: '低',
          aiSensitivityTags: '默认',
          manualSensitive: false,
          createTime: '2024-01-01 10:00:00',
          modifyTime: '2024-01-01 10:00:00',
        },
      ].filter((item) => !databaseSearch || item.databaseName.includes(databaseSearch) || item.instance.includes(databaseSearch));
      const total = mock.length;
      const start = (page - 1) * pageSize;
      const records = mock.slice(start, start + pageSize);
      setDatabaseData(records);
      setDatabasePagination({ current: page, pageSize, total });
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
      // TODO: 调用Clickhouse表列表接口
      // const response = await clickhouseTableApi.getPage({
      //   databaseName: tableSearch,
      // });
      // if (response.code === 200) {
      //   setTableData(response.data.records);
      // }
      // 模拟数据
      const mock = [
        {
          id: 1,
          instance: 'localhost:9000',
          databaseName: 'default',
          tableName: 'users',
          description: '用户表',
          sensitivityLevel: '中',
          sensitivityTags: '用户信息',
          aiSensitivityLevel: '中',
          aiSensitivityTags: '个人信息',
          manualSensitive: true,
          columnInfo: JSON.stringify([
            { name: 'id', type: 'Int64', description: '用户ID' },
            { name: 'username', type: 'String', description: '用户名' },
            { name: 'email', type: 'String', description: '邮箱' },
          ]),
          createTime: '2024-01-01 10:00:00',
          modifyTime: '2024-01-01 10:00:00',
        },
      ].filter((item) => !tableSearch || item.tableName.includes(tableSearch) || item.databaseName.includes(tableSearch));
      const total = mock.length;
      const start = (page - 1) * pageSize;
      const records = mock.slice(start, start + pageSize);
      setTableData(records);
      setTablePagination({ current: page, pageSize, total });
    } catch (error) {
      message.error('网络请求失败');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载实例数据
  useEffect(() => {
    fetchInstances();
  }, []);

  return (<Layout style={{ padding: '24px' }}><Content><Card title="Clickhouse数据资产"><Tabs defaultActiveKey="1" onChange={(key) =>{
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
                    onSearch={() => fetchInstances(1, instancePagination.pageSize)}
                    style={{ width: 200 }}
                  /><Button type="primary" onClick={() => fetchInstances(1, instancePagination.pageSize)}>查询</Button></Space><Table
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
                    onChange: (p, ps) => fetchInstances(p, ps || 10),
                  }}
                /></TabPane><TabPane tab="数据库" key="2"><Space style={{ marginBottom: 16 }}><Search
                    placeholder="搜索数据库"
                    allowClear
                    value={databaseSearch}
                    onChange={(e) =>setDatabaseSearch(e.target.value)}
                    onSearch={() => fetchDatabases(1, databasePagination.pageSize)}
                    style={{ width: 200 }}
                  /><Button type="primary" onClick={() => fetchDatabases(1, databasePagination.pageSize)}>查询</Button></Space><Table
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
                    onChange: (p, ps) => fetchDatabases(p, ps || 10),
                  }}
                /></TabPane><TabPane tab="表" key="3"><Space style={{ marginBottom: 16 }}><Search
                    placeholder="搜索表"
                    allowClear
                    value={tableSearch}
                    onChange={(e) =>setTableSearch(e.target.value)}
                    onSearch={() => fetchTables(1, tablePagination.pageSize)}
                    style={{ width: 200 }}
                  /><Button type="primary" onClick={() => fetchTables(1, tablePagination.pageSize)}>查询</Button></Space><Table
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
                    onChange: (p, ps) => fetchTables(p, ps || 10),
                  }}
                /></TabPane></Tabs></Card></Content></Layout>);
};

export default ClickhouseAsset;