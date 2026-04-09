// API服务封装

import JSEncrypt from 'jsencrypt';

// 基础URL
const BASE_URL = 'http://localhost:8080';

export type AdminAuthInfo = {
    username: string;
    roleCode: string;
    superAdmin: boolean;
    productPermissions: string[];
};

export function getAdminAuth(): AdminAuthInfo | null {
    const raw = localStorage.getItem('adminCenterAuth');
    if (!raw) return null;
    try {
        return JSON.parse(raw) as AdminAuthInfo;
    } catch {
        return null;
    }
}

export function setAdminAuth(auth: AdminAuthInfo | null) {
    if (!auth) {
        localStorage.removeItem('adminCenterAuth');
        return;
    }
    localStorage.setItem('adminCenterAuth', JSON.stringify(auth));
}

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
    const auth = getAdminAuth();
    const publicApi = ['/api/admin-center/account/login', '/api/admin-center/account/current'];
    if (!auth && !publicApi.includes(url)) {
        if (window.location.pathname !== '/login') {
            window.location.replace('/login');
        }
        throw new Error('请先登录');
    }
    const response = await fetch(`${BASE_URL}${url}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(auth ? {
                'X-Admin-Username': auth.username || '',
                'X-Admin-Role': auth.roleCode || '',
                'X-Super-Admin': String(!!auth.superAdmin),
                'X-Product-Permissions': Array.isArray(auth.productPermissions) ? auth.productPermissions.join(',') : '',
            } : {}),
            ...options.headers,
        },
    });

    if (!response.ok) {
        if (response.status === 401 && window.location.pathname !== '/login') {
            setAdminAuth(null);
            window.location.replace('/login');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    if (data && typeof data.code === 'number' && data.code === 401) {
        if (window.location.pathname !== '/login') {
            setAdminAuth(null);
            window.location.replace('/login');
        }
        throw new Error(data.message || '请先登录');
    }
    return data;
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
    // 流式测试AI规则
    testAiRulesStream: async (
        data: any,
        onChunk: (text: string) => void,
        onDone: (result: { aiPassed: boolean; aiDetail: string }) => void,
        onError?: (err: string) => void,
    ) => {
        const auth = getAdminAuth();
        if (!auth) {
            throw new Error('请先登录');
        }
        const response = await fetch(`${BASE_URL}/api/database-policy/test-ai-rules-stream`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Admin-Username': auth.username || '',
                'X-Admin-Role': auth.roleCode || '',
                'X-Super-Admin': String(!!auth.superAdmin),
                'X-Product-Permissions': (auth.productPermissions || []).join(','),
            },
            body: JSON.stringify(data),
        });
        if (!response.ok || !response.body) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';
        let currentEvent = '';
        while (true) {
            const { done, value } = await reader.read();
            if (done) {
                break;
            }
            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';
            for (const raw of lines) {
                const line = raw.trim();
                if (!line) {
                    currentEvent = '';
                    continue;
                }
                if (line.startsWith('event:')) {
                    currentEvent = line.slice(6).trim();
                    continue;
                }
                if (line.startsWith('data:')) {
                    const payload = line.slice(5).trim();
                    if (currentEvent === 'chunk') {
                        onChunk(payload);
                    } else if (currentEvent === 'done') {
                        try {
                            onDone(JSON.parse(payload));
                        } catch {
                            onDone({ aiPassed: false, aiDetail: payload });
                        }
                    } else if (currentEvent === 'error') {
                        onError?.(payload);
                    }
                }
            }
        }
    },
};

/** 编辑数据源时表示「未修改密码」的表单占位，不得当作真实密码加密上传 */
export const DATA_SOURCE_PASSWORD_UNCHANGED_SENTINEL = '***';

export function isDataSourcePasswordUnchanged(raw: unknown): boolean {
    if (raw == null) {
        return true;
    }
    const s = String(raw).trim();
    return s === '' || s === DATA_SOURCE_PASSWORD_UNCHANGED_SENTINEL;
}

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

    // 更新数据源（编辑时密码留空或占位 *** 则不传，后端沿用库中密文）
    update: async (data: any) => {
        const payload = {...data};
        const raw = payload.password;
        if (!isDataSourcePasswordUnchanged(raw)) {
            payload.password = await encryptPassword(String(raw));
        } else {
            delete payload.password;
        }
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-source/update', {
            method: 'POST',
            body: JSON.stringify(payload),
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

    // 测试连接（编辑且未输入新密码时不加密、不传 password，避免请求 get-public-key）
    testConnection: async (data: any) => {
        const payload = {...data};
        const raw = payload.password;
        if (!isDataSourcePasswordUnchanged(raw)) {
            payload.password = await encryptPassword(String(raw));
        } else {
            delete payload.password;
        }
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-source/test-connection', {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },
};

/** MySQL 数据资产离线扫描任务（批量任务） */
export const mysqlOfflineScanJobApi = {
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
        }>('/api/db-asset/mysql/offline-scan-job/list', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
    getById: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: any;
        }>('/api/db-asset/mysql/offline-scan-job/getById', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },
    create: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: number;
        }>('/api/db-asset/mysql/offline-scan-job/create', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },
    update: async (data: any) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/db-asset/mysql/offline-scan-job/update', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },
    delete: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/db-asset/mysql/offline-scan-job/delete', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },
    execute: async (id: number) => {
        return request<{
            code: number;
            message: string;
            data: number;
        }>('/api/db-asset/mysql/offline-scan-job/execute', {
            method: 'POST',
            body: JSON.stringify({id}),
        });
    },
};

/** MySQL 离线扫描任务实例 */
export const mysqlOfflineScanJobInstanceApi = {
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
        }>('/api/db-asset/mysql/offline-scan-job/instance/list', {
            method: 'POST',
            body: JSON.stringify(params),
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

    // 仅更新数据库人工打标（IGNORE/FALSE_POSITIVE/SENSITIVE；清空表示恢复默认）
    updateManualReview: async (params: { id: number; manualReview?: string | null }) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/database/update-manual-review', {
            method: 'POST',
            body: JSON.stringify({
                id: params.id,
                manualReview: params.manualReview === undefined || params.manualReview === null ? '' : params.manualReview,
            }),
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

    // 仅更新表人工打标（IGNORE/FALSE_POSITIVE/SENSITIVE；清空表示恢复默认）
    updateManualReview: async (params: { id: number; manualReview?: string | null }) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/data-asset/mysql/table/update-manual-review', {
            method: 'POST',
            body: JSON.stringify({
                id: params.id,
                manualReview: params.manualReview === undefined || params.manualReview === null ? '' : params.manualReview,
            }),
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

// 数据库概览指标API（基于后端快照表）
export const databaseOverviewApi = {
    getMetrics: async (params: { databaseType: string; metricPeriod?: string; metricTime?: string }) => {
        return request<{
            code: number;
            message: string;
            data: {
                databaseType: string;
                metricPeriod: string;
                metricTime: string;
                metrics: Record<string, string>;
            };
        }>('/api/overview/database/metrics', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
    refreshMetrics: async (params: { databaseType: string; metricTime?: string }) => {
        return request<{
            code: number;
            message: string;
            data: boolean;
        }>('/api/overview/database/refresh', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
};

// 管理中心-账号管理API
export const adminCenterApi = {
    login: async (params: { username: string; password: string }) => {
        return request<{
            code: number;
            message: string;
            data: { success: boolean; username: string; roleCode: string; superAdmin: boolean; productPermissions: string[] };
        }>('/api/admin-center/account/login', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
    current: async () => {
        return request<{
            code: number;
            message: string;
            data: { success: boolean; username: string; roleCode: string; superAdmin: boolean; productPermissions: string[] };
        }>('/api/admin-center/account/current', {
            method: 'POST',
            body: JSON.stringify({}),
        });
    },
    listAccounts: async (params: any) => {
        return request<{
            code: number;
            message: string;
            data: { records: any[]; total: number; current: number; size: number };
        }>('/api/admin-center/account/list', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
    createAccount: async (params: any) => {
        return request<{ code: number; message: string; data: number }>('/api/admin-center/account/create', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
    updateAccount: async (params: any) => {
        return request<{ code: number; message: string; data: boolean }>('/api/admin-center/account/update', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
    resetPassword: async (params: { id: number; password: string }) => {
        return request<{ code: number; message: string; data: boolean }>('/api/admin-center/account/reset-password', {
            method: 'POST',
            body: JSON.stringify(params),
        });
    },
    deleteAccount: async (id: number) => {
        return request<{ code: number; message: string; data: boolean }>('/api/admin-center/account/delete', {
            method: 'POST',
            body: JSON.stringify({ id }),
        });
    },
};