import React, { useState } from 'react';
import { Button, Card, Form, Input, message, Typography } from 'antd';
import { adminCenterApi, setAdminAuth } from '../services/api';

const { Title } = Typography;

const Login: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      const res = await adminCenterApi.login(values);
      if (res.code !== 200 || !res.data) {
        throw new Error(res.message || '登录失败');
      }
      setAdminAuth(res.data);
      message.success('登录成功');
      window.location.replace('/');
    } catch (e: any) {
      if (e?.errorFields) return;
      message.error(e?.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: 'calc(100vh - 64px)', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f7fa' }}>
      <Card style={{ width: 420 }}>
        <Title level={4}>系统登录</Title>
        <Form form={form} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Button type="primary" loading={loading} onClick={handleLogin} block>
            登录
          </Button>
        </Form>
      </Card>
    </div>
  );
};

export default Login;
