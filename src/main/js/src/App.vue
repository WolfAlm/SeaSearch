<template>

    <body>
    <div class="centered box" v-if="!show_information">
        <span style="vertical-align:middle" class="title_grad">SeaSearch</span>
        <div class="row login-box" style="padding-bottom: 10px">
            <div style="flex: 40%; width: 50%">
                <img style="width: 90%; margin-left: 10%" src="./assets/logo.ico" alt="SeaSearch Logo"/>

            </div>
            <div style="flex:60%; width: 50%">
                <h2>Log in:</h2>
                <div class="login-box">
                    <div class="user-box">
                        <input type="text" name="phoneNumber" required="" placeholder=""/>
                        <label></label>
                        <label>Phone number</label>
                    </div>
                </div>
            </div>
        </div>
        <div class="row login-box user-box" style="padding: 0">
            <div style="flex: 40%;">
                <button @click="this.show_information = true;" style="margin-left: 20%; width: 80%; height: 100%">
                    <label>Information</label>
                </button>
            </div>
            <div style="flex:60%;">
                <button style="width: 80%; height: 100%">
                    <label>Send code</label>
                </button>
            </div>
        </div>
    </div>
    <div class="centered box login-box user-box" style="width: 60%; height: 70%" v-else >
        <InfoText/>
        <button @click="this.show_information = false;" style="width: 30%; margin-top: 20px">
            <label>Return</label>
        </button>
    </div>
    </body>
</template>

<script>

import InfoText from "./components/InfoText.vue";

export default {
    name: "Login",
    components: {InfoText},
    data() {
        return {
            show_information: false
        };
    },
    computed: {
        loggedIn() {
            return this.$store.state.auth.status.loggedIn;
        },
    },
    created() {
        // if (this.loggedIn) {
        //   this.$router.push("/profile");
        // }
    },
    methods: {
        handleLogin(user) {
            this.loading = true;
            this.$store.dispatch("auth/login", user).then(
                () => {
                    this.$router.push("/profile");
                },
                (error) => {
                    this.loading = false;
                    this.message =
                        (error.response &&
                            error.response.data &&
                            error.response.data.message) ||
                        error.message ||
                        error.toString();
                }
            );
        },
    },
};
</script>

<style>
@import './assets/styles/base.css';
@import './assets/styles/login.css';

</style>