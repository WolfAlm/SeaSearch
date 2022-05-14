<template>
    <body>
    <div class="centered box" v-if="!showInformation">
        <span style="vertical-align:middle" class="title_grad">SeaSearch</span>
        <div class="row login-box" style="padding-bottom: 10px">
            <div style="flex: 40%; width: 50%">
                <img style="width: 90%; margin-left: 10%" src="../assets/logo.ico" alt="SeaSearch Logo"/>

            </div>
            <div style="flex:60%; width: 50%">
                <div v-if="loginStage === 0">
                    <h2>Log in:</h2>
                    <div class="user-box login-box">
                        <input type="text" name="phoneNumber" @keyup="clearError"
                               required="" placeholder="" v-model="user.phoneNum"/>
                        <label>Telegram phone number</label>
                    </div>
                </div>
                <div v-if="loginStage === 1">
                    <h2>Enter the 5-digit code:</h2>
                    <div class="user-box login-box">
                        <input type="text" name="phoneNumber" @keyup="clearError"
                               required="" placeholder="" v-model="user.code"/>
                        <label>Security code</label>
                    </div>
                </div>
                <div v-if="loginStage === 2">
                    <h2>Enter the cloud password:</h2>
                    <div class="user-box login-box">
                        <input type="password" name="phoneNumber" @keyup="clearError"
                               required="" placeholder="" v-model="user.password"/>
                        <label>Cloud password</label>
                    </div>
                </div>
                <div class="error-feedback">
                    <div class="error-feedback">{{ errorMsg }}</div>
                </div>
            </div>
        </div>
        <div class="row login-box user-box">
            <div style="flex: 40%;">
                <button @click="this.showInformation = true;" style="margin-left: 20%; width: 80%; height: 100%">
                    <label>Information</label>
                </button>
            </div>
            <div style="flex:60%;">
                <button style="width: 80%; height: 100%" @click="handleLogin" :disabled="processing">
                    <label>Submit</label>
                </button>
            </div>
        </div>
    </div>
    <div class="centered box login-box user-box" style="width: 60%; height: 65%" v-else>
        <InfoText/>
        <button @click="this.showInformation = false;" style="width: 30%; margin-top: 20px">
            <label>Return</label>
        </button>
    </div>
    </body>
</template>

<script>

import InfoText from "../components/InfoText.vue";
import {isValidPhoneNumber} from "libphonenumber-js"

export default {
    name: "Login Screen",
    components: {InfoText},

    data() {
        return {
            showInformation: false,
            loginStage: 0,
            user: {
                phoneNum: '',
                code: '',
                password: ''
            },
            errorMsg: '',
            processing: false
        };
    },
    computed: {
        loggedIn() {
            return this.$store.state.auth.status.loggedIn;
        },
    },
    created() {
        if (this.loggedIn) {
            this.proceed();
        }
    },
    methods: {
        onRejected(error) {
            this.errorMsg = error.message || (error.response && error.response.data && error.response.data.message)
                || error.toString();
            // console.log(this.errorMsg);
            console.log(error);
            this.processing = false;
        },
        handleLogin() {
            this.processing = true;
            this.errorMsg = "";
            switch (this.loginStage) {
                case 0:
                    if (!isValidPhoneNumber(this.user.phoneNum.trim())) {
                        this.errorMsg = "Please enter a valid phone number";
                        return;
                    }
                    this.$store.dispatch("auth/loginPhone", this.user).then(
                        () => {
                            this.loginStage++;
                            this.processing = false;
                        }, this.onRejected);
                    break;
                case 1:
                    if (!this.user.code.match('^\\d{5}$')) {
                        this.errorMsg = "Please enter exactly 5 digits";
                        return;
                    }
                    this.$store.dispatch("auth/loginCode", this.user).then(
                        () => this.proceed(),
                        (error) => {
                            if (error === "Need password") {
                                this.loginStage++
                                this.processing = false;
                                return;
                            }
                            this.onRejected(error)
                        });
                    break;
                case 2:
                    this.$store.dispatch("auth/loginPassword", this.user).then(
                        () => this.proceed(),
                        this.onRejected);
            }
        },

        clearError() {
            if (this.errorMsg !== "" && isValidPhoneNumber(this.user.phoneNum.trim())) {
                this.errorMsg = '';
            }
        },
        logout() {
            localStorage.removeItem('user');
        },
        proceed() {
            console.log("Success!")
            this.$router.replace({path: '/profile'});
        }
    },
};
</script>

<style>
@import '../assets/styles/base.css';
@import '../assets/styles/login.css';

</style>