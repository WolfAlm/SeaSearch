<template>
    <body>
    <div class="centered box">
        <div>
            <img style="float:left; width: 10%; margin: 4% 2%" src="../assets/logo.ico" alt="SeaSearch Logo"/>
            <span style="float:left; vertical-align:middle" class="profile_title_grad noselect">SeaSearch</span>
            <button style="margin: 6% 2%;" class="button" @click="logout">Logout</button>
        </div>
        <hr>
        <div>
            <label class="button noselect">
                <input v-model="showDm"  type="checkbox" style="width: 0" >Private
            </label>
            <label class="button noselect">
                <input v-model="showGroups"  type="checkbox"  style="width: 0">Group
            </label>
        </div>
    </div>
    </body>
</template>

<script>
export default {
    name: "ChatList",
    data() {
        return {
            showDm: true,
            showGroups: true
        }
    },
    created() {
        console.log("Hi")
    },
    methods: {
        logout() {
            this.$store.dispatch("auth/logout")
            this.$router.push("/login")
        },
    }
}
</script>

<style scoped>
@import '../assets/styles/base.css';
@import "../assets/styles/profile.css";
</style>