import axios from "axios";
import CryptoJS from "crypto-js";

// 这个 ApiService 文件是一个用于与后端 API 进行交互的工具类，写在 React 项目的前端部分，
// 目的是统一管理和发送请求（如登录、注册、获取房间信息、预定房间、付款等）。
// 你虽然不熟悉 React，但这个文件更多是 JavaScript 逻辑，不涉及 React 组件，下面我一步一步地中文解释给你：

// 这个类 ApiService 负责：

// 加密、保存和读取 token/role。

// 统一封装 axios 请求（如用户注册、登录、获取房间列表、预订房间、支付等）。

// 提供认证状态判断方法。



export default class ApiService {
  static BASE_URL = "http://localhost:9090/api";
  static ENCRYPTION_KEY = "Shu-secret-key";

  // encrypt the data using cryptojs
//   使用 AES 加密和解密 token 或 role，这样你存在 localStorage 中的信息就不是明文。
  static encrypt(token) {
    return CryptoJS.AES.encrypt(token, this.ENCRYPTION_KEY.toString());
  }

  // decrypt the data using cryptojs
  static decrypt(token) {
    const bytes = CryptoJS.AES.decrypt(token, this.ENCRYPTION_KEY);

    return bytes.toString(CryptoJS.enc.Utf8);
  }

  // save token
//   这些方法分别用于保存、读取和清除 token 和 role，存储位置是浏览器的 localStorage。
  static saveToken(token) {
    const encryptedToken = this.encrypt(token);

    localStorage.setItem("token", encryptedToken);
  }

  // retreive token
  static getToken() {
    const encryptedToken = localStorage.getItem("token");

    if (!encryptedToken) return null;

    return this.decrypt(encryptedToken);
  }

  // save role
  static saveRole(role) {
    const encryptedRole = this.encrypt(role);

    localStorage.setItem("role", encryptedRole);
  }

  // get role
  static getRole() {
    const encryptedRole = localStorage.getItem("role");

    if (!encryptedRole) return null;

    return this.decrypt(encryptedRole);
  }

  //
  static clearAuth() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
  }

  //加上 JWT 的 Authorization Token，用于认证请求。
  static getHeader() {
    const token = this.getToken();

    return {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    };
  }

//   ? 四、用户相关接口
// 用户注册 / 登录
  /* auth and User api methods */
  static async registerUser(registrationData) {
    const resp = await axios.post(`${this.BASE_URL}/auth/register`, registrationData);
    return resp.data;
  }

  static async loginUser(loginData) {
    const resp = await axios.post(`${this.BASE_URL}/auth/login`, loginData);
    return resp.data;
  }

  // 获取用户自己的信息、订单，或删除账号。
  static async myProfile() {
    const resp = await axios.get(`${this.BASE_URL}/users/account`, {
      headers: this.getHeader(),
    });
    return resp.data;
  }

  static async myBookings() {
    const resp = await axios.get(`${this.BASE_URL}/users/bookings`, {
      headers: this.getHeader(),
    });
    return resp.data;
  }

  static async deleteAccount() {
    const resp = await axios.delete(`${this.BASE_URL}/users/delete`, {
      headers: this.getHeader(),
    });
    return resp.data;
  }

  // rooms 这些都是操作房间（管理员或用户使用）。包括新增房间、获取所有房型、获取房间详情、删除房间、查询可预订房间等。
  static async addRoom(formData) {
    const resp = await axios.post(`${this.BASE_URL}/rooms/add`, formData, {
      headers: {
        ...this.getHeader(),
        "Content-Type": "multipart/form-data",
      },
    });
    return resp.data;
  }

  // To get all room types
  static async getRoomsTypes() {
    const resp = await axios.get(`${this.BASE_URL}/rooms/types`);

    return resp.data;
  }

  // To get all rooms
  static async getAllRooms() {
    const resp = await axios.get(`${this.BASE_URL}/rooms/all`);

    return resp.data;
  }

  // To get room details
  static async getRoomById(roomId) {
    const resp = await axios.get(`${this.BASE_URL}/rooms/${roomId}`);

    return resp.data;
  }

  // delete room
  static async deleteRoom(roomId) {
    const resp = await axios.delete(`${this.BASE_URL}/rooms/delete/${roomId}`, {
      headers: this.getHeader(),
    });
  }

  static async updateRoom(formData) {
    const resp = await axios.put(`${this.BASE_URL}/rooms/update`, formData, {
      headers: {
        ...this.getHeader(),
        "Content-Type": "multipart/form-data",
      },
    });
    return resp.data;
  }

  static async getAvaliableRooms(checkInDate, checkOutDate, roomType) {
    const resp =
      await axios.get(`${this.BASE_URL}/rooms/avaliable?checkInDate=${checkInDate}&checkOutDate=${checkOutDate}&roomType=${roomType}`);

    return resp.data;
  }

  //   Bookings 预订相关接口
  static async getBookingByReference(bookingCode) {
    const resp = await axios.get(`${this.BASE_URL}/bookings/${bookingCode}`);
    return resp.data;
  }

  static async bookRoom(booking) {
    const resp = await axios.post(`${this.BASE_URL}/bookings`, booking, {
      headers: this.getHeader(),
    });
    return resp.data;
  }
  static async getAllBookings() {
    const resp = await axios.get(`${this.BASE_URL}/bookings/all`, {
      headers: this.getHeader(),
    });
    return resp.data;
  }

  static async updateBooking(booking) {
    const resp = await axios.put(`${this.BASE_URL}/bookings/update`, booking, {
      headers: this.getHeader(),
    });
    return resp.data;
  }

  //   Payments

//   create payment intent 支付相关接口（整合了 Stripe）
  static async proceedForPayment(body){
    const resp = await axios.post(`${this.BASE_URL}/payments/pay`, body, {
      headers: this.getHeader(),
    });
    return resp.data; // return stripe transaction id for this transaction
  }

//   update payment when it has been completed
  static async updateBookingPayment(body){
    const resp = await axios.put(`${this.BASE_URL}/payments/update`, body, {
      headers: this.getHeader(),
    });
    return resp.data; // return stripe transaction id for this transactio   
  }

//   authentication checker
  static logout(){
    this.clearAuth();
  }

  static isAuthenticated(){
    const token = this.getToken();
    return !!token;
  }

  static isAdmin(){
    const role = this.getRole();
    return role === "ADMIN";
  }

  static isCustomer(){
    const role = this.getRole();
    return role === "CUSTOMER";
  }
}


