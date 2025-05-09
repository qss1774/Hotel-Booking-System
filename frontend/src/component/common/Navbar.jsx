import React from "react";
import { useNavigate, NavLink } from "react-router-dom";
import ApiService from "../../service/ApiService";

function Navbar() {

    // 这些方法从 localStorage 获取 token 和 role，判断是否登录、是否是客户或管理员。
    const isAuthenticated = ApiService.isAuthenticated();
    const isCustomer = ApiService.isCustomer();
    const isAdmin = ApiService.isAdmin();

    const navigate = useNavigate();

    // 是在 React Router v6 中使用的一个 路由导航 hook，它的作用是：
// ? 在 JavaScript 中手动跳转到另一个页面（路由重定向）。
    const handleLogout = () => {
        const isLogout = window.confirm("Are you sure you want to logout?");
        if (isLogout) {
            ApiService.logout();
            // 用户做某个操作后自动跳转（比如登录成功、退出、下单完成等）
            navigate("/home");
            // <NavLink to="/home">	用户点击链接按钮跳转
        }
    };

    return ( 
        <nav className="navbar">
            {/* 这部分无论是否登录都会显示。 */}
            <div className="navbar-brand">
                <NavLink to="/home"> IVE Hotel </NavLink>
            </div>
            <ul className="navbar-ul">
                <li>
                    <NavLink to={"/home"} activeClassname="active">Home</NavLink>
                </li>
                <li>
                    <NavLink to={"/rooms"} activeClassname="active">Rooms</NavLink>
                </li>
                <li>
                    <NavLink to={"/find-booking"} activeClassname="active">Find My Bookings</NavLink>
                </li>


                
                {isCustomer &&<li>
                    <NavLink to={"/profile"} activeClassname="active">Profile</NavLink>
                </li>}

                {isAdmin && <li>
                    <NavLink to={"/admin"} activeClassname="active">Admin</NavLink>
                </li>}

                {!isAuthenticated && <li>
                    <NavLink to={"/login"} activeClassname="active">Login</NavLink>
                </li>}


                {!isAuthenticated && <li>
                    <NavLink to={"/register"} activeClassname="active">Register</NavLink>
                </li>}

                {isAuthenticated && <li onClick={handleLogout}>Logout</li>}

            </ul>
        </nav>
    );
}

export default Navbar;

// 如果你希望这个组件能被其他文件引入使用，都需要加上 export default。