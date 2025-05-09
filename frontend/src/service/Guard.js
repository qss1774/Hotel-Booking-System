import React, { Component } from 'react';
import { useLocation, Navigate } from "react-router-dom";
import ApiService from './ApiService';

export const CustomerRoute = ({element: Component}) => {

    // ����һ������ʽ��������ڰ�װһ��·��Ԫ�أ�
// element: Component �� ES6 �Ľ⹹����������ʵ�Ǵ���һ�� React Ԫ�ء�

    const location = useLocation();
    return ApiService.isAuthenticated() ? (
        Component
    ) : (
        <Navigate to="/login" replace state={{ from: location }} />
    );
    // ApiService.isAuthenticated() ���� localStorage ���Ƿ��� token��
// ����У�����Ⱦ�������
// ������ת�� /login�������뵱ǰ����·�� state.from = location����¼���������������
}

export const AdminRoute = ({element: Component}) => {
    const location = useLocation();
    // ApiService.isAdmin() ���ȡ role��ֻ�е����� "ADMIN" ʱ�ŷ��� true��
// ����Ҳ����ת����¼ҳ�档
    return ApiService.isAdmin() ? (
        Component
    ) : (
        <Navigate to="/login" replace state={{ from: location }} />
    );
}