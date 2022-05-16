import axios from 'axios';
import authHeader from './auth-header';

const API_URL = 'http://localhost:8080';

class UserService {

    getChats() {
        return axios.get(API_URL + '/chats', {headers: authHeader()})
    }

    getImage(url) {
        return axios.get(API_URL + url, {headers: authHeader(), responseType: 'blob'})
    }

    getStats(url) {
        return axios.get(API_URL + url, {headers: authHeader()})
    }

}

export default new UserService();
