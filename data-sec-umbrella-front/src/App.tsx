import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout.tsx';
import HomePage from './pages/HomePage.tsx';
import DatabasePolicyPage from './pages/task/DatabasePolicyPage.tsx';
import ApiPolicyPage from './pages/task/ApiPolicyPage.tsx';
import MessagePolicyPage from './pages/task/MessagePolicyPage.tsx';
import LogPolicyPage from './pages/task/LogPolicyPage.tsx';
import RealTimeTaskPage from './pages/task/RealTimeTaskPage.tsx';
import BatchTaskPage from './pages/task/BatchTaskPage.tsx';
import DatabasePage from './pages/asset/DatabasePage.tsx';
import MessageQueuePage from './pages/asset/MessageQueuePage.tsx';
import LogPage from './pages/asset/LogPage.tsx';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainLayout />}> 
          <Route index element={<HomePage />} />
          <Route path="task/policy" element={<DatabasePolicyPage />} />
          <Route path="task/policy/api" element={<ApiPolicyPage />} />
          <Route path="task/policy/message" element={<MessagePolicyPage />} />
          <Route path="task/policy/log" element={<LogPolicyPage />} />
          <Route path="task/realtime" element={<RealTimeTaskPage />} />
          <Route path="task/batch" element={<BatchTaskPage />} />
          <Route path="asset/database" element={<DatabasePage />} />
          <Route path="asset/mq" element={<MessageQueuePage />} />
          <Route path="asset/log" element={<LogPage />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
