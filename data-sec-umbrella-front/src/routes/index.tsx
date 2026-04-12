import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from '../App';
import Home from '../pages/Home';
import DatabaseSecurity from '../pages/DatabaseSecurity';
import ApiSecurity from '../pages/ApiSecurity';
import MqSecurity from '../pages/MqSecurity';
import AdminCenter from '../pages/AdminCenter';
import Login from '../pages/Login';
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
            path: 'overview/mysql',
            element: <DatabaseSecurity />,
          },
          {
            path: 'overview/clickhouse',
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
            path: 'data-asset/mysql',
            element: <MySQLAsset />,
          },
          {
            path: 'data-asset/mysql/instance',
            element: <MySQLAsset />,
          },
          {
            path: 'data-asset/mysql/database',
            element: <MySQLAsset />,
          },
          {
            path: 'data-asset/mysql/table',
            element: <MySQLAsset />,
          },
          {
            path: 'data-asset/clickhouse',
            element: <ClickhouseAsset />,
          },
          {
            path: 'data-asset/clickhouse/instance',
            element: <ClickhouseAsset />,
          },
          {
            path: 'data-asset/clickhouse/database',
            element: <ClickhouseAsset />,
          },
          {
            path: 'data-asset/clickhouse/table',
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
                children: [
                  {
                    path: '',
                    element: <DatabaseSecurity />,
                  },
                  {
                    path: 'mysql',
                    element: <DatabaseSecurity />,
                    children: [
                      {
                        path: '',
                        element: <DatabaseSecurity />,
                      },
                      {
                        path: 'config',
                        element: <DatabaseSecurity />,
                      },
                      {
                        path: 'instances',
                        element: <DatabaseSecurity />,
                      },
                    ],
                  },
                  {
                    path: 'clickhouse',
                    element: <DatabaseSecurity />,
                    children: [
                      {
                        path: '',
                        element: <DatabaseSecurity />,
                      },
                      {
                        path: 'config',
                        element: <DatabaseSecurity />,
                      },
                      {
                        path: 'instances',
                        element: <DatabaseSecurity />,
                      },
                    ],
                  },
                ],
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
        element: <Navigate to="/admin-center/account" replace />,
      },
      {
        path: 'admin-center/account',
        element: <AdminCenter />,
      },
      {
        path: 'admin-center/role',
        element: <AdminCenter />,
      },
      {
        path: 'login',
        element: <Login />,
      },
    ],
  },
]);

export default router;