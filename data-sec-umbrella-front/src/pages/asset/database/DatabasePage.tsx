import React, { useState, useEffect } from 'react';
import { Tabs } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import DatabaseTabContentNew from './DatabaseTabContentNew.tsx';

const DatabasePage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // 从URL路径中获取当前tab，默认为'instance'
  const getPathTab = () => {
    const pathParts = location.pathname.split('/');
    // 检查路径是否已经包含tab信息
    if (pathParts.length >= 4 && ['instance', 'database', 'table'].includes(pathParts[pathParts.length - 1])) {
      return pathParts[pathParts.length - 1];
    }
    return 'instance';
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
    if (pathParts.length >= 4 && ['instance', 'database', 'table'].includes(pathParts[pathParts.length - 1])) {
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
          { key: 'instance', label: '实例' },
          { key: 'database', label: '数据库' },
          { key: 'table', label: '表' }
        ]}
      />
      <DatabaseTabContentNew activeTab={activeTab} />
    </div>
  );
};

export default DatabasePage;