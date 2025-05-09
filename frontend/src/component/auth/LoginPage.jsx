import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import ApiService from "../../service/ApiService";

const LoginPage = () => {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

//   这行使用了 React 的 useState Hook。

// formData 是当前表单的数据（包含 email 和 password 两个字段）

// setFormData 是更新 formData 的函数

// 初始化值是空字符串，表示表单一开始是空的。

  const [error, setError] = useState("");
// 这行也是使用 useState，用来保存错误信息（比如登录失败提示），初始值为空字符串。

  const navigate = useNavigate();
  const { state } = useLocation();

//   这是另一个 React Router 的 hook，用来获取当前页面路由携带的状态。

// 比如你从某个受保护页面跳过来，state 里可能存着跳转前的地址，用来跳回去。



  const redirectPath = state?.from?.pathname || "/home";
//   这是用可选链运算符（?.）来安全地取值。如果state 存在，且 state.from.pathname 存在，就用这个路径

// 否则默认跳转到 /home

  //handle input change
  const handleChange = (e) => {
    const { name, value } = e.target;
    // 这是结构赋值，从触发事件的 HTML 元素中提取：

// name：input 框的 name 属性

// value：input 框当前输入的值
    setFormData((prev) => ({ ...prev, [name]: value }));

    // 这一行是关键，它使用了函数式更新 formData 状态。

// prev 是旧的表单数据

// ...prev 展开旧数据

// [name]: value 用变量作为 key 动态更新字段（如 email 或 password）
  };

/**
 * 
     ↓
handleSubmit 函数触发
     ↓
检查 email 和 password 是否为空
     ↓
调用后端登录接口 loginUser()
     ↓
成功 → 保存 token 和 role → 跳转页面
失败 → 显示错误信息
} e 
 * @returns 
 */

//   当用户点击“登录”按钮时，这个函数会：

// 阻止默认表单行为（避免刷新页面）

// 检查是否填写了 email 和密码

// 调用后端登录接口

// 根据返回的 token 和 role 做保存并跳转

// 捕获错误并显示提示信息
  const handleSubmit = async (e) =>{
    e.preventDefault()
    const {email, password} = formData;

    if (!email || !password) {
        setError("Please fill all input")
        setTimeout(() => setError(""), 5000);
        return;
    }

    try {
        const {status, token, role} = await ApiService.loginUser(formData);
        if (status === 200) {
            ApiService.saveToken(token)
            ApiService.saveRole(role)
            navigate(redirectPath, {replace: true})
        }
        
    } catch (error) {
        setError(error.response?.data?.message || error.message)
        setTimeout(() => setError(""), 5000);
        
    }
  }



  return(
    <div className="auth-container">
        {error && (<p className="error-message">{error}</p>)}

        <h2>Login</h2>
        <form onSubmit={handleSubmit}>
            {["email", "password"].map(
                (field) => (
                    <div className="form-group" key={field}>
                        <label>{field.charAt(0).toLocaleUpperCase() + field.slice(1)}: </label>
                        <input type={field} 
                        name={field}
                        value={formData[field]}
                        onChange={handleChange}
                        required
                        />
                    </div>
                )
            )}
            <button type="submit">Login</button>
        </form>
        <p className="register-link"> Don't have an account? <a href="/register">Register</a></p>

    </div>
)




};





export default LoginPage;
