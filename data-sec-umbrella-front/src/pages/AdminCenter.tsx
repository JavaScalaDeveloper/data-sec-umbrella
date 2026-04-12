import React, { useEffect, useState } from 'react';
import { Button, Form, Input, Layout, Menu, message, Modal, Popconfirm, Select, Space, Table, Tag, Typography } from 'antd';
import { TeamOutlined, UserOutlined } from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { adminCenterApi, setAdminAuth } from '../services/api';

const { Content, Sider } = Layout;
const { Title, Text, Paragraph } = Typography;

/** 内置角色说明（与账号表单可选角色一致） */
const BUILTIN_ROLES = [
    {
        roleCode: 'ADMIN',
        summary: '管理员',
        capability: '可调用写操作类接口；产品权限范围内管理配置（账号增删改需超级管理员）',
        products: 'DATABASE / API / MQ（按账号配置 productPermissions）',
    },
    {
        roleCode: 'OPERATOR',
        summary: '操作员',
        capability: '默认以只读查询为主，具体以接口权限注解为准',
        products: 'DATABASE / API / MQ（按账号配置 productPermissions）',
    },
];

const AdminCenter: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const pathTail = location.pathname.replace(/\/$/, '').split('/').pop();
    const activeSection = pathTail === 'role' ? 'role' : 'account';

    const [loggedIn, setLoggedIn] = useState(false);
    const [loginLoading, setLoginLoading] = useState(false);
    const [listLoading, setListLoading] = useState(false);
    const [records, setRecords] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [size, setSize] = useState(10);
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<any | null>(null);
    const [submitLoading, setSubmitLoading] = useState(false);
    const [passwordModalOpen, setPasswordModalOpen] = useState(false);
    const [passwordSubmitting, setPasswordSubmitting] = useState(false);
    const [loginForm] = Form.useForm();
    const [editForm] = Form.useForm();
    const [passwordForm] = Form.useForm();
    const [authInfo, setAuthInfo] = useState<any>(null);

    const fetchAccounts = async (page = current, pageSize = size) => {
        setListLoading(true);
        try {
            const res = await adminCenterApi.listAccounts({ current: page, size: pageSize });
            if (res.code !== 200) {
                throw new Error(res.message || '查询失败');
            }
            setRecords(res.data?.records || []);
            setTotal(res.data?.total || 0);
            setCurrent(page);
            setSize(pageSize);
        } catch (e: any) {
            message.error(e?.message || '查询账号失败');
        } finally {
            setListLoading(false);
        }
    };

    useEffect(() => {
        if (loggedIn && activeSection === 'account') {
            fetchAccounts(1, size);
        }
    }, [loggedIn, activeSection]);

    const handleLogin = async () => {
        try {
            const values = await loginForm.validateFields();
            setLoginLoading(true);
            const res = await adminCenterApi.login(values);
            if (res.code !== 200) {
                throw new Error(res.message || '登录失败');
            }
            setLoggedIn(true);
            setAuthInfo(res.data || null);
            setAdminAuth(res.data || null);
            message.success(`登录成功，当前角色：${res.data?.roleCode || '-'}`);
        } catch (e: any) {
            if (e?.errorFields) return;
            message.error(e?.message || '登录失败');
        } finally {
            setLoginLoading(false);
        }
    };

    const openCreate = () => {
        if (!authInfo?.superAdmin) {
            message.warning('仅超级管理员可新增账号');
            return;
        }
        setEditing(null);
        editForm.resetFields();
        editForm.setFieldsValue({ roleCode: 'OPERATOR', status: 1, productPermissions: ['DATABASE'] });
        setModalOpen(true);
    };

    const openEdit = (row: any) => {
        if (!authInfo?.superAdmin) {
            message.warning('仅超级管理员可修改账号');
            return;
        }
        setEditing(row);
        editForm.setFieldsValue({ ...row, productPermissions: row.productPermissions || [] });
        setModalOpen(true);
    };

    const openResetPassword = (row: any) => {
        if (!authInfo?.superAdmin) {
            message.warning('仅超级管理员可重置密码');
            return;
        }
        setEditing(row);
        passwordForm.resetFields();
        setPasswordModalOpen(true);
    };

    const submitEdit = async () => {
        try {
            const values = await editForm.validateFields();
            setSubmitLoading(true);
            if (editing?.id) {
                const payload: any = { id: editing.id, username: values.username, roleCode: values.roleCode, status: values.status };
                payload.productPermissions = values.productPermissions || [];
                const res = await adminCenterApi.updateAccount(payload);
                if (res.code !== 200) throw new Error(res.message || '更新失败');
                message.success('更新成功');
            } else {
                const res = await adminCenterApi.createAccount(values);
                if (res.code !== 200) throw new Error(res.message || '创建失败');
                message.success('创建成功');
            }
            setModalOpen(false);
            fetchAccounts(current, size);
        } catch (e: any) {
            if (e?.errorFields) return;
            message.error(e?.message || '提交失败');
        } finally {
            setSubmitLoading(false);
        }
    };

    const submitResetPassword = async () => {
        try {
            const values = await passwordForm.validateFields();
            setPasswordSubmitting(true);
            const res = await adminCenterApi.resetPassword({ id: editing.id, password: values.password });
            if (res.code !== 200) throw new Error(res.message || '重置失败');
            message.success('密码重置成功');
            setPasswordModalOpen(false);
        } catch (e: any) {
            if (e?.errorFields) return;
            message.error(e?.message || '重置失败');
        } finally {
            setPasswordSubmitting(false);
        }
    };

    const deleteAccount = async (id: number) => {
        const res = await adminCenterApi.deleteAccount(id);
        if (res.code !== 200) {
            message.error(res.message || '删除失败');
            return;
        }
        message.success('删除成功');
        fetchAccounts(current, size);
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider width={220} theme="light">
                <Menu
                    mode="inline"
                    selectedKeys={[activeSection]}
                    onClick={(e) => navigate(`/admin-center/${e.key}`)}
                    items={[
                        { key: 'account', icon: <UserOutlined />, label: '账号管理' },
                        { key: 'role', icon: <TeamOutlined />, label: '角色管理' },
                    ]}
                />
            </Sider>
            <Layout>
                <Content style={{ padding: 24 }}>
                    <Title level={3}>管理中心</Title>
                    {!loggedIn ? (
                        <div style={{ maxWidth: 420, marginTop: 24 }}>
                            <Title level={5}>超级管理员登录</Title>
                            <Form form={loginForm} layout="vertical">
                                <Form.Item name="username" label="账号" rules={[{ required: true, message: '请输入账号' }]}>
                                    <Input placeholder="请输入账号" />
                                </Form.Item>
                                <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
                                    <Input.Password placeholder="请输入密码" />
                                </Form.Item>
                                <Button type="primary" loading={loginLoading} onClick={handleLogin}>
                                    登录
                                </Button>
                            </Form>
                        </div>
                    ) : activeSection === 'account' ? (
                        <Space direction="vertical" style={{ width: '100%' }} size={12}>
                            <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                                <Text>可配置产品权限：DATABASE/API/MQ；OPERATOR 仅可调用查询接口</Text>
                                <Button type="primary" disabled={!authInfo?.superAdmin} onClick={openCreate}>新建账号</Button>
                            </Space>
                            <Table
                                rowKey="id"
                                loading={listLoading}
                                dataSource={records}
                                columns={[
                                    { title: 'ID', dataIndex: 'id', width: 80 },
                                    { title: '用户名', dataIndex: 'username' },
                                    { title: '角色', dataIndex: 'roleCode', render: (v: string) => <Tag color="blue">{v || '-'}</Tag> },
                                    {
                                        title: '产品权限',
                                        dataIndex: 'productPermissions',
                                        render: (v: string[]) => (
                                            <Space>
                                                {(v || []).map((p) => <Tag key={p} color="purple">{p}</Tag>)}
                                            </Space>
                                        ),
                                    },
                                    {
                                        title: '状态',
                                        dataIndex: 'status',
                                        render: (v: number) => (v === 1 ? <Tag color="green">启用</Tag> : <Tag color="default">禁用</Tag>),
                                    },
                                    { title: '创建人', dataIndex: 'creator' },
                                    { title: '修改人', dataIndex: 'modifier' },
                                    { title: '创建时间', dataIndex: 'createTime' },
                                    { title: '修改时间', dataIndex: 'modifyTime' },
                                    {
                                        title: '操作',
                                        key: 'actions',
                                        render: (_: any, row: any) => (
                                            <Space>
                                                <Button type="link" disabled={!authInfo?.superAdmin} onClick={() => openEdit(row)}>编辑</Button>
                                                <Button type="link" disabled={!authInfo?.superAdmin} onClick={() => openResetPassword(row)}>重置密码</Button>
                                                <Popconfirm title="确认删除该账号？" onConfirm={() => deleteAccount(row.id)} disabled={!authInfo?.superAdmin}>
                                                    <Button type="link" danger>删除</Button>
                                                </Popconfirm>
                                            </Space>
                                        ),
                                    },
                                ]}
                                pagination={{
                                    current,
                                    pageSize: size,
                                    total,
                                    showSizeChanger: true,
                                    showTotal: (t) => `共 ${t} 条`,
                                    onChange: (p, ps) => fetchAccounts(p, ps),
                                }}
                            />
                        </Space>
                    ) : (
                        <div style={{ marginTop: 8 }}>
                            <Title level={5}>角色管理</Title>
                            <Paragraph type="secondary" style={{ marginBottom: 16 }}>
                                当前系统内置两类业务角色（与新建账号时可选角色一致）。权限矩阵与动态角色后续可在此扩展。
                            </Paragraph>
                            <Table
                                rowKey="roleCode"
                                pagination={false}
                                dataSource={BUILTIN_ROLES}
                                columns={[
                                    {
                                        title: '角色代码',
                                        dataIndex: 'roleCode',
                                        width: 140,
                                        render: (v: string) => <Tag color="blue">{v}</Tag>,
                                    },
                                    { title: '名称', dataIndex: 'summary', width: 120 },
                                    { title: '能力说明', dataIndex: 'capability' },
                                    { title: '产品范围', dataIndex: 'products', width: 280 },
                                ]}
                            />
                        </div>
                    )}
                </Content>
            </Layout>

            <Modal
                title={editing ? '编辑账号' : '新建账号'}
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={submitEdit}
                confirmLoading={submitLoading}
            >
                <Form form={editForm} layout="vertical">
                    <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
                        <Input />
                    </Form.Item>
                    {!editing && (
                        <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
                            <Input.Password />
                        </Form.Item>
                    )}
                    <Form.Item name="roleCode" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
                        <Select options={[{ value: 'ADMIN', label: 'ADMIN' }, { value: 'OPERATOR', label: 'OPERATOR' }]} />
                    </Form.Item>
                    <Form.Item name="productPermissions" label="产品权限" rules={[{ required: true, message: '请选择产品权限' }]}>
                        <Select
                            mode="multiple"
                            options={[
                                { value: 'DATABASE', label: '数据安全保护伞' },
                                { value: 'API', label: 'API安全保护伞' },
                                { value: 'MQ', label: 'MQ安全保护伞' },
                            ]}
                        />
                    </Form.Item>
                    <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
                        <Select options={[{ value: 1, label: '启用' }, { value: 0, label: '禁用' }]} />
                    </Form.Item>
                </Form>
            </Modal>
            <Modal
                title="重置密码"
                open={passwordModalOpen}
                onCancel={() => setPasswordModalOpen(false)}
                onOk={submitResetPassword}
                confirmLoading={passwordSubmitting}
            >
                <Form form={passwordForm} layout="vertical">
                    <Form.Item name="password" label="新密码" rules={[{ required: true, message: '请输入新密码' }]}>
                        <Input.Password />
                    </Form.Item>
                </Form>
            </Modal>
        </Layout>
    );
};

export default AdminCenter;
