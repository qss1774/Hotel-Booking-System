import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import ApiService from "../../service/ApiService";

const LoginPage = () => {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

//   ����ʹ���� React �� useState Hook��

// formData �ǵ�ǰ�������ݣ����� email �� password �����ֶΣ�

// setFormData �Ǹ��� formData �ĺ���

// ��ʼ��ֵ�ǿ��ַ�������ʾ��һ��ʼ�ǿյġ�

  const [error, setError] = useState("");
// ����Ҳ��ʹ�� useState���������������Ϣ�������¼ʧ����ʾ������ʼֵΪ���ַ�����

  const navigate = useNavigate();
  const { state } = useLocation();

//   ������һ�� React Router �� hook��������ȡ��ǰҳ��·��Я����״̬��

// �������ĳ���ܱ���ҳ����������state ����ܴ�����תǰ�ĵ�ַ����������ȥ��



  const redirectPath = state?.from?.pathname || "/home";
//   �����ÿ�ѡ���������?.������ȫ��ȡֵ�����state ���ڣ��� state.from.pathname ���ڣ��������·��

// ����Ĭ����ת�� /home

  //handle input change
  const handleChange = (e) => {
    const { name, value } = e.target;
    // ���ǽṹ��ֵ���Ӵ����¼��� HTML Ԫ������ȡ��

// name��input ��� name ����

// value��input ��ǰ�����ֵ
    setFormData((prev) => ({ ...prev, [name]: value }));

    // ��һ���ǹؼ�����ʹ���˺���ʽ���� formData ״̬��

// prev �Ǿɵı�����

// ...prev չ��������

// [name]: value �ñ�����Ϊ key ��̬�����ֶΣ��� email �� password��
  };

/**
 * 
     ��
handleSubmit ��������
     ��
��� email �� password �Ƿ�Ϊ��
     ��
���ú�˵�¼�ӿ� loginUser()
     ��
�ɹ� �� ���� token �� role �� ��תҳ��
ʧ�� �� ��ʾ������Ϣ
} e 
 * @returns 
 */

//   ���û��������¼����ťʱ����������᣺

// ��ֹĬ�ϱ���Ϊ������ˢ��ҳ�棩

// ����Ƿ���д�� email ������

// ���ú�˵�¼�ӿ�

// ���ݷ��ص� token �� role �����沢��ת

// ���������ʾ��ʾ��Ϣ
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
