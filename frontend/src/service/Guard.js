import React, { Component } from 'react';
import { useLocation, Navigate } from "react-router-dom";
import ApiService from './ApiService';

export const CustomerRoute = ({element: Component}) => {

    // 这是一个函数式组件，用于包装一个路由元素；
// element: Component 是 ES6 的解构重命名，其实是传入一个 React 元素。

    const location = useLocation();
    return ApiService.isAuthenticated() ? (
        Component
    ) : (
        <Navigate to="/login" replace state={{ from: location }} />
    );
    // ApiService.isAuthenticated() 会检查 localStorage 里是否有 token；
// 如果有，就渲染该组件；
// 否则，跳转到 /login，并传入当前访问路径 state.from = location（登录后可以跳回来）。
}

export const AdminRoute = ({element: Component}) => {
    const location = useLocation();
    // ApiService.isAdmin() 会读取 role，只有当它是 "ADMIN" 时才返回 true；
// 否则也会跳转到登录页面。
    return ApiService.isAdmin() ? (
        Component
    ) : (
        <Navigate to="/login" replace state={{ from: location }} />
    );
}