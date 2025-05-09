import axios from "axios";
import CryptoJS from "crypto-js";

// ��� ApiService �ļ���һ���������� API ���н����Ĺ����࣬д�� React ��Ŀ��ǰ�˲��֣�
// Ŀ����ͳһ����ͷ����������¼��ע�ᡢ��ȡ������Ϣ��Ԥ�����䡢����ȣ���
// ����Ȼ����Ϥ React��������ļ������� JavaScript �߼������漰 React �����������һ��һ�������Ľ��͸��㣺

// ����� ApiService ����

// ���ܡ�����Ͷ�ȡ token/role��

// ͳһ��װ axios �������û�ע�ᡢ��¼����ȡ�����б�Ԥ�����䡢֧���ȣ���

// �ṩ��֤״̬�жϷ�����



export default class ApiService {
  static BASE_URL = "http://localhost:9090/api";
  static ENCRYPTION_KEY = "Shu-secret-key";

  // encrypt the data using cryptojs
//   ʹ�� AES ���ܺͽ��� token �� role����������� localStorage �е���Ϣ�Ͳ������ġ�
  static encrypt(token) {
    return CryptoJS.AES.encrypt(token, this.ENCRYPTION_KEY.toString());
  }

  // decrypt the data using cryptojs
  static decrypt(token) {
    const bytes = CryptoJS.AES.decrypt(token, this.ENCRYPTION_KEY);

    return bytes.toString(CryptoJS.enc.Utf8);
  }

  // save token
//   ��Щ�����ֱ����ڱ��桢��ȡ����� token �� role���洢λ����������� localStorage��
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

  //���� JWT �� Authorization Token��������֤����
  static getHeader() {
    const token = this.getToken();

    return {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    };
  }

//   ? �ġ��û���ؽӿ�
// �û�ע�� / ��¼
  /* auth and User api methods */
  static async registerUser(registrationData) {
    const resp = await axios.post(`${this.BASE_URL}/auth/register`, registrationData);
    return resp.data;
  }

  static async loginUser(loginData) {
    const resp = await axios.post(`${this.BASE_URL}/auth/login`, loginData);
    return resp.data;
  }

  // ��ȡ�û��Լ�����Ϣ����������ɾ���˺š�
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

  // rooms ��Щ���ǲ������䣨����Ա���û�ʹ�ã��������������䡢��ȡ���з��͡���ȡ�������顢ɾ�����䡢��ѯ��Ԥ������ȡ�
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

  //   Bookings Ԥ����ؽӿ�
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

//   create payment intent ֧����ؽӿڣ������� Stripe��
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


