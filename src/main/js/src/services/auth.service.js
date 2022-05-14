import axios from 'axios';
import authHeader from "./auth-header";

const API_URL = 'http://localhost:8080/login/';

class AuthService {
    needPassword = false;

    loginPhone(user) {
        return axios
            .post(API_URL + 'phone', {
                phoneNumber: user.phoneNum.replace(/\D+/g, ''),
                accessToken: ''
            })
            .then(response => {
                console.log(response)
                if (response.data.accessToken) {
                    localStorage.setItem('user', JSON.stringify(response.data));
                }
                return response.data;
            });
    }

    loginCode(user) {
        return axios
            .post(API_URL + 'code', {
                phoneNumber: user.phoneNum.replace(/\D+/g, ''),
                code: user.code,
            }, {headers: authHeader()})
            .then(response => {
                console.log(response)
                if (response.status === 204) {
                    this.needPassword = true
                }
                return response.data;
            });
    }

    loginPassword(user) {
        return axios
            .post(API_URL + 'password', {
                phoneNumber: user.phoneNum.replace(/\D+/g, ''),
                code: user.code,
                password: user.password
            }, {headers: authHeader()})
            .then(response => {
                console.log(response);
                this.needPassword = false;
                return response.data;
            });
    }

    logout() {
        localStorage.removeItem('user');
    }

    register(user) {
        return axios.post(API_URL + 'signup', {
            phone: user.phone,
            code: user.code,
            password: user.password
        });
    }
}

export default new AuthService();
