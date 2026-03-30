import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout.tsx';
import HomePage from './pages/HomePage.tsx';
import DatabasePolicyPage from './pages/task/policy/DatabasePolicyPage.tsx';
import ApiPolicyPage from './pages/task/policy/ApiPolicyPage.tsx';
import MessagePolicyPage from './pages/task/policy/MessagePolicyPage.tsx';
import RealTimeTaskPage from './pages/task/RealTimeTaskPage.tsx';
import BatchTaskPage from './pages/task/BatchTaskPage.tsx';
import DatabasePage from './pages/asset/database/DatabasePage.tsx';
import ApiPage from './pages/asset/api/ApiPage.tsx';
import MessageQueuePage from './pages/asset/message/MessageQueuePage.tsx';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainLayout />}> 
          <Route index element={<HomePage />} />
          
          {/* 数据库安全产品 */}
          <Route path="database/overview" element={<HomePage />} />
          <Route path="database/policy" element={<DatabasePolicyPage />} />
          <Route path="database/realtime" element={<RealTimeTaskPage />} />
          <Route path="database/batch" element={<BatchTaskPage />} />
          <Route path="database/asset" element={<DatabasePage />} />
          
          {/* API安全产品 */}
          <Route path="api/overview" element={<HomePage />} />
          <Route path="api/policy" element={<ApiPolicyPage />} />
          <Route path="api/realtime" element={<RealTimeTaskPage />} />
          <Route path="api/batch" element={<BatchTaskPage />} />
          <Route path="api/asset" element={<ApiPage />} />
          
          {/* 消息安全产品 */}
          <Route path="message/overview" element={<HomePage />} />
          <Route path="message/policy" element={<MessagePolicyPage />} />
          <Route path="message/realtime" element={<RealTimeTaskPage />} />
          <Route path="message/batch" element={<BatchTaskPage />} />
          <Route path="message/asset" element={<MessageQueuePage />} />
          
          {/* 兼容旧路由 */}
          <Route path="task/policy" element={<DatabasePolicyPage />} />
          <Route path="task/realtime" element={<RealTimeTaskPage />} />
          <Route path="task/batch" element={<BatchTaskPage />} />
          <Route path="asset/database" element={<DatabasePage />} />
          <Route path="asset/api" element={<ApiPage />} />
          <Route path="asset/mq" element={<MessageQueuePage />} />
        </Route>
        
        {/* 支持环境路径的路由 */}
        <Route path="/env/:env" element={<MainLayout />}> 
          <Route index element={<HomePage />} />
          
          {/* 数据库安全产品 */}
          <Route path="database/overview" element={<HomePage />} />
          <Route path="database/policy" element={<DatabasePolicyPage />} />
          <Route path="database/realtime" element={<RealTimeTaskPage />} />
          <Route path="database/batch" element={<BatchTaskPage />} />
          <Route path="database/asset" element={<DatabasePage />} />
          
          {/* API安全产品 */}
          <Route path="api/overview" element={<HomePage />} />
          <Route path="api/policy" element={<ApiPolicyPage />} />
          <Route path="api/realtime" element={<RealTimeTaskPage />} />
          <Route path="api/batch" element={<BatchTaskPage />} />
          <Route path="api/asset" element={<ApiPage />} />
          
          {/* 消息安全产品 */}
          <Route path="message/overview" element={<HomePage />} />
          <Route path="message/policy" element={<MessagePolicyPage />} />
          <Route path="message/realtime" element={<RealTimeTaskPage />} />
          <Route path="message/batch" element={<BatchTaskPage />} />
          <Route path="message/asset" element={<MessageQueuePage />} />
          
          {/* 兼容旧路由 */}
          <Route path="task/policy" element={<DatabasePolicyPage />} />
          <Route path="task/realtime" element={<RealTimeTaskPage />} />
          <Route path="task/batch" element={<BatchTaskPage />} />
          <Route path="asset/database" element={<DatabasePage />} />
          <Route path="asset/api" element={<ApiPage />} />
          <Route path="asset/mq" element={<MessageQueuePage />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;