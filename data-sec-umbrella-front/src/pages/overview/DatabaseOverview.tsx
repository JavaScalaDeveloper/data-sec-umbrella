import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Card, Col, DatePicker, Empty, Progress, Row, Skeleton, Statistic, Tabs, Tag, Typography, message } from 'antd';
import { SyncOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { useLocation, useNavigate } from 'react-router-dom';
import { databaseOverviewApi } from '../../services/api';

const { Title, Text } = Typography;

type OverviewState = {
  policyTotal: number;
  taskTotal: number;
  taskEnabledTotal: number;
  dataSourceTotal: number;
  batchTaskTotal: number;
  batchTaskInstanceTotal: number;
  instanceTotal: number;
  databaseTotal: number;
  tableTotal: number;
  sensitiveTableTotal: number;
  sensitiveTableExcludeIgnoreTotal: number;
  levelDistribution: Array<{ level: string; count: number }>;
  tagDistribution: Array<{ tag: string; count: number }>;
};

const LEVEL_COLORS: Record<string, string> = {
  '1': '#ffccc7',
  '2': '#ffa39e',
  '3': '#ff7875',
  '4': '#ff4d4f',
  '5': '#cf1322',
};

type MetricPrefix = 'MYSQL' | 'CLICKHOUSE';

function safeJsonObject(raw: string): Record<string, number> {
  try {
    const o = JSON.parse(raw) as Record<string, unknown>;
    const out: Record<string, number> = {};
    if (o && typeof o === 'object') {
      Object.entries(o).forEach(([k, v]) => {
        out[k] = Number(v) || 0;
      });
    }
    return out;
  } catch {
    return {};
  }
}

function parseOverviewMetrics(m: Record<string, string>, prefix: MetricPrefix): OverviewState {
  const levelMap = safeJsonObject(m[`${prefix}_SENSITIVITY_LEVEL_DISTRIBUTION`] || '{}');
  const tagMap = safeJsonObject(m[`${prefix}_SENSITIVITY_TAG_DISTRIBUTION`] || '{}');
  const levelDistribution = ['1', '2', '3', '4', '5'].map((level) => ({
    level,
    count: Number(levelMap[level] || 0),
  }));
  const tagDistribution = Object.entries(tagMap)
    .map(([tag, count]) => ({ tag, count: Number(count || 0) }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 12);
  return {
    policyTotal: Number(m[`${prefix}_POLICY_TOTAL`] || 0),
    taskTotal: Number(m[`${prefix}_TASK_TOTAL`] || 0),
    taskEnabledTotal: Number(m[`${prefix}_TASK_ENABLED_TOTAL`] || 0),
    dataSourceTotal: Number(m[`${prefix}_DATASOURCE_TOTAL`] || 0),
    batchTaskTotal: Number(m[`${prefix}_BATCH_TASK_TOTAL`] || 0),
    batchTaskInstanceTotal: Number(m[`${prefix}_BATCH_TASK_INSTANCE_TOTAL`] || 0),
    instanceTotal: Number(m[`${prefix}_INSTANCE_TOTAL`] || 0),
    databaseTotal: Number(m[`${prefix}_DATABASE_TOTAL`] || 0),
    tableTotal: Number(m[`${prefix}_TABLE_TOTAL`] || 0),
    sensitiveTableTotal: Number(m[`${prefix}_SENSITIVE_TABLE_TOTAL`] || 0),
    sensitiveTableExcludeIgnoreTotal: Number(m[`${prefix}_SENSITIVE_TABLE_EXCLUDE_IGNORE_TOTAL`] || 0),
    levelDistribution,
    tagDistribution,
  };
}

function OverviewPanels({
  data,
  loading,
}: {
  data: OverviewState | null;
  loading: boolean;
}) {
  const sensitivePercent = useMemo(() => {
    if (!data || data.tableTotal <= 0) {
      return 0;
    }
    return Number(((data.sensitiveTableTotal / data.tableTotal) * 100).toFixed(2));
  }, [data]);

  const sensitiveExcludeIgnorePercent = useMemo(() => {
    if (!data || data.tableTotal <= 0) {
      return 0;
    }
    return Number(((data.sensitiveTableExcludeIgnoreTotal / data.tableTotal) * 100).toFixed(2));
  }, [data]);

  if (loading) {
    return <Skeleton active paragraph={{ rows: 10 }} />;
  }
  if (!data) {
    return <Empty description="暂无概览数据（可先点「刷新计算指标」生成当日快照）" />;
  }

  return (
    <>
      <Row gutter={[16, 16]}>
        <Col span={6}><Card><Statistic title="策略数" value={data.policyTotal} /></Card></Col>
        <Col span={6}>
          <Card>
            <Statistic title="任务总数" value={data.taskTotal} />
            <Text type="secondary">已启用：{data.taskEnabledTotal}</Text>
          </Card>
        </Col>
        <Col span={6}><Card><Statistic title="数据源个数" value={data.dataSourceTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="批量任务个数" value={data.batchTaskTotal} /></Card></Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 4 }}>
        <Col span={6}><Card><Statistic title="批量任务实例个数" value={data.batchTaskInstanceTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="实例个数" value={data.instanceTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="数据库个数" value={data.databaseTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="表个数" value={data.tableTotal} /></Card></Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 4 }}>
        <Col span={10}>
          <Card title="敏感表占比">
            <Statistic title="敏感表个数" value={data.sensitiveTableTotal} />
            <Progress
              percent={sensitivePercent}
              strokeColor={{ '0%': '#ff7875', '100%': '#cf1322' }}
              style={{ marginTop: 12 }}
            />
            <Statistic title="排除忽略后敏感表个数" value={data.sensitiveTableExcludeIgnoreTotal} style={{ marginTop: 10 }} />
            <Progress
              percent={sensitiveExcludeIgnorePercent}
              strokeColor={{ '0%': '#ffa940', '100%': '#d46b08' }}
              style={{ marginTop: 12 }}
            />
          </Card>
        </Col>
        <Col span={14}>
          <Card title="敏感等级分布">
            {data.levelDistribution.map((i) => {
              const percent = data.sensitiveTableTotal
                ? Number(((i.count / data.sensitiveTableTotal) * 100).toFixed(2))
                : 0;
              return (
                <Row key={i.level} align="middle" style={{ marginBottom: 10 }}>
                  <Col span={5}>
                    <Tag color={LEVEL_COLORS[i.level] || 'default'}>等级 {i.level}</Tag>
                  </Col>
                  <Col span={15}>
                    <Progress percent={percent} size="small" showInfo={false} strokeColor={LEVEL_COLORS[i.level]} />
                  </Col>
                  <Col span={4} style={{ textAlign: 'right' }}>{i.count}</Col>
                </Row>
              );
            })}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 4 }}>
        <Col span={24}>
          <Card title="敏感标签分布（Top 12）">
            {data.tagDistribution.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签数据" />
            ) : (
              <Row gutter={[8, 12]}>
                {data.tagDistribution.map((item) => (
                  <Col key={item.tag} span={8}>
                    <Card size="small" bodyStyle={{ padding: '10px 12px' }}>
                      <Row justify="space-between" align="middle">
                        <Col><Text strong>{item.tag}</Text></Col>
                        <Col><Tag color="blue">{item.count}</Tag></Col>
                      </Row>
                    </Card>
                  </Col>
                ))}
              </Row>
            )}
          </Card>
        </Col>
      </Row>
    </>
  );
}

