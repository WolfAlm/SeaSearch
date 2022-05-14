<template>
    <router-view/>
</template>

<script>


export default {
    name: "Root",
    created() {
        this.$router.push("/login")
    },
}
</script>

<style>
@import './assets/styles/base.css';

</style>