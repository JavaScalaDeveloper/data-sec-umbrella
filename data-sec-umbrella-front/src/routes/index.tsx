import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import Home from '../pages/Home';
import DatabaseSecurity from '../pages/DatabaseSecurity';
import ApiSecurity from '../pages/ApiSecurity';
import MqSecurity from '../pages/MqSecurity';
import AdminCenter from '../pages/AdminCenter';
import MySQLAsset from '../pages/data-asset/MySQLAsset';
import ClickhouseAsset from '../pages/data-asset/ClickhouseAsset';

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      {
        path: '',
        element: <Home />,
      },
      {
        path: 'database-security',
        element: <DatabaseSecurity />,
        children: [
          {
            path: '',
            element: <DatabaseSecurity />,
          },
          {
            path: 'overview',
            element: <DatabaseSecurity />,
          },
          {
            path: 'policy-management',
            element: <DatabaseSecurity />,
            children: [
              {
                path: '',
                element: <DatabaseSecurity />,
              },
              {
                path: 'mysql',
                element: <DatabaseSecurity />,
              },
              {
                path: 'clickhouse',
                element: <DatabaseSecurity />,
              },
            ],
          },
          {
            path: 'data-source',
            element: <DatabaseSecurity />,
          },
          {
            path: 'data-asset/mysql',
            element: <MySQLAsset />,
          },
          {
            path: 'data-asset/clickhouse',
            element: <ClickhouseAsset />,
          },
          {
            path: 'data-asset',
            element: <DatabaseSecurity />,
          },
          {
            path: 'task-management',
            element: <DatabaseSecurity />,
            children: [
              {
                path: '',
                element: <DatabaseSecurity />,
              },
              {
                path: 'realtime',
                element: <DatabaseSecurity />,
              },
              {
                path: 'batch',
                element: <DatabaseSecurity />,
              },
            ],
          },
          {
            path: 'configuration',
            element: <DatabaseSecurity />,
          },
        ],
      },
      {
        path: 'api-security',
        element: <ApiSecurity />,
        children: [
          {
            path: '',
            element: <ApiSecurity />,
          },
          {
            path: 'overview',
            element: <ApiSecurity />,
          },
          {
            path: 'policy-management',
            element: <ApiSecurity />,
          },
          {
            path: 'api-monitoring',
            element: <ApiSecurity />,
          },
          {
            path: 'api-analysis',
            element: <ApiSecurity />,
          },
        ],
      },
      {
        path: 'mq-security',
        element: <MqSecurity />,
        children: [
          {
            path: '',
            element: <MqSecurity />,
          },
          {
            path: 'overview',
            element: <MqSecurity />,
          },
          {
            path: 'policy-management',
            element: <MqSecurity />,
          },
          {
            path: 'mq-monitoring',
            element: <MqSecurity />,
          },
          {
            path: 'mq-analysis',
            element: <MqSecurity />,
          },
        ],
      },
      {
        path: 'admin-center',
        element: <AdminCenter />,
      },
    ],
  },
]);

export default router;