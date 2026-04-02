// API服务封装

// 基础URL
const BASE_URL = 'http://localhost:8080';

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