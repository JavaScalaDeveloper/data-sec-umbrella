import React from 'react';
import {Typography} from 'antd';

const {Title} = Typography;

const Home: React.FC = () => {
    return (<div style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        backgroundColor: '#f0f2f5'
    }}><img 
            src="/images/数据安全保护伞.jpg" 
            alt="数据安全保护伞" 
            style={{width: '200px', height: '200px', marginBottom: '20px'}}
        /><Title level={1} style={{color: '#1890ff'}}>数据安全保护伞</Title></div>);
};

export default Home;