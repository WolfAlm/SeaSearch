<template>
    <router-view>
        <h1>This is a profile</h1>
    </router-view>
</template>

<script>
export default {
    name: "ChatList",
    created() {
        console.log("Hi")
    },
}
</script>

<style scoped>
@import '../assets/styles/base.css';
@import "../assets/styles/profile.css";
</style>