const DatabaseOverview: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const activeTab = location.pathname.endsWith('/clickhouse') ? 'Clickhouse' : 'MySQL';

  const [selectedDate, setSelectedDate] = useState(dayjs());
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [mysqlData, setMysqlData] = useState<OverviewState | null>(null);
  const [clickhouseData, setClickhouseData] = useState<OverviewState | null>(null);

  useEffect(() => {
    const path = location.pathname;
    if (path.endsWith('/database-security/overview') || path.endsWith('/overview')) {
      navigate('/database-security/overview/mysql', { replace: true });
    }
  }, [location.pathname, navigate]);

  const loadOverview = useCallback(async () => {
    const metricTime = selectedDate.format('YYYYMMDD');
    const loadOne = async (databaseType: string, prefix: MetricPrefix, setData: (v: OverviewState | null) => void) => {
      const res = await databaseOverviewApi.getMetrics({
        databaseType,
        metricPeriod: 'DAY',
        metricTime,
      });
      if (res.code !== 200 || !res.data) {
        message.error(res.message || `加载${databaseType}概览失败`);
        setData(null);
        return;
      }
      const m = res.data.metrics || {};
      setData(parseOverviewMetrics(m, prefix));
    };

    setLoading(true);
    try {
      await Promise.all([
        loadOne('MySQL', 'MYSQL', setMysqlData),
        loadOne('Clickhouse', 'CLICKHOUSE', setClickhouseData),
      ]);
    } catch (e) {
      message.error('网络请求失败');
      console.error(e);
      setMysqlData(null);
      setClickhouseData(null);
    } finally {
      setLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    if (location.pathname.includes('/overview/mysql') || location.pathname.includes('/overview/clickhouse')) {
      void loadOverview();
    }
  }, [location.pathname, selectedDate.valueOf(), loadOverview]);

  const handleManualRefresh = async () => {
    const databaseType = activeTab === 'Clickhouse' ? 'Clickhouse' : 'MySQL';
    setRefreshing(true);
    try {
      const metricTime = selectedDate.format('YYYYMMDD');
      const res = await databaseOverviewApi.refreshMetrics({
        databaseType,
        metricTime,
      });
      if (res.code !== 200) {
        message.error(res.message || '刷新失败');
        return;
      }
      message.success('已触发刷新并完成计算');
      await loadOverview();
    } catch (e) {
      message.error('手动刷新失败');
      console.error(e);
    } finally {
      setRefreshing(false);
    }
  };

  const handleTabChange = (key: string) => {
    const next = key === 'Clickhouse' ? 'clickhouse' : 'mysql';
    navigate(`/database-security/overview/${next}`);
  };

  return (
    <Card bordered={false}>
      <Title level={3} style={{ marginTop: 0 }}>数据安全概览</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 12 }}>
        <Col>
          <Text type="secondary">
            MySQL 与 ClickHouse 使用独立地址；支持按日期查看快照，并可对当前 Tab 引擎触发后端重算
          </Text>
        </Col>
        <Col style={{ display: 'flex', gap: 8 }}>
          <DatePicker
            allowClear={false}
            value={selectedDate}
            onChange={(v) => setSelectedDate(v || dayjs())}
            disabledDate={(d) => !!d && d.isAfter(dayjs(), 'day')}
          />
          <Button icon={<SyncOutlined spin={refreshing} />} loading={refreshing} onClick={handleManualRefresh}>
            刷新计算指标
          </Button>
        </Col>
      </Row>
      <Tabs
        activeKey={activeTab}
        onChange={handleTabChange}
        items={[
          {
            key: 'MySQL',
            label: 'MySQL',
            children: <OverviewPanels data={mysqlData} loading={loading} />,
          },
          {
            key: 'Clickhouse',
            label: 'Clickhouse',
            children: <OverviewPanels data={clickhouseData} loading={loading} />,
          },
        ]}
      />
    </Card>
  );
};

export default DatabaseOverview;
