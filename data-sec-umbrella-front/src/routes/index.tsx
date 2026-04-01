import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import Home from '../pages/Home';
import DatabaseSecurity from '../pages/DatabaseSecurity';
import ApiSecurity from '../pages/ApiSecurity';
import MqSecurity from '../pages/MqSecurity';
import AdminCenter from '../pages/AdminCenter';

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
      },
      {
        path: 'api-security',
        element: <ApiSecurity />,
      },
      {
        path: 'mq-security',
        element: <MqSecurity />,
      },
      {
        path: 'admin-center',
        element: <AdminCenter />,
      },
    ],
  },
]);

export default router;