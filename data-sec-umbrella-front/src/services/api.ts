// API服务封装

import JSEncrypt from 'jsencrypt';

// 基础URL
const BASE_URL = 'http://localhost:8080';

// RSA公钥，从后端获取
let rsaPublicKey: string | null = null;

/**
 * 获取RSA公钥
 */
async function getRSAPublicKey(): Promise<string> {
    if (rsaPublicKey) {
        return rsaPublicKey;
    }

    try {
        const response = await fetch(`${BASE_URL}/api/data-source/get-public-key`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({}),
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const result = await response.json();
        if (result.code === 200 && result.data) {
            rsaPublicKey = result.data;
            console.log('成功获取公钥，长度:', rsaPublicKey?.length);
            return rsaPublicKey || '';
        } else {
            throw new Error(result.message || '获取公钥失败');
        }
    } catch (error) {
        console.error('获取RSA公钥失败:', error);
        throw error;
    }
}

// 请求方法
async function request<T>(url: string, options: RequestInit): Promise<T> {
    const response = await fetch(`${BASE_URL}${url}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
}

// 数据库策略API
export const databasePolicyApi = {
    // 获取分页数据
    getPage: async (params: any) => {
        return request<{
            code: number;
            message: string;
            data: {
                records: any[];
                total: number;
                current: number;
                size: number;
            };
        }>('/api/database-policy/list', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },

    // 根据ID获取策略
    getById: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: any;
        }>('/api/database-policy/getById', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },

    // 创建策略
    create: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: number;
        }>('/api/database-policy/create', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    // 更新策略
    update: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/database-policy/update', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    // 删除策略
    delete: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/database-policy/delete', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },

    // 测试规则
    testRules: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: any;
        }>('/api/database-policy/test-rules', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },
};

// RSA加密工具函数
const encryptPassword = async (password: string): Promise<string> => {
    try {
        const publicKey = await getRSAPublicKey();
        console.log('获取到的公钥:', publicKey);

        const encrypt = new JSEncrypt();
        encrypt.setPublicKey(publicKey);

        const encrypted = encrypt.encrypt(password);
        if (!encrypted) {
            console.error('RSA加密返回空值，公钥可能无效');
            throw new Error('RSA加密失败');
        }

        console.log('加密成功，加密结果长度:', encrypted.length);
        return encrypted;
    } catch (error) {
        console.error('密码加密失败:', error);
        throw new Error(`密码加密失败: ${error instanceof Error ? error.message : '未知错误'}`);
    }
};

// 数据源API
export const dataSourceApi = {
    // 获取分页数据
    getPage: async (params: any) => {
        return request<{
            code: number;
            message: string;
            data: {
                records: any[];
                total: number;
                current: number;
                size: number;
            };
        }>('/api/data-source/list', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },

    // 根据ID获取数据源
    getById: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: any;
        }>('/api/data-source/get-by-id', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },

    // 创建数据源
    create: async (data: any) => {
        const encryptedPassword = await encryptPassword(data.password);
        const encryptedData = {
            ...data,
            password: encryptedPassword
        };
        return request<{
            code: number;
            message: string;
            data: number;
        }>('/api/data-source/create', {
            method: 'POST',
            body: JSON.stringify(encryptedData),
        });
    },

    // 更新数据源
    update: async (data: any) => {
        const encryptedPassword = await encryptPassword(data.password);
        const encryptedData = {
            ...data,
            password: encryptedPassword
        };
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-source/update', {
            method: 'POST',
            body: JSON.stringify(encryptedData),
        });
    },

    // 删除数据源
    delete: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-source/delete', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },

    // 测试连接
    testConnection: async (data: any) => {
        const encryptedPassword = await encryptPassword(data.password);
        const encryptedData = {
            ...data,
            password: encryptedPassword
        };
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-source/test-connection', {
            method: 'POST',
            body: JSON.stringify(encryptedData),
        });
    },
};

// MySQL数据库信息API
export const mysqlDatabaseApi = {
    // 获取分页数据
    getPage: async (params: any) => {
        return request<{
            code: number;
            message: string;
            data: {
                records: any[];
                total: number;
                current: number;
                size: number;
            };
        }>('/api/data-asset/mysql/database/list', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },

    // 根据ID获取数据库信息
    getById: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: any;
        }>('/api/data-asset/mysql/database/get-by-id', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },

    // 创建数据库信息
    create: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: number;
        }>('/api/data-asset/mysql/database/create', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    // 更新数据库信息
    update: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/database/update', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    // 删除数据库信息
    delete: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/database/delete', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },
};

// MySQL表信息API
export const mysqlTableApi = {
    // 获取分页数据
    getPage: async (params: any) => {
        return request<{
            code: number;
            message: string;
            data: {
                records: any[];
                total: number;
                current: number;
                size: number;
            };
        }>('/api/data-asset/mysql/table/list', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },

    // 根据ID获取表信息
    getById: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: any;
        }>('/api/data-asset/mysql/table/get-by-id', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },

    // 创建表信息
    create: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: number;
        }>('/api/data-asset/mysql/table/create', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    // 更新表信息
    update: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/table/update', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    // 删除表信息
    delete: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/table/delete', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },
};

// MySQL资产扫描API
export const mysqlAssetScanApi = {
    // 手动触发MySQL资产扫描
    scan: async () => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/scan', {
            method: 'POST',
            body: JSON.stringify({}),
        });
    },

    // 手动触发数据库扫描
    scanDatabases: async () => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/scan-databases', {
            method: 'POST',
            body: JSON.stringify({}),
        });
    },

    // 手动触发表扫描
    scanTables: async () => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/scan-tables', {
            method: 'POST',
            body: JSON.stringify({}),
        });
    },
};