// API配置文件
const API_CONFIG = {
  // 基础URL
  BASE_URL: 'http://localhost:8080',
  
  // API端点
  ENDPOINTS: {
    // 数据库策略相关
    DATABASE_POLICY: {
      LIST: '/api/database-policy/list',
      CREATE: '/api/database-policy/create',
      UPDATE: '/api/database-policy/update',
      DELETE: '/api/database-policy/delete'
    },
    
    // API策略相关
    API_POLICY: {
      LIST: '/api/api-policy/list',
      CREATE: '/api/api-policy/create',
      UPDATE: '/api/api-policy/update',
      DELETE: '/api/api-policy/delete'
    },
    
    // 消息策略相关
    MESSAGE_POLICY: {
      LIST: '/api/message-policy/list',
      CREATE: '/api/message-policy/create',
      UPDATE: '/api/message-policy/update',
      DELETE: '/api/message-policy/delete'
    },
    
    // 日志策略相关
    LOG_POLICY: {
      LIST: '/api/log-policy/list',
      CREATE: '/api/log-policy/create',
      UPDATE: '/api/log-policy/update',
      DELETE: '/api/log-policy/delete'
    }
  }
};

export default API_CONFIG;