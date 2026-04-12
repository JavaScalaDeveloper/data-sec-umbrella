import React from 'react';
import {Typography} from 'antd';

const {Text, Link} = Typography;

/** 全站页脚（首页由 Home 自带，不在此渲染） */
const SiteFooter: React.FC = () => (
    <footer
        style={{
            padding: '16px 16px 20px',
            textAlign: 'center',
            borderTop: '1px solid rgba(0, 0, 0, 0.06)',
            background: '#fff',
            flexShrink: 0,
        }}
    >
        <div style={{marginBottom: 6}}>
            <Link href="https://arelore.com" target="_blank" rel="noopener noreferrer">
                arelore.com 官网
            </Link>
        </div>
        <Text type="secondary" style={{fontSize: 13}}>
            备案号：鄂ICP备2026013952号-1
        </Text>
    </footer>
);

export default SiteFooter;
