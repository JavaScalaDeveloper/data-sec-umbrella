import React, { useState, useEffect } from 'react';
import { Tabs } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import MessageQueueTabContent from './MessageQueueTabContent.tsx';

const MessageQueuePage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // 从URL路径中获取当前tab，默认为'cluster'
  const getPathTab = () => {
    const pathParts = location.pathname.split('/');
    // 检查路径是否已经包含tab信息
    if (pathParts.length >= 4 && ['cluster', 'topic'].includes(pathParts[pathParts.length - 1])) {
      return pathParts[pathParts.length - 1];
    }
    return 'cluster';
  };
  
  const [activeTab, setActiveTab] = useState(getPathTab());

  // 当URL变化时，更新activeTab状态
  useEffect(() => {
    const pathTab = getPathTab();
    if (pathTab !== activeTab) {
      setActiveTab(pathTab);
    }
  }, [location.pathname]);

  const handleTabChange = (key: string) => {
    setActiveTab(key);
    // 更新URL路径，保持环境前缀
    const pathParts = location.pathname.split('/');
    
    // 如果路径已经包含tab信息，则替换它
    if (pathParts.length >= 4 && ['cluster', 'topic'].includes(pathParts[pathParts.length - 1])) {
      pathParts[pathParts.length - 1] = key;
    } else {
      // 否则添加tab信息
      pathParts.push(key);
    }
    
    const newPath = pathParts.join('/');
    navigate(newPath);
  };

  return (
    <div>
      <Tabs 
        activeKey={activeTab} 
        onChange={handleTabChange}
        items={[
          { key: 'cluster', label: '集群' },
          { key: 'topic', label: 'Topic' }
        ]}
      />
      <MessageQueueTabContent activeTab={activeTab} />
    </div>
  );
};

export default MessageQueuePage;