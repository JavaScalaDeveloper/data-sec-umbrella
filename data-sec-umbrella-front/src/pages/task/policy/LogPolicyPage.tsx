import React from 'react';
import { Result, Button, Tabs } from 'antd';
import { useNavigate } from 'react-router-dom';

const LogPolicyPage: React.FC = () => {
  const navigate = useNavigate();

  // 处理选项卡切换
  const handleTabChange = (key: string) => {
    switch (key) {
      case 'database':
        navigate('/task/policy/database');
        break;
      case 'api':
        navigate('/task/policy/api');
        break;
      case 'message':
        navigate('/task/policy/message');
        break;
      case 'log':
        navigate('/task/policy/log');
        break;
      default:
        navigate('/task/policy/database');
    }
  };

  const handleBack = () => {
    navigate('/task/policy/database');
  };

  return (
    <div>
      <Tabs 
        activeKey="log" 
        onChange={handleTabChange}
        items={[
          { key: 'database', label: '数据库' },
          { key: 'api', label: 'API' },
          { key: 'message', label: '消息' },
          { key: 'log', label: '日志' }
        ]}
      />
      
      <Result
        status="info"
        title="敬请期待"
        subTitle="日志策略页面正在建设中，敬请期待后续版本更新。"
        extra={
          <Button type="primary" onClick={handleBack}>
            返回数据库策略
          </Button>
        }
      />
    </div>
  );
};

export default LogPolicyPage;