import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout.tsx';
import HomePage from './pages/HomePage.tsx';
import DatabasePolicyPage from './pages/task/policy/DatabasePolicyPage.tsx';
import ApiPolicyPage from './pages/task/policy/ApiPolicyPage.tsx';
import MessagePolicyPage from './pages/task/policy/MessagePolicyPage.tsx';
import LogPolicyPage from './pages/task/policy/LogPolicyPage.tsx';
import RealTimeTaskPage from './pages/task/RealTimeTaskPage.tsx';
import BatchTaskPage from './pages/task/BatchTaskPage.tsx';
import DatabasePage from './pages/asset/database/DatabasePage.tsx';
import ApiPage from './pages/asset/api/ApiPage.tsx';
import MessageQueuePage from './pages/asset/message/MessageQueuePage.tsx';
import LogPage from './pages/asset/log/LogPage.tsx';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainLayout />}> 
          <Route index element={<HomePage />} />
          <Route path="task/policy" element={<DatabasePolicyPage />} />
          <Route path="task/policy/database" element={<DatabasePolicyPage />} />
          <Route path="task/policy/api" element={<ApiPolicyPage />} />
          <Route path="task/policy/message" element={<MessagePolicyPage />} />
          <Route path="task/policy/log" element={<LogPolicyPage />} />
          <Route path="task/realtime" element={<RealTimeTaskPage />} />
          <Route path="task/batch" element={<BatchTaskPage />} />
          <Route path="asset/database" element={<DatabasePage />} />
          <Route path="asset/database/instance" element={<DatabasePage />} />
          <Route path="asset/database/database" element={<DatabasePage />} />
          <Route path="asset/database/table" element={<DatabasePage />} />
          <Route path="asset/api" element={<ApiPage />} />
          <Route path="asset/api/domain" element={<ApiPage />} />
          <Route path="asset/api/api" element={<ApiPage />} />
          <Route path="asset/mq" element={<MessageQueuePage />} />
          <Route path="asset/mq/cluster" element={<MessageQueuePage />} />
          <Route path="asset/mq/topic" element={<MessageQueuePage />} />
          <Route path="asset/log" element={<LogPage />} />
        </Route>
        
        {/* 支持环境路径的路由 */}
        <Route path="/env/:env" element={<MainLayout />}> 
          <Route index element={<HomePage />} />
          <Route path="task/policy" element={<DatabasePolicyPage />} />
          <Route path="task/policy/database" element={<DatabasePolicyPage />} />
          <Route path="task/policy/api" element={<ApiPolicyPage />} />
          <Route path="task/policy/message" element={<MessagePolicyPage />} />
          <Route path="task/policy/log" element={<LogPolicyPage />} />
          <Route path="task/realtime" element={<RealTimeTaskPage />} />
          <Route path="task/batch" element={<BatchTaskPage />} />
          <Route path="asset/database" element={<DatabasePage />} />
          <Route path="asset/database/instance" element={<DatabasePage />} />
          <Route path="asset/database/database" element={<DatabasePage />} />
          <Route path="asset/database/table" element={<DatabasePage />} />
          <Route path="asset/api" element={<ApiPage />} />
          <Route path="asset/api/domain" element={<ApiPage />} />
          <Route path="asset/api/api" element={<ApiPage />} />
          <Route path="asset/mq" element={<MessageQueuePage />} />
          <Route path="asset/mq/cluster" element={<MessageQueuePage />} />
          <Route path="asset/mq/topic" element={<MessageQueuePage />} />
          <Route path="asset/log" element={<LogPage />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;