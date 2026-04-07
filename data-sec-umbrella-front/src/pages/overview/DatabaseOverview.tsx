import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Col, DatePicker, Empty, Progress, Row, Skeleton, Statistic, Tabs, Tag, Typography, message } from 'antd';
import { SyncOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
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

const DatabaseOverview: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'MySQL' | 'Clickhouse'>('MySQL');
  const [selectedDate, setSelectedDate] = useState(dayjs());
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [mysqlData, setMysqlData] = useState<OverviewState | null>(null);

  const loadOverview = async () => {
    if (activeTab !== 'MySQL') {
      return;
    }
    setLoading(true);
    try {
      const metricTime = selectedDate.format('YYYYMMDD');
      const res = await databaseOverviewApi.getMetrics({
        databaseType: 'MySQL',
        metricPeriod: 'DAY',
        metricTime,
      });
      if (res.code !== 200 || !res.data) {
        message.error(res.message || '加载概览失败');
        setMysqlData(null);
        return;
      }
      const m = res.data.metrics || {};
      const levelMap = JSON.parse(m.MYSQL_SENSITIVITY_LEVEL_DISTRIBUTION || '{}') as Record<string, number>;
      const tagMap = JSON.parse(m.MYSQL_SENSITIVITY_TAG_DISTRIBUTION || '{}') as Record<string, number>;
      const levelDistribution = ['1', '2', '3', '4', '5'].map((level) => ({
        level,
        count: Number(levelMap[level] || 0),
      }));
      const tagDistribution = Object.entries(tagMap)
        .map(([tag, count]) => ({ tag, count: Number(count || 0) }))
        .sort((a, b) => b.count - a.count)
        .slice(0, 12);
      setMysqlData({
        policyTotal: Number(m.MYSQL_POLICY_TOTAL || 0),
        taskTotal: Number(m.MYSQL_TASK_TOTAL || 0),
        taskEnabledTotal: Number(m.MYSQL_TASK_ENABLED_TOTAL || 0),
        dataSourceTotal: Number(m.MYSQL_DATASOURCE_TOTAL || 0),
        batchTaskTotal: Number(m.MYSQL_BATCH_TASK_TOTAL || 0),
        batchTaskInstanceTotal: Number(m.MYSQL_BATCH_TASK_INSTANCE_TOTAL || 0),
        instanceTotal: Number(m.MYSQL_INSTANCE_TOTAL || 0),
        databaseTotal: Number(m.MYSQL_DATABASE_TOTAL || 0),
        tableTotal: Number(m.MYSQL_TABLE_TOTAL || 0),
        sensitiveTableTotal: Number(m.MYSQL_SENSITIVE_TABLE_TOTAL || 0),
        sensitiveTableExcludeIgnoreTotal: Number(m.MYSQL_SENSITIVE_TABLE_EXCLUDE_IGNORE_TOTAL || 0),
        levelDistribution,
        tagDistribution,
      });
    } catch (e) {
      message.error('网络请求失败');
      console.error(e);
      setMysqlData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOverview();
  }, [activeTab, selectedDate.valueOf()]);

  const handleManualRefresh = async () => {
    if (activeTab !== 'MySQL') {
      message.info('当前仅支持MySQL指标刷新');
      return;
    }
    setRefreshing(true);
    try {
      const metricTime = selectedDate.format('YYYYMMDD');
      const res = await databaseOverviewApi.refreshMetrics({
        databaseType: 'MySQL',
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

  const sensitivePercent = useMemo(() => {
    if (!mysqlData || mysqlData.tableTotal <= 0) {
      return 0;
    }
    return Number(((mysqlData.sensitiveTableTotal / mysqlData.tableTotal) * 100).toFixed(2));
  }, [mysqlData]);

  const sensitiveExcludeIgnorePercent = useMemo(() => {
    if (!mysqlData || mysqlData.tableTotal <= 0) {
      return 0;
    }
    return Number(((mysqlData.sensitiveTableExcludeIgnoreTotal / mysqlData.tableTotal) * 100).toFixed(2));
  }, [mysqlData]);

  const mysqlTab = loading ? (
    <Skeleton active paragraph={{ rows: 10 }} />
  ) : !mysqlData ? (
    <Empty description="暂无概览数据" />
  ) : (
    <>
      <Row gutter={[16, 16]}>
        <Col span={6}><Card><Statistic title="策略数" value={mysqlData.policyTotal} /></Card></Col>
        <Col span={6}>
          <Card>
            <Statistic title="任务总数" value={mysqlData.taskTotal} />
            <Text type="secondary">已启用：{mysqlData.taskEnabledTotal}</Text>
          </Card>
        </Col>
        <Col span={6}><Card><Statistic title="数据源个数" value={mysqlData.dataSourceTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="批量任务个数" value={mysqlData.batchTaskTotal} /></Card></Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 4 }}>
        <Col span={6}><Card><Statistic title="批量任务实例个数" value={mysqlData.batchTaskInstanceTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="实例个数" value={mysqlData.instanceTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="数据库个数" value={mysqlData.databaseTotal} /></Card></Col>
        <Col span={6}><Card><Statistic title="表个数" value={mysqlData.tableTotal} /></Card></Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 4 }}>
        <Col span={10}>
          <Card title="敏感表占比">
            <Statistic title="敏感表个数" value={mysqlData.sensitiveTableTotal} />
            <Progress
              percent={sensitivePercent}
              strokeColor={{ '0%': '#ff7875', '100%': '#cf1322' }}
              style={{ marginTop: 12 }}
            />
            <Statistic title="排除忽略后敏感表个数" value={mysqlData.sensitiveTableExcludeIgnoreTotal} style={{ marginTop: 10 }} />
            <Progress
              percent={sensitiveExcludeIgnorePercent}
              strokeColor={{ '0%': '#ffa940', '100%': '#d46b08' }}
              style={{ marginTop: 12 }}
            />
          </Card>
        </Col>
        <Col span={14}>
          <Card title="敏感等级分布">
            {mysqlData.levelDistribution.map((i) => {
              const percent = mysqlData.sensitiveTableTotal
                ? Number(((i.count / mysqlData.sensitiveTableTotal) * 100).toFixed(2))
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
            {mysqlData.tagDistribution.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签数据" />
            ) : (
              <Row gutter={[8, 12]}>
                {mysqlData.tagDistribution.map((item) => (
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

  return (
    <Card bordered={false}>
      <Title level={3} style={{ marginTop: 0 }}>数据安全概览</Title>
      <Row justify="space-between" align="middle" style={{ marginBottom: 12 }}>
        <Col>
          <Text type="secondary">支持查看历史指标，也可手动触发指定日期重算</Text>
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
        onChange={(k) => setActiveTab((k === 'Clickhouse' ? 'Clickhouse' : 'MySQL'))}
        items={[
          { key: 'MySQL', label: 'MySQL', children: mysqlTab },
          {
            key: 'Clickhouse',
            label: 'Clickhouse',
            children: (
              <Card>
                <Empty description="Clickhouse 指标暂未接入（已支持日期选择）" />
              </Card>
            ),
          },
        ]}
      />
    </Card>
  );
};

export default DatabaseOverview;
