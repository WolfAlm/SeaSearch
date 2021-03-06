import axios from 'axios';
import authHeader from "./auth-header";

const API_URL = 'http://localhost:8080/';

class AuthService {
    needPassword = false;

    loginPhone(user) {
        return axios
            .post(API_URL + 'login/phone', {
                phoneNumber: user.phoneNum.replace(/\D+/g, ''),
                accessToken: ''
            })
            .then(response => {
                if (response.data.accessToken) {
                    localStorage.setItem('user', JSON.stringify(response.data));
                }
                return response.data;
            });
    }

    loginCode(user) {
        return axios
            .post(API_URL + 'login/code', {
                phoneNumber: user.phoneNum.replace(/\D+/g, ''),
                code: user.code,
            }, {headers: authHeader()})
            .then(response => {
                if (response.status === 204) {
                    this.needPassword = true
                }
                return response.data;
            });
    }

    loginPassword(user) {
        return axios
            .post(API_URL + 'login/password', {
                phoneNumber: user.phoneNum.replace(/\D+/g, ''),
                code: user.code,
                password: user.password
            }, {headers: authHeader()})
            .then(response => {
                this.needPassword = false;
                return response.data;
            });
    }

    logout() {
        let res = axios.post(API_URL + 'logout', {}, {headers: authHeader()}).then(
            response => response.data
        )
        localStorage.removeItem('user');
        return res
    }
}

export default new AuthService();
